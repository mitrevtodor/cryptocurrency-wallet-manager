package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.exceptions.InsufficientFundsException;
import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import java.io.IOException;
import java.net.URISyntaxException;

public class BuyCommand extends CommandSkeleton {
    private String username;
    private String id;
    private double quantity;

    public BuyCommand(WalletManager managerParam, String usernameParam, String idParam, double quantityParam) {
        super(managerParam);
        username = usernameParam;
        id = idParam;
        quantity = quantityParam;
    }

    @Override
    public String execute() {
        try {
            return super.getManager().buy(username, id, quantity);
        } catch (NoSuchCurrencyException | InsufficientFundsException e) {
            return e.getMessage();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace(super.getPrintStream());
            return super.getErrorMessage();
        }
    }
}
