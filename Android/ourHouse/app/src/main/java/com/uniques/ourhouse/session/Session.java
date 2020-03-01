package com.uniques.ourhouse.session;

import android.content.Context;

import com.uniques.ourhouse.model.User;

public final class Session {

    private static Session baseSession;

    public static boolean newSession(Context context) {
        baseSession = new Session();
        baseSession.user = new User("Test user", "email@test.com", "1234567890");
        baseSession.database = new LocalStore(context);
//        baseSession.security = new LocalSecurity(context) {
//            @Override
//            DatabaseLink getDatabaseLink() {
//                return baseSession.getDatabase();
//            }
//
//            @Override
//            protected boolean onAuthenticate(UUID id, UUID loginKey) {
//                Settings.STUDENT_LOGIN_KEY.set(loginKey);
//                //todo login
////                baseSession.student = baseSession.database.getStudent(id);
//                return false;
//            }
//        };

        Settings.init(context);
        return true;
    }

    public static Session getSession() {
        return baseSession;
    }

    private User user;
    private DatabaseLink database;
    private SecurityLink security;

    public User getLoggedInUser() {
        return user;
    }

    public DatabaseLink getDatabase() {
        return database;
    }

    public SecurityLink.SecureAuthenticator getSecureAuthenticator() {
        return security.getSecureAuthenticator();
    }
}
