package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.OrderLineRequestDTO;
import org.greta.eshop_api.exposition.dtos.OrderRequestDTO;
import org.greta.eshop_api.exposition.dtos.OrderResponseDTO;
import org.greta.eshop_api.mappers.OrderMapper;
import org.greta.eshop_api.persistence.entities.AddressEntity;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.greta.eshop_api.persistence.entities.OrderLineEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerOrderRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CandleRepository candleRepository;

    public OrderService(CustomerOrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        CandleRepository candleRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.candleRepository = candleRepository;
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

    public OrderResponseDTO create(OrderRequestDTO dto) {
        CustomerEntity customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("La cliente " + dto.customerId() + " n'existe pas."));

        CustomerOrderEntity order = new CustomerOrderEntity();
        order.setCustomer(customer);
        order.setAddress(findDeliveryAddress(customer, dto.addressId()));

        // La clé primaire de order_line est le couple (commande, bougie) : une bougie, une seule ligne
        Set<Integer> orderedCandleIds = new HashSet<>();
        for (OrderLineRequestDTO lineDto : dto.lines()) {
            if (!orderedCandleIds.add(lineDto.candleId())) {
                throw new BusinessRuleException("La bougie " + lineDto.candleId()
                        + " apparaît deux fois : regroupe les quantités sur une seule ligne.");
            }
            CandleEntity candle = candleRepository.findById(lineDto.candleId())
                    .orElseThrow(() -> new ResourceNotFoundException("La bougie " + lineDto.candleId() + " n'existe pas."));
            if (!candle.isOnSale()) {
                throw new BusinessRuleException("La bougie " + candle.getName() + " n'est pas en vente.");
            }

            if (candle.getStock() < lineDto.quantity()) {
                throw new BusinessRuleException("Stock insuffisant pour " + candle.getName()
                        + " : " + candle.getStock() + " en stock, " + lineDto.quantity() + " demandées.");
            }
            candle.setStock(candle.getStock() - lineDto.quantity());
            candleRepository.save(candle);

            OrderLineEntity line = new OrderLineEntity();
            line.setOrder(order);
            line.setCandle(candle);
            line.setQuantity(lineDto.quantity());
            line.setUnitPrice(candle.getPrice());
            order.getLines().add(line);
        }

        CustomerOrderEntity saved = orderRepository.save(order);
        return OrderMapper.toDto(saved);
    }

    // Sans adresse, retrait à l'atelier. Sinon, l'adresse doit être l'une de celles de la cliente
    private AddressEntity findDeliveryAddress(CustomerEntity customer, Integer addressId) {
        if (addressId == null) {
            return null;
        }
        for (AddressEntity address : customer.getAddresses()) {
            if (address.getId().equals(addressId)) {
                return address;
            }
        }
        throw new BusinessRuleException("L'adresse " + addressId + " n'est pas une adresse de cette cliente.");
    }
}
