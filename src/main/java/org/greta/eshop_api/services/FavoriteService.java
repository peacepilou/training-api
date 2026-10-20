package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.FavoriteRequestDTO;
import org.greta.eshop_api.exposition.dtos.FavoriteResponseDTO;
import org.greta.eshop_api.mappers.FavoriteMapper;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.FavoriteEntity;
import org.greta.eshop_api.persistence.entities.FavoriteId;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.persistence.repositories.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CustomerRepository customerRepository;
    private final CandleRepository candleRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, CustomerRepository customerRepository,
                           CandleRepository candleRepository) {
        this.favoriteRepository = favoriteRepository;
        this.customerRepository = customerRepository;
        this.candleRepository = candleRepository;
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

    /**
     * Ajoute une bougie aux favoris d'une cliente.
     * @param customerId l'id de la cliente
     * @param dto la bougie à ajouter
     * @return le favori enregistré, avec sa date d'ajout
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si la cliente ou la bougie n'existe pas
     * @throws org.greta.eshop_api.exceptions.BusinessRuleException si la bougie est déjà dans ses favoris
     */
    public FavoriteResponseDTO add(Integer customerId, FavoriteRequestDTO dto) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("La cliente " + customerId + " n'existe pas."));
        CandleEntity candle = candleRepository.findById(dto.candleId())
                .orElseThrow(() -> new ResourceNotFoundException("La bougie " + dto.candleId() + " n'existe pas."));

        FavoriteId id = new FavoriteId(customerId, dto.candleId());
        // Sans ce test, save() voit un favori existant et tente un UPDATE : la base le refuse, et c'est un 500
        if (favoriteRepository.existsById(id)) {
            throw new BusinessRuleException("La bougie " + dto.candleId() + " est déjà dans les favoris de la cliente " + customerId + ".");
        }

        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setId(id);
        favorite.setCustomer(customer);
        favorite.setCandle(candle);
        FavoriteEntity saved = favoriteRepository.save(favorite);
        return FavoriteMapper.toDto(saved);
    }

    /**
     * Retire une bougie des favoris d'une cliente.
     * @param customerId l'id de la cliente
     * @param candleId l'id de la bougie
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si la bougie n'est pas dans ses favoris
     */
    public void remove(Integer customerId, Integer candleId) {
        FavoriteId id = new FavoriteId(customerId, candleId);
        if (!favoriteRepository.existsById(id)) {
            throw new ResourceNotFoundException("La bougie " + candleId + " n'est pas dans les favoris de la cliente " + customerId + ".");
        }
        favoriteRepository.deleteById(id);
    }

    private void checkCustomerExists(Integer customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("La cliente " + customerId + " n'existe pas.");
        }
    }
}
