package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreLegacySynchronizer {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  void synchronize(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChanged change) {
    if (change.operation() == StoreChanged.Operation.CREATED) {
      legacyStoreManagerGateway.createStoreOnLegacySystem(change.store());
    } else {
      legacyStoreManagerGateway.updateStoreOnLegacySystem(change.store());
    }
  }
}
