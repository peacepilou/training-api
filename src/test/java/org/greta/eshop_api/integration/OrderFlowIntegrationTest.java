package org.greta.eshop_api.integration;

import org.greta.eshop_api.persistence.entities.AddressEntity;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerOrderRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.security.JwtService;
import org.greta.eshop_api.security.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerOrderRepository orderRepository;
    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private String inesToken;
    private Integer customerId;
    private Integer addressId;
    private Integer candleId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        candleRepository.deleteAll();
        customerRepository.deleteAll();

        CustomerEntity ines = new CustomerEntity();
        ines.setEmail("ines.moreau@gmail.com");
        ines.setFirstName("Inès");
        ines.setLastName("Moreau");
        ines.setPasswordHash(passwordEncoder.encode("Secret123!"));
        ines.setRole(Role.USER);
        AddressEntity address = new AddressEntity();
        address.setStreet("12 rue Colbert");
        address.setPostcode("37000");
        address.setCity("Tours");
        address.setCustomer(ines);
        ines.getAddresses().add(address);
        CustomerEntity saved = customerRepository.save(ines);
        customerId = saved.getId();
        addressId = saved.getAddresses().get(0).getId();
        inesToken = jwtService.generateToken(saved);

        CandleEntity candle = new CandleEntity();
        candle.setName("Coton frais");
        candle.setScent("Linge propre");
        candle.setSize("200 g");
        candle.setPrice(new BigDecimal("19.90"));
        candle.setStock(22);
        candleId = candleRepository.save(candle).getId();
    }

    // Le contexte Spring et sa base H2 sont partagés entre les classes de test : une commande laissée ici
    // ferait échouer le candleRepository.deleteAll() de CandleFlowIntegrationTest (clé étrangère)
    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    private String orderBody(int quantity) {
        return "{\"addressId\":" + addressId + ",\"lines\":[{\"candleId\":" + candleId
                + ",\"quantity\":" + quantity + "}]}";
    }

    @Test
    void inesOrdersThreeCandlesAndTheStockDrops() throws Exception {
        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + inesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.lines[0].quantity").value(3));

        assertThat(candleRepository.findById(candleId).orElseThrow().getStock()).isEqualTo(19);
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void orderWithoutTokenAnswers401() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(3)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise."));

        assertThat(candleRepository.findById(candleId).orElseThrow().getStock()).isEqualTo(22);
        assertThat(orderRepository.count()).isZero();
    }
}
