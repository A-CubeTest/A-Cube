package com.ewlab.a_cube.model;

public abstract class Action {

    private static final String TAG = Action.class.getName();

    protected String name = "";
    public static final String BUTTON_TYPE = "Button";
    public static final String VOCAL_TYPE = "Vocal";
    public static final String UNDEFINED_TYPE = "Undefined";

    public String getName() {
        return name;
    }

    public boolean equals(Object other) {

        if(!(other instanceof Action))
            return false;

        Action otherAction = (Action) other;
        return this.getName().equals(otherAction.getName());

    }
}
