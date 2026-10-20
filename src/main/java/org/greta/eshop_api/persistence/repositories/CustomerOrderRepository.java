package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrderEntity, Integer> {

    // LEFT : une commande retirée à l'atelier n'a pas d'adresse, un JOIN simple l'écarterait de la liste
    @Query("SELECT o FROM CustomerOrderEntity o JOIN FETCH o.customer LEFT JOIN FETCH o.address")
    List<CustomerOrderEntity> findAllWithCustomerAndAddress();

    @Query("SELECT o FROM CustomerOrderEntity o JOIN FETCH o.customer LEFT JOIN FETCH o.address WHERE o.customer.id = :customerId")
    List<CustomerOrderEntity> findByCustomerIdWithCustomerAndAddress(@Param("customerId") Integer customerId);
}
