package bg.sofia.uni.fmi.mjt.restapihandler;


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
public class CoinAPIHandlerTest {
    @Mock
    private HttpClient coinHttpClientMock;

    @Mock
    private HttpResponse<String> coinHttpResponseMock;

    @InjectMocks
    private CoinAPIHandler handler;
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

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        when(coinHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(coinHttpResponseMock);

        handler = new CoinAPIHandler(coinHttpClientMock);
    }

    @Test
    public void testCoinAPIHandlerGetAvailableCryptos() throws URISyntaxException, IOException, InterruptedException {
        String expected = "BTC:31304.44872126605" + System.lineSeparator();

        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);
        String result = handler.getAvailableCryptos();

        assertEquals(expected, result, "getAvailableCryptos() not working properly");
    }

    @Test
    public void testCoinAPIHandlerGetCryptoCurrentPriceWhenCurrencyNotInTheMap() {
        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);
        assertThrows(NoSuchCurrencyException.class, () -> handler.getCryptoCurrentPrice("ETH"),
            "Check either the json used for testing or how the information is put into the map when received");
    }

    @Test
    public void testCoinAPIHandlerGetCryptoCurrentPriceBTC()
        throws URISyntaxException, IOException, InterruptedException {
        double expected = 31304.44872126605;

        when(coinHttpResponseMock.body()).thenReturn(jsonBTC);
        double result = handler.getCryptoCurrentPrice("BTC");

        assertEquals(expected, result, 10E-10);
    }
}

