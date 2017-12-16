package model;

public class ID extends Relation {
    private static ID instance = new ID();
    public static ID singleton() {
        return instance;
    }
    private ID() {
        id = "id";
    }
}
