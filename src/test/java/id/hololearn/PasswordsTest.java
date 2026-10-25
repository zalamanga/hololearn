package id.hololearn;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {
  @Test
  void saltedHashesAuthenticateWithoutStoringPlaintext() {
    String password = "contoh-kata-sandi-unik";
    String first = Passwords.hash(password), second = Passwords.hash(password);
    assertNotEquals(first, second);
    assertFalse(first.contains(password));
    assertTrue(Passwords.verify(password, first));
    assertFalse(Passwords.verify("kata-sandi-salah", first));
  }

  @Test
  void corruptOrUnboundedStoredFormatsAreRejected() {
    for (String stored :
        new String[] {
          "plain-password",
          "pbkdf2$999999999$AA==$AA==",
          "pbkdf2$0$AA==$AA==",
          "pbkdf2$x$bad$bad",
          "pbkdf2$600000$AA==$AA=="
        }) assertFalse(Passwords.verify("contoh", stored));
    assertFalse(Passwords.verify(null, null));
  }

  @Test
  void unicodePasswordRoundTrips() {
    String password = "Bacaan-\u4e66\u672c-\u00e9-aman";
    assertTrue(Passwords.verify(password, Passwords.hash(password)));
  }
}
