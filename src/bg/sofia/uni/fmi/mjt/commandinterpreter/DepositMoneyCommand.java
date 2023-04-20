package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.exceptions.NegativeDepositException;
import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

public class DepositMoneyCommand extends CommandSkeleton {
    private String username;
    private double quantity;

    public DepositMoneyCommand(WalletManager managerParam, String usernameParam, double quantityParam) {
        super(managerParam);
        username = usernameParam;
        quantity = quantityParam;
    }

    @Override
    public String execute() {
        try {
            return super.getManager().depositMoney(username, quantity);
        } catch (NegativeDepositException e) {
            return e.getMessage();
        }
    }
}
