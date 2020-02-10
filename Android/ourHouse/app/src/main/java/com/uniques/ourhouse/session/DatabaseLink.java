package com.uniques.ourhouse.session;

import com.uniques.ourhouse.model.User;

import java.util.UUID;

public interface DatabaseLink {
    // TODO schedule save events, potentially cancel events when saving already in progress

    User getUser(UUID id);

    boolean postUser(User user);
}
