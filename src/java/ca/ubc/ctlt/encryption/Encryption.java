package ca.ubc.ctlt.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * Encryption instance
 */
public class Encryption {
    private final String ALGO = "AES";
    private String salt;
    private Key secretKey = null;

    public Encryption() {
        secretKey = generateKey();
    }

    public Encryption(String salt) {
        this.salt = salt;
        secretKey = generateKey();
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
        secretKey = generateKey();
    }

    public String encrypt(String data) {
        String encryptedValue;
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encVal = c.doFinal(data.getBytes());
            encryptedValue = new String(Base64.encodeBase64(encVal), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt value: " + data, e);
        }

        return encryptedValue;
    }

    public String decrypt(String encryptedData) {
        String decryptedValue;
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decordedValue = Base64.decodeBase64(encryptedData.getBytes());
            byte[] decValue = c.doFinal(decordedValue);
            decryptedValue = new String(decValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt value: " + encryptedData, e);
        }

        return decryptedValue;
    }

    private Key generateKey() {
        byte[] key;
        try {
            key = salt.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        key = Arrays.copyOf(key, 16); // use only first 128 bit
        secretKey = new SecretKeySpec(key, ALGO);

        return secretKey;
    }
}