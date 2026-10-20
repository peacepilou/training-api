package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_line")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderLineEntity {

    @EmbeddedId
    private OrderLineId id = new OrderLineId();

    // @MapsId : la partie orderId de la clé est la clé étrangère de cette relation
    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrderEntity order;

    @MapsId("candleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candle_id")
    private CandleEntity candle;

    @Column(nullable = false)
    private int quantity;

    // Le prix payé ce jour-là : celui de la bougie peut changer ensuite
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal unitPrice;
}
