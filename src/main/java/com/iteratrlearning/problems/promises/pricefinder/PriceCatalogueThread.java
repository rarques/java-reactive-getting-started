package com.iteratrlearning.problems.promises.pricefinder;

import com.iteratrlearning.examples.promises.pricefinder.*;

import static com.iteratrlearning.examples.promises.pricefinder.Currency.USD;

public class PriceCatalogueThread {

    private final Catalogue catalogue = new Catalogue();
    private final PriceFinder priceFinder = new PriceFinder();
    private final ExchangeService exchangeService = new ExchangeService();

    private Price price;
    private double exchangeRate;

    public static void main(String[] args) throws InterruptedException {
        new PriceCatalogueThread().findLocalDiscountedPrice(Currency.CHF, "Nexus7");
    }

    private void findLocalDiscountedPrice(final Currency localCurrency, final String productName) throws InterruptedException {
        long time = System.currentTimeMillis();

        // TODO: Extract runnables to specific classes with getters to retrieve the result.

        Runnable productPriceTask = () -> {
            Product product = catalogue.productByName(productName);
            setPrice(priceFinder.findBestPrice(product));
        };

        Runnable exchangeTask = () -> {
            setExchangeRate(exchangeService.lookupExchangeRate(USD, localCurrency));
        };

        Thread priceThread = new Thread(productPriceTask);
        Thread exchangeThread = new Thread(exchangeTask);
        priceThread.start();
        exchangeThread.start();

        priceThread.join();
        exchangeThread.join();

        double localPrice = exchange(price, exchangeRate);

        System.out.printf("A %s will cost us %f %s\n", productName, localPrice, localCurrency);
        System.out.printf("It took us %d ms to calculate this\n", System.currentTimeMillis() - time);
    }

    private double exchange(Price price, double exchangeRate) {
        return Utils.round(price.getAmount() * exchangeRate);
    }

    private void setPrice(Price price) {
        this.price = price;
    }

    private void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

}
