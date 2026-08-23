package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Locale;

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
    try {
      persistAndFlush(entity);
    } catch (PersistenceException exception) {
      if (isActiveBusinessUnitConflict(exception)) {
        throw new WarehouseException(
            WarehouseErrorCode.DUPLICATE_BUSINESS_UNIT_CODE,
            "An active warehouse with business unit code "
                + warehouse.businessUnitCode
                + " already exists.",
            exception);
      }
      throw exception;
    }
    warehouse.id = entity.id;
  }

  @Override
  public void lockLocation(String locationIdentifier) {
    var locationLock =
        getEntityManager()
            .find(
                DbWarehouseLocationLock.class,
                locationIdentifier,
                LockModeType.PESSIMISTIC_WRITE);
    if (locationLock == null) {
      throw new IllegalStateException(
          "No warehouse location lock exists for " + locationIdentifier + ".");
    }
  }

  @Override
  public void archive(Warehouse warehouse) {
    DbWarehouse entity =
        warehouse.id == null ? null : findById(warehouse.id, LockModeType.PESSIMISTIC_WRITE);
    if (entity == null || entity.archivedAt != null) {
      entity = findActiveEntityByBusinessUnitCode(warehouse.businessUnitCode);
    }
    if (entity != null) {
      entity.archivedAt = warehouse.archivedAt;
      entity.activeBusinessUnitCode = null;
      flush();
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
    return find("businessUnitCode = ?1 and archivedAt is null", buCode)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .firstResult();
  }

  private boolean isActiveBusinessUnitConflict(PersistenceException exception) {
    Throwable cause = exception;
    while (cause != null) {
      String message = cause.getMessage();
      String normalizedMessage = message == null ? "" : message.toLowerCase(Locale.ROOT);
      if (normalizedMessage.contains("uk_warehouse_active_business_unit")
          || normalizedMessage.contains("active_business_unit_code")) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }
}
