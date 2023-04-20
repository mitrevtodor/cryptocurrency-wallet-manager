package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.exceptions.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class RegisterUserCommand extends CommandSkeleton {
    private String username;
    private String password;

    public RegisterUserCommand(WalletManager managerParam, String usernameParam, String passwordParam) {
        super(managerParam);
        username = usernameParam;
        password = passwordParam;
    }

    @Override
    public String execute() {
        try {
            return super.getManager().registerUser(username, password);
        } catch (UserAlreadyRegisteredException e) {
            return e.getMessage();
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException |
                 NoSuchAlgorithmException e) {
            e.printStackTrace(super.getPrintStream());
            return getErrorMessage();
        }
    }


}
