package tech.claudioed.users.handlers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.templates.SqlTemplate;
import tech.claudioed.users.data.CreatedUser;
import tech.claudioed.users.data.NewUser;
import tech.claudioed.users.data.User;
import tech.claudioed.users.data.UserParametersMapper;

import java.util.UUID;

public class CreateUserHandler implements Handler<RoutingContext> {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final PgPool client;

  private final Vertx vertx;

  private final DeliveryOptions deliveryOptions = new DeliveryOptions().setTracingPolicy(TracingPolicy.ALWAYS);

  public CreateUserHandler(PgPool client, Vertx vertx) {
    this.client = client;
    this.vertx = vertx;
  }

  @Override
  public void handle(RoutingContext rc) {
    var data = rc.getBody();
    var newUser = Json.decodeValue(data, NewUser.class);
    SqlTemplate<User, SqlResult<Void>> insertTemplate = SqlTemplate
      .forUpdate(client,
        "INSERT INTO users (id, first_name, last_name, email, email_verified, blocked)  VALUES ( #{id}, #{first_name}, #{last_name}, #{email}, #{email_verified}, #{blocked} )")
      .mapFrom(
        UserParametersMapper.INSTANCE);
    this.vertx.eventBus().request("request.create.user",Json.encode(newUser),this.deliveryOptions).onSuccess(message ->{
      var userData = Json.decodeValue(message.body().toString(), CreatedUser.class);
      var user = userData.toData();
      insertTemplate.execute(user).onSuccess(result -> {
        if (result.rowCount() > 0) {
          rc.response().putHeader("content-type", "application/json; charset=utf-8")
            .setStatusCode(201).end(user.toJson().encode());
        } else {
          LOG.error("User was not created ID:" + user.getId());
          rc.response().putHeader("content-type", "application/json; charset=utf-8")
            .setStatusCode(500).end(new JsonObject().encode());
        }
      }).onFailure(err -> {
        LOG.error("Error to execute instruction in database ", err);
        rc.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
          .end();
      });
    }).onFailure(err ->{
      LOG.error("Error on Identity Provider integration");
      rc.response().putHeader("content-type", "application/json; charset=utf-8")
        .setStatusCode(500).end(new JsonObject().encode());
    });
  }

}
