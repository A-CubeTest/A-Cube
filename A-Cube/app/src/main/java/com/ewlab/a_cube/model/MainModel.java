package com.ewlab.a_cube.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.VolumeShaper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainModel {

    private static final String TAG = MainModel.class.getName();

    private static MainModel instance = null;
    private JsonManager jsonManager = null;

    private HashMap<String, Action> actions = null;
    private HashMap<String, Game> games = null;
    private ArrayList<Configuration> configurations = new ArrayList<>();
    private HashMap<String, SVMmodel> svmModels = null;

    private MainModel() {
        jsonManager = new JsonManager(this);

    }

    public void setActions(){
        actions = new HashMap<String, Action>();
        for (Action a: jsonManager.getActionsFromJson()) {
            actions.put(a.getName(), a);
        }
    }

    public void setGames(){
        games = new HashMap<String, Game>();
        for(Game g : jsonManager.getGamesFromJson()){
            games.put(g.getTitle(), g);
        }
    }

    public void setSvmModels(){
        svmModels = new HashMap<String, SVMmodel>();
        for(SVMmodel s : jsonManager.getSVMmodelsFromJson()){
            svmModels.put(s.getName(), s);
        }
    }

    public void setConfigurations(){
        configurations = (ArrayList<Configuration>) jsonManager.getConfigurationFromJson();
    }

    public static synchronized MainModel getInstance() {
        if (instance == null) {
            instance = new MainModel();
        }

        return instance;
    }


    public List<Action> getActions() {

        return new ArrayList<>(actions.values());
    }
    public List<ActionButton> getButtonActions() {
        ArrayList<ActionButton> result = new ArrayList<>();
        for (Action item : actions.values()) {
            if (item instanceof ActionButton) result.add((ActionButton)item);
        }

        return result;
    }
    public List<ActionVocal> getVocalActions() {
        ArrayList<ActionVocal> result = new ArrayList<>();
        for (Action item : actions.values()) {
            if (item instanceof ActionVocal) result.add((ActionVocal)item);
        }

        return result;
    }
    public Action getAction(String name){
        Action action =  actions.get(name);

        return action;
    }


    public ArrayList<Game> getGames(){
        return new ArrayList<>(games.values());
    }
    public Game getGame(String title){
        return games.get(title);
    }
    public Game getGameFromBundleId(String bundleId){
        for(Game game : games.values()){
            if(game.getBundleId().equals(bundleId)){
                return game;
            }
        }

        return null;
    }


    public ArrayList<Configuration> getConfigurations(){
        return configurations;
    }
    public Configuration getConfiguration(String gameName, String confName){

        Configuration thisConf = new Configuration();

        for(Configuration conf : configurations){
            if(conf.getGame().getTitle().equals(gameName) && conf.getConfName().equals(confName)){
                thisConf = conf;
            }
        }

        return thisConf;
    }
    public Configuration getConfigurationFromPackage(String gamePackage, String confName){

        Configuration thisConf = new Configuration();

        for(Configuration conf : configurations){
            if(conf.getGame().getBundleId().equals(gamePackage) && conf.getConfName().equals(confName)){
                thisConf = conf;
            }
        }

        return thisConf;

    }
    public ArrayList<Configuration> getConfigurationsFromGame(String title){
        ArrayList<Configuration> confs = new ArrayList<>();

        for(Configuration conf : configurations){
            if(conf.getGame().getTitle().equals(title)){
                confs.add(conf);
            }
        }

        return confs;
    }
    public ArrayList<Configuration> confWithThisModel(String modelName){
        ArrayList<Configuration> theseConf = new ArrayList<>();

        for(Configuration conf : configurations){
            if(conf.getModel().getName().equals(modelName)){
                theseConf.add(conf);
            }
        }

        return theseConf;
    }

    /**
     * @param vocalActions
     * @return the SVMmodel with all (and only) the sounds names reported
     * Note: the "noise" sound that is always part of the model should not be indicated in the parameter
     */
    public SVMmodel getSVMmodel(List<ActionVocal> vocalActions){

        for(SVMmodel model : svmModels.values()){
            if (model.containsTheSounds( false, vocalActions)) return model;
        }

        return null;
    }
    public SVMmodel getSVMmodel(String modelName){
        String mod = modelName;
        return svmModels.get(modelName);
    }
    public List<SVMmodel> getSvmModels(){
        return new ArrayList<>(svmModels.values());
    }


    /**
     * This method receives an action and, after verifying whether the action is vocal or button type,
     * verifies if it is suitable to be inserted in the Actions Hashmap
     * @param actionToAdd the action that we want to add
     * @return true if the action was added
     */
    public boolean addAction(Action actionToAdd) {
        boolean correct = true;
        //Check for all action items
        if (actions.containsKey(actionToAdd.getName())) {
            correct = false;
        }

        //Checks specific for the action item buttons...
        if (actionToAdd instanceof ActionButton) {
            Log.d(TAG, "button founded");

            ActionButton actionButtonToAdd = (ActionButton) actionToAdd;

            List<ActionButton> buttonActions = this.getButtonActions();

            for(ActionButton b : buttonActions){
                if(b.getDeviceId().equals(actionButtonToAdd.getDeviceId()) && b.getKeyId().equals(actionButtonToAdd.getKeyId())){
                    correct = false;
                }
            }

            if(actionButtonToAdd.getDeviceId()==null | actionButtonToAdd.getKeyId()==null){
                correct = false;
            }


            if(correct){
                actions.put(actionButtonToAdd.getName(), actionButtonToAdd);
            }

        }

        else if (actionToAdd instanceof ActionVocal) {
            Log.d(TAG, "vocal founded");

            ActionVocal actionVocalToAdd = (ActionVocal) actionToAdd;

            if(correct){
                actions.put(actionVocalToAdd.getName(), actionVocalToAdd);

            }else{
                ActionVocal vocalToChange = (ActionVocal) actions.get(actionToAdd.getName());
                vocalToChange.addFiles(actionVocalToAdd.getFiles());
                actions.put(vocalToChange.getName(), vocalToChange);
            }
        }

        return correct;
    }

    public Game addNewGame(Game newGame){
        boolean gameAdded = true;

        for(Game g : games.values()){
            if(g.getBundleId().equals(newGame.getBundleId())){
                gameAdded = false;
            }
        }

        if(gameAdded) {
//            Configuration newConf = new Configuration("ConfigurationN°1", newGame);
//            newConf.setSelected();
//            configurations.add(newConf);
            games.put(newGame.getTitle(), newGame);
        }

        return newGame;
    }

    public boolean addNewConfiguration(Configuration conf){
        boolean correct = true;
        boolean alreadyExistActiveConf = false;

        for(Configuration c : configurations){
            if(c.getConfName().equals(conf.getConfName()) && c.getGame().equals(conf.getGame())) {
                correct = false;
            }
        }

        if(correct){

            for(Configuration c : configurations){
                if(c.getGame().equals(conf.getGame()) && c.getSelected()){
                    alreadyExistActiveConf = true;
                }
            }

            if(!alreadyExistActiveConf){
                conf.setSelected();
            }

            configurations.add(conf);
        }

        return correct;
    }

    /**
     * Add a new model and first check that there isn't one with same sounds.
     * @param newModel
     * @return true if the model was added
     */
    public boolean addNewModel(SVMmodel newModel) {
        boolean canAdd = true;

        for(SVMmodel model : svmModels.values()){
            if(model.containsTheSounds( true, newModel.getSounds())) {
                canAdd = false;
            }
        }

        if(canAdd){
            svmModels.put(newModel.getName(), newModel);
        }

        return canAdd;
    }

    /**
     * It receives the event that you want to add and, if it is a new version of an existing event, also that event.
     * The method adds a new event, modifies a pre-existing one or chooses not to add according to different situations.
     * @param gameTitle, oldEvent, newEvent
     * @return true if the new event is added or if an old one is changed
     */
    public boolean saveEvent(String gameTitle, Event oldEvent, Event newEvent){

        boolean saved = false;

        Game thisGame = MainModel.getInstance().getGame(gameTitle);

        //alreadyExistInGames is true if an event already exists with the same name associated with the game
        //modifyingExistingGame is true if we are modifying a pre-existent event, then oldEvent! = null
        Event eventThatAlreadyExist = thisGame.getEvent(newEvent.getName());
        Log.d(TAG, "eventThatAlreadyExist "+eventThatAlreadyExist.getName());
        boolean alreadyExistInGames = (eventThatAlreadyExist.getName()!=null);
        boolean modifyingExistingGame = (oldEvent!=null);

        Log.d(TAG, "alreadyExistInGames: "+alreadyExistInGames+"  "+newEvent.getName());

        if(alreadyExistInGames && modifyingExistingGame){
            Log.d(TAG, "removed old, added new Event: "+oldEvent.getName()+"  "+newEvent.getName());
            thisGame.removeEvent(oldEvent);
            thisGame.addEvent(newEvent);
            MainModel.getInstance().writeGamesJson();
            saved = true;
        }

        if(!alreadyExistInGames && !modifyingExistingGame){
            Log.d(TAG, "added new Event");
            thisGame.addEvent(newEvent);
            MainModel.getInstance().writeGamesJson();
            saved = true;

        }

        return saved;
    }

    /**
     * It receives the link that you want to add and, if it is a new version of an existing link, also that link.
     * The method adds a new link, modifies a pre-existing one or chooses not to add according to different situations.
     * @param gameTitle, nameCondig, oldLink, newLink
     * @return true if the new event is added or if an old one is changed
     */
    public boolean saveLink(String gameTitle, String nameConfig, Link oldLink, Link newLink){  //Dovrai aggiungere le altre caratteristiche di un Link
        Log.d(TAG, "saveLink");

        boolean linkSaved = false;
        Configuration thisConfig = MainModel.getInstance().getConfiguration(gameTitle, nameConfig);

        //alreadyExistInConfig is true if an event already exists with the same name associated with the game
        //modifyingExistingLink is true if we are modifying a pre-existent event, then oldEvent! = null
        boolean alreadyExistInConfig = (thisConfig.getLink(newLink.getEvent().getName())!=null);
        boolean modifyingExistingLink = (oldLink!=null);

        //if it exists and we are modifying it, we delete the previous copy to create an updated one
        if(alreadyExistInConfig && modifyingExistingLink){
            Log.d(TAG, "deleted old link, added a new one: "+newLink.getEvent().getName());
            thisConfig.getLinks().remove(oldLink);
            thisConfig.addLink(newLink);
            MainModel.getInstance().writeConfigurationsJson();
            linkSaved = true;
        }

        //if the link does not exist in the configuration we add it without other controls
        if(!alreadyExistInConfig){
            Log.d(TAG, "added a new Link");

            thisConfig.addLink(newLink);
            MainModel.getInstance().writeConfigurationsJson();
            linkSaved = true;
        }

        return linkSaved;
    }

    /**
     * Removes the Game with the indicated title and all the configuratios that refer to it
     * @param title
     * @return the removed gameItem, if any.
     */
    public Game removeGame (String title){
        ArrayList<Configuration> configurationsToRemove = new ArrayList<Configuration>() ;

        for(Configuration conf : configurations){
            if(conf.getGame().getTitle().equals(title)){
                configurationsToRemove.add(conf);
            }
        }

        if(configurationsToRemove.size()>0){
            for(Configuration conf : configurationsToRemove){
                configurations.remove(conf);
            }
        }


        return games.remove(title);
    }

    /**
     * Removes the Action with the indicated name. In case that action is used in a configuration,
     * that configuration action is set to "" . If the Action removed is a ActionVocal this method deletes all the files related to it
     * and all the SVMmodel that have at least one file in common.
     * @param name the name of the action to delete
     * @return the deleted action, if any
     */
    public Action removeAction(String name) {

        //set the deleted action to null
        for(Configuration conf : configurations){
            for(Link link : conf.getLinks()){
                if(link.getAction()!=null){
                    if(link.getAction().getName().equals(name)){
                        link.setAction(null);
                    }
                }
            }
        }

        Action thisAction = getAction(name);

        //if the action is vocal..
        if(thisAction instanceof ActionVocal){
            ArrayList<SVMmodel> modelsRemoved = new ArrayList<SVMmodel>();
            //we delete all the sounds that refers to her...
            ((ActionVocal) thisAction).deleteAllSounds();


            //and all the svmModel that uses one of these sounds
            for (SVMmodel mod : svmModels.values()) {

                if (mod.containsSound(thisAction.getName())) {
                    mod.prepareForDelete();
                    modelsRemoved.add(mod);
                }

            }

            for(SVMmodel model : modelsRemoved){
                svmModels.remove(model.getName());

                for(Configuration conf : configurations){
                    if(conf.hasModel() && conf.getModel().equals(model)){
                        conf.setModel(null);
                    }
                }
            }

        }


        return actions.remove(name);
    }

    public String findVocalActionFromFile(String fileName){
        String label = "";

        List<ActionVocal> allVocalActions = getVocalActions();

        for(ActionVocal aiv : allVocalActions){
            if(aiv.getFiles().contains(fileName)){
                label = aiv.getName();
            }
        }

        return label;
    }

    public boolean removeFileVocalAction(String actionName, String fileName){ //questo metodo elimina tutti i nomi dei modelli che fanno riferimento all'audio eliminato in modo che vadano trainati nuovamente
        boolean deleted = false;

        ActionVocal thisVocalAction = (ActionVocal) actions.get(actionName);
        Set<String> files = thisVocalAction.getFiles();

        for(String file : files){
            if(file.equals(fileName)){
                deleted = thisVocalAction.deleteFile(fileName);
            }
        }

        if(thisVocalAction.getFiles().size()==0){
            this.removeAction(thisVocalAction.getName());
        }

        return deleted;
    }

    /**
     * Removes the Configuration with the indicated name that refers to the indicated game (appName). If the Configuration
     * is removed we remove all the links in it and also all the events belonging to the game to which the configuration refers
     * @param appName the name of the app
     * @param confName the name of the configuration
     * @return true if the configuration has been deleted
     */

    public boolean removeConfiguration(String appName, String confName){
        boolean removed = false;
        Configuration confToRemove = new Configuration();

        for(Configuration conf : configurations){
            if(conf.getGame().getTitle().equals(appName) && conf.getConfName().equals(confName)){
                confToRemove = conf;
                removed = true;
            }
        }

        if(removed){
            List<Link> linksToRemove = confToRemove.getLinks();
            List<Event> events = confToRemove.getGame().getEvents();
            ArrayList<Event> eventToRemove = new ArrayList<>();

            for(Link l : linksToRemove){
                for(Event e : events){
                    if(l.getEvent().equals(e)){
                        eventToRemove.add(e);
                    }
                }
            }

            for(Event e : eventToRemove){
                confToRemove.getGame().removeEvent(e);
            }

            configurations.remove(confToRemove);
        }

        return removed;
    }

    public boolean removeConfiguration(Configuration confToRemove){
        boolean remove = false;

        for(Configuration c : configurations){
            if(c.equals(confToRemove)){
                remove = true;
            }
        }

        if(remove){
            List<Link> linksToRemove = confToRemove.getLinks();
            List<Event> events = confToRemove.getGame().getEvents();
            ArrayList<Event> eventToRemove = new ArrayList<>();

            for(Link l : linksToRemove){
                for(Event e : events){
                    if(l.getEvent().equals(e)){
                        eventToRemove.add(e);
                    }
                }
            }

            for(Event e : eventToRemove){
                confToRemove.getGame().removeEvent(e);
            }

            configurations.remove(confToRemove);
        }

        return remove;
    }

    public boolean removeModel(String modelName){
        boolean deleted = false;

        if(svmModels.remove(modelName)!=null){
            deleted = true;
            for(Configuration conf : configurations){
                if(conf.getModel().getName().equals(modelName)){
                    conf.setModel(null);
                }
            }
        }

        return deleted;
    }


    public void writeActionsJson() {
        JsonManager.writeActions(this.getActions());
    }

    public void writeGamesJson(){ JsonManager.writeGames(this.games.values()); }

    public void writeConfigurationsJson(){ JsonManager.writeConfigurations(configurations); }

    public void writeModelJson(){ JsonManager.writeSVMmodels(this.svmModels.values()); }



    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;

        }catch(Exception e){

            e.getMessage();
            return null;
        }
    }

    public String getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 1, baos);
        byte[] b = baos.toByteArray();
        String encodeImage = Base64.encodeToString( b , Base64.NO_WRAP);

        return encodeImage;
    }
    /**
     * This method returns a value which indicates if the selected configuration of a specified game has been configured correctly
     * @param title of the game to check
     * @return double between 0.0 and 1.0
     */
    public double matchingUpdate(String title){
        double matching = 0.0;
        double num = 0.0;
        double denom = 0.0;

        ArrayList<Configuration> gameConf = MainModel.getInstance().getConfigurationsFromGame(title);


        for(Configuration conf : gameConf){
            if(conf.getSelected()) {
                if (!conf.getVocalActions().isEmpty() && conf.getModel() == null) {
                    denom++;
                }

                for (Link link : conf.getLinks()) {
                    Log.d(TAG, link.getEvent().getName());
                    if (link.getAction() == null) {
                        denom++;

                    } else {
                        num++;
                        denom++;
                    }
                }
            }
        }

        if(denom>0.0) {
            matching = num / denom;

        }else{
            matching = 0.0;
        }

        Log.d(TAG, title+" num: "+num+" "+denom);
        return matching;
    }

    /**
     * This method receives as input the name of a vocalAction and removes any SVMmodel that contains it and
     * any reference of the model present in the configurations
     * @param sound the name of the action
     */
    public void removeModelWithThisSound(String sound) {
        ArrayList<String> modelsToDelete = new ArrayList<>();

        for (SVMmodel mod : svmModels.values()) {
            boolean delete = mod.containsSound(sound);

            if (delete) {
                modelsToDelete.add(mod.getName());
                boolean a = svmModels.remove(mod.getName(), mod);

                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube/Models/" + mod.getName();
                File fileToDelete = new File(filePath);
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }

                Log.d(TAG, "SVMmodel removed: "+mod.getName()+" "+a);

                for (Configuration conf : configurations) {
                    if (conf.hasModel() && conf.getModel().equals(mod)) {
                        conf.setModel(null);
                    }
                }
            }
        }
    }

    public void setSelectedConfiguration(Configuration conf){
        conf.setSelected();

        for(Configuration c : configurations){
            if(c.getGame().equals(conf.getGame()) && !c.getConfName().equals(conf.getConfName())){
                c.setUnselected();
            }
        }
    }
}

