package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this(warehouseStore, new LocationGateway());
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    var location = WarehouseValidation.validateCommon(warehouse, locationResolver);
    warehouseStore.lockLocation(location.identification);
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WarehouseException(
          WarehouseErrorCode.DUPLICATE_BUSINESS_UNIT_CODE,
          "An active warehouse with business unit code "
              + warehouse.businessUnitCode
              + " already exists.");
    }
    WarehouseValidation.validateLocationAvailability(warehouse, location, null, warehouseStore);

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}
