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
 *
 * <p>
 * In BLTI message transmission, the following identifiable
 * information are possibly transmitted to external tool providers:
 * <ul>
 *     <li>username</li>
 *     <li>student ID</li>
 *     <li>Batch UID</li>
 *     <li>external ID</li>
 *     <li>email address</li>
 *     <li>given name</li>
 *     <li>family name</li>
 *     <li>full name</li>
 * </ul>.
 * When encryption function in BLTI building block is enabled, those
 * information will be encrypted using Advanced Encryption Standard
 * (AES) with Cipher-block chaining (CBC) mode and PKCS#5 padding.
 * Once encrypted, the messages are sent to external tool provider.
 * This includes LTI launch message and extension messages. The building
 * block also has ability to decrypt the messages when external tool
 * sending back the grade. The identifier in the grade message are
 * encrypted and will be decrypted by the building block in order to
 * identify the user in Blackboard.
 * </p>
 * <p>
 * <b>Encryption/Decryption Key</b>
 * In the building block configuration page, there is a
 * encryption/decryption key to be set by Blackboard administrator.
 * The different keys should be set to different BLTI tool providers
 * to ensure the same message are encrypted into different cipher text
 * to prevent the cross reference identification.
 * </p>
 * <p>
 * <b>Initialization Vector (IV) in CBC</b>
 *  The following fields are encrypted with Default IV as they can
 * be selected as ID for external tool provider in BLTI settings.
 * We need to be able to decrypt them with a known IV.
 * <ul>
 *     <li>ExternalId</li>
 *     <li>Username</li>
 *     <li>StudentId</li>
 *     <li>BatchUid</li>
 * </ul>
 * Other attributes are encrypted with hash of the external ID as
 * the IV in order to avoid encrypted message collision when the
 * attributes have the same value.
 * </p><p>
 * <b>Email</b>
 * Some tools check the email format. Therefore, only the username (the
 * string before @) is encrypted. In order to prevent leaking information
 * of the email domain, a pseudo domain can be provided by blackboard
 * administrator. In that case, the email sent to external tool provider
 * will be "encrypted_value@pseudo domain". Another usage for this approach
 * is to set up the email forwarding on the pseudo domain so that when the
 * email sent from external tool, the message still can reach to the user.
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
     * Generate the SHA-1 hash of the string and return specified length
     *
     * @param original original string
     * @param length   length needed to be returned
     * @return         the hash of the string with specified length
     */
    public static byte[] hash(String original, int length) {
        byte[] key;
        try {
            key = original.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if (length == 0) {
            return key;
        } else {
            return Arrays.copyOf(key, length); // use only first "length" bit
        }
    }

    public static byte[] hash(String original) {
        return hash(original, 0);
    }

    /**
     * Generate the SHA-1 hash of the string. Only first 16 bytes are returned
     *
     * @param original original string
     * @return         first 16 bytes of the hash
     */
    public static byte[] hashKey(String original) {
        return hash(original, 16);
    }
}