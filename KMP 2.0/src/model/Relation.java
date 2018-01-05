package model;

/**
 * This class is used to represent a Relation object in the database.
 */
public class Relation extends Data {
    public Relation(String id) {
        this.id = id;
    }

    public Relation() {

    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Relation)) {
            return false;
        }
        Relation relation = (Relation) object;
        return this == relation || this.id.equals(relation.id);
    }
}
