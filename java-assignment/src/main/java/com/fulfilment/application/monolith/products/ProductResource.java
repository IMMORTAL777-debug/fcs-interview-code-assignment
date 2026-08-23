package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.common.exceptions.ResourceErrorCode;
import com.fulfilment.application.monolith.common.exceptions.ResourceException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductRepository productRepository;

  @GET
  public List<Product> get() {
    return productRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.PRODUCT_NOT_FOUND, "Product with id " + id + " does not exist.");
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    if (product.id != null) {
      throw new ResourceException(
          ResourceErrorCode.PRODUCT_ID_NOT_ALLOWED,
          "Id must not be supplied when creating a product.");
    }

    productRepository.persist(product);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    if (product.name == null) {
      throw new ResourceException(
          ResourceErrorCode.PRODUCT_NAME_REQUIRED, "Product name is required.");
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.PRODUCT_NOT_FOUND, "Product with id " + id + " does not exist.");
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new ResourceException(
          ResourceErrorCode.PRODUCT_NOT_FOUND, "Product with id " + id + " does not exist.");
    }
    productRepository.delete(entity);
    return Response.status(204).build();
  }
}
