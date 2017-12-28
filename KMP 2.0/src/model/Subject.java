package model;

public class Subject extends Data {


    public Subject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Subject)) {
            return false;
        }
        Subject subject = (Subject) object;
        if(subject == this) {
            return true;
        }
        return this.id.equals(subject.id);
    }
}
