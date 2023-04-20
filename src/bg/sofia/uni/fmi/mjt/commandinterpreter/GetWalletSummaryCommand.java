package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import java.io.IOException;
import java.net.URISyntaxException;

public class GetWalletSummaryCommand extends CommandSkeleton {
    private String username;

    public GetWalletSummaryCommand(WalletManager managerParam, String usernameParam) {
        super(managerParam);
        username = usernameParam;
    }

    @Override
    public String execute() {
        try {
            return super.getManager().getWalletSummary(username);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace(super.getPrintStream());
            return super.getErrorMessage();
        }
    }
}
