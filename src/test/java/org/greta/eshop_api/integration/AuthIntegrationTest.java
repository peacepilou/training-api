package org.greta.eshop_api.integration;

import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.security.JwtService;
import org.greta.eshop_api.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class AuthIntegrationTest {

    private static final String REGISTER = """
            {"email":"jade.mercier@free.fr","password":"Secret123!","firstName":"Jade","lastName":"Mercier"}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private CustomerEntity persistCustomer(String email, String rawPassword, Role role) {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail(email);
        customer.setFirstName("Jade");
        customer.setLastName("Mercier");
        customer.setPasswordHash(passwordEncoder.encode(rawPassword));
        customer.setRole(role);
        return customerRepository.save(customer);
    }

    @Test
    void registerThenLoginThenReadOwnProfile() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        String login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("jade.mercier@free.fr", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = jsonMapper.readTree(login).get("token").asString();

        mockMvc.perform(get("/user/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jade.mercier@free.fr"));
    }

    @Test
    void tooShortPasswordIsRefusedAtRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER.replace("Secret123!", "x")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void wrongPasswordAnswers401() throws Exception {
        persistCustomer("jade.mercier@free.fr", "Secret123!", Role.USER);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("jade.mercier@free.fr", "faux")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants incorrects."));
    }

    @Test
    void missingTokenAnswers401() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise."));
    }

    @Test
    void expiredTokenAnswers401() throws Exception {
        CustomerEntity customer = persistCustomer("jade.mercier@free.fr", "Secret123!", Role.USER);
        // Même clé que l'application, mais une durée négative : le jeton est déjà expiré
        String expired = new JwtService(jwtSecret, -1000).generateToken(customer);

        mockMvc.perform(get("/user/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token expiré"));
    }

    @Test
    void userRoleCannotCreateACandle() throws Exception {
        CustomerEntity customer = persistCustomer("jade.mercier@free.fr", "Secret123!", Role.USER);
        String token = new JwtService(jwtSecret, 60_000).generateToken(customer);

        mockMvc.perform(post("/candles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès refusé pour ce rôle."));
    }
}
