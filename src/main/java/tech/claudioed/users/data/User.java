package tech.claudioed.users.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.RowMapped;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
@ParametersMapped(formatter = SnakeCase.class)
public class User {

  private String id;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  private String email;
  @Column(name = "email_verified")
  private Boolean emailVerified;

  private Boolean blocked;

  public User() {
  }

  public User(JsonObject json) {
    UserConverter.fromJson(json, this);
  }

  private User(String id, String firstName, String lastName, String email,
    Boolean emailVerified,Boolean blocked) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.emailVerified = emailVerified;
    this.blocked = blocked;
  }

  public static User createUser(String id, String firstName, String lastName, String email,
    Boolean emailVerified,Boolean blocked) {
    return new User(id, firstName, lastName, email, emailVerified,blocked);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    UserConverter.toJson(this, json);
    return json;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public Boolean getBlocked() {
    return blocked;
  }

  public void setBlocked(Boolean blocked) {
    this.blocked = blocked;
  }
}
