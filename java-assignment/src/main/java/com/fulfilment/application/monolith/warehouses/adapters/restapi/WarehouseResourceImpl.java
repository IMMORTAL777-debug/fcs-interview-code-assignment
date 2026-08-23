package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import java.util.List;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @ResponseStatus(201)
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    if (data.getId() != null) {
      throw new WarehouseException(
          WarehouseErrorCode.WAREHOUSE_ID_NOT_ALLOWED,
          "Id must not be supplied when creating a warehouse.");
    }
    var warehouse = toDomain(data);
    createWarehouseOperation.create(warehouse);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    return toWarehouseResponse(findActiveById(id));
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    archiveWarehouseOperation.archive(findActiveById(id));
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    if (data.getBusinessUnitCode() != null
        && !businessUnitCode.equalsIgnoreCase(data.getBusinessUnitCode())) {
      throw new WarehouseException(
          WarehouseErrorCode.BUSINESS_UNIT_CODE_MISMATCH,
          "The request business unit code must match the path business unit code.");
    }
    var warehouse = toDomain(data);
    warehouse.businessUnitCode = businessUnitCode;
    replaceWarehouseOperation.replace(warehouse);
    return toWarehouseResponse(warehouse);
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse findActiveById(
      String id) {
    final Long databaseId;
    try {
      databaseId = Long.valueOf(id);
    } catch (NumberFormatException exception) {
      throw new WarehouseException(
          WarehouseErrorCode.INVALID_WAREHOUSE_ID, "Warehouse id must be a number.");
    }

    var warehouse = warehouseRepository.findActiveById(databaseId);
    if (warehouse == null) {
      throw new WarehouseException(
          WarehouseErrorCode.WAREHOUSE_NOT_FOUND,
          "Warehouse with id " + id + " was not found.");
    }
    return warehouse;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(
      Warehouse data) {
    var warehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.id == null ? null : warehouse.id.toString());
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
