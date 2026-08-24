package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreLegacySynchronizationTest {

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  void synchronizesCreateAndUpdateAfterSuccessfulTransactions() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", "TONSTAD", "quantityProductsInStock", 99))
        .when()
        .post("store")
        .then()
        .statusCode(500)
        .body("code", equalTo("INTERNAL_SERVER_ERROR"))
        .body("status", equalTo(500))
        .body("exceptionType", nullValue());

    verify(legacyStoreManagerGateway, never())
        .createStoreOnLegacySystem(argThat(store -> store.name.equals("TONSTAD")));

    String storeName = "POST_COMMIT_STORE";
    Long id =
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", storeName, "quantityProductsInStock", 7))
            .when()
            .post("store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    verify(legacyStoreManagerGateway, timeout(1000))
        .createStoreOnLegacySystem(
            argThat(store -> store.id.equals(id) && store.name.equals(storeName)));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", storeName + "_UPDATED", "quantityProductsInStock", 9))
        .when()
        .put("store/" + id)
        .then()
        .statusCode(200);

    verify(legacyStoreManagerGateway, timeout(1000))
        .updateStoreOnLegacySystem(
            argThat(
                store ->
                    store.id.equals(id)
                        && store.name.equals(storeName + "_UPDATED")
                        && store.quantityProductsInStock == 9));
  }

  @Test
  void patchesOnlyTheFieldsProvidedAndRejectsAnEmptyPatch() {
    String originalName = "PATCH_TEST_STORE";
    Long id =
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", originalName, "quantityProductsInStock", 7))
            .when()
            .post("store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("quantityProductsInStock", 11))
        .when()
        .patch("store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo(originalName))
        .body("quantityProductsInStock", equalTo(11));

    String updatedName = originalName + "_RENAMED";
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", updatedName))
        .when()
        .patch("store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo(updatedName))
        .body("quantityProductsInStock", equalTo(11));

    verify(legacyStoreManagerGateway, timeout(1000))
        .updateStoreOnLegacySystem(
            argThat(
                store ->
                    store.id.equals(id)
                        && store.name.equals(updatedName)
                        && store.quantityProductsInStock == 11));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch("store/" + id)
        .then()
        .statusCode(422)
        .body("code", equalTo("STORE_PATCH_EMPTY"))
        .body("status", equalTo(422));
  }

  @Test
  void supportsStoreReadsDeletesAndValidationErrors() {
    given().when().get("store").then().statusCode(200);
    given().when().get("store/1").then().statusCode(200).body("name", equalTo("TONSTAD"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("id", 999, "name", "INVALID_STORE", "quantityProductsInStock", 1))
        .when()
        .post("store")
        .then()
        .statusCode(422)
        .body("code", equalTo("STORE_ID_NOT_ALLOWED"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("quantityProductsInStock", 1))
        .when()
        .put("store/1")
        .then()
        .statusCode(422)
        .body("code", equalTo("STORE_NAME_REQUIRED"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", "MISSING_STORE", "quantityProductsInStock", 1))
        .when()
        .put("store/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo("STORE_NOT_FOUND"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", " "))
        .when()
        .patch("store/1")
        .then()
        .statusCode(422)
        .body("code", equalTo("STORE_NAME_REQUIRED"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", "MISSING_STORE"))
        .when()
        .patch("store/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo("STORE_NOT_FOUND"));

    Long id =
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "DELETE_TEST_STORE", "quantityProductsInStock", 2))
            .when()
            .post("store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given().when().delete("store/" + id).then().statusCode(204);
    given()
        .when()
        .delete("store/" + id)
        .then()
        .statusCode(404)
        .body("code", equalTo("STORE_NOT_FOUND"));
  }
}
