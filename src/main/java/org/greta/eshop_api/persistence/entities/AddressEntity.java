package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddressEntity extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String street;

    // CHAR(5) en base : en nombre, 01000 perdrait son zéro
    @Column(nullable = false, length = 5)
    private String postcode;

    @Column(nullable = false, length = 80)
    private String city;

    // LAZY explicite : par défaut, JPA charge un @ManyToOne tout de suite (EAGER)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
}
