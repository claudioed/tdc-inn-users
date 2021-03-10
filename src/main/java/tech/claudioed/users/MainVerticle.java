package tech.claudioed.users;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.micrometer.PrometheusScrapingHandler;
import tech.claudioed.users.handlers.UpdateUserHandler;
import tech.claudioed.users.infra.DatasourceConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import tech.claudioed.users.handlers.CreateUserHandler;
import tech.claudioed.users.handlers.FindUserHandler;

public class MainVerticle extends AbstractVerticle {

  Logger LOG = LoggerFactory.getLogger(this.getClass());

  private DatasourceConfig datasourceConfig;

  @Override
  public void start(Promise<Void> startPromise) {
    initConfig().compose(cfg ->{
      this.datasourceConfig = new DatasourceConfig(cfg.getJsonObject("database", new JsonObject()));
      PgConnectOptions connectOptions = new PgConnectOptions()
        .setPort(this.datasourceConfig.getPort())
        .setHost(this.datasourceConfig.getHost())
        .setDatabase(this.datasourceConfig.getDatabase())
        .setUser(this.datasourceConfig.getUser())
        .setPassword(this.datasourceConfig.getPassword());
      PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(5);
      PgPool client = PgPool.pool(this.vertx,connectOptions, poolOptions);
      Future<Void> builderPromise = RouterBuilder.create(vertx, "src/main/resources/oas.yaml")
        .compose(builder -> {
          builder.operation("get-users-userId").handler(new FindUserHandler(client));
          builder.operation("create-user").handler(new CreateUserHandler(client, vertx));
          builder.operation("update-user").handler(new UpdateUserHandler(client));
          var router = builder.createRouter();
          router.errorHandler(400, ctx -> {
            LOG.error("Bad Request", ctx.failure());
          });
          // Infra endpoints
          router.route("/metrics").handler(PrometheusScrapingHandler.create());
          var healthCheckHandler = HealthCheckHandler.create(this.vertx);
          healthCheckHandler.register("database",
            promise -> client.getConnection(connection -> {
              if (connection.failed()) {
                promise.fail(connection.cause());
              } else {
                connection.result().close();
                promise.complete(Status.OK());
              }
            }));
          router.route("/health").handler(healthCheckHandler);
          var server = vertx.createHttpServer(new HttpServerOptions().setPort(9090))
            .requestHandler(router);
          return server.listen().mapEmpty();
        });
      var updateDbFuture = updateDB().onComplete((Handler<AsyncResult<Void>>) builderPromise).onComplete(startPromise);
      return updateDbFuture;
    }).mapEmpty();
  }

  public Future<Void> updateDB(){
    return Future.future(ft ->{
      Configuration config = new FluentConfiguration()
        .dataSource(datasourceConfig.jdbcUrl(), datasourceConfig.getUser(), datasourceConfig.getPassword());
      Flyway flyway = new Flyway(config);
      flyway.migrate();
      ft.complete();
    });
  }

  public Future<JsonObject> initConfig(){
    var configPath = System.getenv("VERTX_CONFIG_PATH");
    LOG.info("Config Path: " + configPath);
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", configPath));
    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
    return retriever.getConfig();
  }

}
