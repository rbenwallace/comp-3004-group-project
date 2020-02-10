package com.uniques.ourhouse;

import java.util.Date;

public class Task {
    private String description;
    private Date dueDate;
    private int frequency;
    private int difficulty;

    public Task(String description, Date dueDate, int frequency, int difficulty) {
        this.description = description;
        this.dueDate = dueDate;
        this.frequency = frequency;
        this.difficulty = difficulty;
    }
}
