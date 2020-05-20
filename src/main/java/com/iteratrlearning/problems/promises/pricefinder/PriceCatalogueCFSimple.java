package com.iteratrlearning.problems.promises.pricefinder;

import static com.iteratrlearning.examples.promises.pricefinder.Currency.USD;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.iteratrlearning.examples.promises.pricefinder.Catalogue;
import com.iteratrlearning.examples.promises.pricefinder.Currency;
import com.iteratrlearning.examples.promises.pricefinder.ExchangeService;
import com.iteratrlearning.examples.promises.pricefinder.Price;
import com.iteratrlearning.examples.promises.pricefinder.PriceFinder;
import com.iteratrlearning.examples.promises.pricefinder.Product;
import com.iteratrlearning.examples.promises.pricefinder.Utils;
import java.util.concurrent.CompletableFuture;

public class PriceCatalogueCFSimple {

    private final Catalogue catalogue = new Catalogue();
    private final PriceFinder priceFinder = new PriceFinder();
    private final ExchangeService exchangeService = new ExchangeService();

    public static void main(String[] args) {
        new PriceCatalogueCFSimple().findLocalDiscountedPrice(Currency.CHF, "Nexus7");
    }

    private void findLocalDiscountedPrice(final Currency localCurrency, final String productName) {
        long time = System.currentTimeMillis();

        CompletableFuture<Product> productCompletableFuture = supplyAsync(() -> catalogue.productByName(productName));

        CompletableFuture<Price> priceCompletableFuture = supplyAsync(
                () -> priceFinder.findBestPrice(productCompletableFuture.join ()));

        CompletableFuture<Double> doubleCompletableFuture = supplyAsync(
                () -> exchangeService.lookupExchangeRate(USD, localCurrency));

        double localPrice = exchange(priceCompletableFuture.join(), doubleCompletableFuture.join());

        System.out.printf("A %s will cost us %f %s\n", productName, localPrice, localCurrency);
        System.out.printf("It took us %d ms to calculate this\n", System.currentTimeMillis() - time);
    }

    private double exchange(Price price, double exchangeRate) {
        return Utils.round(price.getAmount() * exchangeRate);
    }

}

