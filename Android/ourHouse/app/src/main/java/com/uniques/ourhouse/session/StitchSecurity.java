package com.uniques.ourhouse.session;

import java.util.UUID;

public abstract class StitchSecurity extends SecurityLink {

    @Override
    SecureAuthenticator getSecureAuthenticator() {
        return null;
    }

    @Override
    protected boolean autoAuthenticate(UUID id, UUID loginKey) {
        return false;
    }
}
