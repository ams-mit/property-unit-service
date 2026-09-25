package lk.ac.kln.property_unit_service.repository;

import lk.ac.kln.property_unit_service.model.Ownership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnershipRepository extends JpaRepository<Ownership, Long> {
    List<Ownership> findByUnitId(Long unitId);
    List<Ownership> findByOwnerId(String ownerId);
}
