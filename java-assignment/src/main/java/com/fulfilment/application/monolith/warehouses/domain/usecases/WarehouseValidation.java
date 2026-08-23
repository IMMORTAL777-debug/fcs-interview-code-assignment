package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.Locale;

final class WarehouseValidation {

  private WarehouseValidation() {}

  static Location validateCommon(Warehouse warehouse, LocationResolver locationResolver) {
    if (warehouse == null) {
      throw new WarehouseException(
          WarehouseErrorCode.WAREHOUSE_DATA_REQUIRED, "Warehouse data is required.");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new WarehouseException(
          WarehouseErrorCode.BUSINESS_UNIT_CODE_REQUIRED, "Business unit code is required.");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new WarehouseException(WarehouseErrorCode.LOCATION_REQUIRED, "Location is required.");
    }
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new WarehouseException(
          WarehouseErrorCode.INVALID_CAPACITY, "Capacity must be greater than zero.");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new WarehouseException(
          WarehouseErrorCode.INVALID_STOCK, "Stock cannot be negative.");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new WarehouseException(
          WarehouseErrorCode.STOCK_EXCEEDS_CAPACITY,
          "Warehouse capacity cannot be lower than its stock.");
    }

    warehouse.businessUnitCode = warehouse.businessUnitCode.trim().toUpperCase(Locale.ROOT);
    warehouse.location = warehouse.location.trim();
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WarehouseException(
          WarehouseErrorCode.LOCATION_NOT_FOUND, "Unknown location: " + warehouse.location);
    }
    warehouse.location = location.identification;
    return location;
  }

  static void validateLocationAvailability(
      Warehouse candidate,
      Location location,
      Warehouse warehouseBeingReplaced,
      WarehouseStore warehouseStore) {
    var warehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(warehouse -> warehouse.location.equalsIgnoreCase(location.identification))
            .filter(warehouse -> !isSameWarehouse(warehouse, warehouseBeingReplaced))
            .toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseException(
          WarehouseErrorCode.LOCATION_WAREHOUSE_LIMIT_REACHED,
          "Location " + location.identification + " has reached its warehouse limit.");
    }

    long allocatedCapacity =
        warehousesAtLocation.stream().mapToLong(warehouse -> warehouse.capacity).sum();
    if (allocatedCapacity + candidate.capacity > location.maxCapacity) {
      throw new WarehouseException(
          WarehouseErrorCode.LOCATION_CAPACITY_EXCEEDED,
          "Total warehouse capacity at "
              + location.identification
              + " cannot exceed "
              + location.maxCapacity
              + ".");
    }
  }

  private static boolean isSameWarehouse(Warehouse first, Warehouse second) {
    if (second == null) {
      return false;
    }
    if (first.id != null && second.id != null) {
      return first.id.equals(second.id);
    }
    return first.businessUnitCode.equalsIgnoreCase(second.businessUnitCode);
  }
}
