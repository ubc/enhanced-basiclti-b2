package ca.ubc.ctlt.encryption;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * Created by compass on 2/7/2014.
 */
public class EncryptionTest {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @Test
    public void testEncrypt() throws Exception {
        Encryption encryptor = new Encryption("1231232143412", "Random vector");

        assertEquals(encryptor.encrypt("19803030"), "q3BxzRQGA/4QT3r58m4bbA==");
    }

    @Test
    public void testDecrypt() throws Exception {
        Encryption encryptor = new Encryption("1231232143412", "Random vector");

        assertEquals(encryptor.decrypt("q3BxzRQGA/4QT3r58m4bbA=="), "19803030");
    }

    @Test
    public void testConstructorWithoutParameter() throws Exception {
        Encryption encryptor = new Encryption();

        assertEquals(encryptor.decrypt(encryptor.encrypt("19803030")), "19803030");
    }

    @Test
    public void testConstructorWithKey() throws Exception {
        Encryption encryptor = new Encryption("SecretKey");

        assertEquals(encryptor.decrypt(encryptor.encrypt("19803030")), "19803030");
    }

    @Test
    public void testSetSecretKey() throws Exception {
        Encryption encryptor = new Encryption();
        encryptor.setKeyString("SecretKey");

        assertEquals(encryptor.decrypt(encryptor.encrypt("19803030")), "19803030");
    }

    @Test
    public void testSetIV() throws Exception {
        Encryption encryptor = new Encryption("SecretKey");
        encryptor.setIv("Vector");

        assertEquals(encryptor.decrypt(encryptor.encrypt("19803030")), "19803030");
    }

    @Test
    public void testDifferentIV() throws Exception {
        Encryption encryptor = new Encryption("SecretKey");

        assertEquals(encryptor.decrypt(encryptor.encrypt("19803030", "Vector"), "Vector"), "19803030");

        assertNotEquals(encryptor.encrypt("19803030", "Vector"), encryptor.encrypt("19803030", "Vector1"));

    }
}