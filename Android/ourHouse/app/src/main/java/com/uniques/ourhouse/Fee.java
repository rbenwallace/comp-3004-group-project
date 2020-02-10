package com.uniques.ourhouse;

public class Fee {
    private String description;
    private float amount;
    private int frequency;

    public Fee(String description, float amount, int frequency) {
        this.description = description;
        this.amount = amount;
        this.frequency = frequency;
    }
}
