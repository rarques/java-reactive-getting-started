package com.iteratrlearning.problems.promises.pricefinder;

import static com.iteratrlearning.examples.promises.pricefinder.Currency.USD;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.iteratrlearning.examples.promises.pricefinder.AsyncExchangeServiceFaulty;
import com.iteratrlearning.examples.promises.pricefinder.Catalogue;
import com.iteratrlearning.examples.promises.pricefinder.Currency;
import com.iteratrlearning.examples.promises.pricefinder.Price;
import com.iteratrlearning.examples.promises.pricefinder.PriceFinder;
import com.iteratrlearning.examples.promises.pricefinder.Product;
import com.iteratrlearning.examples.promises.pricefinder.Utils;
import java.util.concurrent.CompletableFuture;

public class PriceCatalogueFaulty {

    private final Catalogue catalogue = new Catalogue();
    private final PriceFinder priceFinder = new PriceFinder();
    private final AsyncExchangeServiceFaulty exchangeService = new AsyncExchangeServiceFaulty();
    private final CatalogueService catalogueService = new CatalogueService(catalogue);

    public static void main(String[] args) throws InterruptedException {
        new PriceCatalogueFaulty().findLocalDiscountedPrice(Currency.CHF, "Nexus7");
    }

    private void findLocalDiscountedPrice(final Currency localCurrency, final String productName) {
        long time = System.currentTimeMillis();

        try {
            catalogueService.getProduct(productName)
                    .thenCompose(this::findBestPricePerProduct)
                    .thenCombine(lookupExchangeRate(localCurrency), this::exchange)
                    .thenApply(localPrice -> {
                        String output = String
                                .format("A %s will cost us %f %s\n", productName, localPrice, localCurrency);
                        output += String
                                .format("It took us %d ms to calculate this\n", System.currentTimeMillis() - time);
                        return output;
                    })
                    .handle((result, ex) -> {
                        if (result != null) {
                            System.out.println("Operation successful.");
                            return result;
                        } else {
                            System.out.println("Exception!: " + ex);
                            return "Sorry dear user, fuck you";
                        }
                    })
//                    .exceptionally(ex -> "Sorry dear user, fuck you")
                    .thenAccept(System.out::println)
                    .join();

        } catch (Exception e) {
            System.out.println("Sorry try again next time: " + e.getCause().getMessage());
        }
    }

    /*
    * Method moved to class CatalogueService to simulate a Service returning a completable future instead
    * of wrapping the call in this same class in a method.

    private CompletableFuture<Product> getProduct(String productName) {
        return catalogueService.getProduct(productName);
    }
    */

    private CompletableFuture<Price> findBestPricePerProduct(Product product) {
        return supplyAsync(() -> priceFinder.findBestPrice(product));
    }

    private CompletableFuture<Double> lookupExchangeRate(Currency localCurrency) {
        return exchangeService.lookupExchangeRateAsync(USD, localCurrency);
    }

    private double exchange(Price price, double exchangeRate) {
        return Utils.round(price.getAmount() * exchangeRate);
    }

}
