package id.hololearn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
  private Database() {}

  public static Connection open() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException ex) {
      throw new SQLException("JDBC driver unavailable", ex);
    }
    String host = setting("DB_HOST", "127.0.0.1");
    String port = setting("DB_PORT", "3306");
    String name = setting("DB_NAME", "hololearn");
    String url =
        "jdbc:mysql://"
            + host
            + ":"
            + port
            + "/"
            + name
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
    return DriverManager.getConnection(
        url, setting("DB_USER", "hololearn"), required("DB_PASSWORD"));
  }

  public static String required(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank())
      throw new IllegalStateException("Missing environment variable: " + key);
    return value;
  }

  private static String setting(String key, String fallback) {
    String value = System.getenv(key);
    return value == null || value.isBlank() ? fallback : value;
  }
}
