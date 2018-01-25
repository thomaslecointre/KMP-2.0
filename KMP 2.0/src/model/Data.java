package model;

import java.io.Serializable;

/**
 * This abstract class is used to represent all types of objects in the database.
 */
public abstract class Data implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;

    public String getId() {
        return id;
    }
    
}
