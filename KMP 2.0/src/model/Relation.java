package model;

public class Relation extends Data {
    public Relation(String id) {
        this.id = id;
    }

    public Relation() {

    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Relation)) {
            return false;
        }
        Relation relation = (Relation) object;
        if(this == relation) {
            return true;
        }
        return this.id.equals(relation.id);
    }
}
