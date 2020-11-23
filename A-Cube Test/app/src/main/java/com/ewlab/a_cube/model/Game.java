package com.ewlab.a_cube.model;

import java.util.ArrayList;

public class Game {

    private String bundleId = "";
    private String title = "";
    private String icon = "";
    private ArrayList<Event> events = new ArrayList<>();

    public Game() { }

    public Game(String bundleId, String title, String icon) {
        this.bundleId = bundleId;
        this.title = title;
        this.icon = icon;
    }

    public Game(String bundleId, String title, String icon, ArrayList<Event> events) {
        this.bundleId = bundleId;
        this.title = title;
        this.icon = icon;
        this.events = events;
    }

    public Game(String bundleId, String title, ArrayList<Event> events) {
        this.bundleId = bundleId;
        this.title = title;
        this.events = events;
    }

    public boolean addEvent(Event newEvent){

        for(Event event : events)
            if(event.getName().equals(newEvent.getName()))
                return false;

        events.add(newEvent);

        return true;
    }

    public boolean removeEvent(Event event) { return events.remove(event); }

    public String getBundleId() { return bundleId; }

    public String getTitle() { return title; }

    public String getIcon() { return icon; }

    public ArrayList<Event> getEvents() { return events; }

    public Event getEvent(String name) {

        Event event = new Event();

        for(Event eventItem : events)
            if(eventItem.getName().equals(name))
                event = eventItem;

        return event;

    }

    public boolean equals(Object other) {

        if(!(other instanceof Game)){
            return false;
        }

        Game otherGame = (Game) other;
        return this.getBundleId().equals(otherGame.getBundleId());
    }

}
