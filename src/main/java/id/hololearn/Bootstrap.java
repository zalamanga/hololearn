package id.hololearn;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public final class Bootstrap implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    try {
      Repository repository = new Repository();
      if (!repository.hasAdmin())
        repository.createAccount(
            "Pengelola Hololearn",
            Validation.email(Database.required("ADMIN_EMAIL")),
            Validation.password(Database.required("ADMIN_PASSWORD")),
            "admin");
    } catch (Exception ex) {
      event
          .getServletContext()
          .log(
              "Hololearn startup failed: check database/schema and admin environment (details withheld).");
      throw new IllegalStateException("Hololearn configuration unavailable");
    }
  }
}
