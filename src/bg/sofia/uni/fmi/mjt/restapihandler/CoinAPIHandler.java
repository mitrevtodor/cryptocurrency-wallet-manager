package bg.sofia.uni.fmi.mjt.restapihandler;

import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import bg.sofia.uni.fmi.mjt.walletmanager.Cryptocurrency;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoinAPIHandler {
    private LocalDateTime timeOfLastRequest;
    private Map<String, Double> responseEntities;
    private Gson converter;
    final HttpClient client;
    private static final int MAX_TIME_BEFORE_NEW_REQUEST = 30;
    private static final int MIN_PRICE_OF_CRYPTO_TO_BE_SHOWN = 50;
    private static final int NUMBER_OF_CURRENCIES_TO_BE_SHOWN = 20;

    private void update() throws URISyntaxException, IOException, InterruptedException {
        if (timeOfLastRequest.until(LocalDateTime.now(), ChronoUnit.MINUTES) > MAX_TIME_BEFORE_NEW_REQUEST) {
            URI uri = new URI("https", "rest.coinapi.io", "/v1/assets", null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .setHeader("X-CoinAPI-Key", "Enter your api key here").build();
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            timeOfLastRequest = LocalDateTime.now();

            Type type = new TypeToken<List<Cryptocurrency>>() {
            }.getType();
            List<Cryptocurrency> currenciesUnfiltered = converter.fromJson(response, type);
            currenciesUnfiltered.stream()
                .forEach(currency -> {
                    if (currency.isCrypto()) {
                        responseEntities.put(currency.getId(), currency.getPriceBought());
                    }
                });
        }
    }

    public CoinAPIHandler(HttpClient httpClient) {
        responseEntities = new HashMap<>();
        timeOfLastRequest = LocalDateTime.now().minusHours(1);
        converter = new Gson();
        client = httpClient;
    }

    public String getAvailableCryptos() throws URISyntaxException, IOException, InterruptedException {
        update();
        StringBuilder builder = new StringBuilder();
        responseEntities.keySet().stream().filter(key -> responseEntities.get(key) >= MIN_PRICE_OF_CRYPTO_TO_BE_SHOWN)
            .limit(NUMBER_OF_CURRENCIES_TO_BE_SHOWN).forEach(key ->
            builder.append(key).append(':').append(responseEntities.get(key)).append(System.lineSeparator()));
        return builder.toString();
    }

    public double getCryptoCurrentPrice(String cryptoId) throws URISyntaxException, IOException, InterruptedException {
        update();
        if (!responseEntities.containsKey(cryptoId)) {
            throw new NoSuchCurrencyException("The given currency is either non-existent or is not a cryptocurrency.");
        }
        return responseEntities.get(cryptoId);
    }
}
