package bg.sofia.uni.fmi.mjt.userdatabase;

import bg.sofia.uni.fmi.mjt.exceptions.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.UserNotRegisteredException;
import bg.sofia.uni.fmi.mjt.exceptions.WrongPasswordException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
    private Map<String, String> userTable;
    private static final File USER_FILE = new File("UserFile.txt");
    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static final String ALGORITHM = "AES";

    private static final String SECRET_KEY_STRING = "secrete";
    private static final int NEW_LENGTH_KEY = 16;
    private Gson converter;
    public static final Type JSON_TYPE_USERS = new TypeToken<Map<String, String>>() {
    }.getType();

    private void prepareSecretKey(String myKey) throws NoSuchAlgorithmException {
        MessageDigest sha = null;
        key = myKey.getBytes(StandardCharsets.UTF_8);
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, NEW_LENGTH_KEY);
        secretKey = new SecretKeySpec(key, ALGORITHM);
    }

    private String encryptPassword(String strToEncrypt)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
        BadPaddingException {
        prepareSecretKey(SECRET_KEY_STRING);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }

    private String decryptPassword(String username)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
        BadPaddingException {
        String strToDecrypt = userTable.get(username);
        prepareSecretKey(SECRET_KEY_STRING);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }

    public UserDatabase() {
        converter = new Gson();
        userTable = new HashMap<>();
    }

    public void registerUser(String username, String password)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException,
        NoSuchAlgorithmException {
        if (userTable.containsKey(username)) {
            throw new UserAlreadyRegisteredException("User with passed username has already been registered." +
                " Please try a different username or contact support.");
        }

        String encryptedPassword = encryptPassword(password);
        userTable.put(username, encryptedPassword);
    }

    public void login(String username, String password)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException,
        NoSuchAlgorithmException {
        if (!userTable.containsKey(username)) {
            throw new UserNotRegisteredException("User with passed username has not yet been registered. " +
                "Please check your username and try again or contact support.");
        }
        if (!decryptPassword(username).equals(password)) {
            throw new WrongPasswordException("Passed password does not match " +
                "the password associated with passed username." +
                    "Please check your password and try again or contact support.");
        }
    }

    public void writeToFile() throws IOException {
        try (var writerUsers = new PrintWriter(new FileWriter(USER_FILE), true)) {
            String result = converter.toJson(userTable, JSON_TYPE_USERS);
            writerUsers.println(result);
        }
    }

    public void readFromFile() throws IOException {
        if (USER_FILE.length() == 0) {
            return;
        }
        try (var readerUsers = new BufferedReader(new FileReader(USER_FILE))) {
            String result = readerUsers.readLine();
            Map<String, String> userData = converter.fromJson(result, JSON_TYPE_USERS);
            userData.keySet().stream().forEach(username -> userTable.putIfAbsent(username, userData.get(username)));
        }
    }

    //methods that should be used only for testing
    public boolean isRegistered(String username) {
        return userTable.containsKey(username);
    }

    public Map<String, String> getUserTable() {
        return userTable;
    }
}

