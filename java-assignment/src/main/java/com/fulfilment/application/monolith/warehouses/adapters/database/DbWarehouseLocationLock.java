package com.fulfilment.application.monolith.warehouses.adapters.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouse_location_lock")
public class DbWarehouseLocationLock {

  @Id
  @Column(length = 40)
  public String location;
}
