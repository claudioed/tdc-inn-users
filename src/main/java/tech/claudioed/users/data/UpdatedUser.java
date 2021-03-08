package tech.claudioed.users.data;

public class UpdatedUser {

  private String userId;

  private Boolean blocked;

  public UpdatedUser() {
  }

  private UpdatedUser(String userId, Boolean blocked) {
    this.userId = userId;
    this.blocked = blocked;
  }

  public static UpdatedUser createNew(String usedId, Boolean blocked) {
    return new UpdatedUser(usedId, blocked);
  }

  public Boolean getBlocked() {
    return blocked;
  }

  public void setBlocked(Boolean blocked) {
    this.blocked = blocked;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
