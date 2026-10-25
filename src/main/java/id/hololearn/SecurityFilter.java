package id.hololearn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter("/*")
public final class SecurityFilter implements Filter {
  private static final SecureRandom RANDOM = new SecureRandom();

  public static String token(HttpSession session) {
    String token = (String) session.getAttribute("csrf");
    if (token == null) {
      byte[] bytes = new byte[32];
      RANDOM.nextBytes(bytes);
      token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
      session.setAttribute("csrf", token);
    }
    return token;
  }

  @Override
  public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) input;
    HttpServletResponse response = (HttpServletResponse) output;
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    response.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'");
    String path = request.getServletPath();
    if (!path.startsWith("/assets/") && !path.equals("/health")) {
      response.setHeader("Cache-Control", "no-store");
      String token = token(request.getSession());
      request.setAttribute("csrf", token);
      if (request.getMethod().equals("POST")) {
        String submitted = request.getParameter("csrf");
        if (submitted == null
            || !MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8))) {
          response.setStatus(403);
          request.setAttribute(
              "error", "Formulir sudah kedaluwarsa. Buka kembali halaman lalu coba lagi.");
          request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
          return;
        }
      }
    }
    chain.doFilter(request, response);
  }
}
