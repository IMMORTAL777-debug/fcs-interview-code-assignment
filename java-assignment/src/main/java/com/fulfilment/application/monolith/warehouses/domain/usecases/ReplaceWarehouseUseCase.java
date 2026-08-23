package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this(warehouseStore, new LocationGateway());
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    var location = WarehouseValidation.validateCommon(newWarehouse, locationResolver);
    var currentWarehouse =
        warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (currentWarehouse == null) {
      throw WarehouseException.notFound(
          "No active warehouse exists with business unit code "
              + newWarehouse.businessUnitCode
              + ".");
    }
    if (!currentWarehouse.stock.equals(newWarehouse.stock)) {
      throw WarehouseException.invalid(
          "Replacement stock must match the current warehouse stock of "
              + currentWarehouse.stock
              + ".");
    }

    WarehouseValidation.validateLocationAvailability(
        newWarehouse, location, currentWarehouse, warehouseStore);

    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.remove(currentWarehouse);

    newWarehouse.id = null;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
