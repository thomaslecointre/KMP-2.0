package persistence;

import model.ID;
import model.Relation;
import model.Subject;

import java.util.HashMap;
import java.util.Set;


public class EntryData {
    private HashMap<Relation, Subject> relationMap;

    public EntryData() {
        relationMap = new HashMap<>();
        relationMap.put(ID.singleton(), null);
    }

    public Set<Relation> keySet() {
        return relationMap.keySet();
    }

    public void put(Relation relation, Subject subject) {
        relationMap.put(relation, subject);
    }

    public void setID(Subject subject) {
        relationMap.put(ID.singleton(), subject);
    }

    public String getIdAsString() {
        String id = relationMap.get(ID.singleton()).getId();
        return id;
    }

    public Subject get(Relation relation) {
        return relationMap.get(relation);
    }
}
