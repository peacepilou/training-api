package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// La colonne status range le statut en minuscules ('pending', 'shipped'…), comme seed.sql
@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        return status.name().toLowerCase();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String value) {
        return OrderStatus.valueOf(value.toUpperCase());
    }
}
