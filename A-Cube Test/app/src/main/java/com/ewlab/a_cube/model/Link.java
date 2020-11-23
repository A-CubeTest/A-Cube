package com.ewlab.a_cube.model;

public class Link {

    private Event eventObject = null;
    private Action actionObject = null;
    private Action actionStopObject = null;

    private int markerColor = 0;
    private int markerSize = 0;

    private double duration;

    private Screen screenshot;


    public Link() { }

    public Link (Event event, int markerColor, int markerSize) {

        this.eventObject = event;

        this.markerColor = markerColor;
        this.markerSize = markerSize;
    }

    public Link (Event event, Action action, int markerColor, int markerSize){
        this.eventObject = event;
        this.actionObject = action;

        this.markerColor = markerColor;
        this.markerSize = markerSize;
    }

    public Link (Event event, Action action, int markerColor, int markerSize, Screen screenshot) {

        this.eventObject = event;
        this.actionObject = action;
        this.markerColor = markerColor;
        this.markerSize = markerSize;
        this.screenshot = screenshot;

    }

    public Event getEvent() { return eventObject; }

    public Action getAction() { return actionObject; }

    public int getMarkerColor() { return markerColor; }

    public int getMarkerSize() { return markerSize; }

    public Action getActionStop() { return actionStopObject; }

    public double getDuration() { return duration; }

    public Screen getScreenshot() { return screenshot; }



    public void setEvent(Event eventName) { this.eventObject = eventName; }

    public void setAction(Action actionName) { this.actionObject = actionName; }

    public void setMarkerColor(int markerColor) { this.markerColor = markerColor; }

    public void setMarkerSize(int markerSize) { this.markerSize = markerSize; }

    public void setActionStop(Action actionStopName) { this.actionStopObject = actionStopName; }

    public void setDuration(double duration) { this.duration = duration; }

    public void setScreenshot(Screen screenshot) { this.screenshot = screenshot; }

    /**
     * This method checks if the link has an action and, if the link is an On / Off link,
     * if it also has an action Stop
     * @return true if the link is fully defined
     */
    public boolean isFullyDefined() {

        if(eventObject != null && eventObject.getType() != null) {

            if(!eventObject.getType().equals(Event.LONG_TAP_ON_OFF_TYPE))
                return actionObject != null;
            else
                return (actionObject != null && actionStopObject != null);

        }
        else
            return false;
    }

    @Override
    public String toString() {
        return "Link {" +
                "eventObject=" + eventObject +
                ", actionObject=" + actionObject +
                ", actionStopObject=" + actionStopObject +
                ", markerColor=" + markerColor +
                ", markerSize=" + markerSize +
                ", duration=" + duration +
                ", screenshot=" + screenshot +
                '}';
    }
}
