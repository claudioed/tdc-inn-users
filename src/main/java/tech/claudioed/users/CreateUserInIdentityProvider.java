package tech.claudioed.users;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
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

  private Keycloak keycloak;

  @Override
  public void start(Promise<Void> startPromise) {
    initConfig().onSuccess(cfg ->{
      var identityProviderConfig = new IdentityProviderConfig(cfg.getJsonObject("idp"));
      this.vertx.executeBlocking(handler -> {
        this.keycloak = KeycloakBuilder.builder()
          .serverUrl(identityProviderConfig.getServerUrl())
          .realm(identityProviderConfig.getRealm())
          .grantType(OAuth2Constants.PASSWORD)
          .clientId(identityProviderConfig.getClientId())
          .clientSecret(identityProviderConfig.getClientSecret())
          .username(identityProviderConfig.getUser())
          .password(identityProviderConfig.getPassword())
          .build();
        handler.complete(keycloak);
      }).onSuccess(message -> {
        this.vertx.eventBus().consumer("request.create.user", handler -> {
          var newUser = Json.decodeValue(handler.body().toString(), NewUser.class);
          UserRepresentation kcUser = new UserRepresentation();
          kcUser.setUsername(newUser.getEmail());
          kcUser.setFirstName(newUser.getFirstName());
          kcUser.setLastName(newUser.getLastName());
          kcUser.setEmail(newUser.getEmail());
          kcUser.setEnabled(Boolean.TRUE);
          var accessToken = keycloak.tokenManager().getAccessToken();
          RealmResource realmResource = keycloak.realm(identityProviderConfig.getRealm());
          UsersResource usersResource = realmResource.users();
          Response response = usersResource.create(kcUser);

          var userId = CreatedResponseUtil.getCreatedId(response);

          CredentialRepresentation passwordCred = new CredentialRepresentation();
          passwordCred.setTemporary(false);
          passwordCred.setType(CredentialRepresentation.PASSWORD);
          passwordCred.setValue(newUser.getPassword());
          UserResource userResource = usersResource.get(userId);
          userResource.resetPassword(passwordCred);
          var userData = CreatedUser
            .createNew(userId, newUser.getFirstName(), newUser.getLastName(),
              newUser.getEmail());

          handler.reply(Json.encode(userData));
        });
        startPromise.complete();
      });
    });
  }

  public Future<JsonObject> initConfig(){
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "src/main/resources/config.json"));

    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
    return retriever.getConfig();
  }

}
