package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Une association N-N avec un attribut (added_at) : une entité à part entière, pas un @ManyToMany
@Entity
@Table(name = "favorite")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FavoriteEntity {

    @EmbeddedId
    private FavoriteId id = new FavoriteId();

    @MapsId("customerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @MapsId("candleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candle_id")
    private CandleEntity candle;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    public void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
