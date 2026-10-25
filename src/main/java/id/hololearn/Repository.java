package id.hololearn;

import id.hololearn.Models.Account;
import id.hololearn.Models.Entry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Repository {
  private Account account(ResultSet rs) throws SQLException {
    return new Account(
        rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("role"));
  }

  public Account findAccount(long id) throws SQLException {
    try (Connection c = Database.open();
        PreparedStatement p =
            c.prepareStatement("SELECT id,name,email,role FROM accounts WHERE id=?")) {
      p.setLong(1, id);
      try (ResultSet rs = p.executeQuery()) {
        return rs.next() ? account(rs) : null;
      }
    }
  }

  public Account authenticate(String email, String password) throws SQLException {
    try (Connection c = Database.open();
        PreparedStatement p = c.prepareStatement("SELECT * FROM accounts WHERE email=?")) {
      p.setString(1, email);
      try (ResultSet rs = p.executeQuery()) {
        return rs.next() && Passwords.verify(password, rs.getString("password_hash"))
            ? account(rs)
            : null;
      }
    }
  }

  public long createAccount(String name, String email, String password, String role)
      throws SQLException {
    String hash = Passwords.hash(password);
    try (Connection c = Database.open();
        PreparedStatement p =
            c.prepareStatement(
                "INSERT INTO accounts(name,email,password_hash,role) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, name);
      p.setString(2, email);
      p.setString(3, hash);
      p.setString(4, role);
      p.executeUpdate();
      try (ResultSet rs = p.getGeneratedKeys()) {
        if (rs.next()) return rs.getLong(1);
      }
    }
    throw new SQLException("Account key unavailable");
  }

  public void updateProfile(long id, String name, String email, String newPassword)
      throws SQLException {
    String sql =
        newPassword.isEmpty()
            ? "UPDATE accounts SET name=?,email=? WHERE id=?"
            : "UPDATE accounts SET name=?,email=?,password_hash=? WHERE id=?";
    try (Connection c = Database.open();
        PreparedStatement p = c.prepareStatement(sql)) {
      p.setString(1, name);
      p.setString(2, email);
      if (newPassword.isEmpty()) p.setLong(3, id);
      else {
        p.setString(3, Passwords.hash(newPassword));
        p.setLong(4, id);
      }
      changed(p.executeUpdate());
    }
  }

  public List<Account> accounts() throws SQLException {
    List<Account> rows = new ArrayList<>();
    try (Connection c = Database.open();
        PreparedStatement p =
            c.prepareStatement("SELECT id,name,email,role FROM accounts ORDER BY id DESC");
        ResultSet rs = p.executeQuery()) {
      while (rs.next()) rows.add(account(rs));
    }
    return rows;
  }

  public List<Entry> entries(String kind) throws SQLException {
    String sql =
        switch (Validation.kind(kind)) {
          case "books" -> "SELECT id,title,description,image FROM books ORDER BY id DESC";
          case "courses" -> "SELECT id,title,topic,url FROM courses ORDER BY id DESC";
          case "feedback" -> "SELECT id,name,comment,email FROM feedback ORDER BY id DESC";
          default -> throw new IllegalArgumentException("Daftar akun memakai jalur berbeda.");
        };
    List<Entry> rows = new ArrayList<>();
    try (Connection c = Database.open();
        PreparedStatement p = c.prepareStatement(sql);
        ResultSet rs = p.executeQuery()) {
      while (rs.next())
        rows.add(new Entry(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }
    return rows;
  }

  public Entry find(String kind, long id) throws SQLException {
    return entries(kind).stream().filter(e -> e.id() == id).findFirst().orElse(null);
  }

  public void saveEntry(String kind, long id, String title, String detail, String extra)
      throws SQLException {
    String table =
        switch (Validation.kind(kind)) {
          case "books" -> "books";
          case "courses" -> "courses";
          case "feedback" -> "feedback";
          default -> throw new IllegalArgumentException("Jenis data salah.");
        };
    String columns =
        switch (table) {
          case "books" -> "title=?,description=?,image=?";
          case "courses" -> "title=?,topic=?,url=?";
          default -> "name=?,comment=?,email=?";
        };
    String sql =
        id == 0
            ? "INSERT INTO " + table + " SET " + columns
            : "UPDATE " + table + " SET " + columns + " WHERE id=?";
    try (Connection c = Database.open();
        PreparedStatement p = c.prepareStatement(sql)) {
      p.setString(1, title);
      p.setString(2, detail);
      p.setString(3, extra);
      if (id != 0) p.setLong(4, id);
      changed(p.executeUpdate());
    }
  }

  public void saveAccount(long id, String name, String email, String password, String role)
      throws SQLException {
    if (id == 0) {
      createAccount(name, email, password, role);
      return;
    }
    try (Connection c = Database.open()) {
      c.setAutoCommit(false);
      try {
        guardLastAdmin(c, id, "admin".equals(role));
        String sql =
            password.isEmpty()
                ? "UPDATE accounts SET name=?,email=?,role=? WHERE id=?"
                : "UPDATE accounts SET name=?,email=?,role=?,password_hash=? WHERE id=?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
          p.setString(1, name);
          p.setString(2, email);
          p.setString(3, role);
          if (password.isEmpty()) p.setLong(4, id);
          else {
            p.setString(4, Passwords.hash(password));
            p.setLong(5, id);
          }
          changed(p.executeUpdate());
        }
        c.commit();
      } catch (SQLException | RuntimeException ex) {
        c.rollback();
        throw ex;
      }
    }
  }

  public void delete(String kind, long id) throws SQLException {
    String table = Validation.kind(kind);
    try (Connection c = Database.open()) {
      c.setAutoCommit(false);
      try {
        if (table.equals("accounts")) guardLastAdmin(c, id, false);
        try (PreparedStatement p = c.prepareStatement("DELETE FROM " + table + " WHERE id=?")) {
          p.setLong(1, id);
          changed(p.executeUpdate());
        }
        c.commit();
      } catch (SQLException | RuntimeException ex) {
        c.rollback();
        throw ex;
      }
    }
  }

  private void guardLastAdmin(Connection c, long id, boolean remainsAdmin) throws SQLException {
    if (remainsAdmin) return;
    List<Long> admins = new ArrayList<>();
    try (PreparedStatement p =
            c.prepareStatement(
                "SELECT id FROM accounts WHERE role='admin' ORDER BY id FOR UPDATE");
        ResultSet rs = p.executeQuery()) {
      while (rs.next()) admins.add(rs.getLong(1));
    }
    if (admins.size() == 1 && admins.contains(id))
      throw new IllegalArgumentException(
          "Admin terakhir tidak dapat dihapus atau diubah menjadi user.");
  }

  public boolean hasAdmin() throws SQLException {
    try (Connection c = Database.open();
        PreparedStatement p =
            c.prepareStatement("SELECT id FROM accounts WHERE role='admin' LIMIT 1");
        ResultSet rs = p.executeQuery()) {
      return rs.next();
    }
  }

  public boolean healthy() throws SQLException {
    try (Connection c = Database.open()) {
      return c.isValid(2);
    }
  }

  private void changed(int rows) {
    if (rows == 0) throw new IllegalArgumentException("Data tidak ditemukan atau sudah dihapus.");
  }
}
