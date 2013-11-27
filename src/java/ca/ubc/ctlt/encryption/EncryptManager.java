package ca.ubc.ctlt.encryption;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Ronald
 * Date: 2013-10-21
 * Time: 5:31 PM
 * Manages the actual ctlt.encryption and decription process
 */
public class EncryptManager {

//	Properties encryptedObj;
	Encryption encryptInstance = new Encryption();
	
//    public static void main(String[] args) throws Exception {
//
//        String password = "mypassword";
//        String passwordEnc = Encryption.encrypt(password);
//        String passwordDec = Encryption.decrypt(passwordEnc);
//
//        System.out.println("Plain Text : " + password);
//        System.out.println("Encrypted Text : " + passwordEnc);
//        System.out.println("Decrypted Text : " + passwordDec);
//    }
    public Properties encrypt(Properties propObj)
    {
//    	System.out.println(propObj.containsKey("user_id"));
    	String userIDString = propObj.getProperty("user_id");
    	String nameGiven = propObj.getProperty("lis_person_name_given");
    	String nameFamily = propObj.getProperty("lis_person_name_family");
    	String nameFull = propObj.getProperty("lis_person_name_full");
    	String emailPrimary = propObj.getProperty("lis_person_contact_email_primary");
    	String sourceDID = propObj.getProperty("lis_person_sourcedid");

    	
    	System.out.println(userIDString);
    	System.out.println(nameGiven);
    	System.out.println(nameFamily);
    	System.out.println(nameFull);
    	System.out.println(emailPrimary);
    	System.out.println(sourceDID);	
    	 
    	 try {
			propObj.setProperty("user_id", encryptInstance.encrypt(userIDString));
			propObj.setProperty("lis_person_name_given", encryptInstance.encrypt(nameGiven));
			propObj.setProperty("lis_person_name_family", encryptInstance.encrypt(nameFamily));
			propObj.setProperty("lis_person_name_full", encryptInstance.encrypt(nameFull));
			propObj.setProperty("lis_person_contact_email_primary", encryptInstance.encrypt(emailPrimary));
			propObj.setProperty("lis_person_sourcedid", encryptInstance.encrypt(sourceDID));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    	
     	String userIDStringEnc = propObj.getProperty("user_id");
     	String nameGivenEnc = propObj.getProperty("lis_person_name_given");
     	String nameFamilyEnc = propObj.getProperty("lis_person_name_family");
     	String nameFullEnc = propObj.getProperty("lis_person_name_full");
     	String emailPrimaryEnc = propObj.getProperty("lis_person_contact_email_primary");
     	String sourceDIDEnc = propObj.getProperty("lis_person_sourcedid");
     	
     	
    	System.out.println(userIDStringEnc);
    	System.out.println(nameGivenEnc);
    	System.out.println(nameFamilyEnc);
    	System.out.println(nameFullEnc);
    	System.out.println(emailPrimaryEnc);
    	System.out.println(sourceDIDEnc);	
    	
    	return propObj;
    }

}
