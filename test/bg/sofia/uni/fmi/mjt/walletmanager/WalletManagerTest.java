package bg.sofia.uni.fmi.mjt.walletmanager;

import bg.sofia.uni.fmi.mjt.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.exceptions.NegativeDepositException;
import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import bg.sofia.uni.fmi.mjt.exceptions.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.UserNotRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.WrongPasswordException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletManagerTest {
    @Mock
    private HttpClient coinHttpClientMock;

    @Mock
    private HttpResponse<String> coinHttpResponseMock;

    @InjectMocks
    private WalletManager manager;

    private String jsonBTC = "[\n" +
        "  {\n" +
        "    \"asset_id\": \"BTC\",\n" +
        "    \"name\": \"Bitcoin\",\n" +
        "    \"type_is_crypto\": 1,\n" +
        "    \"data_start\": \"2010-07-17\",\n" +
        "    \"data_end\": \"2021-01-24\",\n" +
        "    \"data_quote_start\": \"2014-02-24T17:43:05.0000000Z\",\n" +
        "    \"data_quote_end\": \"2021-01-24T19:07:51.7954142Z\",\n" +
        "    \"data_orderbook_start\": \"2014-02-24T17:43:05.0000000Z\",\n" +
        "    \"data_orderbook_end\": \"2020-08-05T14:38:38.3413202Z\",\n" +
        "    \"data_trade_start\": \"2010-07-17T23:09:17.0000000Z\",\n" +
        "    \"data_trade_end\": \"2021-01-24T19:08:47.4460000Z\",\n" +
        "    \"data_symbols_count\": 46840,\n" +
        "    \"volume_1hrs_usd\": 9160288508835.92,\n" +
        "    \"volume_1day_usd\": 197928243055426.88,\n" +
        "    \"volume_1mth_usd\": 11571260516151083.22,\n" +
        "    \"price_usd\": 31304.448721266051267349441838,\n" +
        "    \"id_icon\": \"4caf2b16-a017-4e26-a348-2cea69c34cba\"\n" +
        "  }\n" +
        "]";

    private double priceFirstBTC = 31304.448721266051267349441838;
    private double priceSecondBTC = 31307.448721266051267349441838;

    private static Reader readerUsers;
    private static Writer writerUsers;
    private static Reader readerWallets;
    private static Writer writerWallets;
    private static String forStringReaders = "";

    private static final Path USER_FILE_PATH=Path.of("UserFile.txt");
    private static final Path WALLET_FILE_PATH=Path.of("WalletFile.txt");
    private static final Path USER_FILE_BACKUP_PATH=Path.of("UserFileBackup.txt");
    private static final Path WALLET_FILE_BACKUP_PATH=Path.of("WalletFileBackup.txt");

    private static void readFromAndWriteToFile(Path filePathFrom, Path filePathTo) {
        try (var bufferedReader = Files.newBufferedReader(filePathFrom);
             var bufferedWriter = Files.newBufferedWriter(filePathTo)) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }
    }

    private static void truncateFile(Path filePath) {
        try (FileWriter writer = new FileWriter(filePath.toFile(),false)) {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void clearFiles() {
        readFromAndWriteToFile(USER_FILE_PATH,USER_FILE_BACKUP_PATH);
        truncateFile(USER_FILE_PATH);

        readFromAndWriteToFile(WALLET_FILE_PATH,WALLET_FILE_BACKUP_PATH);
        truncateFile(WALLET_FILE_PATH);
    }

    private static void restoreFiles() {
        readFromAndWriteToFile(USER_FILE_BACKUP_PATH, USER_FILE_PATH);
        truncateFile(USER_FILE_BACKUP_PATH);

        readFromAndWriteToFile(WALLET_FILE_BACKUP_PATH, WALLET_FILE_PATH);
        truncateFile(WALLET_FILE_BACKUP_PATH);
    }

    @BeforeAll
    public static void setUp() {
        readerUsers = new StringReader(forStringReaders);
        writerUsers = new StringWriter();
        readerWallets = new StringReader(forStringReaders);
        writerWallets = new StringWriter();

        clearFiles();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        readerUsers.close();
        writerUsers.close();
        readerWallets.close();
        writerWallets.close();

        restoreFiles();
    }

    @BeforeEach
    public void setUpClass() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        manager = new WalletManager(coinHttpClientMock);
    }

    @Test
    public void testRegisterUserSuccessful()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String result = manager.registerUser("hailHodor", "HoldTheDoor");
        String expected = "User successfully registered. Please log into your account before proceeding.";
        assertTrue(manager.containsUser("hailHodor"), "User had not been registered successfully");
        assertEquals(expected, result, "Exception was thrown, check encryption.");
    }

    @Test
    public void testRegisterUserWhenUserHasAlreadyBeenRegistered()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String result = manager.registerUser("hailHodor", "HoldTheDoor");
        String expected = "User successfully registered. Please log into your account before proceeding.";
        assertTrue(manager.containsUser("hailHodor"), "User had not been registered successfully");
        assertThrows(UserAlreadyRegisteredException.class,
            () -> manager.registerUser("hailHodor", "HoldTheDoor"),
            "User was already registered,but was still added to the database");
    }

    @Test
    public void testLoginWhenUserHasNotYetBeenRegistered() {
        assertThrows(UserNotRegisteredException.class, () -> manager.login("hailHodor", "HoldTheDoor"));
    }

    @Test
    public void testLoginWithIncorrectUsername()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String result = manager.registerUser("hailHodor", "HoldTheDoor");
        String expected = "User successfully registered. Please log into your account before proceeding.";
        assertEquals(expected, result, "Exception was thrown, check encryption.");
        assertThrows(UserNotRegisteredException.class, () -> manager.login("stoyo", "javaRocks"));
    }

    @Test
    public void testLoginWithIncorrectPassword()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String result = manager.registerUser("hailHodor", "HoldTheDoor");
        String expected = "User successfully registered. Please log into your account before proceeding.";
        assertEquals(expected, result, "Exception was thrown, check encryption.");
        assertThrows(WrongPasswordException.class, () -> manager.login("hailHodor", "DoorTheHold"));
    }

    @Test
    public void testLoginWithValidParameters()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String resultRegister = manager.registerUser("hailHodor", "HoldTheDoor");
        String expectedRegister = "User successfully registered. Please log into your account before proceeding.";
        assertEquals(expectedRegister, resultRegister, "Exception was thrown, check encryption.");

        String resultLogin = manager.login("hailHodor", "HoldTheDoor");
        String expectedLogin = "Login successful.";
        assertEquals(expectedLogin, resultLogin,
            "Check if the user has been registered or if the parameters are correct.");
    }

    @Test
    public void testListOfferings()
        throws IOException, InterruptedException, IllegalBlockSizeException, NoSuchPaddingException,
        BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        String resultRegister = manager.registerUser("hailHodor", "HoldTheDoor");
        String expectedRegister = "User successfully registered. Please log into your account before proceeding.";
        assertEquals(expectedRegister, resultRegister, "Exception was thrown, check encryption.");

        String expectedOfferings = "BTC:31304.44872126605" + System.lineSeparator();
        String resultOfferings = manager.listOfferings("hailHodor");
        assertEquals(expectedOfferings, resultOfferings, "listOfferings not working properly." +
            " Check listOfferings() method in class Wallet.");
    }

    @Test
    public void testDepositMoneyWithNegativeQuantity()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String resultRegister = manager.registerUser("hailHodor", "HoldTheDoor");
        assertThrows(NegativeDepositException.class, () -> manager.depositMoney("hailHodor", -50),
            "depositMoney() should fail when negative amount is passed");
    }

    @Test
    public void testDepositMoneyWithValidParameters()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String resultRegister = manager.registerUser("hailHodor", "HoldTheDoor");
        String result = manager.depositMoney("hailHodor", 50);
        String expected = 50.0 + "$ successfully deposited.";
        assertEquals(50, manager.getUserWallet("hailHodor").getBalance(), 10E-10);
        assertEquals(expected, result, "The right amount was not deposited");
    }

    @Test
    public void testBuyWithInsufficientFund()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        String resultRegister = manager.registerUser("hailHodor", "HoldTheDoor");
        assertThrows(InsufficientFundsException.class, () -> manager.buy("hailHodor", "BTC", 50),
            "balance is less than what the user wants to spend, should throw InsufficientException");
    }

    @Test
    public void testBuyWithValidParameters()
        throws IOException, InterruptedException, IllegalBlockSizeException, NoSuchPaddingException,
        BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        manager.registerUser("hailHodor", "HoldTheDoor");
        manager.getUserWallet("hailHodor").depositMoney(50);
        String result = manager.buy("hailHodor", "BTC", 50);
        String expected = "Buy operation successful.";
        double quantityExpected = 50.0 / priceFirstBTC;
        Cryptocurrency currency = new Cryptocurrency("BTC", priceFirstBTC, 1);
        double quantityResult = manager.getUserWallet("hailHodor").getCryptoEntityQuantity(currency);
        assertEquals(quantityExpected, quantityResult, 10E-10);
        assertEquals(expected, result, "buy() metod did not return the proper value");
    }

    @Test
    public void testSellWithInvalidCryptoId()
        throws IOException, InterruptedException, IllegalBlockSizeException, NoSuchPaddingException,
        BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        manager.registerUser("hailHodor", "HoldTheDoor");
        manager.depositMoney("hailHodor", 100);
        assertThrows(NoSuchCurrencyException.class, () -> manager.sell("hailHodor", "USD"),
            "sellCrypto should throw an exception when balance<the moneySpent parameter");
    }

    @Test
    public void testSellWithValidCryptoId()
        throws IOException, InterruptedException, IllegalBlockSizeException, NoSuchPaddingException,
        BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        manager.registerUser("hailHodor", "HoldTheDoor");
        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceSecondBTC;
        Cryptocurrency first = new Cryptocurrency("BTC", priceFirstBTC, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", priceSecondBTC, 1);
        Wallet wallet = manager.getUserWallet("hailHodor");
        wallet.setCryptoEntityQuantity(first, quantityFirst);
        wallet.setCryptoEntityQuantity(second, quantitySecond);
        //because, given the test's logic, the price when sellCrypto() is called is the same as priceFirstBTC
        double profit =
            quantityFirst * (priceFirstBTC - priceFirstBTC) + quantitySecond * (priceFirstBTC - priceSecondBTC);
        //balance is 0 at the beginning given the test's logic.
        //after selling all btc entities it should be equal to:
        double balance = (quantityFirst + quantitySecond) * priceFirstBTC;
        String resultString = manager.sell("hailHodor", "BTC");

        assertEquals(balance, wallet.getBalance(), 10E-10);
        assertEquals(profit, wallet.getProfitThusFar(), 10E-10);
        assertEquals("Sell operation successful.", resultString,
            "sell() method did not return the proper string.");
    }

    @Test
    public void testGetWalletSummary()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException, IOException, InterruptedException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        manager.registerUser("hailHodor", "HoldTheDoor");
        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceSecondBTC;
        Cryptocurrency first = new Cryptocurrency("BTC", priceFirstBTC, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", priceSecondBTC, 1);
        Wallet wallet = manager.getUserWallet("hailHodor");
        wallet.setCryptoEntityQuantity(first, quantityFirst);
        wallet.setCryptoEntityQuantity(second, quantitySecond);
        String expected = "Current balance:0.0" + System.lineSeparator()
            + "id:BTC quantity:" + quantityFirst + System.lineSeparator()
            + "id:BTC quantity:" + quantitySecond + System.lineSeparator();

        assertEquals(expected, manager.getWalletSummary("hailHodor"),
            "Check whether the information from the map is passed correctly as CurrencyInfo objects.");
    }

    @Test
    public void testGetWalletOverallSummary()
        throws URISyntaxException, IOException, InterruptedException, IllegalBlockSizeException, NoSuchPaddingException,
        BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        manager.registerUser("hailHodor", "HoldTheDoor");
        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceSecondBTC;
        //given the test's logic, current price will be equal to priceFirstBTC
        double profitFirst = quantityFirst * (priceFirstBTC - priceFirstBTC);
        double profitSecond = +quantitySecond * (priceFirstBTC - priceSecondBTC);
        Cryptocurrency first = new Cryptocurrency("BTC", priceFirstBTC, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", priceSecondBTC, 1);
        Wallet wallet = manager.getUserWallet("hailHodor");
        wallet.setCryptoEntityQuantity(first, quantityFirst);
        wallet.setCryptoEntityQuantity(second, quantitySecond);
        String expected = "id:BTC quantity:" + quantityFirst + " profit:" + profitFirst + System.lineSeparator()
            + "id:BTC quantity:" + quantitySecond + " profit:" + profitSecond + System.lineSeparator()
            + "Current profit:" + wallet.getProfitThusFar() + System.lineSeparator();

        assertEquals(expected, manager.getWalletOverallSummary("hailHodor"),
            "Check whether the information from the map is passed correctly as CurrencyInfo objects.");
    }
}
