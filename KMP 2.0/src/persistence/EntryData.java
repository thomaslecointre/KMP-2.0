package persistence;

import model.ID;
import model.Relation;
import model.Subject;

import java.util.*;
import java.util.function.Supplier;


public class EntryData {
	private ID id;
    private HashMap<Relation, HashSet<Subject>> relationMap;

    protected EntryData() {
        relationMap = new HashMap<>();
    }

    public Set<Relation> relations() {
        return relationMap.keySet();
    }

    public Collection<HashSet<Subject>> values() {
        return relationMap.values();
    }

    public void put(Relation relation, Subject subject) {
    	HashSet<Subject> subjects = relationMap.get(relation);
    	if(subjects != null) {
    		subjects.add(subject);
    		relationMap.put(relation, subjects);
    	} else {
    		subjects = new HashSet<Subject>();
    		subjects.add(subject);
    		relationMap.put(relation, subjects);
    	}
    }

    public void setID(Subject subject) {
    	if (id != null) {
    		id.setSubject(subject);
    	} else {
    		id = new ID(subject);
    	}
    }

    public String getIdAsString() {
        return id.getSubject().getId();
    }

    public HashSet<Subject> get(Relation relation) {
        return relationMap.get(relation);
    }

    public boolean containsKey(Relation relation) {
        return relationMap.containsKey(relation);
    }
}
