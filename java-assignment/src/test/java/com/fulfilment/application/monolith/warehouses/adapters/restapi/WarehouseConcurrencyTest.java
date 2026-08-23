package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseConcurrencyTest {

  @Test
  void serializesConcurrentCreationsAtTheSameLocation() throws Exception {
    var first = warehouse("MWH.CONCURRENT.001", "HELMOND-001");
    var second = warehouse("MWH.CONCURRENT.002", "HELMOND-001");

    List<Response> responses = postConcurrently(first, second);
    try {
      assertEquals(
          List.of(201, 409), responses.stream().map(Response::statusCode).sorted().toList());

      Response conflict =
          responses.stream()
              .filter(response -> response.statusCode() == 409)
              .findFirst()
              .orElseThrow();
      assertEquals("LOCATION_WAREHOUSE_LIMIT_REACHED", conflict.jsonPath().getString("code"));
    } finally {
      archiveCreatedWarehouses(responses);
    }
  }

  @Test
  void allowsOnlyOneActiveWarehouseForAConcurrentBusinessUnitCode() throws Exception {
    String businessUnitCode = "MWH.CONCURRENT.DUPLICATE";
    var first = warehouse(businessUnitCode, "AMSTERDAM-002");
    var second = warehouse(businessUnitCode, "EINDHOVEN-001");

    List<Response> responses = postConcurrently(first, second);
    try {
      assertEquals(
          List.of(201, 409), responses.stream().map(Response::statusCode).sorted().toList());

      Response conflict =
          responses.stream()
              .filter(response -> response.statusCode() == 409)
              .findFirst()
              .orElseThrow();
      assertEquals("DUPLICATE_BUSINESS_UNIT_CODE", conflict.jsonPath().getString("code"));
    } finally {
      archiveCreatedWarehouses(responses);
    }
  }

  private List<Response> postConcurrently(
      Map<String, Object> first, Map<String, Object> second) throws Exception {
    var ready = new CountDownLatch(2);
    var start = new CountDownLatch(1);
    var executor = Executors.newFixedThreadPool(2);
    try {
      Callable<Response> firstRequest = request(first, ready, start);
      Callable<Response> secondRequest = request(second, ready, start);
      var firstResult = executor.submit(firstRequest);
      var secondResult = executor.submit(secondRequest);

      assertTrue(ready.await(5, TimeUnit.SECONDS), "Concurrent requests did not become ready");
      start.countDown();
      return List.of(firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS));
    } finally {
      executor.shutdownNow();
    }
  }

  private Callable<Response> request(
      Map<String, Object> warehouse, CountDownLatch ready, CountDownLatch start) {
    return () -> {
      ready.countDown();
      assertTrue(start.await(5, TimeUnit.SECONDS), "Concurrent requests were not released");
      return given()
          .contentType(ContentType.JSON)
          .body(warehouse)
          .when()
          .post("warehouse")
          .then()
          .extract()
          .response();
    };
  }

  private Map<String, Object> warehouse(String businessUnitCode, String location) {
    return Map.of(
        "businessUnitCode", businessUnitCode,
        "location", location,
        "capacity", 20,
        "stock", 5);
  }

  private void archiveCreatedWarehouses(List<Response> responses) {
    responses.stream()
        .filter(response -> response.statusCode() == 201)
        .map(response -> response.jsonPath().getString("id"))
        .forEach(id -> given().when().delete("warehouse/" + id).then().statusCode(204));
  }
}
