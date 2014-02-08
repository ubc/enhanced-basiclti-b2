package ca.ubc.ctlt.encryption;

import java.util.Properties;

/**
 * Manages the actual ctlt.encryption and description process
 */

public class EncryptManager {

    Encryption encryptInstance = new Encryption();
    String[] propertyNames = {"user_id", "lis_person_name_given", "lis_person_name_family", "lis_person_name_full", "lis_person_contact_email_primary", "lis_person_sourcedid"};

    public Properties encrypt(Properties propObj) {
        return encrypt(propObj, "");
    }

    public Properties encrypt(Properties propObj, String salt) {
        encryptInstance.setSalt(salt);
        try {
            for (String property : propertyNames) {
                if (propObj.getProperty(property) != null) {
                    String value = propObj.getProperty(property);

                    if ("lis_person_contact_email_primary".equals(property)) {
                        // split the email address by @ and encrypt the first part
                        String[] email = value.split("(?=@)");
                        propObj.setProperty(property, encryptInstance.encrypt(email[0]) + (email.length > 1 ? email[1] : ""));
                    } else {
                        propObj.setProperty(property, encryptInstance.encrypt(value));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return propObj;
    }
}