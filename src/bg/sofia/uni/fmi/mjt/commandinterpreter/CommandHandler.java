package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;

public class CommandHandler {
    private static WalletManager manager;

    public CommandHandler() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        HttpClient httpClient = HttpClient.newHttpClient();
        manager = new WalletManager(httpClient);

    }

    public String executeCommand(Command command) {
        return command.execute();
    }

    public WalletManager getManager() {
        return manager;
    }
}





