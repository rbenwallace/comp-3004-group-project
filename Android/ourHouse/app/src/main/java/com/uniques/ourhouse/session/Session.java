package com.uniques.ourhouse.session;

import android.content.Context;
import android.util.Log;

import com.uniques.ourhouse.model.User;

import java.util.UUID;

public final class Session {

    private static Session baseSession;

    public static boolean newSession(Context context) {
        baseSession = new Session();
        baseSession.database = new LocalStore(context);
        baseSession.security = new WeakLocalSecurity(context) {
            @Override
            DatabaseLink getDatabaseLink() {
                return baseSession.getDatabase();
            }

            @Override
            protected boolean onAuthenticate(UUID id, UUID loginKey) {
                Settings.USER_LOGIN_KEY.set(loginKey);
                baseSession.user = baseSession.database.getUser(id);
                Log.d("Session", "loginKey= " + loginKey + " success= " + (baseSession.user != null));
                return baseSession.user != null;
            }
        };
        Settings.init(context);

        return Settings.USER_LOGIN_KEY.get() != null
                && baseSession.security.validateKey(Settings.USER_LOGIN_KEY.get());
    }

    public static Session getSession() {
        return baseSession;
    }

    private DatabaseLink database;
    private SecurityLink security;
    private User user;

    public DatabaseLink getDatabase() {
        return database;
    }

    public SecurityLink.SecureAuthenticator getSecureAuthenticator() {
        return security.getSecureAuthenticator();
    }
}
