package id.hololearn;

public final class Models {
  private Models() {}

  public record Account(long id, String name, String email, String role) {
    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getEmail() {
      return email;
    }

    public String getRole() {
      return role;
    }

    public boolean isAdmin() {
      return "admin".equals(role);
    }
  }

  public record Entry(long id, String title, String detail, String extra) {
    public long getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public String getDetail() {
      return detail;
    }

    public String getExtra() {
      return extra;
    }
  }
}
