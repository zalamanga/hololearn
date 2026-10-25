package id.hololearn;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {
  @Test
  void rejectsExecutableAndCredentialBearingCourseUrls() {
    for (String url :
        new String[] {
          "javascript:alert(1)",
          "data:text/html,test",
          "//example.test/a",
          "https://name:pass@example.test/a",
          "/relative",
          "file:///etc/passwd"
        }) assertThrows(IllegalArgumentException.class, () -> Validation.url(url));
    assertEquals("https://dev.java/learn/", Validation.url("https://dev.java/learn/"));
  }

  @Test
  void rejectsArbitrarySqlTableAndRoleInput() {
    assertThrows(
        IllegalArgumentException.class, () -> Validation.kind("books; DROP TABLE accounts"));
    assertThrows(IllegalArgumentException.class, () -> Validation.role("owner"));
    assertThrows(IllegalArgumentException.class, () -> Validation.kind(null));
  }

  @Test
  void idsMustBePositiveAndFitLong() {
    for (String id : new String[] {"0", "-1", "x", "999999999999999999999"})
      assertThrows(IllegalArgumentException.class, () -> Validation.id(id));
    assertEquals(42, Validation.id("42"));
  }

  @Test
  void validatesPasswordLengthWithoutTrimmingUserPassword() {
    assertThrows(IllegalArgumentException.class, () -> Validation.password("short"));
    assertThrows(IllegalArgumentException.class, () -> Validation.password("x".repeat(129)));
    assertEquals("  cukup panjang  ", Validation.password("  cukup panjang  "));
  }

  @Test
  void catalogImageCannotEscapeLocalAssets() {
    assertThrows(IllegalArgumentException.class, () -> Validation.image("../WEB-INF/web.xml"));
    assertThrows(
        IllegalArgumentException.class, () -> Validation.image("https://example.test/a.png"));
    assertThrows(IllegalArgumentException.class, () -> Validation.image(null));
    assertEquals("Math.webp", Validation.image("Math.webp"));
  }

  @Test
  void validatesEmailAndBoundedRequiredText() {
    assertEquals("member@example.test", Validation.email(" Member@EXAMPLE.test "));
    assertThrows(IllegalArgumentException.class, () -> Validation.email("bad@example"));
    assertThrows(IllegalArgumentException.class, () -> Validation.text("   ", "Nama", 80));
    assertThrows(IllegalArgumentException.class, () -> Validation.text("x".repeat(81), "Nama", 80));
  }
}
