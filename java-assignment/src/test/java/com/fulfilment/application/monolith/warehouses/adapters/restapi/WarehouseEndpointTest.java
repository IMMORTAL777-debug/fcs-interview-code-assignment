package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseEndpointTest {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  void supportsTheWarehouseLifecycleAndValidationErrors() {
    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", hasItems("MWH.001", "MWH.012", "MWH.023"));

    given()
        .when()
        .get("warehouse/1")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.001"));

    Map<String, Object> newWarehouse =
        Map.of(
            "businessUnitCode", "MWH.900",
            "location", "AMSTERDAM-001",
            "capacity", 30,
            "stock", 12);

    String createdId =
        given()
            .contentType(ContentType.JSON)
            .body(newWarehouse)
            .when()
            .post("warehouse")
            .then()
            .statusCode(201)
            .body("businessUnitCode", equalTo("MWH.900"))
            .extract()
            .path("id");

    given()
        .contentType(ContentType.JSON)
        .body(newWarehouse)
        .when()
        .post("warehouse")
        .then()
        .statusCode(409);

    Map<String, Object> replacement =
        Map.of(
            "businessUnitCode", "MWH.900",
            "location", "AMSTERDAM-002",
            "capacity", 40,
            "stock", 12);
    String replacementId =
        given()
            .contentType(ContentType.JSON)
            .body(replacement)
            .when()
            .post("warehouse/MWH.900/replacement")
            .then()
            .statusCode(200)
            .body("location", equalTo("AMSTERDAM-002"))
            .extract()
            .path("id");

    var replacementHistory =
        warehouseRepository.find("businessUnitCode", "MWH.900").list();
    assertEquals(2, replacementHistory.size());
    assertNotNull(replacementHistory.get(0).archivedAt);

    given().when().get("warehouse/" + createdId).then().statusCode(404);
    given().when().delete("warehouse/" + replacementId).then().statusCode(204);
    given().when().get("warehouse/" + replacementId).then().statusCode(404);

    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", not(hasItems("MWH.900")))
        .body("$", hasSize(3));
  }
}
