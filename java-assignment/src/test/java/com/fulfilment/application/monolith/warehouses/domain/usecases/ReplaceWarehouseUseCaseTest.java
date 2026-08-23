package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCaseTest.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  @Test
  void archivesOldWarehouseAndCreatesReplacement() {
    var store = new InMemoryWarehouseStore();
    var original = warehouse("MWH.100", "AMSTERDAM-001", 50, 10);
    store.create(original);
    var replacement = warehouse("MWH.100", "AMSTERDAM-001", 60, 10);

    new ReplaceWarehouseUseCase(store, new LocationGateway()).replace(replacement);

    assertEquals(2, store.history.size());
    assertNotNull(original.archivedAt);
    assertEquals(replacement, store.findByBusinessUnitCode("MWH.100"));
    assertEquals(1, store.getAll().size());
  }

  @Test
  void rejectsMissingWarehouseAndStockMismatch() {
    var store = new InMemoryWarehouseStore();
    var useCase = new ReplaceWarehouseUseCase(store, new LocationGateway());

    assertEquals(
        WarehouseErrorCode.WAREHOUSE_NOT_FOUND,
        assertThrows(
                WarehouseException.class,
                () -> useCase.replace(warehouse("MWH.404", "AMSTERDAM-001", 50, 10)))
            .errorCode());

    store.create(warehouse("MWH.100", "AMSTERDAM-001", 50, 10));
    var stockMismatch =
        assertThrows(
            WarehouseException.class,
            () -> useCase.replace(warehouse("MWH.100", "AMSTERDAM-001", 50, 9)));
    assertEquals(WarehouseErrorCode.REPLACEMENT_STOCK_MISMATCH, stockMismatch.errorCode());
  }

  @Test
  void replacementMustAccommodateExistingStock() {
    var store = new InMemoryWarehouseStore();
    store.create(warehouse("MWH.100", "AMSTERDAM-001", 50, 10));

    var exception =
        assertThrows(
            WarehouseException.class,
            () ->
                new ReplaceWarehouseUseCase(store, new LocationGateway())
                    .replace(warehouse("MWH.100", "AMSTERDAM-001", 9, 10)));

    assertEquals(WarehouseErrorCode.STOCK_EXCEEDS_CAPACITY, exception.errorCode());
  }
}
