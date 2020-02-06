package com.uniques.ourhouse.session;

import android.content.Context;

import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import java.io.File;
import java.util.UUID;


abstract class LocalSecurity extends SecurityLink {
    private static final String SECURITY_FILE = "security.json";
    private Context context;

    LocalSecurity(Context context) {
        this.context = context;
    }

    @Override
    protected SecureAuthenticator getSecureAuthenticator() {
        //todo return
        return null;
    }

    @Override
    protected boolean validateLogin(String email, String password) {
        EasyJSON json = open();
        for (JSONElement obj : json.getRootNode().getChildren()) {
            if (obj.valueOf("email").equals(email)
                    && obj.valueOf("password").equals(password)) {
                return onAuthenticate(
                        UUID.fromString(obj.valueOf("id")),
                        UUID.fromString(obj.valueOf("loginKey")));
            }
        }
        return false;
    }

    @Override
    protected boolean validateKey(UUID loginKey) {
        EasyJSON json = open();
        for (JSONElement obj : json.getRootNode().getChildren()) {
            if (obj.valueOf("loginKey").equals(loginKey.toString())) {
                return onAuthenticate(UUID.fromString(obj.valueOf("id")), loginKey);
            }
        }
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
