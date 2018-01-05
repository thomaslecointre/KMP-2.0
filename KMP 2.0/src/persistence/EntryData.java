package persistence;

import model.ID;
import model.Relation;
import model.Subject;

import java.util.*;
import java.util.function.Supplier;

/**
 * This class is used to store in the form of a map between Relation objects and Subject objects. Every EntryData instance has an ID field.
 */
public class EntryData {
	private ID id;
    private HashMap<Relation, HashSet<Subject>> relationMap;

    EntryData() {
        relationMap = new HashMap<>();
    }

    /**
     * Returns the all Relation object keys of the relationMap field.
     * @return a set of relations.
     */
    public Set<Relation> relations() {
        return relationMap.keySet();
    }

    /**
     * Returns a collection of all sets of subjects from the relationMap field.
     * @return a collection of sets of subjects.
     */
    public Collection<HashSet<Subject>> values() {
        return relationMap.values();
    }

    /**
     * Puts a Subject instance into the set of subjects associated with the relation parameter.
     * @param relation an instance of Relation.
     * @param subject an instance of Subject.
     */
    public void put(Relation relation, Subject subject) {
    	HashSet<Subject> subjects = relationMap.get(relation);
    	if(subjects != null) {
    		subjects.add(subject);
    		relationMap.put(relation, subjects);
    	} else {
    		subjects = new HashSet<>();
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

    /**
     * Returns the set of subjects associated with a particular relation.
     * @param relation an instance of Relation
     * @return a set of subjects.
     */
    public HashSet<Subject> getSubjects(Relation relation) {
        return relationMap.get(relation);
    }

    /**
     * Returns a boolean indicating whether an instance of Relation is present in this EntryData.
     * @param relation an instance of Relation.
     * @return a boolean indicating the presence of relation.
     */
    public boolean containsKey(Relation relation) {
        return relationMap.containsKey(relation);
    }
}
