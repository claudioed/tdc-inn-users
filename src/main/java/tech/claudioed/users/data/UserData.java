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

public class UserData {

  @Column(name = "id")
  private String id;

  @Column(name = "blocked")
  private Boolean blocked;

  public UserData() {
  }

  public UserData(JsonObject json) {
    UserDataConverter.fromJson(json, this);
  }

  public UserData(Boolean blocked, String id) {
    this.blocked = blocked;
    this.id = id;
  }

  public Boolean getBlocked() {
    return blocked;
  }

  public void setBlocked(Boolean blocked) {
    this.blocked = blocked;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
