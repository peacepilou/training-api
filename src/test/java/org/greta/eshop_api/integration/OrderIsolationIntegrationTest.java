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

// Léa Garnier et Hugo Lefebvre, deux clientes de seed.sql : chacun ses commandes
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

    private CustomerEntity lea;
    private CustomerEntity hugo;
    private String leaToken;
    private Integer hugoOrderId;
    private Integer candleId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        candleRepository.deleteAll();
        customerRepository.deleteAll();

        lea = customerRepository.save(customer("lea.garnier@laposte.net", "Léa", "Garnier"));
        hugo = customerRepository.save(customer("hugo.lefebvre@gmail.com", "Hugo", "Lefebvre"));
        leaToken = jwtService.generateToken(lea);

        CandleEntity candle = new CandleEntity();
        candle.setName("Cèdre et cuir");
        candle.setScent("Cèdre");
        candle.setSize("500 g");
        candle.setPrice(new BigDecimal("49.00"));
        candle.setStock(3);
        candle = candleRepository.save(candle);
        candleId = candle.getId();

        CustomerOrderEntity hugoOrder = new CustomerOrderEntity();
        hugoOrder.setCustomer(hugo);
        OrderLineEntity line = new OrderLineEntity();
        line.setOrder(hugoOrder);
        line.setCandle(candle);
        line.setQuantity(1);
        line.setUnitPrice(candle.getPrice());
        hugoOrder.getLines().add(line);
        hugoOrderId = orderRepository.save(hugoOrder).getId();
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
    void leaCannotReadHugosOrder() throws Exception {
        // 404 et pas 403 : Léa n'apprend même pas que la commande existe
        mockMvc.perform(get("/orders/" + hugoOrderId).header("Authorization", "Bearer " + leaToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("La commande " + hugoOrderId + " n'existe pas."));

        mockMvc.perform(get("/orders").header("Authorization", "Bearer " + leaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void leaCannotOrderForHugo() throws Exception {
        // Un customerId glissé dans le corps est ignoré : la cliente vient du jeton
        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + leaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + hugo.getId() + ",\"lines\":[{\"candleId\":" + candleId + ",\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(lea.getId()));

        mockMvc.perform(get("/orders").header("Authorization", "Bearer " + jwtService.generateToken(hugo)))
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
