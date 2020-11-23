package com.ewlab.a_cube.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private String confName = "";
    private Game game;
    private boolean selected = false;
    private SVMmodel svMmodel;
    private ArrayList<Link> links = new ArrayList<>();
    private ArrayList<Screen> screens = new ArrayList<>();

    public Configuration() {

    }

    public Configuration(String confName, Game game){
        this.confName = confName;
        this.game = game;
    }

    public Configuration(String confName, Game game, ArrayList<Link> links){
        this.confName = confName;
        this.game = game;
        this.links = links;
    }

    public Configuration(String confName, Game game, SVMmodel svMmodel) {

        this.confName = confName;
        this.game = game;
        this.svMmodel = svMmodel;

    }

    public Configuration(String confName, Game game, SVMmodel svMmodel, ArrayList<Link> links) {

        this.confName = confName;
        this.game = game;
        this.svMmodel = svMmodel;
        this.links = links;

    }

    public Configuration(String confName, Game game, SVMmodel svMmodel, ArrayList<Link> links, ArrayList<Screen> screens) {

        this.confName = confName;
        this.game = game;
        this.svMmodel = svMmodel;
        this.links = links;
        this.screens = screens;

    }

    public String getConfName() { return confName; }

    public Game getGame() { return game; }

    public boolean getSelected() { return selected; }

    public SVMmodel getModel() { return svMmodel; }

    public ArrayList<Link> getLinks() { return links; }

    public ArrayList<Screen> getScreens() { return screens; }

    public Link getLink(String event) {

        Link link = new Link();

        for(Link l : links) {

            if(l.getEvent().getName().equals(event))
                link = l;

        }

        return link;
    }

    public Link getLink2(String event) {

        Link link = new Link();


        for(Link l : MainModel.getInstance().getLinksFromConf(this)) {

            if(l.getEvent().getName().equals(event))
                link = l;

        }

        return link;
    }

    public Link getLinkFromAction(String action) {

        for(Link l : links) {

            if(l.getAction() != null && l.getAction().getName().equals(action))
                return l;

        }

        return null;
    }

    public Link getLinkFromActionStop(String actionStop) {

        for(Link l : links) {

            if(l.getActionStop()!=null && l.getActionStop().getName().equals(actionStop))
                return l;

        }

        return null;
    }

    /*
    public ArrayList<Action> getActions() {

        ArrayList<Action> actions = new ArrayList<>();

        for(Link l : links) {

            if(l.getAction() != null) {

                actions.add(l.getAction());

                if(l.getActionStop()!=null)
                    actions.add(l.getActionStop());

            }
        }

        return actions;
    }

     */
    //NUOVO getActions()
    public ArrayList<Action> getActions() {

        ArrayList<Action> actions = new ArrayList<>();
        Log.d("RECORDER_SERVICE","screens.size(): "+screens.size());
        for(Screen s : screens) {

            for(Link l : s.getLinks()) {

                if(l.getAction() != null) {

                    actions.add(l.getAction());

                    if(l.getActionStop()!=null)
                        actions.add(l.getActionStop());

                }
            }

        }

        return actions;
    }

    public ArrayList<Action> getActions(ArrayList<Screen> screens) {

        ArrayList<Action> actions = new ArrayList<>();
        Log.d("RECORDER_SERVICE","screens.size(): "+screens.size());
        for(Screen s : screens) {

            for(Link l : s.getLinks()) {

                if(l.getAction() != null) {

                    actions.add(l.getAction());

                    if(l.getActionStop()!=null)
                        actions.add(l.getActionStop());

                }
            }

        }

        return actions;
    }

    public ArrayList<ActionButton> getButtonActions() {
        ArrayList<ActionButton> result = new ArrayList<>();

        for(Action action : this.getActions()){
            if(action instanceof ActionButton){
                result.add((ActionButton) action);
            }
        }

        return result;
    }

    public List<ActionVocal> getVocalActions(){

        ArrayList<ActionVocal> result = new ArrayList<>();

        for(Action action : this.getActions()){
            if(action instanceof ActionVocal){
                result.add((ActionVocal) action);
            }
        }

        return result;
    }

    public void setSelected(){
        selected = true;
    }
    public void setUnselected(){
        selected = false;
    }

    /**
     * This method checks if a new link can be added in this configuration and, if the conditions are positive, adds it.
     * @param newLink the link we want to add in the configuration
     * @return true if the link is added
     */
    public boolean addLink(Link newLink){
        Action thisLinkAction = newLink.getAction();

        //we check that there is no link with the same event in this configuration
        for(Link link : links){
            if(link.getEvent().equals(newLink.getEvent())){
                return false;
            } }

        //check that in the configuration there are no overlapping uses concerning the action or stopAction
        for(Link l : links){
            if(l.getAction()!=null && newLink.getAction()!=null && l.getAction().equals(newLink.getAction())){
                return false;
            }else if( l.getActionStop()!=null && l.getActionStop().equals(newLink.getActionStop())
                    | newLink.getActionStop()!=null && l.getAction().equals(newLink.getActionStop())
                    | l.getActionStop()!=null && newLink.getActionStop()!=null && l.getActionStop().equals(newLink.getAction())){
                return false;
            }
        }

        //if the link has a vocal control action if there is a new model for my conf, if it does not exist assign null
        if(thisLinkAction instanceof ActionVocal){

            List<ActionVocal> vocalActions = this.getVocalActions();
            vocalActions.add((ActionVocal) thisLinkAction);

            svMmodel = MainModel.getInstance().getSVMmodel(vocalActions);
        }

        return links.add(newLink);
    }

    /**
     * This method checks if the input link received can be deleted from the configuration. If this is possible it returns true and performs some cascading changes and deletions.
     * If the link is deleted, it is checked whether it presented a vocal action and the configuration of the svmModel is modified accordingly. Furthermore, the event in the link is also deleted in the game to which it belonged.
     * @param linkToRemove the link we want to remove
     * @return true if the link is removed
     */
    public boolean removeLink(Link linkToRemove){
        boolean removable = false;

        for(Link l : links){
            if(linkToRemove.getEvent().equals(l.getEvent())){
                removable = true;
            }
        }

        if(removable) {
            //check if the link to be removed has a vocal action
            if (linkToRemove.getAction() instanceof ActionVocal) {
                Action thisAction = linkToRemove.getAction();

                //I take all the vocal actions present in the links of this configuration
                List<ActionVocal> vocalActions = this.getVocalActions();

                //I'm looking for a new svmModel for this configuration now that I've removed a vocal action
                svMmodel = MainModel.getInstance().getSVMmodel(vocalActions);
            }

            //I delete the event that is part of the link also removed from the game to which it belonged
            MainModel.getInstance().getGame(game.getTitle()).removeEvent(linkToRemove.getEvent());

            links.remove(linkToRemove);
        }

        return removable;
    }

    public void setModel(SVMmodel svmModel){ this.svMmodel = svmModel; }

    public boolean hasModel(){
        return svMmodel != null;
    }

    private boolean isSvmModelNeeded(){
        for(Action action : this.getActions()){
            if(action instanceof ActionVocal){
                return true;
            }
        }

        return false;
    }


    /**
     * This method returns the number of defined links, a link is considered defined if it has an event action pair
     * or, in the case of Long tap - On / Off links, if it also has an action Stop
     * @return int
     */

    public int definedLinks() {

        int i = 0;

        for(Link l : links) {

            if(l.isFullyDefined())
                i++;

        }

        return i;
    }

    public int undefinedLinks() {
        int i = 0;

        for(Link l : links) {

            if(!l.isFullyDefined())
                i++;

        }

        return i;
    }

    /*
    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String s) {
        screenshot = s;
    }

     */


}
