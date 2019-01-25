package com.chatcamp.uikit.database;

public class Diff<T> {

    public enum CHANGE {
        REMOVE,
        INSERT,
        UPDATE
    }

    private CHANGE change;
    private T model;
    private int position;


    public CHANGE getChange() {
        return change;
    }

    public void setChange(CHANGE change) {
        this.change = change;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
