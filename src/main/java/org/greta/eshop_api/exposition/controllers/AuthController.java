package org.greta.eshop_api.exposition.controllers;

import jakarta.validation.Valid;
import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exposition.dtos.LoginRequestDTO;
import org.greta.eshop_api.exposition.dtos.LoginResponseDTO;
import org.greta.eshop_api.exposition.dtos.RegisterRequestDTO;
import org.greta.eshop_api.exposition.dtos.UserResponseDTO;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.security.JwtService;
import org.greta.eshop_api.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomerRepository customerRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Cet email est déjà utilisé.");
        }
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail(request.email());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setRole(Role.USER);
        CustomerEntity saved = customerRepository.save(customer);

        UserResponseDTO response = new UserResponseDTO(saved.getId(), saved.getEmail(), saved.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // Lève BadCredentialsException si l'email ou le mot de passe ne correspond pas
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        CustomerEntity customer = (CustomerEntity) authentication.getPrincipal();
        String token = jwtService.generateToken(customer);

        LoginResponseDTO response = new LoginResponseDTO(token, customer.getEmail(), customer.getRole().name());
        return ResponseEntity.ok(response);
    }
}
