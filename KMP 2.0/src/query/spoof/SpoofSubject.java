package query.spoof;

import java.util.ArrayList;
import java.util.HashSet;

import model.Subject;
import query.TransactionHandler;

/**
 * Class used to represent mappings for certain WHERE variables.
 */
public class SpoofSubject extends SpoofData {

	private HashSet<Subject> subjects;

	public SpoofSubject() {
		subjects = new HashSet<>();
	}

	public SpoofSubject(int key, Subject id) {
		this();
		this.key = key;
		this.id = id;
	}

	public SpoofSubject(SpoofSubject spoofSubject) {
		this(spoofSubject.key, spoofSubject.id);
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
	 * Adds all instances of Subjects from the subjects parameter to the subjects
	 * field.
	 * 
	 * @param subjects
	 *            an set of subjects.
	 */
	public void addSubjects(HashSet<Subject> subjects) {
		this.subjects.addAll(subjects);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SpoofSubject)) {
			return false;
		}
		SpoofSubject spoofResult = (SpoofSubject) object;
		return this == spoofResult || this.subjects.containsAll(spoofResult.subjects)
				|| spoofResult.subjects.containsAll(this.subjects);
	}

	public HashSet<Subject> getSubjects() {
		return subjects;
	}

	public ArrayList<SpoofSubject> transferID(TransactionHandler transactionHandler) {
		ArrayList<SpoofSubject> newSpoofResults = new ArrayList();
		for (Subject subject : subjects) {
			int newKey = transactionHandler.getDatabase().findKey(subject);
			if (newKey != 0) {
				newSpoofResults.add(new SpoofSubject(newKey, subject));
			}
		}
		return newSpoofResults;
	}

}