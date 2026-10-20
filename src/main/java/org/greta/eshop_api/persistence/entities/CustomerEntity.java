package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.greta.eshop_api.security.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "customer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// La cliente est aussi le compte de connexion : Spring Security la lit à travers UserDetails
public class CustomerEntity extends BaseEntity implements UserDetails {

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    // Le mot de passe rendu illisible par bcrypt, jamais le mot de passe lui-même
    @Column(nullable = false)
    private String passwordHash;

    // Colonne ajoutée par database/security.sql
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    // La base supprime les adresses avec la cliente (ON DELETE CASCADE) : JPA fait de même.
    // Ses avis et ses favoris aussi, côté base seulement : aucune collection à déclarer ici
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AddressEntity> addresses = new ArrayList<>();

    // Pas de REMOVE : une commande est une trace de facturation. La clé étrangère de
    // customer_order refuse de supprimer une cliente qui a commandé (ERROR 1451)
    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<CustomerOrderEntity> orders = new ArrayList<>();

    // Spring Security attend le préfixe ROLE_ : hasRole("USER") cherche "ROLE_USER"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    // L'email sert d'identifiant de connexion
    @Override
    public String getUsername() {
        return email;
    }
}
