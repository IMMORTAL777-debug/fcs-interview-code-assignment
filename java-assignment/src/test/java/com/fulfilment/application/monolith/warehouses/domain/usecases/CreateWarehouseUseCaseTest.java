package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  @Test
  void createsWarehouseWhenAllRulesPass() {
    var store = new InMemoryWarehouseStore();
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());
    var warehouse = warehouse("MWH.100", "AMSTERDAM-002", 40, 15);

    useCase.create(warehouse);

    assertEquals(warehouse, store.findByBusinessUnitCode("MWH.100"));
    assertNotNull(warehouse.id);
    assertNotNull(warehouse.createdAt);
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    var store = new InMemoryWarehouseStore();
    store.create(warehouse("MWH.100", "AMSTERDAM-002", 20, 5));
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());

    var exception =
        assertThrows(
            WarehouseException.class,
            () -> useCase.create(warehouse("MWH.100", "AMSTERDAM-002", 20, 5)));

    assertEquals(WarehouseErrorCode.DUPLICATE_BUSINESS_UNIT_CODE, exception.errorCode());
  }

  @Test
  void rejectsUnknownLocationAndInsufficientCapacity() {
    var store = new InMemoryWarehouseStore();
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());

    var unknownLocation =
        assertThrows(
            WarehouseException.class,
            () -> useCase.create(warehouse("MWH.101", "UNKNOWN", 20, 5)));
    var insufficientCapacity =
        assertThrows(
            WarehouseException.class,
            () -> useCase.create(warehouse("MWH.102", "AMSTERDAM-002", 10, 11)));

    assertEquals(WarehouseErrorCode.LOCATION_NOT_FOUND, unknownLocation.errorCode());
    assertEquals(WarehouseErrorCode.STOCK_EXCEEDS_CAPACITY, insufficientCapacity.errorCode());
  }

  @Test
  void enforcesLocationWarehouseAndAggregateCapacityLimits() {
    var store = new InMemoryWarehouseStore();
    store.create(warehouse("MWH.100", "ZWOLLE-002", 30, 5));
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());

    var capacityLimit =
        assertThrows(
            WarehouseException.class,
            () -> useCase.create(warehouse("MWH.101", "ZWOLLE-002", 21, 5)));
    assertEquals(WarehouseErrorCode.LOCATION_CAPACITY_EXCEEDED, capacityLimit.errorCode());

    useCase.create(warehouse("MWH.101", "ZWOLLE-002", 20, 5));
    var warehouseLimit =
        assertThrows(
            WarehouseException.class,
            () -> useCase.create(warehouse("MWH.102", "ZWOLLE-002", 1, 0)));
    assertEquals(
        WarehouseErrorCode.LOCATION_WAREHOUSE_LIMIT_REACHED, warehouseLimit.errorCode());
  }

  static Warehouse warehouse(String code, String location, int capacity, int stock) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
