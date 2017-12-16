package persistence;

import model.Relation;
import model.Subject;

import java.util.HashMap;

public class TransactionHandler {

    private Database database;

    public TransactionHandler() {
        database = new Database();
    }

    public void requestUndo() {
        database.removeLastEntry();
        System.out.println("Previous entry removed.");
        requestShow();
    }

    public void requestReset() {
        database.reset();
        requestShow();
    }

    public void requestInsert(String insertion) {
        String[] splitInsertion = insertion.split(" ");
        String id = splitInsertion[0];
        int databaseEntryNumber = database.queryKey(id);
        EntryData entryData;
        boolean newEntryData = false;

        if(databaseEntryNumber == 0) {
            entryData = new EntryData();
            newEntryData = true;
            Subject subjectId = database.findSubject(id);

            if(subjectId != null) {
                entryData.setID(subjectId);
            } else {
                Subject newSubject = new Subject(id);
                entryData.setID(newSubject);
                database.addSubject(newSubject);
            }

        } else {
            entryData = database.getEntryData(databaseEntryNumber);
        }

        for(int index = 1; index + 1 < splitInsertion.length; index += 2) {
            Relation relation = database.findRelation(splitInsertion[index]);

            if(relation == null) {
                relation = new Relation(splitInsertion[index]);
                database.addRelation(relation);
            }
            Subject subject = database.findSubject(splitInsertion[++index]);

            if(subject == null) {
                subject = new Subject(splitInsertion[index]);
                database.addSubject(subject);
            }

            entryData.put(relation, subject);
        }

        if(newEntryData) {
            database.insert(entryData);
        }

        requestShow();
    }

    public void requestShow() {
        System.out.println(database);
    }

    // TODO
    public void requestQuery(String query) {
        String[] splitQuery = query.split(":");
        String select = splitQuery[0];
        String where = splitQuery[1];
        HashMap<Character, Object> variableMappings = new HashMap<>();
    }
}
