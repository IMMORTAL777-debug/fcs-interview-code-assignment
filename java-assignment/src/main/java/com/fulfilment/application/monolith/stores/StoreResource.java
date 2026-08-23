package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.common.exceptions.ResourceErrorCode;
import com.fulfilment.application.monolith.common.exceptions.ResourceException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject Event<StoreChanged> storeChanges;

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.STORE_NOT_FOUND, "Store with id " + id + " does not exist.");
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new ResourceException(
          ResourceErrorCode.STORE_ID_NOT_ALLOWED, "Id must not be supplied when creating a store.");
    }

    store.persist();

    storeChanges.fire(StoreChanged.created(snapshot(store)));

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new ResourceException(
          ResourceErrorCode.STORE_NAME_REQUIRED, "Store name is required.");
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.STORE_NOT_FOUND, "Store with id " + id + " does not exist.");
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    storeChanges.fire(StoreChanged.updated(snapshot(entity)));

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, StorePatchRequest patch) {
    if (patch == null || (patch.name == null && patch.quantityProductsInStock == null)) {
      throw new ResourceException(
          ResourceErrorCode.STORE_PATCH_EMPTY, "At least one store field must be supplied.");
    }
    if (patch.name != null && patch.name.isBlank()) {
      throw new ResourceException(
          ResourceErrorCode.STORE_NAME_REQUIRED, "Store name must not be blank.");
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.STORE_NOT_FOUND, "Store with id " + id + " does not exist.");
    }

    if (patch.name != null) {
      entity.name = patch.name;
    }

    if (patch.quantityProductsInStock != null) {
      entity.quantityProductsInStock = patch.quantityProductsInStock;
    }

    storeChanges.fire(StoreChanged.updated(snapshot(entity)));

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.STORE_NOT_FOUND, "Store with id " + id + " does not exist.");
    }
    entity.delete();
    return Response.status(204).build();
  }

  private Store snapshot(Store source) {
    var snapshot = new Store();
    snapshot.id = source.id;
    snapshot.name = source.name;
    snapshot.quantityProductsInStock = source.quantityProductsInStock;
    return snapshot;
  }
}
