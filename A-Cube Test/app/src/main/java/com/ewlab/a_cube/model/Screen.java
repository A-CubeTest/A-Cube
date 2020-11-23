package com.ewlab.a_cube.model;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Screen {

    private String image, name;
    private Configuration configuration;
    private ArrayList<Link> links = new ArrayList<>();
    private boolean portrait;
    private Game game;
    private SVMmodel svMmodel;

    public Screen() {

    }

    public Screen(String name, String image, Configuration configuration, boolean portrait) {

        this.name = name;
        this.image = image;
        this.configuration = configuration;
        this.portrait = portrait;

    }

    public Screen(String name, String image, Configuration configuration) {

        this.name = name;
        this.image = image;
        this.configuration = configuration;
    }

    public Screen(String name, String image, boolean portrait, Configuration configuration) {

        this.name = name;
        this.image = image;
        this.portrait = portrait;
        this.configuration = configuration;
    }

    public Screen(String name, String image, Configuration configuration, ArrayList<Link> links) {

        this.name = name;
        this.image = image;
        this.configuration = configuration;
        this.links = links;
    }

    public Screen(String name, String image, boolean portrait, Configuration configuration, ArrayList<Link> links) {

        this.name = name;
        this.image = image;
        this.portrait = portrait;
        this.configuration = configuration;
        this.links = links;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ArrayList<Link> getLinks() { return links; }

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

    public boolean removeLink(Link linkToRemove) {

        boolean removable = false;

        for(Link l : links) {

            if(linkToRemove.getEvent().equals(l.getEvent())) {

                removable = true;
                break;

            }

        }

        if(removable) {
            //CONTROLLO SE IL LINK DA RIMUOVERE HA UN'AZIONE VOCALE
            if (linkToRemove.getAction() instanceof ActionVocal) {

                Action thisAction = linkToRemove.getAction();

                //I take all the vocal actions present in the links of this configuration
                List<ActionVocal> vocalActions = this.getVocalActions();

                //I'm looking for a new svmModel for this configuration now that I've removed a vocal action
                svMmodel = MainModel.getInstance().getSVMmodel(vocalActions);
            }

            //ELIMINO ANCHE L'EVENTO CHE FA PARTE DEL LINK DAL FILE games.json
            configuration.getGame().removeEvent(linkToRemove.getEvent());

            links.remove(linkToRemove);
        }

        return removable;
    }

    public List<ActionVocal> getVocalActions() {

        ArrayList<ActionVocal> result = new ArrayList<>();

        for(Action action : this.getActions())
            if(action instanceof ActionVocal)
                result.add((ActionVocal) action);

        return result;
    }

    public boolean addLink(Link newLink){

        Action thisLinkAction = newLink.getAction();

        //CONTROLLO CHE IN ALTRE SCHERMATE DELLA CONF DI CUI FA PARTE QUESTO SCREEN NON CI SIANO ALTRI LINK UGUALI
        //QUESTO CONTROLLO è NECESSARIO ORA, MA IN FUTURO QUANDO SI POTRA' RICONOSCERE GLI SCREEN NON SARA' PIù NECESSARIO
        for(Link link : links)
            if(link.getEvent().equals(newLink.getEvent()))
                return false;

        //check that in the configuration there are no overlapping uses concerning the action or stopAction
        for(Link l : links) {

            if(l.getAction() != null && newLink.getAction() != null && l.getAction().equals(newLink.getAction())) {
                return false;
            }
            else if( l.getActionStop() != null && l.getActionStop().equals(newLink.getActionStop())
                    | newLink.getActionStop()!=null && l.getAction().equals(newLink.getActionStop())
                    | l.getActionStop()!=null && newLink.getActionStop()!=null && l.getActionStop().equals(newLink.getAction())) {

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

    public Link getLink(String event) {

        Link link = new Link();

        for(Link l : links) {

            if(l.getEvent().getName().equals(event))
                link = l;

        }

        return link;
    }

    public boolean isPortrait() {
        return portrait;
    }

    public void setPortrait(boolean portrait) {
        this.portrait = portrait;
    }



}
