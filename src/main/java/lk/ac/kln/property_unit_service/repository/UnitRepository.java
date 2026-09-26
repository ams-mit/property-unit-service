package lk.ac.kln.property_unit_service.repository;

import lk.ac.kln.property_unit_service.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
}
