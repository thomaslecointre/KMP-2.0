package model;

import java.io.Serializable;

/**
 * This class is used to represent the id associated with a database entry.
 */
public class ID implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

	@Override
	public String toString() {
		return subject.toString();
	}
}
