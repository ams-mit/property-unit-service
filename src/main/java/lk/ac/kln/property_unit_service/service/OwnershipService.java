package lk.ac.kln.property_unit_service.service;

import lk.ac.kln.property_unit_service.exception.ExternalValidationException;
import lk.ac.kln.property_unit_service.exception.ShareLimitExceededException;
import lk.ac.kln.property_unit_service.model.Ownership;
import lk.ac.kln.property_unit_service.repository.OwnershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OwnershipService {

    private final OwnershipRepository ownershipRepository;
    private final RestTemplate restTemplate;
    private final String identityServiceUrl;

    @Autowired
    public OwnershipService(OwnershipRepository ownershipRepository,
                            RestTemplate restTemplate,
                            @Value("${group1.identity.service.url}") String identityServiceUrl) {
        this.ownershipRepository = ownershipRepository;
        this.restTemplate = restTemplate;
        this.identityServiceUrl = identityServiceUrl;
    }

    public Ownership createOwnership(Ownership ownership) {
        // a. Make a GET request using RestTemplate to validate owner
        String validateUrl = identityServiceUrl + "/api/v1/internal/owners/" + ownership.getOwnerId() + "/validate";
        
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(validateUrl, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ExternalValidationException("External validation failed for owner: " + ownership.getOwnerId());
            }
        } catch (RestClientException e) {
            // b. If the external validation fails, throw a custom exception.
            throw new ExternalValidationException("Failed to validate owner: " + ownership.getOwnerId(), e);
        }

        // c. Fetch all existing ownerships for the given unitId from the repository. Sum their sharePercentage.
        List<Ownership> existingOwnerships = ownershipRepository.findByUnitId(ownership.getUnitId());
        
        BigDecimal currentTotalShare = existingOwnerships.stream()
                .map(Ownership::getSharePercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // d. If the new sharePercentage + the existing sum > 100.00, throw a ShareLimitExceededException.
        BigDecimal newTotalShare = currentTotalShare.add(ownership.getSharePercentage());
        if (newTotalShare.compareTo(new BigDecimal("100.00")) > 0) {
            throw new ShareLimitExceededException("Total share percentage cannot exceed 100.00. Current total: " 
                    + currentTotalShare + ", Attempted to add: " + ownership.getSharePercentage());
        }

        // e. Save and return the ownership.
        return ownershipRepository.save(ownership);
    }

    public List<Ownership> getOwnershipsByUnitId(Long unitId) {
        return ownershipRepository.findByUnitId(unitId);
    }

    public List<Ownership> getOwnershipsByOwnerId(String ownerId) {
        return ownershipRepository.findByOwnerId(ownerId);
    }
}
