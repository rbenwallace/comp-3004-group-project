package com.uniques.ourhouse.session;

import com.uniques.ourhouse.util.Model;

public interface DatabaseLink {
    // TODO schedule save events, potentially cancel events when saving already in progress

    Model get();

    boolean post();
}
