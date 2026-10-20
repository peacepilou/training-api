package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

// La clé primaire composée de favorite : le couple (customer_id, candle_id)
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class FavoriteId implements Serializable {

    private Integer customerId;

    private Integer candleId;
}
