package bg.sofia.uni.fmi.mjt.walletmanager;

import bg.sofia.uni.fmi.mjt.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.exceptions.NegativeDepositException;
import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import bg.sofia.uni.fmi.mjt.restapihandler.CoinAPIHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Wallet {
    private final Map<Cryptocurrency, Double> cryptocurrencies;
    private double balance;
    private double profitThusFar;
    private static transient CoinAPIHandler handler;

    private void sellCrypto(String id, double currentPrice) {
        Set<Cryptocurrency> cryptoEntities = cryptocurrencies.keySet().stream()
            .filter(cryptocurrency -> id.equals(cryptocurrency.getId())).collect(Collectors.toSet());

        if (cryptoEntities.isEmpty()) {
            throw new NoSuchCurrencyException("There are no entities of the specified currency in your wallet.");
        }

        double cryptoQuantity;
        double cryptoIncome;
        double cryptoProfit;
        for (Cryptocurrency entity : cryptoEntities) {
            cryptoQuantity = cryptocurrencies.get(entity);
            cryptoIncome = cryptoQuantity * currentPrice;
            balance += cryptoIncome;
            cryptoProfit = cryptoQuantity * (currentPrice - entity.getPriceBought());
            profitThusFar += cryptoProfit;
            cryptocurrencies.remove(entity);
        }
    }

    private List<CurrencyInfo> convertCryptoEntities() throws URISyntaxException, IOException, InterruptedException {
        List<CurrencyInfo> infoList = new ArrayList<>();
        double currentPrice;
        Set<Cryptocurrency> cryptos = cryptocurrencies.keySet();
        for (Cryptocurrency currency : cryptos) {
            currentPrice = handler.getCryptoCurrentPrice(currency.getId());
            infoList.add(new CurrencyInfo(currency, cryptocurrencies.get(currency), currentPrice));
        }
        return infoList;
    }

    public Wallet(HttpClient client) {
        cryptocurrencies = new HashMap<>();
        balance = 0;
        profitThusFar = 0;
        handler = new CoinAPIHandler(client);
    }

    public void depositMoney(double money) {
        if (money <= 0) {
            throw new NegativeDepositException("Please enter a positive quantity of money.");
        }
        balance += money;
    }

    public void buyCrypto(String id, double moneySpent)
        throws URISyntaxException, IOException, InterruptedException, NoSuchCurrencyException {
        if (balance < moneySpent) {
            throw new InsufficientFundsException("Wallet balance is less than the acquired amount for transaction.");
        }
        double currentPrice = handler.getCryptoCurrentPrice(id);
        balance -= moneySpent;
        double quantity = moneySpent / currentPrice;
        Cryptocurrency newCryptocurrency = new Cryptocurrency(id, currentPrice, 1);
        if (cryptocurrencies.containsKey(newCryptocurrency)) {
            Double newQuantity = cryptocurrencies.get(newCryptocurrency);
            newQuantity += quantity;
            cryptocurrencies.replace(newCryptocurrency, newQuantity);
        } else {
            cryptocurrencies.put(newCryptocurrency, quantity);
        }
    }

    public void sellCrypto(String id)
        throws URISyntaxException, IOException, InterruptedException, NoSuchCurrencyException {
        double currentPrice = handler.getCryptoCurrentPrice(id);
        sellCrypto(id, currentPrice);
    }

    public String listOfferings()
        throws URISyntaxException, IOException, InterruptedException {
        return handler.getAvailableCryptos();
    }

    public String getWalletSummary() throws URISyntaxException, IOException, InterruptedException {
        List<CurrencyInfo> infoList = convertCryptoEntities();
        StringBuilder result = new StringBuilder();
        result.append("Current balance:").append(balance).append(System.lineSeparator());
        infoList.forEach(currencyInfo -> result.append(currencyInfo.toStringWithoutProfitInfo()));
        return result.toString();
    }

    public String getWalletOverallSummary() throws URISyntaxException, IOException, InterruptedException {
        List<CurrencyInfo> infoList = convertCryptoEntities();
        StringBuilder result = new StringBuilder();
        infoList.forEach(currencyInfo -> result.append(currencyInfo.toStringWithProfitInfo()));
        result.append("Current profit:").append(profitThusFar).append(System.lineSeparator());
        return result.toString();
    }

    public double getBalance() {
        return balance;
    }

    public double getProfitThusFar() {
        return profitThusFar;
    }

    public double getCryptoEntityQuantity(Cryptocurrency cryptocurrency) {
        return cryptocurrencies.get(cryptocurrency);
    }

    //used only for easier testing of sellCrypto method. To be used only with valid parameters
    // (meaning it is an existing cryptocurrency and quantity is positive).
    public void setCryptoEntityQuantity(Cryptocurrency cryptocurrency, double quantity) {
        cryptocurrencies.put(cryptocurrency, quantity);
    }
}


