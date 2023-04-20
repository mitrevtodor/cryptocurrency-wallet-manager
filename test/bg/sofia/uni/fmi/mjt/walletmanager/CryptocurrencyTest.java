package bg.sofia.uni.fmi.mjt.walletmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CryptocurrencyTest {
    @Test
    public void testEqualsMethodWithEqualCryptos() {
        //two currencies are equal if and only if they have the same id and the same price
        Cryptocurrency first = new Cryptocurrency("BTC", 21_800.80, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", 21_800.80, 1);
        assertEquals(first, second, "Should be equal.");
    }

    @Test
    public void testEqualsMethodWithSameCryptosWithDifferentPrices() {
        //two currencies are equal if and only if they have the same id and the same price
        Cryptocurrency first = new Cryptocurrency("BTC", 21_800.80, 1);
        Cryptocurrency second = new Cryptocurrency("BTC", 21_900.80, 1);
        assertNotEquals(first, second, "Should not be equal.");
    }
}

