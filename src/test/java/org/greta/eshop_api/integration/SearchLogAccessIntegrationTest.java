package org.greta.eshop_api.integration;

import org.greta.eshop_api.exposition.dtos.SearchLogResponseDTO;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.security.JwtService;
import org.greta.eshop_api.security.Role;
import org.greta.eshop_api.services.SearchLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Qui lit le journal des recherches : personne sans jeton, pas une cliente, seulement Camille
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class SearchLogAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    // Le test porte sur la porte de la route, pas sur MongoDB : le journal est un faux
    @MockitoBean
    private SearchLogService searchLogService;

    private String camilleToken;
    private String inesToken;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        camilleToken = jwtService.generateToken(persistCustomer("camille@example.com", "Camille", "Renard", Role.ADMIN));
        inesToken = jwtService.generateToken(persistCustomer("ines.moreau@gmail.com", "Inès", "Moreau", Role.USER));
    }

    private CustomerEntity persistCustomer(String email, String firstName, String lastName, Role role) {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPasswordHash(passwordEncoder.encode("Secret123!"));
        customer.setRole(role);
        return customerRepository.save(customer);
    }

    @Test
    void noTokenAnswers401() throws Exception {
        mockMvc.perform(get("/admin/search-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise."));

        verify(searchLogService, never()).findLatest();
    }

    @Test
    void customerTokenAnswers403() throws Exception {
        mockMvc.perform(get("/admin/search-logs").header("Authorization", "Bearer " + inesToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès refusé pour ce rôle."));

        verify(searchLogService, never()).findLatest();
    }

    @Test
    void adminTokenReadsTheLog() throws Exception {
        when(searchLogService.findLatest()).thenReturn(List.of(
                new SearchLogResponseDTO("6ad73fe507da3156b3d3513b", LocalDateTime.now(), Map.of("keyword", "chocolat"), 0)));

        mockMvc.perform(get("/admin/search-logs").header("Authorization", "Bearer " + camilleToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filters.keyword").value("chocolat"));
    }
}
