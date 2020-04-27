package com.iteratrlearning.problems.promises.pricefinder;

import static com.iteratrlearning.examples.promises.pricefinder.Currency.USD;

import com.iteratrlearning.examples.promises.pricefinder.Catalogue;
import com.iteratrlearning.examples.promises.pricefinder.Currency;
import com.iteratrlearning.examples.promises.pricefinder.ExchangeService;
import com.iteratrlearning.examples.promises.pricefinder.Price;
import com.iteratrlearning.examples.promises.pricefinder.PriceFinder;
import com.iteratrlearning.examples.promises.pricefinder.Product;
import com.iteratrlearning.examples.promises.pricefinder.Utils;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PriceCatalogueFuture {

    private final Catalogue catalogue = new Catalogue();
    private final PriceFinder priceFinder = new PriceFinder();
    private final ExchangeService exchangeService = new ExchangeService();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        new PriceCatalogueFuture().findLocalDiscountedPrice(Currency.CHF, "Nexus7");
    }

    private void findLocalDiscountedPrice(final Currency localCurrency, final String productName)
            throws InterruptedException, ExecutionException, TimeoutException {
        long time = System.currentTimeMillis();

        Future<Product> futureProduct = executor.submit(() -> catalogue.productByName(productName));

        Future<Price> futurePrice = executor.submit(() -> priceFinder.findBestPrice(futureProduct.get()));

        Future<Double> futureExchange = executor.submit(() -> exchangeService.lookupExchangeRate(USD, localCurrency));

        Price price = futurePrice.get(2, TimeUnit.SECONDS);
        Double exchangeRate = futureExchange.get(2, TimeUnit.SECONDS);

        double localPrice = exchange(price, exchangeRate);

        System.out.printf("A %s will cost us %f %s\n", productName, localPrice, localCurrency);
        System.out.printf("It took us %d ms to calculate this\n", System.currentTimeMillis() - time);

        executor.shutdownNow();
    }

    private double exchange(Price price, double exchangeRate) {
        return Utils.round(price.getAmount() * exchangeRate);
    }

}

