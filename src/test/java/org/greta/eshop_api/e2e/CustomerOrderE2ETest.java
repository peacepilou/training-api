package org.greta.eshop_api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// Vrai serveur sur un port libre, vraie base MySQL dans Docker, vraies requêtes HTTP
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers
class CustomerOrderE2ETest {

    // Les scripts de database/, joués par MySQL à la création du conteneur, dans l'ordre de leur nom :
    // la base du test est celle du cours, et ddl-auto=validate la vérifie au démarrage
    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4")
            .withDatabaseName("candle_shop")
            .withCopyFileToContainer(MountableFile.forHostPath("database/schema.sql"), "/docker-entrypoint-initdb.d/1-schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath("database/seed.sql"), "/docker-entrypoint-initdb.d/2-seed.sql")
            .withCopyFileToContainer(MountableFile.forHostPath("database/security.sql"), "/docker-entrypoint-initdb.d/3-security.sql");

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper jsonMapper;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void registerLoginThenOrder() throws Exception {
        HttpResponse<String> register = send("POST", "/auth/register", null,
                "{\"email\":\"noah.richard@example.com\",\"password\":\"Secret123!\",\"firstName\":\"Noah\",\"lastName\":\"Richard\"}");
        assertThat(register.statusCode()).isEqualTo(201);
        int customerId = jsonMapper.readTree(register.body()).get("id").asInt();

        HttpResponse<String> login = send("POST", "/auth/login", null,
                "{\"email\":\"noah.richard@example.com\",\"password\":\"Secret123!\"}");
        assertThat(login.statusCode()).isEqualTo(200);
        String token = jsonMapper.readTree(login.body()).get("token").asString();

        // Sans jeton, la route protégée refuse
        assertThat(send("POST", "/orders", null, "{}").statusCode()).isEqualTo(401);

        // La bougie 2, Feu de bois, vient de seed.sql : 8 en stock. Sans adresse : retrait à l'atelier
        HttpResponse<String> order = send("POST", "/orders", token,
                "{\"lines\":[{\"candleId\":2,\"quantity\":2}]}");
        assertThat(order.statusCode()).isEqualTo(201);
        JsonNode created = jsonMapper.readTree(order.body());
        assertThat(created.get("status").asString()).isEqualTo("PENDING");
        assertThat(created.get("customerId").asInt()).isEqualTo(customerId);
        assertThat(created.get("lines").get(0).get("quantity").asInt()).isEqualTo(2);

        HttpResponse<String> candle = send("GET", "/candles/2", null, null);
        assertThat(jsonMapper.readTree(candle.body()).get("stock").asInt()).isEqualTo(6);
    }
}
