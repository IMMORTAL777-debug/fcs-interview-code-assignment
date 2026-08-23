package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
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
      throw new WarehouseException(
          WarehouseErrorCode.WAREHOUSE_NOT_FOUND, "Warehouse was not found.");
    }
    var currentWarehouse = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (currentWarehouse == null) {
      throw new WarehouseException(
          WarehouseErrorCode.WAREHOUSE_NOT_FOUND,
          "No active warehouse exists with business unit code "
              + warehouse.businessUnitCode
              + ".");
    }

    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.remove(currentWarehouse);
  }
}
