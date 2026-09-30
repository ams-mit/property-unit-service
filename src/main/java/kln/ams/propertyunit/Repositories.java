package kln.ams.propertyunit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

interface Buildings extends JpaRepository<Building,Long>, JpaSpecificationExecutor<Building> {
    boolean existsByBuildingCodeIgnoreCase(String code);
    Optional<Building> findByPublicId(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from Building b where b.publicId=:id") Optional<Building> lockByPublicId(UUID id);
}
interface Floors extends JpaRepository<Floor,Long> {
    Optional<Floor> findByPublicId(UUID id);
}
interface UnitTypes extends JpaRepository<UnitType,Long>, JpaSpecificationExecutor<UnitType> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<UnitType> findByPublicId(UUID id);
}
interface Units extends JpaRepository<Unit,Long>, JpaSpecificationExecutor<Unit> {
    Optional<Unit> findByPublicId(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from Unit u where u.publicId=:id") Optional<Unit> lockByPublicId(UUID id);
    boolean existsByFloorBuildingIdAndUnitNumberIgnoreCase(Long buildingId,String unitNumber);
}
interface Ownerships extends JpaRepository<Ownership,Long>, JpaSpecificationExecutor<Ownership> {
    List<Ownership> findByUnitId(Long unitId);
    List<Ownership> findByOwnerId(UUID ownerId);
}
