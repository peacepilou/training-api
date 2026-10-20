package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    // Le mot de passe rendu illisible par bcrypt, jamais le mot de passe lui-même
    @Column(nullable = false)
    private String passwordHash;

    // La base supprime les adresses avec la cliente (ON DELETE CASCADE) : JPA fait de même.
    // Ses avis et ses favoris aussi, côté base seulement : aucune collection à déclarer ici
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AddressEntity> addresses = new ArrayList<>();

    // Pas de REMOVE : une commande est une trace de facturation. La clé étrangère de
    // customer_order refuse de supprimer une cliente qui a commandé (ERROR 1451)
    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<CustomerOrderEntity> orders = new ArrayList<>();
}
