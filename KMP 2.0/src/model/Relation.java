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
	public enum Properties {
		SYMMETRY, TRANSITIVITY
	}
	private EnumMap<Properties, Boolean> properties;
	
	public void setSymmetric() {
		properties.put(Properties.SYMMETRY, true);
	}
	
	public void setTransitive() {
		properties.put(Properties.TRANSITIVITY, true);
	}
	
	public Relation(String id) {
        this.id = id;
    }

    public Relation() {
    	properties = new EnumMap<>(Properties.class);
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
}
