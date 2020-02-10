package com.uniques.ourhouse.session;

import java.util.UUID;

abstract class SecurityLink {

    abstract DatabaseLink getDatabaseLink();

    protected abstract boolean onAuthenticate(UUID id, UUID loginKey);

    protected abstract SecureAuthenticator getSecureAuthenticator();

    protected abstract boolean validateLogin(String email, String password);

    protected abstract boolean validateKey(UUID loginKey);

    public interface SecureAuthenticator {

        boolean newUser(String fullName, String email, String phoneNumber, String password);

        boolean authenticate(String email, String password);
    }
}
