package ca.ubc.ctlt.encryption;

import blackboard.data.user.User;
import blackboard.persist.Id;

/**
 * A wrapper class of BB user class with encryption feature.
 */
public class UserWrapper extends User {
    private User user;
    private Encryption encryptor;
    private boolean isEncrypt;
    private String pseudoDomain;

    public UserWrapper(User user, Encryption encryptor, boolean isEncrypt) {
        this.user = user;
        this.encryptor = encryptor;
        this.isEncrypt = isEncrypt;
        // use user ID external string as initialization vector
        this.encryptor.setIv(user.getId().getExternalString());
    }

    public void setPseudoDomain(String pseudoDomain) {
        this.pseudoDomain = pseudoDomain;
    }

    /**
     * Internal ID is used to retrieve other information. So no encryption
     * @return  Id
     */
    public Id getId() {
        return user.getId();
    }

    public String getExternalId() {
        return isEncrypt ? encryptor.encrypt(user.getId().getExternalString(), Encryption.DEFAULT_IV) : user.getId().getExternalString();
    }

    public String getUsername() {
        return isEncrypt ? encryptor.encrypt(user.getUserName(), Encryption.DEFAULT_IV) : user.getUserName();
    }

    public String getStudentId() {
        return isEncrypt ? encryptor.encrypt(user.getStudentId(), Encryption.DEFAULT_IV) : user.getStudentId();
    }

    public String getBatchUid() {
        return isEncrypt ? encryptor.encrypt(user.getBatchUid(), Encryption.DEFAULT_IV) : user.getBatchUid();
    }

    public String getEmailAddress() {
        String value = user.getEmailAddress();
        if (value == null) {
            return null;
        }
        String[] email = value.split("@");

        String domain = email.length > 1 ? email[1] : "";
        domain = (null == pseudoDomain || pseudoDomain.isEmpty()) ? domain : pseudoDomain;

        return isEncrypt ? encryptor.encrypt(email[0]) + "@" + domain : user.getEmailAddress();
    }

    public String getGivenName() {
        return isEncrypt ? encryptor.encrypt(user.getGivenName()) : user.getGivenName();
    }

    public String getFamilyName() {
        return isEncrypt ? encryptor.encrypt(user.getFamilyName()) : user.getFamilyName();
    }

    public String getFullName() {
        String fullname = user.getGivenName();
        if ((user.getMiddleName() != null) && (user.getMiddleName().length() > 0)) {
            fullname += " " + user.getMiddleName();
        }
        fullname += " " + user.getFamilyName();

        return isEncrypt ? encryptor.encrypt(fullname) : fullname;
    }

    public SystemRole getSystemRole() {
        return user.getSystemRole();
    }

    public String getLocale() {
        return user.getLocale();
    }
}
