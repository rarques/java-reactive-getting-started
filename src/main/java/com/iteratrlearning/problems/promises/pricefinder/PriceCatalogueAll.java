package com.iteratrlearning.problems.promises.pricefinder;

import static com.iteratrlearning.examples.promises.pricefinder.Currency.USD;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

import com.iteratrlearning.examples.promises.pricefinder.Catalogue;
import com.iteratrlearning.examples.promises.pricefinder.Currency;
import com.iteratrlearning.examples.promises.pricefinder.ExchangeService;
import com.iteratrlearning.examples.promises.pricefinder.Price;
import com.iteratrlearning.examples.promises.pricefinder.PriceFinder;
import com.iteratrlearning.examples.promises.pricefinder.Product;
import com.iteratrlearning.examples.promises.pricefinder.Utils;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PriceCatalogueAll {

    private final PriceFinder priceFinder = new PriceFinder();
    private final ExchangeService exchangeService = new ExchangeService();

    public static void main(String[] args) {

        new PriceCatalogueAll().findAllDiscountedPrice(Currency.CHF, Catalogue.products);
    }

    private void findAllDiscountedPrice(final Currency localCurrency, List<Product> products) {
        long time = System.currentTimeMillis();

        CompletableFuture<Double> exchangeRateFuture = findExchangeRate(localCurrency);

        List<CompletableFuture<Price>> pricesFuture = products.stream()
                .map(this::findBestPrice)
                .collect(toList());

        CompletableFuture<List<Price>> listPricesFuture = sequence(pricesFuture);

        CompletableFuture<Double> totalPriceFuture = listPricesFuture
                .thenCombine(exchangeRateFuture, (prices, exchangeRate) -> {
                    return prices.stream()
                            .mapToDouble(price -> exchange(price, exchangeRate))
                            .sum();
                });

        Double totalPrice = totalPriceFuture.join();

        System.out.printf("The total price is %f %s\n", totalPrice, localCurrency);
        System.out.printf("It took us %d ms to calculate this\n", System.currentTimeMillis() - time);
    }

//    System.out.printf("A %s will cost us %f %s\n", product.getName(), localPrice, localCurrency);

    private CompletableFuture<Price> findBestPrice(Product product) {
        return supplyAsync(() -> priceFinder.findBestPrice(product));
    }

    private CompletableFuture<Double> findExchangeRate(Currency localCurrency) {
        return supplyAsync(() -> exchangeService.lookupExchangeRate(USD, localCurrency));
    }

    private double exchange(Price price, double exchangeRate) {
        return Utils.round(price.getAmount() * exchangeRate);
    }

    private <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allFuturesDone =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allFuturesDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(toList()));
    }
}
