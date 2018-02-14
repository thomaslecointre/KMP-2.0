package model;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * This class is used to represent a Relation object in the database.
 */
public class Relation extends Data implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum Properties implements Serializable {
		REFLEXIVE, IRREFLEXIVE, SYMMETRIC, ANTISYMMETRIC, ASYMMETRIC, TRANSITIVE
	}
	private EnumMap<Properties, Boolean> properties;
	
	public void setProperty(Properties property, boolean state) {
		properties.put(property, state);
	}
	
	public Relation(String id) {
		properties = new EnumMap<>(Properties.class);
    	for (Properties property : Properties.values()) {
    		properties.put(property, false);
    	}
        this.id = id;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Relation)) {
            return false;
        }
        Relation relation = (Relation) object;
        return this == relation || this.id.equals(relation.id);
    }

	@Override
	public String toString() {
		return id;
	}

	/**
	 * Checks the state of a property
	 * @param property an element of the Properties
	 * @return the state of the property
	 */
	public boolean isPropertyActive(Properties property) {
		return properties.get(property);
	}
	
	public EnumMap<Properties, Boolean> getProperties() {
		return properties;
	}
}
