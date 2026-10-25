package org.greta.eshop_api.integration;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.greta.eshop_api.persistence.entities.OrderLineEntity;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Inès Moreau et Lucas Bernard, deux clients de seed.sql : chacun ses commandes
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class OrderIsolationIntegrationTest {

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

    private CustomerEntity ines;
    private CustomerEntity lucas;
    private String inesToken;
    private Integer lucasOrderId;
    private Integer candleId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        candleRepository.deleteAll();
        customerRepository.deleteAll();

        ines = customerRepository.save(customer("ines.moreau@gmail.com", "Inès", "Moreau"));
        lucas = customerRepository.save(customer("lucas.bernard@orange.fr", "Lucas", "Bernard"));
        inesToken = jwtService.generateToken(ines);

        CandleEntity candle = new CandleEntity();
        candle.setName("Cèdre et cuir");
        candle.setScent("Cèdre");
        candle.setSize("500 g");
        candle.setPrice(new BigDecimal("49.00"));
        candle.setStock(3);
        candle = candleRepository.save(candle);
        candleId = candle.getId();

        CustomerOrderEntity lucasOrder = new CustomerOrderEntity();
        lucasOrder.setCustomer(lucas);
        OrderLineEntity line = new OrderLineEntity();
        line.setOrder(lucasOrder);
        line.setCandle(candle);
        line.setQuantity(1);
        line.setUnitPrice(candle.getPrice());
        lucasOrder.getLines().add(line);
        lucasOrderId = orderRepository.save(lucasOrder).getId();
    }

    // Base H2 partagée entre les classes de test : les commandes laissées ici bloqueraient leurs deleteAll()
    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    private CustomerEntity customer(String email, String firstName, String lastName) {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPasswordHash(passwordEncoder.encode("Secret123!"));
        customer.setRole(Role.USER);
        return customer;
    }

    @Test
    void inesCannotReadLucasOrder() throws Exception {
        mockMvc.perform(get("/orders/" + lucasOrderId).header("Authorization", "Bearer " + inesToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("La commande " + lucasOrderId + " n'existe pas."));

        mockMvc.perform(get("/orders").header("Authorization", "Bearer " + inesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void inesCannotOrderForLucas() throws Exception {
        // Un customerId glissé dans le corps est ignoré : la cliente vient du jeton
        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + inesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + lucas.getId() + ",\"lines\":[{\"candleId\":" + candleId + ",\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(ines.getId()));

        mockMvc.perform(get("/orders").header("Authorization", "Bearer " + jwtService.generateToken(lucas)))
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
