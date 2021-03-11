package tech.claudioed.users;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import tech.claudioed.users.data.CreatedUser;
import tech.claudioed.users.data.NewUser;
import tech.claudioed.users.infra.IdentityProviderConfig;

import javax.ws.rs.core.Response;

public class CreateUserInIdentityProvider extends AbstractVerticle {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private Keycloak keycloak;

  @Override
  public void start(Promise<Void> startPromise) {
    initConfig().onSuccess(cfg ->{
      var identityProviderConfig = new IdentityProviderConfig(cfg.getJsonObject("idp"));
      this.vertx.executeBlocking(handler -> {
        LOG.info("Creating Identity Provider Client...");
        this.keycloak = KeycloakBuilder.builder()
          .serverUrl(identityProviderConfig.getServerUrl())
          .realm(identityProviderConfig.getRealm())
          .grantType(OAuth2Constants.PASSWORD)
          .clientId(identityProviderConfig.getClientId())
          .clientSecret(identityProviderConfig.getClientSecret())
          .username(identityProviderConfig.getUser())
          .password(identityProviderConfig.getPassword())
          .build();
        LOG.info("Identity Provider Client created successfully");
        handler.complete(keycloak);
      }).onSuccess(message -> {
        this.vertx.eventBus().consumer("request.create.user", handler -> {
          var newUser = Json.decodeValue(handler.body().toString(), NewUser.class);
          LOG.info("Starting user creation in IDP EMAIL: " + newUser.getEmail());
          UserRepresentation kcUser = new UserRepresentation();
          kcUser.setUsername(newUser.getEmail());
          kcUser.setFirstName(newUser.getFirstName());
          kcUser.setLastName(newUser.getLastName());
          kcUser.setEmail(newUser.getEmail());
          kcUser.setEnabled(Boolean.TRUE);
          LOG.info("Obtaining access token on IDP");
          var accessToken = keycloak.tokenManager().getAccessToken();
          LOG.info("Token was obtained successfully");
          RealmResource realmResource = keycloak.realm(identityProviderConfig.getRealm());
          UsersResource usersResource = realmResource.users();
          LOG.info("Creating user resource on IDP EMAIL: "+newUser.getEmail());
          Response response = usersResource.create(kcUser);
          var userId = CreatedResponseUtil.getCreatedId(response);
          LOG.info("User resource created successfully on IDP EMAIL: "+newUser.getEmail());
          LOG.info("Updating user resource credential on IDP EMAIL: "+newUser.getEmail());
          CredentialRepresentation passwordCred = new CredentialRepresentation();
          passwordCred.setTemporary(false);
          passwordCred.setType(CredentialRepresentation.PASSWORD);
          passwordCred.setValue(newUser.getPassword());
          UserResource userResource = usersResource.get(userId);
          userResource.resetPassword(passwordCred);
          LOG.info("User resource credential updated successfully on IDP EMAIL: "+newUser.getEmail());
          var userData = CreatedUser
            .createNew(userId, newUser.getFirstName(), newUser.getLastName(),
              newUser.getEmail());
          LOG.info("User created successfully in IDP EMAIL: " + newUser.getEmail());
          handler.reply(Json.encode(userData));
        });
        startPromise.complete();
      });
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
