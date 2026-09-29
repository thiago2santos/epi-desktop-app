package br.com.easynr6.gestaoepi.shared.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "easy-nr6.auth.provider", havingValue = "keycloak")
public class KeycloakAuthenticationProvider implements AuthenticationProvider {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KeycloakAuthenticationProvider.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient;
  private final String tokenEndpoint;
  private final String clientId;
  private final String clientSecret;

  public KeycloakAuthenticationProvider(
      @Value("${easy-nr6.auth.keycloak.base-url:http://localhost:8080}") String baseUrl,
      @Value("${easy-nr6.auth.keycloak.realm:easynr6}") String realm,
      @Value("${easy-nr6.auth.keycloak.client-id:easy-nr6-desktop}") String clientId,
      @Value("${easy-nr6.auth.keycloak.client-secret:}") String clientSecret) {
    this.httpClient = HttpClient.newHttpClient();
    this.tokenEndpoint =
        baseUrl.replaceAll("/+$", "")
            + "/realms/"
            + realm.trim()
            + "/protocol/openid-connect/token";
    this.clientId = clientId;
    this.clientSecret = clientSecret == null ? "" : clientSecret;
  }

  @Override
  public AuthenticationResult autenticar(String login, String senha) {
    String username = login == null ? "" : login.trim();
    String rawCredential = senha == null ? "" : senha;
    if (username.isBlank() || rawCredential.isBlank()) {
      return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
    }

    try {
      HttpResponse<String> response =
          httpClient.send(
              buildTokenRequest(username, rawCredential), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 400 || response.statusCode() == 401) {
        return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
      }
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        LOGGER.warn(
            "Falha inesperada no IAM provider. status={}, endpoint={}",
            response.statusCode(),
            tokenEndpoint);
        return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
      }

      JsonNode tokenPayload = decodeAccessTokenPayload(response.body());
      EnumSet<Papel> papeis = parseRoles(tokenPayload.path("realm_access").path("roles"));
      if (papeis.isEmpty()) {
        return AuthenticationResult.denied(AuthenticationStatus.NO_ROLE);
      }

      String preferredUsername = asTextOrDefault(tokenPayload.path("preferred_username"), username);
      String nome = asTextOrDefault(tokenPayload.path("name"), preferredUsername);
      Long userId = stableUserId(tokenPayload.path("sub").asText(preferredUsername));
      UsuarioAutenticado autenticado =
          new UsuarioAutenticado(userId, nome, preferredUsername, papeis);
      return AuthenticationResult.success(autenticado);
    } catch (IOException | InterruptedException ex) {
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      LOGGER.error("Erro ao autenticar no provider IAM Keycloak.", ex);
      return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
    }
  }

  private HttpRequest buildTokenRequest(String username, String password) {
    StringBuilder form =
        new StringBuilder()
            .append("grant_type=password")
            .append("&client_id=")
            .append(urlEncode(clientId))
            .append("&username=")
            .append(urlEncode(username))
            .append("&password=")
            .append(urlEncode(password));
    if (!clientSecret.isBlank()) {
      form.append("&client_secret=").append(urlEncode(clientSecret));
    }
    return HttpRequest.newBuilder(URI.create(tokenEndpoint))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
        .build();
  }

  private JsonNode decodeAccessTokenPayload(String responseBody) throws IOException {
    JsonNode responseJson = objectMapper.readTree(responseBody);
    String accessToken = responseJson.path("access_token").asText("");
    if (accessToken.isBlank()) {
      throw new IOException("Resposta do token sem access_token.");
    }
    String[] tokenParts = accessToken.split("\\.");
    if (tokenParts.length < 2) {
      throw new IOException("Formato de JWT invalido.");
    }
    byte[] payloadBytes = Base64.getUrlDecoder().decode(tokenParts[1]);
    return objectMapper.readTree(payloadBytes);
  }

  private static EnumSet<Papel> parseRoles(JsonNode rolesNode) {
    EnumSet<Papel> papeis = EnumSet.noneOf(Papel.class);
    if (rolesNode == null || !rolesNode.isArray()) {
      return papeis;
    }
    for (JsonNode roleNode : rolesNode) {
      String role = roleNode.asText("");
      try {
        papeis.add(Papel.valueOf(role.toUpperCase()));
      } catch (IllegalArgumentException ignored) {
        // Roles externas sem mapeamento local sao ignoradas.
      }
    }
    return papeis;
  }

  private static Long stableUserId(String subject) {
    long hash = Integer.toUnsignedLong(subject.hashCode());
    return hash == 0 ? 1L : hash;
  }

  private static String asTextOrDefault(JsonNode node, String fallback) {
    if (node == null) {
      return fallback;
    }
    String text = node.asText("");
    return text.isBlank() ? fallback : text;
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
