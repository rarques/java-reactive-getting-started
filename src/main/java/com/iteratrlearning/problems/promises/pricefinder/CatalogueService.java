package com.iteratrlearning.problems.promises.pricefinder;

import com.iteratrlearning.examples.promises.pricefinder.Catalogue;
import com.iteratrlearning.examples.promises.pricefinder.Product;
import java.util.concurrent.CompletableFuture;

public class CatalogueService {

    private final Catalogue catalogue;

    public CatalogueService(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    public CompletableFuture<Product> getProduct(String productName) {
        return CompletableFuture.supplyAsync(() -> catalogue.productByName(productName));
    }
}