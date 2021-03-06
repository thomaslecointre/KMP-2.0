package persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import model.ID;
import model.Relation;
import model.Subject;

/**
 * This class is used to store in the form of a map between Relation objects and
 * Subject objects. Every EntryData instance has an ID field.
 */
public class EntryData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ID id;
	private HashMap<Relation, ArrayList<Subject>> relationMap;

	public EntryData() {
		relationMap = new HashMap<>();
	}

	/**
	 * Returns the all Relation object keys of the relationMap field.
	 * 
	 * @return a set of relations.
	 */
	public Set<Relation> getRelations() {
		return relationMap.keySet();
	}

	/**
	 * Returns a collection of all sets of subjects from the relationMap field.
	 * @return 
	 * 
	 * @return a collection of sets of subjects.
	 */
	public Collection<ArrayList<Subject>> subjects() {
		return relationMap.values();
	}

	/**
	 * Puts a Subject instance into the set of subjects associated with the relation
	 * parameter.
	 * 
	 * @param relation
	 *            an instance of Relation.
	 * @param subject
	 *            an instance of Subject.
	 */
	public void put(Relation relation, Subject subject) {
		ArrayList<Subject> subjects = relationMap.get(relation);
		if (subjects != null) {
			if (!subjects.contains(subject)) {
				subjects.add(subject);
			}
			relationMap.put(relation, subjects);
		} else {
			subjects = new ArrayList<>();
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
	 * 
	 * @param relation
	 *            an instance of Relation
	 * @return 
	 * @return a set of subjects.
	 */
	public ArrayList<Subject> getSubjects(Relation relation) {
		return relationMap.get(relation);
	}

	/**
	 * Returns a boolean indicating whether an instance of Relation is present in
	 * this EntryData.
	 * 
	 * @param relation
	 *            an instance of Relation.
	 * @return a boolean indicating the presence of relation.
	 */
	public boolean hasRelation(Relation relation) {
		return relationMap.containsKey(relation);
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("id => ").append(this.getIdAsString()).append(" | ");
		for (Relation relation : this.getRelations()) {
			res.append(relation.getId()).append(" => ");
			ArrayList<Subject> subjects = this.getSubjects(relation);
			if (subjects.size() > 1) {
				res.append("{ ");
				for (Subject subject : subjects) {
					res.append(subject.getId()).append(' ');
				}
				res.append("} | ");
			} else if (subjects.size() == 1) {
				res.append(((Subject) subjects.toArray()[0]).getId());
				res.append(" | ");
			}
		}
		return res.toString();
	}

	public ID getID() {
		return id;
	}
	
	/**
	 * Checks in the relationMap if the relation contains the subject
	 * @param relation the relation searched
	 * @param subject the subject searched
	 * @return if the relation contains the subject or not
	 */
	public boolean relationContainsSubject(Relation relation, Subject subject) {
		if (relationMap.get(relation).contains(subject)) {
			return true;
		}
		return false;
	}

	/**
	 * Deletes all the subjects of a relation
	 * @param relation the relation that has to be purged
	 */
	public void purgeSubjectsFromRelation(Relation relation) {
		relationMap.get(relation).clear();
	}

	/**
	 * Deletes the subject from the list of the relation 
	 * @param relation the relation containing the subject that has to be removed
	 */
	public void removeIdFromRelation(Relation relation) {
		relationMap.get(relation).remove(this.id.getSubject());
	}

	/**
	 * Gets the subject links to the ID
	 * @return the subjects links to the ID
	 */
	public Subject getIDSubject() {
		return this.id.getSubject();
	}

	/**
	 * Deletes a subject contained in a relation
	 * @param relation the relation that has to be changed 
	 * @param subject the subject that has to be removed
	 */
	public void removeSubjectFromRelation(Relation relation, Subject subject) {
		relationMap.get(relation).remove(subject);
	}
}