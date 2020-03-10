package com.uniques.ourhouse.session;

import com.uniques.ourhouse.model.User;

import java.util.UUID;

public abstract class SecurityLink {

    abstract SecureAuthenticator getSecureAuthenticator();

    protected abstract boolean autoAuthenticate(UUID id, UUID loginKey);

    abstract class SecureAuthenticator implements AuthenticationListener {
        AuthenticationListener authListener;
        SecureAuthenticator() {
            authListener = new AuthenticationListener() {
                @Override
                public void onRegistered(User user) {
                }
                @Override
                public void onSuccess(UUID userId, UUID loginKey) {
                }
                @Override
                public void onFail(String email, String password) {
                }
            };
        }

        /**
         * This function creates a new user account in a database. The function should first create a new
         * {@link com.uniques.ourhouse.model.User} instance, then associate the User's id with the supplied
         * email address and password.
         * @see User#getId()
         * @see AuthenticationListener#onRegistered(User)
         * @param email supplied email address
         * @param password supplied password
         */
        public abstract void registerUser(String email, String password);

        public void listenToAuth(AuthenticationListener authListener) {
            this.authListener = authListener;
        }

        /**
         * Make sure you call {@link #listenToAuth(AuthenticationListener)} before you call this function!
         * @param email supplied email
         * @param password supplied password
         * @see AuthenticationListener#onSuccess(UUID, UUID)
         * @see AuthenticationListener#onFail(String, String)
         */
        public abstract void authenticateUser(String email, String password);
    }

    interface AuthenticationListener {
        /**
         * The secure authenticator should call this function to notify that the user was created.
         * @param user secure authenticator must supply the User object it created during registration
         * @see User
         */
        void onRegistered(User user);

        /**
         * The secure authenticator should call this function through its {@link SecureAuthenticator#authListener}
         * if a {@link SecureAuthenticator#authenticateUser(String, String)} request was successful
         * @param userId id of the authenticated user (pulled from a database)
         * @param loginKey secure authenticator must supply a random UUID to auto-login the user next time
         */
        void onSuccess(UUID userId, UUID loginKey);

        /**
         * The secure authenticator should call this function through its {@link SecureAuthenticator#authListener}
         * if a {@link SecureAuthenticator#authenticateUser(String, String)} request was successful
         * @param email the email used in the authentication request
         * @param password the password used in the authentication request
         */
        void onFail(String email, String password);
    }
}
