package org.greta.eshop_api.integration;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.ReviewEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerOrderRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.persistence.repositories.ReviewRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Inès dépose un avis sur Lavande de Provence ; Hugo sert de « faux auteur » glissé dans le corps
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class ReviewIntegrationTest {

    private static final String REVIEW = "{\"rating\":5,\"comment\":\"Elle sent la Provence jusque dans le couloir.\"}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReviewRepository reviewRepository;
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
    private CustomerEntity hugo;
    private String inesToken;
    private CandleEntity lavender;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        candleRepository.deleteAll();
        customerRepository.deleteAll();

        ines = customerRepository.save(customer("ines.moreau@gmail.com", "Inès", "Moreau"));
        hugo = customerRepository.save(customer("hugo.lefebvre@gmail.com", "Hugo", "Lefebvre"));
        inesToken = jwtService.generateToken(ines);

        lavender = new CandleEntity();
        lavender.setName("Lavande de Provence");
        lavender.setScent("Lavande");
        lavender.setSize("200 g");
        lavender.setPrice(new BigDecimal("22.00"));
        lavender.setStock(10);
        lavender = candleRepository.save(lavender);
    }

    // Base H2 partagée entre les classes de test : les avis laissés ici bloqueraient leurs deleteAll()
    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
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
    void postingWithoutTokenAnswers401() throws Exception {
        mockMvc.perform(post("/candles/" + lavender.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REVIEW))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise."));

        assertEquals(0, reviewRepository.count());
    }

    @Test
    void theAuthorComesFromTheTokenNotFromTheBody() throws Exception {
        // Le corps prétend que Hugo écrit : le DTO n'a pas de customerId, Jackson l'ignore
        String claimsHugo = "{\"customerId\":" + hugo.getId() + ",\"rating\":5,\"comment\":\"Elle sent la Provence jusque dans le couloir.\"}";

        mockMvc.perform(post("/candles/" + lavender.getId() + "/reviews")
                        .header("Authorization", "Bearer " + inesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(claimsHugo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("Inès"))
                .andExpect(jsonPath("$.approved").value(false));

        // La base confirme : une seule ligne, et elle est à Inès
        List<ReviewEntity> reviews = reviewRepository.findAll();
        assertEquals(1, reviews.size());
        assertEquals(ines.getId(), reviews.get(0).getCustomer().getId());
    }

    @Test
    void ratingOutOfBoundsAnswers400() throws Exception {
        mockMvc.perform(post("/candles/" + lavender.getId() + "/reviews")
                        .header("Authorization", "Bearer " + inesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REVIEW.replace("\"rating\":5", "\"rating\":6")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.rating").value("La note va de 1 à 5"));

        mockMvc.perform(post("/candles/" + lavender.getId() + "/reviews")
                        .header("Authorization", "Bearer " + inesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REVIEW.replace("\"rating\":5", "\"rating\":0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.rating").value("La note va de 1 à 5"));

        assertEquals(0, reviewRepository.count());
    }

    @Test
    void anyoneReadsTheApprovedReviewsOnly() throws Exception {
        reviewRepository.save(review(hugo, 4, "Douce et fraîche.", true));
        reviewRepository.save(review(ines, 5, "Pas encore relu par Camille.", false));

        mockMvc.perform(get("/candles/" + lavender.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author").value("Hugo"));
    }

    private ReviewEntity review(CustomerEntity author, int rating, String comment, boolean approved) {
        ReviewEntity review = new ReviewEntity();
        review.setCustomer(author);
        review.setCandle(lavender);
        review.setRating(rating);
        review.setComment(comment);
        review.setApproved(approved);
        return review;
    }
}
