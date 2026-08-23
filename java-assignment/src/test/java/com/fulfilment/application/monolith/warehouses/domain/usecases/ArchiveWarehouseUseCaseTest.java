package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCaseTest.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  @Test
  void archivesAnActiveWarehouseWithoutDeletingHistory() {
    var store = new InMemoryWarehouseStore();
    var warehouse = warehouse("MWH.100", "AMSTERDAM-001", 50, 10);
    store.create(warehouse);

    new ArchiveWarehouseUseCase(store).archive(warehouse);

    assertEquals(0, store.getAll().size());
    assertEquals(1, store.history.size());
    assertNotNull(store.history.get(0).archivedAt);
  }

  @Test
  void rejectsArchivingAnUnknownWarehouse() {
    var store = new InMemoryWarehouseStore();

    var exception =
        assertThrows(
            WarehouseException.class,
            () ->
                new ArchiveWarehouseUseCase(store)
                    .archive(warehouse("MWH.404", "AMSTERDAM-001", 50, 10)));

    assertEquals(WarehouseErrorCode.WAREHOUSE_NOT_FOUND, exception.errorCode());
  }
}
