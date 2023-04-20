package bg.sofia.uni.fmi.mjt.userdatabase;

import bg.sofia.uni.fmi.mjt.exceptions.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.UserNotRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.WrongPasswordException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDatabaseTest {
    private static Reader readerUsers;
    private static Writer writerUsers;
    private static UserDatabase database;
    private static String forStringReader = "";

    @BeforeAll
    public static void setUp() {
        readerUsers = new StringReader(forStringReader);
        writerUsers = new StringWriter();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        readerUsers.close();
        writerUsers.close();
    }

    @BeforeEach
    public void setDatabase() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        database = new UserDatabase();
    }

    @Test
    public void testRegisterUser()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        database.registerUser("hailHodor", "HoldTheDoor");
        assertTrue(database.isRegistered("hailHodor"), "User should have been registered.");
    }

    @Test
    public void testRegisterUserWithAlreadyRegisteredUserInfo()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        database.registerUser("hailHodor", "HoldTheDoor");

        assertThrows(UserAlreadyRegisteredException.class,
            () -> database.registerUser("hailHodor", "HoldTheDoor"),
            "User has already been registered but the exception has not been thrown.");
    }

    @Test
    public void testLoginWhenUserHasNotBeenRegisteredYet() {
        assertThrows(UserNotRegisteredException.class, () -> database.login("hailHodor", "HoldTheDoor"),
            "User has not been registered but the appropriate exception was not thrown.");
    }

    @Test
    public void testLoginWithIncorrectPassword()
        throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
        InvalidKeyException {
        database.registerUser("hailHodor", "HoldTheDoor");
        assertThrows(WrongPasswordException.class, () -> database.login("hailHodor", "HoldThe"),
            "Password was incorrect but the appropriate exception was not thrown. " +
                "Check the encrypting and decrypting methods");
    }

}
