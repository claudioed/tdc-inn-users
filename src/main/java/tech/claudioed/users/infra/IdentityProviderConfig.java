package tech.claudioed.users.infra;

import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;

public class IdentityProviderConfig {

  private final String serverUrl;
  private final String realm;
  private final String clientId;
  private final String clientSecret;
  private final String user;
  private final String password;

  public IdentityProviderConfig(JsonObject datasourceConfig) {
    serverUrl = datasourceConfig.getString("serverUrl", "http://localhost:8080/");
    realm = datasourceConfig.getString("realm", "tdc-innovation");
    clientId = datasourceConfig.getString("client_id", "realm-management");
    clientSecret = datasourceConfig.getString("client_secret", "6f97aa24-c4c2-47c0-b2da-b9e5750fe050");
    user = datasourceConfig.getString("user", "admin");
    password = datasourceConfig.getString("password", "admin");
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getRealm() {
    return realm;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}

