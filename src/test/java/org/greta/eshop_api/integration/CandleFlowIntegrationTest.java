package org.greta.eshop_api.integration;

import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.security.JwtService;
import org.greta.eshop_api.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pas de @Transactional sur la classe : chaque requête est validée en base comme en vrai,
// la base est vidée à la main avant chaque test
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class CandleFlowIntegrationTest {

    private static final String CANDLE = """
            {"name":"Sapin des Vosges","scent":"Sapin","size":"500 g","price":48.00,"stock":5,"onSale":true}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        candleRepository.deleteAll();
        customerRepository.deleteAll();
        CustomerEntity camille = new CustomerEntity();
        camille.setEmail("camille@example.com");
        camille.setFirstName("Camille");
        camille.setLastName("Renard");
        camille.setPasswordHash(passwordEncoder.encode("Admin123!"));
        camille.setRole(Role.ADMIN);
        adminToken = jwtService.generateToken(customerRepository.save(camille));
    }

    @Test
    void adminCreatesReadsUpdatesAndDeletesACandle() throws Exception {
        // Création : les POST passent sans with(csrf()), la SecurityConfig coupe le CSRF
        String created = mockMvc.perform(post("/candles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDLE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sapin des Vosges"))
                .andReturn().getResponse().getContentAsString();
        int id = jsonMapper.readTree(created).get("id").asInt();

        // Lecture publique, sans jeton
        mockMvc.perform(get("/candles/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(5));

        mockMvc.perform(put("/candles/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDLE.replace("\"stock\":5", "\"stock\":2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(2));
        assertThat(candleRepository.findById(id).orElseThrow().getStock()).isEqualTo(2);

        mockMvc.perform(delete("/candles/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        assertThat(candleRepository.existsById(id)).isFalse();
    }

    @Test
    void invalidCandleIsRejectedWithFieldErrors() throws Exception {
        mockMvc.perform(post("/candles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDLE.replace("\"500 g\"", "\"un gros\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.size").value("La taille s'écrit en grammes, par exemple 200 g"));
        assertThat(candleRepository.count()).isZero();
    }

    @Test
    void businessRuleAnswers400() throws Exception {
        mockMvc.perform(post("/candles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDLE.replace("48.00", "10000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le prix ne peut pas dépasser 9 999,99 €."));
    }

    @Test
    void sqlInjectionInTheNameSearchFindsNothing() throws Exception {
        mockMvc.perform(post("/candles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDLE))
                .andExpect(status().isCreated());

        // Requête paramétrée : la saisie reste une valeur, cherchée telle quelle dans les noms
        mockMvc.perform(get("/candles/search").param("keyword", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void missingCandleAnswers404() throws Exception {
        mockMvc.perform(get("/candles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("La bougie 999 n'existe pas."));
    }
}
