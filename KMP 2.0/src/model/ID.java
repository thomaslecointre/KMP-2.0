package model;

/**
 * This class is used to represent the id associated with a database entry.
 */
public class ID {

	private Subject subject;

	public ID() {

	}

	public ID(Subject subject) {
		this.subject = subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Subject getSubject() {
		return subject;
	}

}
