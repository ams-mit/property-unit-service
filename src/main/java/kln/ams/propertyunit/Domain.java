package kln.ams.propertyunit;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

enum RecordStatus { ACTIVE, INACTIVE }
enum UnitStatus { AVAILABLE, RESERVED, OCCUPIED, UNDER_MAINTENANCE, INACTIVE }

@Entity @Table(name="buildings")
class Building {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false,columnDefinition="BINARY(16)") UUID publicId=UUID.randomUUID();
    @Column(name="building_code",nullable=false,unique=true,length=50) String buildingCode;
    @Column(nullable=false) String name;
    @Column(nullable=false) String address;
    @Column(length=500) String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) RecordStatus status=RecordStatus.ACTIVE;
    @CreationTimestamp @Column(name="created_at") Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at") Instant updatedAt;
    @OneToMany(mappedBy="building",cascade=CascadeType.ALL) List<Floor> floors=new ArrayList<>();
    Long getId(){return id;} UUID getPublicId(){return publicId;} RecordStatus getStatus(){return status;}
}

@Entity @Table(name="floors")
class Floor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false,columnDefinition="BINARY(16)") UUID publicId=UUID.randomUUID();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="building_id") Building building;
    @Column(name="floor_number",nullable=false) Integer floorNumber;
    @Column(name="floor_name",length=50) String name;
    @Column(length=500) String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) RecordStatus status=RecordStatus.ACTIVE;
    @CreationTimestamp @Column(name="created_at") Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at") Instant updatedAt;
    Long getId(){return id;} UUID getPublicId(){return publicId;} Building getBuilding(){return building;} RecordStatus getStatus(){return status;}
}

@Entity @Table(name="unit_types")
class UnitType {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false,columnDefinition="BINARY(16)") UUID publicId=UUID.randomUUID();
    @Column(nullable=false,unique=true,length=50) String code;
    @Column(name="type_name",nullable=false) String name;
    @Column(length=500) String description;
    @Column(name="capacity_limit",nullable=false) Integer capacity;
    @Column(name="base_rent",nullable=false) BigDecimal legacyBaseRent=BigDecimal.ZERO;
    @Column(name="amenities_summary") String legacyAmenitiesSummary;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) RecordStatus status=RecordStatus.ACTIVE;
    @CreationTimestamp @Column(name="created_at") Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at") Instant updatedAt;
    UUID getPublicId(){return publicId;} RecordStatus getStatus(){return status;} Integer getCapacity(){return capacity;}
}

@Entity @Table(name="units")
class Unit {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false,columnDefinition="BINARY(16)") UUID publicId=UUID.randomUUID();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="floor_id") Floor floor;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="unit_type_id") UnitType unitType;
    @Column(name="unit_number",nullable=false,length=50) String unitNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) UnitStatus status=UnitStatus.AVAILABLE;
    @CreationTimestamp @Column(name="created_at") Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at") Instant updatedAt;
    Floor getFloor(){return floor;} UnitType getUnitType(){return unitType;} UUID getPublicId(){return publicId;}
}

@Entity @Table(name="ownerships")
class Ownership {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(name="public_id",nullable=false,unique=true,updatable=false,columnDefinition="BINARY(16)") UUID publicId=UUID.randomUUID();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="unit_id") Unit unit;
    @JdbcTypeCode(SqlTypes.VARCHAR) @Column(name="owner_id",nullable=false) UUID ownerId;
    @Column(name="share_percentage",nullable=false,precision=5,scale=2) BigDecimal ownershipPercentage;
    @Column(name="start_date",nullable=false) LocalDate startDate;
    @Column(name="end_date") LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) RecordStatus status=RecordStatus.ACTIVE;
    @CreationTimestamp @Column(name="created_at") Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at") Instant updatedAt;
    Unit getUnit(){return unit;}
}
