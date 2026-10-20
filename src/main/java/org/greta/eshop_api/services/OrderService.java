package org.greta.eshop_api.services;

import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.greta.eshop_api.persistence.repositories.CustomerOrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;

    public OrderService(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<String> findAllSummaries() {
        List<CustomerOrderEntity> orders = orderRepository.findAllWithCustomerAndAddress();
        List<String> summaries = new ArrayList<>();
        for (CustomerOrderEntity order : orders) {
            String customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
            String delivery = order.getAddress() == null ? "retrait à l'atelier" : "livraison à " + order.getAddress().getCity();
            summaries.add("Commande " + order.getId() + " : " + customerName + ", " + delivery + " (" + order.getStatus() + ")");
        }
        return summaries;
    }
}
