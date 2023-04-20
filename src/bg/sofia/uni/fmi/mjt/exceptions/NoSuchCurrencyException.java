package bg.sofia.uni.fmi.mjt.exceptions;

public class NoSuchCurrencyException extends RuntimeException {
    public NoSuchCurrencyException(String message) {
        super(message);
    }

    public NoSuchCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
