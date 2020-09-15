package com.ewlab.a_cube.model;

public class Event {

    private String name;
    private String type;

    public static final String TAP_TYPE = "Tap";

    public static final String SWIPE_UP_TYPE = "Swipe - Up";
    public static final String SWIPE_DOWN_TYPE = "Swipe - Down";
    public static final String SWIPE_RIGHT_TYPE = "Swipe - Right";
    public static final String SWIPE_LEFT_TYPE = "Swipe - Left";

    public static final String LONG_TAP_INPUT_LENGHT_TYPE = "Long Tap - input length";
    public static final String LONG_TAP_ON_OFF_TYPE = "Long Tap - ON/OFF";
    public static final String LONG_TAP_TIMED_TYPE = "Long Tap - timed";


    private double x;
    private double y;
    private String screenshot;
    private boolean portrait;

    public Event(){}

    public Event(String name, String type, double x, double y, String screenshot, boolean portrait){
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.screenshot = screenshot;
        this.portrait = portrait;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getScreenshot() { return screenshot; }
    public boolean getPortrait(){return portrait;}

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setScreenshot(String screenshot) { this.screenshot = screenshot; }
    public void setPortrait(boolean portrait){this.portrait = portrait; }


    public boolean equals(Object other){
        if(!(other instanceof Event)){
            return false;
        }

        Event otherEvent = (Event) other;
        return this.getName().equals(otherEvent.getName());
    }
}
