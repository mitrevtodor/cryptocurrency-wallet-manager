package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public abstract class CommandSkeleton implements Command {
    private WalletManager manager;
    private File file;
    private PrintStream ps;
    private final String errorMessage = "Unable to connect to the server." +
        " Please contact support and provide them the logs in the error.log file";

    public CommandSkeleton(WalletManager managerParam) {
        manager = managerParam;
        file = new File("error.log");
        try {
            ps = new PrintStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public WalletManager getManager() {
        return manager;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public PrintStream getPrintStream() {
        return ps;
    }
}
