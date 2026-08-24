package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));

    given()
        .when()
        .get(path + "/1")
        .then()
        .statusCode(404)
        .body("code", equalTo("PRODUCT_NOT_FOUND"))
        .body("status", equalTo(404));
  }

  @Test
  void createsUpdatesAndValidatesProducts() {
    Map<String, Object> product =
        Map.of(
            "name", "COVERAGE_PRODUCT",
            "description", "Coverage product",
            "price", 12.50,
            "stock", 8);

    Long id =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post("product")
            .then()
            .statusCode(201)
            .body("name", equalTo("COVERAGE_PRODUCT"))
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .when()
        .get("product/" + id)
        .then()
        .statusCode(200)
        .body("description", equalTo("Coverage product"));

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "name", "COVERAGE_PRODUCT_UPDATED",
                "description", "Updated",
                "price", 15.75,
                "stock", 11))
        .when()
        .put("product/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("COVERAGE_PRODUCT_UPDATED"))
        .body("stock", equalTo(11));

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "id", 999,
                "name", "INVALID_PRODUCT",
                "stock", 1))
        .when()
        .post("product")
        .then()
        .statusCode(422)
        .body("code", equalTo("PRODUCT_ID_NOT_ALLOWED"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("stock", 1))
        .when()
        .put("product/" + id)
        .then()
        .statusCode(422)
        .body("code", equalTo("PRODUCT_NAME_REQUIRED"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", "MISSING_PRODUCT", "stock", 1))
        .when()
        .put("product/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo("PRODUCT_NOT_FOUND"));

    given().when().delete("product/" + id).then().statusCode(204);
    given()
        .when()
        .delete("product/" + id)
        .then()
        .statusCode(404)
        .body("code", equalTo("PRODUCT_NOT_FOUND"));
  }
}
