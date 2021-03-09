package tech.claudioed.users.handlers;

import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.templates.SqlTemplate;
import tech.claudioed.users.data.UpdateUser;
import tech.claudioed.users.data.UpdatedUser;
import tech.claudioed.users.data.UserData;
import tech.claudioed.users.data.UserDataParametersMapper;

public class UpdateUserHandler implements Handler<RoutingContext> {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final PgPool client;

  public UpdateUserHandler(PgPool client) {
    this.client = client;
  }

  @Override
  public void handle(RoutingContext rc) {
    var data = rc.getBody();
    var userId = rc.pathParam("userId");
    LOG.info("Updating user with id" + userId);
    var newUserData = Json.decodeValue(data, UpdateUser.class);
    SqlTemplate<UserData, SqlResult<Void>> updateTemplate = SqlTemplate
      .forUpdate(client,
        "UPDATE users SET blocked = #{blocked} WHERE id = #{id} ")
      .mapFrom(UserDataParametersMapper.INSTANCE);
    var updateValue = new UserData(newUserData.getBlocked(), userId);
    updateTemplate.execute(updateValue).onSuccess(result -> {
        if (result.rowCount() > 0) {
          LOG.info("User updated successfully ID:" + userId);
          rc.response().putHeader("content-type", "application/json; charset=utf-8")
            .setStatusCode(200).end(Json.encode(UpdatedUser.createNew(userId,newUserData.getBlocked())));
        } else {
          LOG.error("User was not updated ID:" + userId);
          rc.response().putHeader("content-type", "application/json; charset=utf-8")
            .setStatusCode(500).end(new JsonObject().encode());
        }
      }).onFailure(err -> {
        LOG.error("User update fail, error to execute instruction ID" + userId,err);
        rc.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400).end();
      });
  }

}
