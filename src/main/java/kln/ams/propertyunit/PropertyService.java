package kln.ams.propertyunit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PropertyService {
    private final Buildings buildings; private final Floors floors; private final UnitTypes types;
    private final Units units; private final Ownerships ownerships; private final ResidentClient residents;
    PropertyService(Buildings buildings,Floors floors,UnitTypes types,Units units,Ownerships ownerships,ResidentClient residents) {
        this.buildings=buildings;this.floors=floors;this.types=types;this.units=units;this.ownerships=ownerships;this.residents=residents;
    }
    static PageRequest page(int page,int size) {
        if(page<0 || size<1 || size>100) throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","page must be nonnegative and size must be 1 to 100");
        return PageRequest.of(page,size);
    }
    static Pagination pagination(Page<?> result) { return new Pagination(result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages(),result.hasNext(),result.hasPrevious()); }
    static boolean available(Unit u) { return u.status==UnitStatus.AVAILABLE && u.getFloor().getStatus()==RecordStatus.ACTIVE && u.getFloor().getBuilding().getStatus()==RecordStatus.ACTIVE; }
    static FloorView view(Floor f) {return new FloorView(f.publicId,f.getBuilding().getPublicId(),f.floorNumber,f.name,f.description,f.status);}
    static BuildingView view(Building b) {return new BuildingView(b.publicId,b.buildingCode,b.name,b.address,b.description,b.status,b.floors.stream().map(PropertyService::view).toList(),b.createdAt,b.updatedAt);}
    static UnitTypeView view(UnitType t) {return new UnitTypeView(t.publicId,t.code,t.name,t.description,t.capacity,t.status,t.createdAt,t.updatedAt);}
    static UnitView view(Unit u) {return new UnitView(u.publicId,u.unitNumber,u.getFloor().getBuilding().getPublicId(),u.getFloor().getPublicId(),u.getUnitType().getPublicId(),u.status,available(u),u.createdAt,u.updatedAt);}
    static OwnershipView view(Ownership o) {return new OwnershipView(o.publicId,o.getUnit().getPublicId(),o.ownerId,o.ownershipPercentage,o.startDate,o.endDate,o.status,o.createdAt,o.updatedAt);}

    @Transactional BuildingView create(BuildingInput input) {
        if(buildings.existsByBuildingCodeIgnoreCase(input.buildingCode())) throw conflict("BUILDING_ALREADY_EXISTS","Building code already exists");
        Building b=new Building();b.buildingCode=input.buildingCode().trim();b.name=input.name().trim();b.address=input.address().trim();b.description=input.description();
        if(input.floors()!=null) {
            var numbers=new HashSet<Integer>();
            for(FloorInput item:input.floors()) {
                if(!numbers.add(item.floorNumber())) throw conflict("VALIDATION_ERROR","Duplicate floor number");
                Floor f=new Floor();f.building=b;f.floorNumber=item.floorNumber();f.name=item.name();f.description=item.description();b.floors.add(f);
            }
        }
        return view(buildings.saveAndFlush(b));
    }
    @Transactional(readOnly=true) Page<BuildingView> buildings(int page,int size,String search,RecordStatus status) {
        Specification<Building> spec=(r,q,b)->b.conjunction();
        if(search!=null&&!search.isBlank()) {String pattern="%"+search.toLowerCase()+"%";spec=spec.and((r,q,b)->b.or(b.like(b.lower(r.get("name")),pattern),b.like(b.lower(r.get("buildingCode")),pattern)));}
        if(status!=null) spec=spec.and((r,q,b)->b.equal(r.get("status"),status));
        return buildings.findAll(spec,page(page,size)).map(PropertyService::view);
    }
    @Transactional UnitTypeView create(UnitTypeInput input) {
        if(types.existsByCodeIgnoreCase(input.code())) throw conflict("UNIT_TYPE_ALREADY_EXISTS","Unit type code already exists");
        UnitType t=new UnitType();t.code=input.code().trim();t.name=input.name().trim();t.description=input.description();t.capacity=input.capacity();
        return view(types.saveAndFlush(t));
    }
    @Transactional(readOnly=true) Page<UnitTypeView> types(int page,int size,RecordStatus status) {
        Specification<UnitType> spec=(r,q,b)->status==null?b.conjunction():b.equal(r.get("status"),status);
        return types.findAll(spec,page(page,size)).map(PropertyService::view);
    }
    @Transactional UnitView create(UnitInput input) {
        Building b=buildings.lockByPublicId(input.buildingId()).orElseThrow(()->missing("BUILDING_NOT_FOUND","Building not found"));
        Floor f=floors.findByPublicId(input.floorId()).orElseThrow(()->missing("FLOOR_NOT_FOUND","Floor not found"));
        UnitType t=types.findByPublicId(input.unitTypeId()).orElseThrow(()->missing("UNIT_TYPE_NOT_FOUND","Unit type not found"));
        if(!Objects.equals(f.getBuilding().getId(),b.id)) throw conflict("INVALID_FLOOR_BUILDING_RELATIONSHIP","Floor does not belong to building");
        if(b.status!=RecordStatus.ACTIVE || f.status!=RecordStatus.ACTIVE || t.status!=RecordStatus.ACTIVE) throw conflict("UNIT_STATUS_INVALID","Inactive property reference");
        if(units.existsByFloorBuildingIdAndUnitNumberIgnoreCase(b.id,input.unitNumber())) throw conflict("UNIT_ALREADY_EXISTS","Unit number already exists in building");
        Unit u=new Unit();u.floor=f;u.unitType=t;u.unitNumber=input.unitNumber().trim();return view(units.saveAndFlush(u));
    }
    @Transactional(readOnly=true) Page<UnitView> units(int page,int size,UUID buildingId,UUID floorId,UUID typeId,UnitStatus status,Boolean availability,String unitNumber) {
        requireManagementScope();
        Specification<Unit> spec=(r,q,b)->b.conjunction();
        if(buildingId!=null) spec=spec.and((r,q,b)->b.equal(r.get("floor").get("building").get("publicId"),buildingId));
        if(floorId!=null) spec=spec.and((r,q,b)->b.equal(r.get("floor").get("publicId"),floorId));
        if(typeId!=null) spec=spec.and((r,q,b)->b.equal(r.get("unitType").get("publicId"),typeId));
        if(status!=null) spec=spec.and((r,q,b)->b.equal(r.get("status"),status));
        if(unitNumber!=null&&!unitNumber.isBlank()) {String pattern="%"+unitNumber.toLowerCase()+"%";spec=spec.and((r,q,b)->b.like(b.lower(r.get("unitNumber")),pattern));}
        if(availability!=null) spec=spec.and((r,q,b)->{
            var active=b.and(b.equal(r.get("status"),UnitStatus.AVAILABLE),b.equal(r.get("floor").get("status"),RecordStatus.ACTIVE),b.equal(r.get("floor").get("building").get("status"),RecordStatus.ACTIVE));
            return availability?active:b.not(active);
        });
        return units.findAll(spec,page(page,size)).map(PropertyService::view);
    }
    @Transactional OwnershipView create(OwnershipInput input) {
        Unit u=units.lockByPublicId(input.unitId()).orElseThrow(()->missing("UNIT_NOT_FOUND","Unit not found"));
        if(input.endDate()!=null && input.startDate().isAfter(input.endDate())) throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","startDate must be on or before endDate");
        if(u.status==UnitStatus.INACTIVE || u.getFloor().getStatus()==RecordStatus.INACTIVE || u.getFloor().getBuilding().getStatus()==RecordStatus.INACTIVE) throw conflict("OWNERSHIP_CONFLICT","Inactive unit cannot be owned");
        residents.validateOwner(input.ownerId());
        List<Ownership> active=ownerships.findByUnitId(u.id).stream().filter(o->o.status==RecordStatus.ACTIVE && overlaps(input.startDate(),input.endDate(),o.startDate,o.endDate)).toList();
        if(active.stream().anyMatch(o->o.ownerId.equals(input.ownerId()))) throw conflict("OWNERSHIP_CONFLICT","Owner already has an overlapping ownership record");
        BigDecimal concurrent=active.stream().map(o->o.ownershipPercentage).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(concurrent.add(input.ownershipPercentage()).compareTo(new BigDecimal("100.00"))>0) throw conflict("OWNERSHIP_CONFLICT","Ownership percentage exceeds 100 for overlapping records");
        Ownership o=new Ownership();o.unit=u;o.ownerId=input.ownerId();o.ownershipPercentage=input.ownershipPercentage();o.startDate=input.startDate();o.endDate=input.endDate();
        return view(ownerships.saveAndFlush(o));
    }
    static boolean overlaps(LocalDate aStart,LocalDate aEnd,LocalDate bStart,LocalDate bEnd) {
        return (aEnd==null || !aEnd.isBefore(bStart)) && (bEnd==null || !bEnd.isBefore(aStart));
    }
    @Transactional(readOnly=true) Page<OwnershipView> ownerships(int page,int size,UUID unitId,UUID ownerId,RecordStatus status) {
        requireManagementScope();
        Specification<Ownership> spec=(r,q,b)->b.conjunction();
        if(unitId!=null) spec=spec.and((r,q,b)->b.equal(r.get("unit").get("publicId"),unitId));
        if(ownerId!=null) spec=spec.and((r,q,b)->b.equal(r.get("ownerId"),ownerId));
        if(status!=null) spec=spec.and((r,q,b)->b.equal(r.get("status"),status));
        return ownerships.findAll(spec,page(page,size)).map(PropertyService::view);
    }
    @Transactional(readOnly=true) Unit unit(UUID id) {return units.findByPublicId(id).orElseThrow(()->missing("UNIT_NOT_FOUND","Unit not found"));}
    @Transactional(readOnly=true) boolean exists(UUID id) {return units.findByPublicId(id).isPresent();}
    @Transactional(readOnly=true) UnitValidation validation(UUID id) {
        Unit u=unit(id);return new UnitValidation(id,true,u.status,available(u),u.getFloor().getBuilding().getPublicId(),u.getFloor().getPublicId(),u.getUnitType().getPublicId(),u.getUnitType().getCapacity());
    }
    @Transactional(readOnly=true) UnitStatusView status(UUID id) {
        Unit u=unit(id);return new UnitStatusView(id,u.status,available(u));
    }
    @Transactional(readOnly=true) UnitOwnership ownership(UUID id) {
        Unit u=unit(id);LocalDate today=LocalDate.now();
        return new UnitOwnership(id,ownerships.findByUnitId(u.id).stream().filter(o->o.status==RecordStatus.ACTIVE && !o.startDate.isAfter(today) && (o.endDate==null || !o.endDate.isBefore(today))).map(PropertyService::view).toList());
    }
    private void requireManagementScope() {
        boolean manager=SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_SYSTEM_ADMINISTRATOR")||a.getAuthority().equals("ROLE_APARTMENT_MANAGER"));
        if(!manager) throw new ApiException(HttpStatus.FORBIDDEN,"FORBIDDEN","Resident property scope cannot be verified from the available provider contract");
    }
    static ApiException missing(String code,String message) {return new ApiException(HttpStatus.NOT_FOUND,code,message);}
    static ApiException conflict(String code,String message) {return new ApiException(HttpStatus.CONFLICT,code,message);}
}
