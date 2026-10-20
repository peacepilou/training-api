package org.greta.eshop_api.mappers;

import org.greta.eshop_api.exposition.dtos.OrderLineResponseDTO;
import org.greta.eshop_api.exposition.dtos.OrderResponseDTO;
import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.greta.eshop_api.persistence.entities.OrderLineEntity;

import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    public static OrderResponseDTO toDto(CustomerOrderEntity order) {
        List<OrderLineResponseDTO> lines = new ArrayList<>();
        for (OrderLineEntity line : order.getLines()) {
            lines.add(new OrderLineResponseDTO(
                    line.getCandle().getId(),
                    line.getCandle().getName(),
                    line.getQuantity(),
                    line.getUnitPrice()
            ));
        }
        Integer addressId = order.getAddress() == null ? null : order.getAddress().getId();
        return new OrderResponseDTO(order.getId(), order.getStatus().name(), order.getCustomer().getId(), addressId, lines);
    }
}
