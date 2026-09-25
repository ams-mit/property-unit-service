package lk.ac.kln.property_unit_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Entity
@Table(name = "unit_types")
public class UnitType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Type name is required")
    @Column(name = "type_name", nullable = false)
    private String typeName;

    @PositiveOrZero(message = "Base rent must be zero or positive")
    @Column(name = "base_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRent;

    @PositiveOrZero(message = "Capacity limit must be zero or positive")
    @Column(name = "capacity_limit", nullable = false)
    private Integer capacityLimit;

    @Column(name = "amenities_summary", columnDefinition = "TEXT")
    private String amenitiesSummary;

    public UnitType() {
    }

    public UnitType(Long id, String typeName, BigDecimal baseRent, Integer capacityLimit, String amenitiesSummary) {
        this.id = id;
        this.typeName = typeName;
        this.baseRent = baseRent;
        this.capacityLimit = capacityLimit;
        this.amenitiesSummary = amenitiesSummary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public BigDecimal getBaseRent() {
        return baseRent;
    }

    public void setBaseRent(BigDecimal baseRent) {
        this.baseRent = baseRent;
    }

    public Integer getCapacityLimit() {
        return capacityLimit;
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    public String getAmenitiesSummary() {
        return amenitiesSummary;
    }

    public void setAmenitiesSummary(String amenitiesSummary) {
        this.amenitiesSummary = amenitiesSummary;
    }
}
