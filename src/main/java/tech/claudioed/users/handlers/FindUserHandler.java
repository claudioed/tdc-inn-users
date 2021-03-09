package tech.claudioed.users.handlers;

import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.templates.SqlTemplate;
import java.util.Collections;
import tech.claudioed.users.data.UserRowMapper;

public class FindUserHandler implements Handler<RoutingContext> {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final PgPool client;

  public FindUserHandler(PgPool client) {
    this.client = client;
  }

  @Override
  public void handle(RoutingContext rc) {
    var userId = rc.pathParam("userId");
    SqlTemplate.forQuery(this.client,"SELECT * FROM users WHERE id=#{id}").mapTo(UserRowMapper.INSTANCE).execute(
      Collections.singletonMap("id",userId)).onSuccess(rows ->{
        if(rows.iterator().hasNext()){
          LOG.info("Loading user ID: " + userId);
          var user = rows.iterator().next();
          rc.response().putHeader("content-type", "application/json; charset=utf-8").end(user.toJson().encode());
        }else{
          LOG.error("User was not found ID: " + userId);
          rc.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(404).end();
        }
    }).onFailure(err ->{
      LOG.error("Error to execute instruction in database ", err);
      rc.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400).end();
    });
  }
}
