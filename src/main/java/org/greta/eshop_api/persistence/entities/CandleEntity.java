package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "candle")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CandleEntity extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40)
    private String scent;

    // Le poids de cire, écrit comme sur l'étiquette : "200 g"
    @Column(nullable = false, length = 10)
    private String size;

    // L'argent en BigDecimal : un double ne sait pas écrire 0.1 exactement
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean onSale = true;
}
