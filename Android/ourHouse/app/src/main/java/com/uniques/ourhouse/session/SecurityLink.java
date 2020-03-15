package com.uniques.ourhouse.session;

import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.User;

import org.bson.types.ObjectId;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class SecurityLink {

    public abstract SecureAuthenticator getSecureAuthenticator();

    protected abstract boolean autoAuthenticate();

    abstract ObjectId getLoggedInUserId();

    public abstract static class SecureAuthenticator {

//        AuthenticationListener authListener;
//
//        SecureAuthenticator() {
//            authListener = new AuthenticationListener() {
//                @Override
//                public void onRegistered(User user) {
//                }
//
//                @Override
//                public void onSuccess(UUID userId, UUID loginKey) {
//                }
//
//                @Override
//                public void onFail(String email, String password) {
//                }
//            };
//        }

        /**
         * This function creates a new user account in a database. The function should first create a new
         * {@link com.uniques.ourhouse.model.User} instance, then associate the User's id with the supplied
         * email address and password.
         * @see User#getId()
//         * @see AuthenticationListener#onRegistered(User)
         * @param email supplied email address
         * @param password supplied password
         */
        public abstract void registerUser(String email, String password, Consumer<Exception> callback);

//        public void listenToAuth(AuthenticationListener authListener) {
//            this.authListener = authListener;
//        }

        /**
         * @param username supplied username
         * @param password supplied password
//         * @see AuthenticationListener#onSuccess(UUID, UUID)
//         * @see AuthenticationListener#onFail(String, String)
         */
        public abstract void authenticateUser(String username, String password, BiConsumer<Exception, ObjectId> callback);

        /**
         * Logout the user from the current session
         * @param activity the current activity
         */
        public abstract void logout(FragmentActivity activity, Consumer<Boolean> consumer);
    }

//    interface AuthenticationListener {
//        /**
//         * The secure authenticator should call this function to notify that the user was created.
//         * @param user secure authenticator must supply the User object it created during registration
//         * @see User
//         */
//        void onRegistered(User user);
//
//        /**
//         * The secure authenticator should call this function through its {@link SecureAuthenticator#authListener}
//         * if a {@link SecureAuthenticator#authenticateUser(String, String, BiConsumer)} request was successful
//         * @param userId id of the authenticated user (pulled from a database)
//         * @param loginKey secure authenticator must supply a random UUID to auto-login the user next time
//         */
//        void onSuccess(UUID userId, UUID loginKey);
//
//        /**
//         * The secure authenticator should call this function through its {@link SecureAuthenticator#authListener}
//         * if a {@link SecureAuthenticator#authenticateUser(String, String, BiConsumer)} request was successful
//         * @param email the email used in the authentication request
//         * @param password the password used in the authentication request
//         */
//        void onFail(String email, String password);
//    }
}
