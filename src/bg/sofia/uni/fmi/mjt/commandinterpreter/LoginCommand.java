package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.exceptions.UserNotRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.WrongPasswordException;
import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class LoginCommand extends CommandSkeleton {
    private String username;
    private String password;

    public LoginCommand(WalletManager managerParam, String usernameParam, String passwordParam) {
        super(managerParam);
        username = usernameParam;
        password = passwordParam;
    }

    @Override
    public String execute() {
        try {
            return super.getManager().login(username, password);
        } catch (UserNotRegisteredException e) {
            return e.getMessage();
        } catch (WrongPasswordException e) {
            return e.getMessage();
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException |
                 NoSuchAlgorithmException e) {
            e.printStackTrace(super.getPrintStream());
            return super.getErrorMessage();
        }
    }
}
