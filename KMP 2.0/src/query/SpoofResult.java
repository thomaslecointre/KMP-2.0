package query;

import java.util.HashSet;

import model.Subject;

/**
 * Class used to represent mappings for certain WHERE variables.
 */
public class SpoofResult {
	private int key;
	private HashSet<Subject> subjects = new HashSet<>();

	public SpoofResult(int key) {
		this.key = key;
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
		return this == spoofResult || this.getKey() == spoofResult.getKey() && this.subjects.equals(spoofResult.subjects);
	}

	public int getKey() {
		return key;
	}
	
	public HashSet<Subject> getSubjects() {
		return subjects;
	}
}