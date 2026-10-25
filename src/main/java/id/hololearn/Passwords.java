package id.hololearn;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
  private static final int ITERATIONS = 600_000;
  private static final SecureRandom RANDOM = new SecureRandom();

  private Passwords() {}

  public static String hash(String password) {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);
    return "pbkdf2$"
        + ITERATIONS
        + "$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  public static boolean verify(String password, String stored) {
    if (password == null || password.length() > 128 || stored == null) return false;
    try {
      String[] pieces = stored.split("\\$");
      if (pieces.length != 4 || !pieces[0].equals("pbkdf2")) return false;
      int iterations = Integer.parseInt(pieces[1]);
      byte[] salt = Base64.getDecoder().decode(pieces[2]);
      byte[] expected = Base64.getDecoder().decode(pieces[3]);
      if (iterations < 100_000
          || iterations > 1_000_000
          || salt.length != 16
          || expected.length != 32) return false;
      return MessageDigest.isEqual(expected, derive(password, salt, iterations));
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  private static byte[] derive(String password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Password hashing unavailable", ex);
    } finally {
      spec.clearPassword();
    }
  }
}
