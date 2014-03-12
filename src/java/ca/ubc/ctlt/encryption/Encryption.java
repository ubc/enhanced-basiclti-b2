package ca.ubc.ctlt.encryption;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Encryption instance
 */
public class Encryption {
    public final static String DEFAULT_KEY = "RANDOM KEY STRING";
    public final static String DEFAULT_IV = "RANDOM IV STRING";

    private final String algo = "AES";
    private final String mode = "CBC";
    private final String padding = "PKCS5Padding";
    private final String transformation = algo + "/" + mode + "/" + padding;
    private Key secretKey = null;
    private IvParameterSpec ivSpec = null;
    private Cipher cipher;

    public Encryption() {
        this(DEFAULT_KEY, DEFAULT_IV);
    }

    public Encryption(String keyString) {
        this(keyString, DEFAULT_IV);
    }

    public Encryption(String keyString, String iv) {
        this.secretKey = new SecretKeySpec(hashKey(keyString), algo);
        this.ivSpec = new IvParameterSpec(hashKey(iv));
        initCipher();
    }

    public void setKeyString(String keyString) {
        secretKey = new SecretKeySpec(hashKey(keyString), algo);
        initCipher();
    }

    public IvParameterSpec getIvSpec() {
        return ivSpec;
    }

    public void setIv(String iv) {
        this.ivSpec = new IvParameterSpec(hashKey(iv));
        initCipher();
    }

    public Key getSecretKey() {
        return secretKey;
    }

    private void initCipher() {
        try {
            if (null == cipher) {
                cipher = Cipher.getInstance(transformation);
            }
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), getIvSpec());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize cipher.", e);
        }
    }

    /**
     * Encrypt the data using AES
     *
     * @param data plain text data to be encrypted
     * @return     the encrypted text
     */
    public String encrypt(String data) {
        return encrypt(data, getIvSpec());
    }

    /**
     * Encrypt the data using AES using a initialization vector given from
     * parameter, this allows the internal stored IV to be ignored temporarily.
     *
     * @param data plain text data to be encrypted
     * @param iv   initialization vector to be used for encryption
     * @return     the encrypted text
     */
    public String encrypt(String data, String iv) {
        return encrypt(data, new IvParameterSpec(hashKey(iv)));
    }

    /**
     * Encrypt the data using AES
     *
     * @param data   plain text data to be encrypted
     * @param ivSpec initialization vector spec object
     * @return       the encrypted text
     */
    public String encrypt(String data, IvParameterSpec ivSpec) {
        String encryptedValue;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), ivSpec);
            byte[] encVal = cipher.doFinal(data.getBytes());
            encryptedValue = new String(Base64.encodeBase64(encVal), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt value: " + data, e);
        }

        return encryptedValue;
    }

    /**
     * Decrypt the data using AES
     *
     * @param encryptedData encrypted data
     * @return              decrypted string
     */
    public String decrypt(String encryptedData) {
        return decrypt(encryptedData, getIvSpec());
    }

    /**
     * decrypt the data using AES using a initialization vector given from
     * parameter, this allows the internal stored IV to be ignored temporarily.
     *
     * @param encryptedData encrypted data
     * @param iv            initialization vector to be used for encryption
     * @return              decrypted string
     */
    public String decrypt(String encryptedData, String iv) {
        return decrypt(encryptedData, new IvParameterSpec(hashKey(iv)));
    }

    /**
     * Decrypt the data using AES
     *
     * @param encryptedData encrypted data
     * @param ivSpec        initialization vector spec object
     * @return              decrypted string
     */
    public String decrypt(String encryptedData, IvParameterSpec ivSpec) {
        String decryptedValue;
        try {
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivSpec);

            byte[] decordedValue = Base64.decodeBase64(encryptedData.getBytes());
            byte[] decValue = cipher.doFinal(decordedValue);
            decryptedValue = new String(decValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt value: " + encryptedData, e);
        }

        return decryptedValue;
    }

    /**
     * Generate the SHA-1 hash of the string. Only first 16 bytes are returned
     *
     * @param original original string
     * @return         first 16 bytes of the hash
     */
    private byte[] hashKey(String original) {
        byte[] key;
        try {
            key = original.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Arrays.copyOf(key, 16); // use only first 128 bit
    }
}