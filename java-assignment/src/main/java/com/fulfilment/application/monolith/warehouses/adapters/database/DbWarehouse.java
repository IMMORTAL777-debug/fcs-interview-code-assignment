package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "warehouse",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_warehouse_active_business_unit",
            columnNames = "active_business_unit_code"))
@Cacheable
public class DbWarehouse {

  @Id @GeneratedValue public Long id;

  public String businessUnitCode;

  @Column(name = "active_business_unit_code")
  public String activeBusinessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  @Version public long version;

  public DbWarehouse() {}

  public static DbWarehouse fromWarehouse(Warehouse warehouse) {
    var entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.activeBusinessUnitCode =
        warehouse.archivedAt == null ? warehouse.businessUnitCode : null;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
    return entity;
  }

  public Warehouse toWarehouse() {
    var warehouse = new Warehouse();
    warehouse.id = this.id;
    warehouse.businessUnitCode = this.businessUnitCode;
    warehouse.location = this.location;
    warehouse.capacity = this.capacity;
    warehouse.stock = this.stock;
    warehouse.createdAt = this.createdAt;
    warehouse.archivedAt = this.archivedAt;
    return warehouse;
  }
}
