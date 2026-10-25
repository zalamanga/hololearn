package id.hololearn;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public final class Validation {
  public static final Set<String> IMAGES =
      Set.of("Math.webp", "Science.webp", "Physics.webp", "History1.jpeg");

  private Validation() {}

  public static String text(String value, String label, int max) {
    String clean = value == null ? "" : value.strip();
    if (clean.isEmpty() || clean.length() > max || clean.indexOf('\0') >= 0)
      throw new IllegalArgumentException(label + " wajib diisi, maksimal " + max + " karakter.");
    return clean;
  }

  public static String email(String value) {
    String clean = text(value, "Email", 160).toLowerCase(Locale.ROOT);
    if (!clean.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      throw new IllegalArgumentException("Format email belum benar.");
    return clean;
  }

  public static String password(String value) {
    if (value == null || value.length() < 12 || value.length() > 128)
      throw new IllegalArgumentException("Kata sandi harus berisi 12 sampai 128 karakter.");
    return value;
  }

  public static long id(String value) {
    try {
      long result = Long.parseLong(value);
      if (result <= 0) throw new NumberFormatException();
      return result;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("ID data tidak valid.");
    }
  }

  public static String image(String value) {
    if (value == null || !IMAGES.contains(value))
      throw new IllegalArgumentException("Pilih sampul dari daftar yang tersedia.");
    return value;
  }

  public static String url(String value) {
    String clean = text(value, "Tautan course", 1000);
    try {
      URI uri = URI.create(clean);
      if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
          || uri.getHost() == null
          || uri.getUserInfo() != null) throw new IllegalArgumentException();
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Tautan course harus berupa URL http atau https tanpa kredensial.");
    }
    return clean;
  }

  public static String kind(String value) {
    if (!Set.of("books", "courses", "accounts", "feedback").contains(value == null ? "" : value))
      throw new IllegalArgumentException("Jenis data tidak dikenal.");
    return value;
  }

  public static String role(String value) {
    if (!"user".equals(value) && !"admin".equals(value))
      throw new IllegalArgumentException("Peran akun tidak valid.");
    return value;
  }
}
