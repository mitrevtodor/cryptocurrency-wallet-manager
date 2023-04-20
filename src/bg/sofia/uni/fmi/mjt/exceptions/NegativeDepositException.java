package bg.sofia.uni.fmi.mjt.exceptions;

public class NegativeDepositException extends RuntimeException {
    public NegativeDepositException(String message) {
        super(message);
    }

    public NegativeDepositException(String message, Throwable cause) {
        super(message, cause);
    }
}
