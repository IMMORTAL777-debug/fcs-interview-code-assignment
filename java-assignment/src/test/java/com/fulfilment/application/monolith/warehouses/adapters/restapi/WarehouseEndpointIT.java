package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

  @Test
  public void testSimpleListWarehouses() {

    final String path = "warehouse";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void createsArchivesAndHidesAWarehouse() {
    final String path = "warehouse";
    Map<String, Object> warehouse =
        Map.of(
            "businessUnitCode", "MWH.IT.901",
            "location", "EINDHOVEN-001",
            "capacity", 20,
            "stock", 4);

    String id =
        given()
            .contentType(ContentType.JSON)
            .body(warehouse)
            .when()
            .post(path)
            .then()
            .statusCode(201)
            .body("businessUnitCode", equalTo("MWH.IT.901"))
            .extract()
            .path("id");

    given().when().delete(path + "/" + id).then().statusCode(204);

    given()
        .when()
        .get(path + "/" + id)
        .then()
        .statusCode(404)
        .body("code", equalTo("WAREHOUSE_NOT_FOUND"));
  }
}
