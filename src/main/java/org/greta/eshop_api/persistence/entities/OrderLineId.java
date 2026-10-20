package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

// La clé primaire composée de order_line : le couple (order_id, candle_id)
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderLineId implements Serializable {

    private Integer orderId;

    private Integer candleId;
}
