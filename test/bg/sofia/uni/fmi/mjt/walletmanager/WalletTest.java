package bg.sofia.uni.fmi.mjt.walletmanager;

import bg.sofia.uni.fmi.mjt.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.exceptions.NegativeDepositException;
import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletTest {
    @Mock
    private HttpClient coinHttpClientMock;

    @Mock
    private HttpResponse<String> coinHttpResponseMock;

    @InjectMocks
    private Wallet wallet;

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

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        wallet = new Wallet(coinHttpClientMock);
    }

    @Test
    public void testDepositMoneyWithNegativeParameter() {
        assertThrows(NegativeDepositException.class, () -> wallet.depositMoney(-100),
            "depositMoney() should throw an exception when the passed parameter is negative.");
    }

    @Test
    public void testDepositMoneyWithCorrectParameter() {
        wallet.depositMoney(100);
        assertEquals(100, wallet.getBalance(),
            "Balance should be incremented with the passed as parameter quantity.");
    }

    @Test
    public void testBuyCryptoWhenFundsAreInsufficient() throws URISyntaxException, IOException, InterruptedException,
        NoSuchCurrencyException {
        assertThrows(InsufficientFundsException.class, () -> wallet.buyCrypto("BTC", 100),
            "buyCrypto should throw an exception when balance<the moneySpent parameter");
    }

    @Test
    public void testBuyCryptoWithInvalidCryptoId() throws IOException, InterruptedException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);
        wallet.depositMoney(100);
        assertThrows(NoSuchCurrencyException.class, () -> wallet.buyCrypto("USD", 100),
            "buyCrypto should throw an exception when balance<the moneySpent parameter");
    }

    @Test
    public void testBuyCryptoWithValidParameters() throws IOException, InterruptedException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceFirstBTC;
        Cryptocurrency currency = new Cryptocurrency("BTC", priceFirstBTC, 1);
        wallet.depositMoney(100);
        wallet.buyCrypto("BTC", 50);
        wallet.buyCrypto("BTC", 40);
        assertEquals(100 - moneySpentFirst - moneySpentSecond, wallet.getBalance(), 10E-10);
        assertEquals(quantityFirst + quantitySecond, wallet.getCryptoEntityQuantity(currency), 10E-10);
    }

    @Test
    public void testSellCryptoWithInvalidCryptoId() throws IOException, InterruptedException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);
        wallet.depositMoney(100);
        assertThrows(NoSuchCurrencyException.class, () -> wallet.sellCrypto("USD"),
            "sellCrypto should throw an exception when balance<the moneySpent parameter");
    }

    @Test
    public void testSellCryptoWithValidCryptoId() throws IOException, InterruptedException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceSecondBTC;
        Cryptocurrency first = new Cryptocurrency("BTC", priceFirstBTC, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", priceSecondBTC, 1);
        wallet.setCryptoEntityQuantity(first, quantityFirst);
        wallet.setCryptoEntityQuantity(second, quantitySecond);
        //because, given the test's logic, the price when sellCrypto() is called is the same as priceFirstBTC
        double profit =
            quantityFirst * (priceFirstBTC - priceFirstBTC) + quantitySecond * (priceFirstBTC - priceSecondBTC);
        //balance is 0 at the beginning given the test's logic.
        //after selling all btc entities it should be equal to:
        double balance = (quantityFirst + quantitySecond) * priceFirstBTC;
        wallet.sellCrypto("BTC");

        assertEquals(balance, wallet.getBalance(), 10E-10);
        assertEquals(profit, wallet.getProfitThusFar(), 10E-10);
    }

    @Test
    public void testListOfferings() throws IOException, InterruptedException, URISyntaxException {
        String expected = "BTC:31304.44872126605" + System.lineSeparator();
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);
        String result = wallet.listOfferings();

        assertEquals(expected, result, "listOfferings not working properly." +
            " Check getAvailableCryptos() method in the handler.");
    }

    @Test
    public void testGetWalletSummary() throws IOException, InterruptedException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceSecondBTC;
        Cryptocurrency first = new Cryptocurrency("BTC", priceFirstBTC, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", priceSecondBTC, 1);
        wallet.setCryptoEntityQuantity(first, quantityFirst);
        wallet.setCryptoEntityQuantity(second, quantitySecond);
        String expected = "Current balance:0.0" + System.lineSeparator()
            + "id:BTC quantity:" + quantityFirst + System.lineSeparator()
            + "id:BTC quantity:" + quantitySecond + System.lineSeparator();

        assertEquals(expected, wallet.getWalletSummary(),
            "Check whether the information from the map is passed correctly as CurrencyInfo objects.");
    }

    @Test
    public void testGetWalletOverallSummary() throws IOException, InterruptedException, URISyntaxException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);

        double moneySpentFirst = 50;
        double moneySpentSecond = 40;
        double quantityFirst = moneySpentFirst / priceFirstBTC;
        double quantitySecond = moneySpentSecond / priceSecondBTC;
        //given the test's logic, current price will be equal to priceFirstBTC
        double profitFirst = quantityFirst * (priceFirstBTC - priceFirstBTC);
        double profitSecond = +quantitySecond * (priceFirstBTC - priceSecondBTC);
        Cryptocurrency first = new Cryptocurrency("BTC", priceFirstBTC, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", priceSecondBTC, 1);
        wallet.setCryptoEntityQuantity(first, quantityFirst);
        wallet.setCryptoEntityQuantity(second, quantitySecond);
        String expected = "id:BTC quantity:" + quantityFirst + " profit:" + profitFirst + System.lineSeparator()
            + "id:BTC quantity:" + quantitySecond + " profit:" + profitSecond + System.lineSeparator()
            + "Current profit:" + wallet.getProfitThusFar() + System.lineSeparator();

        assertEquals(expected, wallet.getWalletOverallSummary(),
            "Check whether the information from the map is passed correctly as CurrencyInfo objects.");
    }
}

