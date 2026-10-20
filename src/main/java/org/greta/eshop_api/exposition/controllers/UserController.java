package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.exposition.dtos.UserResponseDTO;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal CustomerEntity customer) {
        UserResponseDTO response = new UserResponseDTO(customer.getId(), customer.getEmail(), customer.getRole().name());
        return ResponseEntity.ok(response);
    }
}
