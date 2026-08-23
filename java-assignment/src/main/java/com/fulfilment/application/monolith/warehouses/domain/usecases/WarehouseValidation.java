package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.Locale;

final class WarehouseValidation {

  private WarehouseValidation() {}

  static Location validateCommon(Warehouse warehouse, LocationResolver locationResolver) {
    if (warehouse == null) {
      throw WarehouseException.invalid("Warehouse data is required.");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw WarehouseException.invalid("Business unit code is required.");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw WarehouseException.invalid("Location is required.");
    }
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw WarehouseException.invalid("Capacity must be greater than zero.");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw WarehouseException.invalid("Stock cannot be negative.");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw WarehouseException.invalid("Warehouse capacity cannot be lower than its stock.");
    }

    warehouse.businessUnitCode = warehouse.businessUnitCode.trim().toUpperCase(Locale.ROOT);
    warehouse.location = warehouse.location.trim();
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw WarehouseException.invalid("Unknown location: " + warehouse.location);
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
      throw WarehouseException.conflict(
          "Location " + location.identification + " has reached its warehouse limit.");
    }

    long allocatedCapacity =
        warehousesAtLocation.stream().mapToLong(warehouse -> warehouse.capacity).sum();
    if (allocatedCapacity + candidate.capacity > location.maxCapacity) {
      throw WarehouseException.invalid(
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
