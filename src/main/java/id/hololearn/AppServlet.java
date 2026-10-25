package id.hololearn;

import id.hololearn.Models.Account;
import id.hololearn.Models.Entry;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/")
public final class AppServlet extends HttpServlet {
  private final Repository repository = new Repository();

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    if (req.getServletPath().startsWith("/assets/")) {
      getServletContext().getNamedDispatcher("default").forward(req, res);
      return;
    }
    if (req.getServletPath().equals("/health")) {
      try {
        if (!repository.healthy()) res.setStatus(503);
      } catch (SQLException ex) {
        res.setStatus(503);
      }
      res.setContentType("text/plain;charset=UTF-8");
      res.getWriter().print(res.getStatus() == 200 ? "ok" : "unavailable");
      return;
    }
    try {
      Account user = current(req);
      req.setAttribute("user", user);
      String path = req.getServletPath();
      if (path.startsWith("/admin") || path.equals("/profile") || path.equals("/courses")) {
        if (user == null) {
          redirect(req, res, "/login");
          return;
        }
        if (path.startsWith("/admin") && !user.isAdmin()) {
          fail(req, res, 403, "Halaman ini hanya untuk pengelola.");
          return;
        }
      }
      super.service(req, res);
    } catch (SQLException ex) {
      getServletContext()
          .log(
              "Database request failed (SQL state: "
                  + ex.getSQLState()
                  + ", no query or parameters logged).");
      fail(
          req,
          res,
          503,
          "Database belum dapat dihubungi. Coba lagi setelah layanan database siap.");
    } catch (IllegalArgumentException ex) {
      fail(req, res, 400, ex.getMessage());
    }
  }

  private Account current(HttpServletRequest req) throws SQLException {
    Object id = req.getSession().getAttribute("accountId");
    if (!(id instanceof Long)) return null;
    Account user = repository.findAccount((Long) id);
    if (user == null) req.getSession().removeAttribute("accountId");
    return user;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
    try {
      Object flash = req.getSession().getAttribute("flash");
      if (flash != null) {
        req.setAttribute("message", flash);
        req.getSession().removeAttribute("flash");
      }
      switch (req.getServletPath()) {
        case "/" -> view(req, res, "home", "Belajar dari satu tempat");
        case "/books" -> {
          req.setAttribute("entries", repository.entries("books"));
          view(req, res, "books", "Katalog e-book");
        }
        case "/courses" -> {
          req.setAttribute("entries", repository.entries("courses"));
          view(req, res, "courses", "Course");
        }
        case "/login" -> view(req, res, "login", "Masuk");
        case "/register" -> view(req, res, "register", "Daftar akun");
        case "/contact" -> view(req, res, "contact", "Kirim masukan");
        case "/profile" -> view(req, res, "profile", "Profil saya");
        case "/about" -> view(req, res, "about", "Tentang dan penggunaan data");
        case "/admin" -> admin(req, res);
        case "/admin/edit", "/admin/remove" -> edit(req, res);
        default -> fail(req, res, 404, "Halaman yang kamu cari tidak ditemukan.");
      }
    } catch (SQLException ex) {
      getServletContext().log("Database read unavailable (no query or parameters logged).");
      fail(
          req,
          res,
          503,
          "Database belum dapat dihubungi. Coba lagi setelah layanan database siap.");
    }
  }

  private void admin(HttpServletRequest req, HttpServletResponse res)
      throws SQLException, ServletException, IOException {
    String kind =
        req.getParameter("kind") == null ? "books" : Validation.kind(req.getParameter("kind"));
    req.setAttribute("kind", kind);
    if (kind.equals("accounts")) req.setAttribute("accounts", repository.accounts());
    else req.setAttribute("entries", repository.entries(kind));
    view(req, res, "admin", "Kelola " + label(kind));
  }

  private void edit(HttpServletRequest req, HttpServletResponse res)
      throws SQLException, ServletException, IOException {
    String kind = Validation.kind(req.getParameter("kind"));
    long id = optionalId(req);
    req.setAttribute("kind", kind);
    req.setAttribute("id", id);
    req.setAttribute("kindLabel", label(kind));
    if (id > 0) {
      Object item =
          kind.equals("accounts") ? repository.findAccount(id) : repository.find(kind, id);
      if (item == null) {
        fail(req, res, 404, "Data ini tidak ditemukan atau sudah dihapus.");
        return;
      }
      req.setAttribute(kind.equals("accounts") ? "account" : "entry", item);
    }
    if (req.getServletPath().equals("/admin/remove") && id == 0) {
      fail(req, res, 400, "Pilih data yang ingin dihapus.");
      return;
    }
    boolean removing = req.getServletPath().equals("/admin/remove");
    view(
        req,
        res,
        removing ? "remove" : "edit",
        (removing ? "Hapus " : id == 0 ? "Tambah " : "Ubah ") + label(kind));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
    String path = req.getServletPath();
    try {
      Account user = (Account) req.getAttribute("user");
      switch (path) {
        case "/login" -> {
          String email = Validation.email(req.getParameter("email"));
          String password = req.getParameter("password");
          if (password == null || password.length() > 128)
            throw new IllegalArgumentException("Email atau kata sandi tidak cocok.");
          Account account = repository.authenticate(email, password);
          if (account == null)
            throw new IllegalArgumentException("Email atau kata sandi tidak cocok.");
          signIn(req, account.id());
          redirect(req, res, account.isAdmin() ? "/admin" : "/courses");
        }
        case "/register" -> {
          String name = Validation.text(req.getParameter("name"), "Nama", 80);
          String email = Validation.email(req.getParameter("email"));
          String password = Validation.password(req.getParameter("password"));
          signIn(req, repository.createAccount(name, email, password, "user"));
          success(req, res, "/courses", "Akun dibuat. Selamat belajar!");
        }
        case "/logout" -> {
          req.getSession().invalidate();
          redirect(req, res, "/");
        }
        case "/contact" -> {
          repository.saveEntry(
              "feedback",
              0,
              Validation.text(req.getParameter("name"), "Nama", 80),
              Validation.text(req.getParameter("comment"), "Masukan", 2000),
              Validation.email(req.getParameter("email")));
          success(
              req, res, "/contact", "Masukan tersimpan. Terima kasih sudah membantu Hololearn.");
        }
        case "/profile" -> {
          String password = value(req, "newPassword");
          if (!password.isEmpty()) {
            Validation.password(password);
            if (repository.authenticate(user.email(), req.getParameter("oldPassword")) == null)
              throw new IllegalArgumentException("Kata sandi saat ini tidak cocok.");
          }
          repository.updateProfile(
              user.id(),
              Validation.text(req.getParameter("name"), "Nama", 80),
              Validation.email(req.getParameter("email")),
              password);
          if (!password.isEmpty()) {
            req.changeSessionId();
            req.getSession().removeAttribute("csrf");
          }
          success(req, res, "/profile", "Profil diperbarui.");
        }
        case "/admin/save" -> {
          String kind = Validation.kind(req.getParameter("kind"));
          long id = optionalId(req);
          if (kind.equals("accounts")) {
            String password = value(req, "password");
            if (id == 0 || !password.isEmpty()) Validation.password(password);
            repository.saveAccount(
                id,
                Validation.text(req.getParameter("name"), "Nama", 80),
                Validation.email(req.getParameter("email")),
                password,
                Validation.role(req.getParameter("role")));
          } else {
            String title =
                Validation.text(
                    req.getParameter("title"),
                    kind.equals("feedback") ? "Nama" : "Judul",
                    kind.equals("feedback") ? 80 : 160);
            String detail =
                Validation.text(
                    req.getParameter("detail"),
                    kind.equals("books")
                        ? "Deskripsi"
                        : kind.equals("courses") ? "Topik" : "Masukan",
                    kind.equals("courses") ? 500 : 2000);
            String extra =
                switch (kind) {
                  case "books" -> Validation.image(req.getParameter("extra"));
                  case "courses" -> Validation.url(req.getParameter("extra"));
                  default -> Validation.email(req.getParameter("extra"));
                };
            repository.saveEntry(kind, id, title, detail, extra);
          }
          success(req, res, "/admin?kind=" + kind, "Data " + label(kind) + " disimpan.");
        }
        case "/admin/delete" -> {
          String kind = Validation.kind(req.getParameter("kind"));
          long id = Validation.id(req.getParameter("id"));
          repository.delete(kind, id);
          success(req, res, "/admin?kind=" + kind, "Data " + label(kind) + " dihapus.");
        }
        default -> fail(req, res, 405, "Metode ini tidak tersedia untuk halaman tersebut.");
      }
    } catch (IllegalArgumentException ex) {
      formError(req, res, path, ex.getMessage());
    } catch (SQLException ex) {
      if ("23000".equals(ex.getSQLState()))
        formError(req, res, path, "Email sudah terdaftar. Gunakan email lain.");
      else {
        getServletContext().log("Database write unavailable (no query or parameters logged).");
        fail(req, res, 503, "Perubahan belum dapat disimpan. Coba lagi setelah database siap.");
      }
    }
  }

  private void formError(
      HttpServletRequest req, HttpServletResponse res, String path, String message)
      throws ServletException, IOException {
    res.setStatus(400);
    req.setAttribute("error", message);
    String page =
        switch (path) {
          case "/login" -> "login";
          case "/register" -> "register";
          case "/profile" -> "profile";
          case "/contact" -> "contact";
          default -> null;
        };
    if (page == null) {
      try {
        String kind = Validation.kind(req.getParameter("kind"));
        req.setAttribute("kind", kind);
        req.setAttribute("id", optionalId(req));
        req.setAttribute("kindLabel", label(kind));
        if (path.equals("/admin/save")) {
          req.setAttribute(
              "entry",
              new Entry(
                  optionalId(req), value(req, "title"), value(req, "detail"), value(req, "extra")));
          req.setAttribute(
              "account",
              new Account(
                  optionalId(req), value(req, "name"), value(req, "email"), value(req, "role")));
          view(req, res, "edit", "Periksa kembali data");
          return;
        }
      } catch (IllegalArgumentException ignored) {
      }
      view(req, res, "error", "Perubahan belum disimpan");
    } else view(req, res, page, "Periksa kembali formulir");
  }

  private void signIn(HttpServletRequest req, long id) {
    req.getSession().invalidate();
    HttpSession session = req.getSession(true);
    session.setAttribute("accountId", id);
    session.setMaxInactiveInterval(1800);
  }

  private long optionalId(HttpServletRequest req) {
    String value = req.getParameter("id");
    return value == null || value.isBlank() || value.equals("0") ? 0 : Validation.id(value);
  }

  private String value(HttpServletRequest req, String key) {
    String value = req.getParameter(key);
    return value == null ? "" : value;
  }

  private String label(String kind) {
    return switch (kind) {
      case "books" -> "buku";
      case "courses" -> "course";
      case "accounts" -> "akun";
      default -> "masukan";
    };
  }

  private void success(HttpServletRequest req, HttpServletResponse res, String path, String message)
      throws IOException {
    req.getSession().setAttribute("flash", message);
    redirect(req, res, path);
  }

  private void redirect(HttpServletRequest req, HttpServletResponse res, String path) {
    res.setStatus(303);
    res.setHeader("Location", req.getContextPath() + path);
  }

  private void view(HttpServletRequest req, HttpServletResponse res, String page, String title)
      throws ServletException, IOException {
    req.setAttribute("title", title);
    req.getRequestDispatcher("/WEB-INF/views/" + page + ".jsp").forward(req, res);
  }

  private void fail(HttpServletRequest req, HttpServletResponse res, int status, String message)
      throws ServletException, IOException {
    res.setStatus(status);
    req.setAttribute("error", message);
    view(req, res, "error", "Permintaan belum berhasil");
  }
}
