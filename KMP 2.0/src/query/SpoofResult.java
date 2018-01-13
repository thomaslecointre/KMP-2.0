package query;

import java.util.ArrayList;
import java.util.HashSet;

import model.Subject;
import persistence.Database;

/**
 * Class used to represent mappings for certain WHERE variables.
 */
public class SpoofResult {
	
	private int key;
	private Subject id;
	private HashSet<Subject> subjects;
	
	public SpoofResult(int key, Subject id) {
		this.key = key;
		this.id = id;
		this.subjects = new HashSet<>();
	}
	
	public SpoofResult(SpoofResult spoofResult) {
		this(spoofResult.key, spoofResult.id);
	}

	/**
	 * Adds an instance of Subject to the subjects field.
	 * 
	 * @param subject
	 *            an instance of Subject.
	 */
	public void addSubject(Subject subject) {
		this.subjects.add(subject);
	}

	/**
	 * Adds all instances of Subjects from the subjects parameter to the
	 * subjects field.
	 * 
	 * @param subjects
	 *            an set of subjects.
	 */
	public void addSubjects(HashSet<Subject> subjects) {
		this.subjects.addAll(subjects);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SpoofResult)) {
			return false;
		}
		SpoofResult spoofResult = (SpoofResult) object;
		return this == spoofResult || this.subjects.containsAll(spoofResult.subjects) || spoofResult.subjects.containsAll(this.subjects);
	}

	public int getKey() {
		return key;
	}
	
	
	public HashSet<Subject> getSubjects() {
		return subjects;
	}

	public ArrayList<SpoofResult> transferID(TransactionHandler transactionHandler) {
		ArrayList<SpoofResult> newSpoofResults = new ArrayList();
		for (Subject subject : subjects) {
			int newKey = transactionHandler.getDatabase().findKey(subject);
			if (newKey != 0) {
				newSpoofResults.add(new SpoofResult(newKey, subject));
			} else {
				transactionHandler.flushEntry(key);
			}
		}
		return newSpoofResults;
	}

	public Subject getID() {
		return id;
	}

	
}