package tech.claudioed.users;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.tracing.opentracing.OpenTracingOptions;

public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args ){
    LOG.info("Starting users deployment...");
    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
      new MicrometerMetricsOptions()
        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
        .setEnabled(true))
      .setTracingOptions(new OpenTracingOptions()));
    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(new CreateUserInIdentityProvider());
    LOG.info("Users deployment was started successfully");
  }

}
