package com.uniques.ourhouse.util.easyjson;

public class UnexpectedTokenException extends EasyJSONException {

    UnexpectedTokenException(String details) {
        super(UNEXPECTED_TOKEN, details);
    }
}
