package bg.sofia.uni.fmi.mjt.walletmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyInfoTest {
    Cryptocurrency currency = new Cryptocurrency("BTC", 21_800.80, 1);
    CurrencyInfo info = new CurrencyInfo(currency, 100, 21_675.80);

    @Test
    public void testCurrencyInfoConstructor() {
        assertEquals(currency.getId(), info.getId(), "Id is different");
        assertEquals(100, info.getQuantity());
        assertEquals(-12_500, info.getProfit(), "Profit is not calculated correctly");
    }

    @Test
    public void testCurrencyInfoToStringWithoutProfitInfo() {
        String expected = "id:BTC quantity:" + 100.0 + System.lineSeparator();
        assertEquals(expected, info.toStringWithoutProfitInfo(),
            "Info without profit is not passed to string correctly");
    }

    @Test
    public void testCurrencyInfoToStringWithProfitInfo() {
        String expected = "id:BTC quantity:" + 100.0 + " profit:" + -12_500.0 + System.lineSeparator();
        assertEquals(expected, info.toStringWithProfitInfo(), "Info without profit is not passed to string correctly");
    }
}
