package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null", Sort.by("id"))
        .list().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var entity = DbWarehouse.fromWarehouse(warehouse);
    persist(entity);
    warehouse.id = entity.id;
  }

  @Override
  public void update(Warehouse warehouse) {
    var entity = findActiveEntityByBusinessUnitCode(warehouse.businessUnitCode);
    if (entity == null) {
      return;
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = warehouse.id == null ? null : findById(warehouse.id);
    if (entity == null || entity.archivedAt != null) {
      entity = findActiveEntityByBusinessUnitCode(warehouse.businessUnitCode);
    }
    if (entity != null) {
      entity.archivedAt = warehouse.archivedAt;
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    var entity = findActiveEntityByBusinessUnitCode(buCode);
    return entity == null ? null : entity.toWarehouse();
  }

  public Warehouse findActiveById(Long id) {
    var entity = findById(id);
    return entity == null || entity.archivedAt != null ? null : entity.toWarehouse();
  }

  private DbWarehouse findActiveEntityByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      return null;
    }
    return find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
  }
}
