package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrderEntity, Integer> {
}
