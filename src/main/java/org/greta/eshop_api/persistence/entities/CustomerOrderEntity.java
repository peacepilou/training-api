package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order") // 'order' est un mot réservé SQL
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Convert(converter = OrderStatusConverter.class)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    // LAZY explicite : par défaut, JPA charge un @ManyToOne tout de suite (EAGER)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    // Vide si la cliente retire sa commande à l'atelier : le 0,1 de SHIP TO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    // Une ligne n'a aucun sens hors de sa commande : tout se propage, comme le ON DELETE CASCADE de la base
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLineEntity> lines = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        orderedAt = LocalDateTime.now();
    }
}
