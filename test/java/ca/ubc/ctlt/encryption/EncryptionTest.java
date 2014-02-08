package ca.ubc.ctlt.encryption;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Created by compass on 2/7/2014.
 */
public class EncryptionTest {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @Test
    public void testEncrypt() throws Exception {
        Encryption encryptor  = new Encryption("1231232143412");

        assertEquals(encryptor.encrypt("19803030"), "dZxwjpNgI1GBDnMY4jlGjg==");
    }

    @Test
    public void testDecrypt() throws Exception {
        Encryption encryptor  = new Encryption("1231232143412");

        assertEquals(encryptor.decrypt("dZxwjpNgI1GBDnMY4jlGjg=="), "19803030");
    }
}
