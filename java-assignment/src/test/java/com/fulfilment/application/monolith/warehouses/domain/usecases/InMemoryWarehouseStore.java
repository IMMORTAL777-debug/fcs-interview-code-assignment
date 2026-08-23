package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;

class InMemoryWarehouseStore implements WarehouseStore {

  final List<Warehouse> history = new ArrayList<>();
  private long nextId = 1;

  @Override
  public List<Warehouse> getAll() {
    return history.stream().filter(warehouse -> warehouse.archivedAt == null).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    warehouse.id = nextId++;
    history.add(warehouse);
  }

  @Override
  public void lockLocation(String locationIdentifier) {}

  @Override
  public void archive(Warehouse warehouse) {
    history.stream()
        .filter(stored -> stored.id.equals(warehouse.id))
        .findFirst()
        .ifPresent(stored -> stored.archivedAt = warehouse.archivedAt);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    return getAll().stream()
        .filter(warehouse -> warehouse.businessUnitCode.equalsIgnoreCase(buCode))
        .findFirst()
        .orElse(null);
  }
}
