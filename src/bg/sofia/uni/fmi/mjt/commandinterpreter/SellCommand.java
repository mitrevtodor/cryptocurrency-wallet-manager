package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.exceptions.NoSuchCurrencyException;
import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import java.io.IOException;
import java.net.URISyntaxException;

public class SellCommand extends CommandSkeleton {
    private String username;
    private String id;

    public SellCommand(WalletManager managerParam, String usernameParam, String idParam) {
        super(managerParam);
        username = usernameParam;
        id = idParam;
    }

    @Override
    public String execute() {
        try {
            return super.getManager().sell(username, id);
        } catch (NoSuchCurrencyException e) {
            return e.getMessage();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace(super.getPrintStream());
            return super.getErrorMessage();
        }
    }
}