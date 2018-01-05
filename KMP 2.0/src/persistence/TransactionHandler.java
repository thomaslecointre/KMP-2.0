package persistence;

import model.Relation;
import model.Subject;

import java.util.*;

/**
 * This class is an intermediary request handling class and communicates with Database.
 */
public class TransactionHandler {

    private Database database;

    public TransactionHandler() {
        database = new Database();
    }

    /**
     * Restores the database to its previous state.
     */
    // TODO
    public void requestUndo() {

    }

    /**
     * Restores the database to its initial state.
     */
    public void requestReset() {
        database.reset();
        requestShow();
    }

    /**
     * Handles an insertion request.
     * @param insertion a string corresponding to a user entry.
     */
    public void requestInsert(String insertion) {
        String[] splitInsertion = insertion.split(" ");
        String id = splitInsertion[0];
        int key = database.findKey(id);
        EntryData entryData;
        boolean newEntryData = false;

        if(key == 0) {
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
            entryData = database.getEntryData(key);
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

    /**
     * Prints the database on standard output.
     */
    public void requestShow() {
        System.out.println(database);
    }

    /**
     * Class used to represent mappings for certain WHERE variables.
     */
    private class SpoofResult {
        int key;
        HashSet<Subject> subjects = new HashSet<>();

        SpoofResult(int key) {
            this.key = key;
        }

        /**
         * Adds an instance of Subject to the subjects field.
         * @param subject an instance of Subject.
         */
        void addSubject(Subject subject) {
            this.subjects.add(subject);
        }

        /**
         * Adds all instances of Subjects from the subjects parameter to the subjects field.
         * @param subjects an set of subjects.
         */
        void addSubjects(HashSet<Subject> subjects) {
            this.subjects.addAll(subjects);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof SpoofResult)) {
                return false;
            }
            SpoofResult spoofResult = (SpoofResult) object;
            return this == spoofResult || this.key == spoofResult.key && this.subjects.equals(spoofResult.subjects);
        }
    }

    // TODO

    /**
     * Method used to process queries of all sorts defined by a loosely based SPARQL-like language.
     * The method splits the query into two at the beginning, the left side corresponding to the SELECT phase,
     * the right corresponding to the WHERE phase.
     * @param query a string corresponding to a user entry.
     */
    public void requestQuery(String query) {
        // Split the query into its SELECT phase and its WHERE phase
        String[] splitQuery = query.split(":");
        
        String selectStatement = splitQuery[0];
        String[] selectorStrings = selectStatement.split(", ");

        // Prepare mappings for SELECT variables
        HashMap<String, Object> selectorMappings = new HashMap<>();
        for(String selectorString : selectorStrings) {
        	selectorMappings.put(selectorString, null);
        }

        
        String whereStatement = splitQuery[1];
        // Split each WHERE substatement by '&'
        String[] conditionStatements = whereStatement.split("& ");

        HashMap<String, SpoofResult> conditionMappings = new HashMap<>();
        // Iterate through all the conditions of the WHERE statement
        for(String conditionStatement : conditionStatements) {

            String[] conditionStrings = conditionStatement.split(" ");

        	// Evaluating left side of the condition
        	String left = conditionStrings[0];
            HashSet<SpoofResult> spoofResultsLeft = new HashSet<>();
            if(left.charAt(0) == '?') {
                Set<Integer> keys = database.getAllKeys();
                for(Integer key : keys) {
                    spoofResultsLeft.add(new SpoofResult(key));
                }
            } else {
                // Test if left is an id or an int value (key)
                int key = 0;
                try {
                    key = Integer.parseInt(left);
                } catch (NumberFormatException e) {
                    key = database.findKey(left);
                } finally {
                    if (key != 0) {
                        spoofResultsLeft.add(new SpoofResult(key));
                    }
                }
            }

            // Evaluating the middle of the condition (relation)
            String middle = conditionStrings[1];
            HashSet<Relation> relations = new HashSet<>();
            if(middle.charAt(0) == '?') {
                for(SpoofResult spoofResult : spoofResultsLeft) {
                    EntryData entryData = database.getEntryData(spoofResult.key);
                    relations.addAll(entryData.relations());
                }
            } else {
                Relation relation = database.findRelation(middle);
                if (relation != null) {
                    relations.add(relation);
                }
            }

            // Evaluating the right side of the condition
            String right = conditionStrings[2];
            HashSet<SpoofResult> spoofResultsRight = new HashSet<>();
            if(right.charAt(0) == '?') {
                if(middle.charAt(0) == '?') {
                    for(SpoofResult spoofResultLeft : spoofResultsLeft) {
                        EntryData entryData = database.getEntryData(spoofResultLeft.key);
                        if (entryData != null) {
                            SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.key);
                            for(Relation relation : relations) {
                                if(entryData.containsKey(relation)) {
                                    spoofResultRight.addSubjects(entryData.getSubjects(relation));
                                }
                            }
                            spoofResultsRight.add(spoofResultRight);
                        }
                    }
                } else {
                    Relation relation = relations.iterator().next();
                    if (relation != null) {
                        for(SpoofResult spoofResultLeft : spoofResultsLeft) {
                            EntryData entryData = database.getEntryData(spoofResultLeft.key);
                            if (entryData != null) {
                                HashSet<Subject> subjects = entryData.getSubjects(relation);
                                SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.key);
                                spoofResultRight.addSubjects(subjects);
                                spoofResultsRight.add(spoofResultRight);
                            }
                        }
                    }
                }
            } else {
                Subject subject = database.findSubject(right);
                if (subject != null) {
                    if (middle.charAt(0) != '?') {
                       for (SpoofResult spoofResultLeft : spoofResultsLeft) {
                           EntryData entryData = database.getEntryData(spoofResultLeft.key);
                           SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.key);
                           for(Relation relation : relations) {
                               if(entryData.containsKey(relation)) {
                                   if(entryData.getSubjects(relation).contains(subject)) {
                                       spoofResultRight.addSubject(subject);
                                       spoofResultsRight.add(spoofResultRight);
                                       break;
                                   }
                               }
                           }
                       }
                    } else {
                        Relation relation = relations.iterator().next();
                        for (SpoofResult spoofResultLeft : spoofResultsLeft) {
                            EntryData entryData = database.getEntryData(spoofResultLeft.key);
                            SpoofResult spoofResultRight = new SpoofResult(spoofResultLeft.key);
                            if(entryData.containsKey(relation)) {
                                if(entryData.getSubjects(relation).contains(subject)) {
                                    spoofResultRight.addSubject(subject);
                                    spoofResultsRight.add(spoofResultRight);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
































