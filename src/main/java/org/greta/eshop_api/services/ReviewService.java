package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.ReviewRequestDTO;
import org.greta.eshop_api.exposition.dtos.ReviewResponseDTO;
import org.greta.eshop_api.mappers.ReviewMapper;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.ReviewEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.persistence.repositories.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final CandleRepository candleRepository;

    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository,
                         CandleRepository candleRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.candleRepository = candleRepository;
    }

    /**
     * Renvoie les avis publiés d'une bougie, du plus récent au plus ancien.
     * @param candleId l'id de la bougie
     * @return ses avis relus par Camille, éventuellement aucun
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si la bougie n'existe pas
     */
    public List<ReviewResponseDTO> findApprovedByCandle(Integer candleId) {
        if (!candleRepository.existsById(candleId)) {
            throw new ResourceNotFoundException("La bougie " + candleId + " n'existe pas.");
        }
        List<ReviewResponseDTO> dtos = new ArrayList<>();
        for (ReviewEntity review : reviewRepository.findApprovedByCandleId(candleId)) {
            dtos.add(ReviewMapper.toDto(review));
        }
        return dtos;
    }

    /**
     * Enregistre l'avis d'une cliente sur une bougie, en attente de relecture.
     * @param customerId l'id de la cliente, lu dans son jeton
     * @param candleId l'id de la bougie
     * @param dto la note et le commentaire
     * @return l'avis enregistré, non publié
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si la cliente ou la bougie n'existe pas
     */
    public ReviewResponseDTO create(Integer customerId, Integer candleId, ReviewRequestDTO dto) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("La cliente " + customerId + " n'existe pas."));
        CandleEntity candle = candleRepository.findById(candleId)
                .orElseThrow(() -> new ResourceNotFoundException("La bougie " + candleId + " n'existe pas."));

        ReviewEntity review = new ReviewEntity();
        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setCustomer(customer);
        review.setCandle(candle);
        ReviewEntity saved = reviewRepository.save(review);
        return ReviewMapper.toDto(saved);
    }
}
