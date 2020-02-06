package com.uniques.ourhouse.session;

import android.content.Context;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

final class LocalStore implements DatabaseLink {
    private static final String PEOPLE_FILE = "people.json";
    private static final String CLUBS_FILE = "clubs.json";
    private static final String EVENTS_FILE = "events.json";
    private static final String TODOS_FILE = "todos.json";
    private static final String COURSES_FILE = "courses.json";
    private static final String EVALUATIONS_FILE = "evaluations.json";

    private Context context;

    LocalStore(Context context) {
        this.context = context;
    }

    @Override
    public Model get() {
        return null;
    }

    @Override
    public boolean post() {
        return false;
    }

    private JSONElement searchLocal(String fileName, UUID id) {
        return Objects.requireNonNull(Objects.requireNonNull(retrieveLocal(fileName)).search(id.toString()));
    }

    private boolean saveLocal(String filename, Indexable model) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(filename));
        json.putStructure(model.getId().toString(), model.toJSON());
        try {
            json.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private EasyJSON retrieveLocal(String fileName) {
        File file = getLocalFile(fileName);
        if (!file.exists()) {
            populateStores();
            return retrieveLocal(fileName);
        }
        try {
            return EasyJSON.open(file);
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getLocalFile(String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    private void populateStores() {
//        Student student = new Student("A", "Scholar", "scholar@school.edu", new Date());
//        Student dude = new Student("Another", "Straight-shooter", "topshot@school.edu", new Date());
//        Teacher mark = new Teacher("Mark", "Lanthier", "mark.lanthier@school.edu", new Date());
//        Teacher christine = new Teacher("Christine", "Laurendeau", "christine.laurendeau@school.edu", new Date());
//        Teacher eaket = new Teacher("Christopher", "Eaket", "christine.laurendeau@school.edu", new Date());
//
//        Subject comp = student.getCalendar().newSubject("Computer Science", "COMP");
//        Subject digh = student.getCalendar().newSubject("Digital Humanities", "DIGH");
//
//        populateNewStore(PEOPLE_FILE, student, dude, mark, christine, eaket);
//
//        Course java = comp.newCourse("Intro to Java", "1406", mark);
//        java.newClass("B", new Schedule());
//
//        Course cplus = comp.newCourse("Intro to Software Engineering", "2404", christine);
//        cplus.newClass("A", new Schedule());
//
//        Course intro = digh.newCourse("Intro to Digital Hums", "2001", eaket);
//
//        populateNewStore(COURSES_FILE, java, cplus, intro);
//
//        java.getEvaluation().newDeliverable("Assignment 2", "", new Schedule());
//        java.getEvaluation().newTest("Midterm", "Topics 1-6", new Schedule());
//        java.getEvaluation().newTest("Final", "Sucks to be u", new Schedule());
//
//        cplus.getEvaluation().newDeliverable("Assignment 1", "Bullsh$t", new Schedule());
//        cplus.getEvaluation().newDeliverable("Assignment 2", "", new Schedule());
//        cplus.getEvaluation().newTest("Midterm", "Topics 1-4", new Schedule());
//        cplus.getEvaluation().newTest("Final", "Sucks to be u", new Schedule());
//
//        intro.newClass("A", new Schedule());
//        intro.getEvaluation().newDeliverable("Project analysis", "", new Schedule());
//        intro.getEvaluation().newDeliverable("Response paper", "", new Schedule());
//        intro.getEvaluation().newDeliverable("Design doc", "", new Schedule());
//        intro.getEvaluation().newDeliverable("Final paper", "", new Schedule());
//
//        populateNewStore(EVALUATIONS_FILE, java.getEvaluation(), cplus.getEvaluation(), intro.getEvaluation());
//
//        populateNewStore(EVENTS_FILE,
//                student.getCalendar().newEvent("Rate my prof day", "", new Schedule()));
//        populateNewStore(TODOS_FILE,
//                student.getCalendar().newTodo("Contest A1 marks", "TA:paul"));
//
//        Club club = student.newClub("Retro Music Club", "Synth pop lovers welcome too :)");
//        club.addMember(dude);
//        populateNewStore(CLUBS_FILE, club);
    }

    private void populateNewStore(String fileName, Indexable... models) {
        EasyJSON store = EasyJSON.create(getLocalFile(fileName));
        store.getRootNode().setType(SafeJSONElementType.ARRAY);
        for (Indexable model : models) {
            store.putStructure(model.getId().toString(), model.toJSON());
        }
        try {
            store.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
    }
}
