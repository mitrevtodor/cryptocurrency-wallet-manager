package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandInterfaceTest {
    @Mock
    private HttpClient coinHttpClientMock;

    @Mock
    private HttpResponse<String> coinHttpResponseMock;
    @InjectMocks
    private WalletManager manager;

    private static final Path USER_FILE_PATH=Path.of("UserFile.txt");
    private static final Path WALLET_FILE_PATH=Path.of("WalletFile.txt");
    private static final Path USER_FILE_BACKUP_PATH=Path.of("UserFileBackup.txt");
    private static final Path WALLET_FILE_BACKUP_PATH=Path.of("WalletFileBackup.txt");

    private static void readFromAndWriteToFile(Path filePathFrom, Path filePathTo) {
        try (var bufferedReader = Files.newBufferedReader(filePathFrom);
                var bufferedWriter = Files.newBufferedWriter(filePathTo)) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }
    }

    private static void truncateFile(Path filePath) {
        try (FileWriter writer = new FileWriter(filePath.toFile(),false)) {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void clearFiles() {
        readFromAndWriteToFile(USER_FILE_PATH,USER_FILE_BACKUP_PATH);
        truncateFile(USER_FILE_PATH);

        readFromAndWriteToFile(WALLET_FILE_PATH,WALLET_FILE_BACKUP_PATH);
        truncateFile(WALLET_FILE_PATH);
    }

    @AfterAll
    public static void restoreFiles() {
        readFromAndWriteToFile(USER_FILE_BACKUP_PATH, USER_FILE_PATH);
        truncateFile(USER_FILE_BACKUP_PATH);

        readFromAndWriteToFile(WALLET_FILE_BACKUP_PATH, WALLET_FILE_PATH);
        truncateFile(WALLET_FILE_BACKUP_PATH);
    }

    @BeforeEach
    public void setUp() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        manager = new WalletManager(coinHttpClientMock);
    }

    @Test
    public void testRegisterUserCommandSuccessful() {
        RegisterUserCommand command = new RegisterUserCommand(manager, "hodor", "hodor");
        String result = command.execute();
        String expected = "User successfully registered. Please log into your account before proceeding.";
        assertEquals(expected, result, "Method does not return the proper message when registration is successful.");
    }

    @Test
    public void testRegisterUserCommandWithAlreadyRegisteredUser() {
        RegisterUserCommand command1 = new RegisterUserCommand(manager, "hodor", "hodor");
        command1.execute();
        RegisterUserCommand command2 = new RegisterUserCommand(manager, "hodor", "hodor");
        String result = command2.execute();
        String expected = "User with passed username has already been registered." +
            " Please try a different username or contact support.";
        assertEquals(expected, result, "UserAlreadyRegisteredException message should have been thrown");
    }

    @Test
    public void testLoginCommandSuccessful() {
        RegisterUserCommand registerCommand = new RegisterUserCommand(manager, "hodor", "hodor");
        registerCommand.execute();
        LoginCommand loginCommand = new LoginCommand(manager, "hodor", "hodor");
        String result = loginCommand.execute();
        String expected = "Login successful.";
        assertEquals(expected, result, "User was registered but could not be logged.");
    }

    @Test
    public void testLoginCommandWithUnregisteredUser() {
        LoginCommand loginCommand = new LoginCommand(manager, "hodor", "hodor");
        String result = loginCommand.execute();
        String expected = "User with passed username has not yet been registered. " +
            "Please check your username and try again or contact support.";
        assertEquals(expected, result, "Method should have returned a UserNotRegisteredException message");
    }

    @Test
    public void testLoginCommandWithIncorrectPassword() {
        RegisterUserCommand registerCommand = new RegisterUserCommand(manager, "hodor", "hodor");
        registerCommand.execute();
        LoginCommand loginCommand = new LoginCommand(manager, "hodor", "dohor");
        String result = loginCommand.execute();
        String expected = "Passed password does not match the password associated with passed username." +
            "Please check your password and try again or contact support.";
        assertEquals(expected, result, "Method should have returned a WrongPasswordException message");
    }

    @Test
    public void testDepositMoneyCommandSuccessful() {
        RegisterUserCommand registerCommand = new RegisterUserCommand(manager, "hodor", "hodor");
        registerCommand.execute();
        LoginCommand loginCommand = new LoginCommand(manager, "hodor", "hodor");
        loginCommand.execute();
        DepositMoneyCommand depositMoneyCommand = new DepositMoneyCommand(manager, "hodor", 100.0);
        String result = depositMoneyCommand.execute();
        String expected = "100.0$ successfully deposited.";
        assertEquals(expected, result, "The amount deposited differs from the one passed as parameter.");
    }

    @Test
    public void testDepositMoneyCommandWithNegativeParameter() {
        RegisterUserCommand registerCommand = new RegisterUserCommand(manager, "hodor", "hodor");
        registerCommand.execute();
        LoginCommand loginCommand = new LoginCommand(manager, "hodor", "hodor");
        loginCommand.execute();
        DepositMoneyCommand depositMoneyCommand = new DepositMoneyCommand(manager, "hodor", -10.0);
        String result = depositMoneyCommand.execute();
        String expected = "Please enter a positive quantity of money.";
        assertEquals(expected, result, "A NegativeDepositException should have been thrown");
    }

    @Test
    public void testCommandInterpreterInvalid() {
        CommandInterpreter interpreter=new CommandInterpreter(manager,"gehurghue gierhgie gehuge");
        String result=interpreter.getCommand().execute();
        String expected="Command is incorrect. Please enter a valid command.";
        assertEquals(expected,result,"Command should be incorrect.");
    }

    @Test
    public void testCommandInterpreterRegister() {
        CommandInterpreter interpreter=new CommandInterpreter(manager,"register hodor hodor");
        String result=interpreter.getCommand().execute();
        String expected="User successfully registered. Please log into your account before proceeding.";
        assertEquals(expected,result,"Method does not return the proper message when registration is successful.");
    }

    @Test
    public void testCommandInterpreterLogin() {
        RegisterUserCommand registerUserCommand=new RegisterUserCommand(manager,"hodor", "hodor");
        registerUserCommand.execute();
        CommandInterpreter interpreter=new CommandInterpreter(manager,"login hodor hodor");
        String result=interpreter.getCommand().execute();
        String expected="Login successful.";
        assertEquals(expected,result,"User was registered but could not be logged.");
    }
}
