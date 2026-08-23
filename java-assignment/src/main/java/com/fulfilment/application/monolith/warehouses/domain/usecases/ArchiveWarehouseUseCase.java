package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  @Transactional
  public void archive(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null) {
      throw WarehouseException.notFound("Warehouse was not found.");
    }
    var currentWarehouse = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (currentWarehouse == null) {
      throw WarehouseException.notFound(
          "No active warehouse exists with business unit code "
              + warehouse.businessUnitCode
              + ".");
    }

    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.remove(currentWarehouse);
  }
}
