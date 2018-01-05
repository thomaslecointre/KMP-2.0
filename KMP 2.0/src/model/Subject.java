package model;

/**
 * This class is used to represent both future Instance or Class objects.
 * Every new object in the database either starts off as a Relation object or a Subject object.
 */
public class Subject extends Data {

    public Subject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Subject)) {
            return false;
        }
        Subject subject = (Subject) object;
        return subject == this || this.id.equals(subject.id);
    }
}
