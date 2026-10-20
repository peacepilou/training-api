package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.FavoriteResponseDTO;
import org.greta.eshop_api.mappers.FavoriteMapper;
import org.greta.eshop_api.persistence.entities.FavoriteEntity;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.persistence.repositories.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CustomerRepository customerRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, CustomerRepository customerRepository) {
        this.favoriteRepository = favoriteRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Renvoie les bougies favorites d'une cliente.
     * @param customerId l'id de la cliente
     * @return ses favoris, éventuellement aucun
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si la cliente n'existe pas
     */
    public List<FavoriteResponseDTO> findByCustomer(Integer customerId) {
        checkCustomerExists(customerId);
        List<FavoriteResponseDTO> dtos = new ArrayList<>();
        for (FavoriteEntity favorite : favoriteRepository.findByCustomerId(customerId)) {
            dtos.add(FavoriteMapper.toDto(favorite));
        }
        return dtos;
    }

    private void checkCustomerExists(Integer customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("La cliente " + customerId + " n'existe pas.");
        }
    }
}
