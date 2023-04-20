package bg.sofia.uni.fmi.mjt.walletmanager;

import bg.sofia.uni.fmi.mjt.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.exceptions.NegativeDepositException;
import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import bg.sofia.uni.fmi.mjt.exceptions.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.UserNotRegisteredException;
import bg.sofia.uni.fmi.mjt.userdatabase.UserDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class WalletManager {
    private UserDatabase usersInfo;
    private Map<String, Wallet> walletMap;
    private Gson converter;
    private static final File WALLET_FILE = new File("WalletFile.txt");
    private static final Type JSON_TYPE_WALLETS = new TypeToken<Map<String, Wallet>>() {
    }.getType();

    private final HttpClient client;

    public WalletManager(HttpClient clientParam)
        throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        usersInfo = new UserDatabase();
        walletMap = new HashMap<>();
        client = clientParam;
        converter = new GsonBuilder().enableComplexMapKeySerialization().create();
        readWalletManagerFromFile();
        usersInfo.readFromFile();
    }

    public String registerUser(String username, String password)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UserAlreadyRegisteredException,
        NoSuchPaddingException, NoSuchAlgorithmException {
        usersInfo.registerUser(username, password);
        walletMap.put(username, new Wallet(client));
        return "User successfully registered. Please log into your account before proceeding.";
    }

    public String login(String username, String password)
        throws UserNotRegisteredException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchPaddingException, NoSuchAlgorithmException {
        usersInfo.login(username, password);
        return "Login successful.";
    }

    public String listOfferings(String username) throws URISyntaxException, IOException, InterruptedException {
        return walletMap.get(username).listOfferings();
    }

    public String depositMoney(String username, double quantity) throws NegativeDepositException {
        walletMap.get(username).depositMoney(quantity);
        return quantity + "$ successfully deposited.";
    }

    public String buy(String username, String id, double quantity)
        throws URISyntaxException, IOException, InterruptedException, NoSuchCurrencyException,
        InsufficientFundsException {
        walletMap.get(username).buyCrypto(id, quantity);
        return "Buy operation successful.";
    }

    public String sell(String username, String id)
        throws URISyntaxException, IOException, InterruptedException, NoSuchCurrencyException {
        walletMap.get(username).sellCrypto(id);
        return "Sell operation successful.";
    }

    public String getWalletSummary(String username) throws URISyntaxException, IOException, InterruptedException {
        return walletMap.get(username).getWalletSummary();
    }

    public String getWalletOverallSummary(String username)
        throws URISyntaxException, IOException, InterruptedException {
        return walletMap.get(username).getWalletOverallSummary();
    }

    public void writeWalletManagerToFile() throws IOException {
        try (var writerWallets = new PrintWriter(new FileWriter(WALLET_FILE), true)) {
            String result = converter.toJson(walletMap);
            writerWallets.println(result);
            usersInfo.writeToFile();
        }
    }

    public void readWalletManagerFromFile() throws IOException {
        if (WALLET_FILE.length() == 0) {
            return;
        }
        try (var readerWallets = new BufferedReader(new FileReader(WALLET_FILE))) {
            String result = readerWallets.readLine();
            Map<String, Wallet> walletData = converter.fromJson(result, JSON_TYPE_WALLETS);
            walletData.keySet().stream().forEach(username -> walletMap.putIfAbsent(username, walletData.get(username)));
        }
    }

    //methods used only for testing
    public boolean containsUser(String username) {
        return usersInfo.isRegistered(username);
    }

    public Wallet getUserWallet(String username) {
        return walletMap.get(username);
    }
}

