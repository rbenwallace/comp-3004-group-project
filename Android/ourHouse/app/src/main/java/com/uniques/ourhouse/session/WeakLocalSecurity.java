package com.uniques.ourhouse.session;

import android.content.Context;
import android.util.Log;

import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import java.io.File;
import java.util.UUID;


abstract class WeakLocalSecurity extends SecurityLink {
    private static final String SECURITY_FILE = "security.json";
    private Context context;

    WeakLocalSecurity(Context context) {
        this.context = context;
    }

    /**
     * There is no secure authenticator (this is a weak security class)
     */
    @Override
    protected SecureAuthenticator getSecureAuthenticator() {
        return new SecureAuthenticator() {
            @Override
            public boolean newUser(String fullName, String email, String phoneNumber, String password) {
                EasyJSON json = open();
                for (JSONElement obj : json.getRootNode().getChildren()) {
                    if (obj.valueOf("email").equals(email)
                            && obj.valueOf("password").equals(password)) {
                        return false;
                    }
                }
                User user = new User(fullName, email, phoneNumber);
                if (getDatabaseLink().postUser(user)) {
                    JSONElement struct = json.putStructure(""); // insert an object into the array
                    struct.putPrimitive("id", user.getId().toString());
                    struct.putPrimitive("email", email);
                    struct.putPrimitive("password", password);
                    UUID loginKey = UUID.randomUUID();
                    struct.putPrimitive("loginKey", loginKey.toString());
                    try {
                        json.save();
                        return onAuthenticate(user.getId(), loginKey);
                    } catch (EasyJSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean authenticate(String email, String password) {
                return validateLogin(email, password);
            }
        };
    }

    @Override
    protected boolean validateLogin(String email, String password) {
        EasyJSON json = open();
        for (JSONElement obj : json.getRootNode().getChildren()) {
            if (obj.valueOf("email").equals(email)
                    && obj.valueOf("password").equals(password)) {
                Log.d("WeakLocalSecurity", "login valid");
                return onAuthenticate(
                        UUID.fromString(obj.valueOf("id")),
                        UUID.fromString(obj.valueOf("loginKey")));
            }
        }
        Log.d("WeakLocalSecurity", "login invalid");
        return false;
    }

    @Override
    protected boolean validateKey(UUID loginKey) {
        EasyJSON json = open();
        for (JSONElement obj : json.getRootNode().getChildren()) {
            if (obj.valueOf("loginKey").equals(loginKey.toString())) {
                Log.d("WeakLocalSecurity", "auto-login valid");
                return onAuthenticate(UUID.fromString(obj.valueOf("id")), loginKey);
            }
        }
        Log.d("WeakLocalSecurity", "auto-login invalid");
        return false;
    }

    private EasyJSON open() {
        File file = new File(context.getFilesDir(), SECURITY_FILE);
        if (file.exists()) {
            try {
                return EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
        }
        EasyJSON json = EasyJSON.create(file);
        json.getRootNode().setType(SafeJSONElementType.ARRAY);
        return json;
    }
}
