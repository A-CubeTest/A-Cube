package com.ewlab.a_cube.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
    private JsonManager jsonManager;

    private HashMap<String, Action> actions = null;
    private HashMap<String, Game> games = null;
    private ArrayList<Configuration> configurations = new ArrayList<>();
    private ArrayList<Screen> screens = new ArrayList<>();
    private  ArrayList<Link> links = new ArrayList<>();
    private HashMap<String, SVMmodel> svmModels = null;

    public Game lastGame;


    //METODO PER OTTENERE UN'ISTANZA DEL MainModel
    public static synchronized MainModel getInstance() {

        if (instance == null)
            instance = new MainModel();

        return instance;

    }

    //METODO PER OTTENERE UN ISTANZA DEL JsonManager
    private MainModel() {
        jsonManager = new JsonManager(this);
    }

//-----------------------------------------------------------------------------------------------------------------------------------

    /** METODI PER GESTIRE LE AZIONI */

    //METODO PER SETTARE IL MainModel CON LE AZIONI PRESENTI NEL FILE actions.json
    public void setActions() {
        actions = new HashMap<String, Action>();
        for (Action a: jsonManager.getActionsFromJson()) {
            actions.put(a.getName(), a);
        }
    }

    //METODO PER OTTENERE LA LISTA DELLE AZIONI DAL MainModel
    public List<Action> getActions() {

        if (actions != null)
            return new ArrayList<>(actions.values());
        else
            return new ArrayList<>();

    }

    //METODO PER OTTENERE LE AZIONI DI TIPO Button PRESENTI NEL MainModel
    public List<ActionButton> getButtonActions() {

        ArrayList<ActionButton> result = new ArrayList<>();

        for (Action item : actions.values())
            if (item instanceof ActionButton)
                result.add((ActionButton)item);

        return result;
    }

    //METODO PER OTTENERE LE AZIONI DI TIPO Vocal PRESENTI NEL MainModel
    public List<ActionVocal> getVocalActions() {
        ArrayList<ActionVocal> result = new ArrayList<>();
        for (Action item : actions.values()) {
            if (item instanceof ActionVocal) result.add((ActionVocal)item);
        }
        return result;
    }

    //METODO PER OTTENERE UN Azione CON UN DETERMINATO NOME  DAL MainModel
    public Action getAction(String name) {  return actions.get(name); }

    //METODO PER OTTENERE UN Azione CON UN DETERMINATO INDICE  DAL MainModel
    public Action getAction(int index) { return actions.get(index); }


    /**
     * This method receives an action and, after verifying whether the action is vocal or button type,
     * verifies if it is suitable to be inserted in the Actions Hashmap
     * @param actionToAdd the action that we want to add
     * @return true if the action was added
     */
    //METODO PER AGGIUNGERE UN AZIONE AL MainModel
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

            for(ActionButton b : buttonActions) {

                if(b.getDeviceId().equals(actionButtonToAdd.getDeviceId()) &&
                        b.getKeyId().equals(actionButtonToAdd.getKeyId())) {

                    correct = false;
                    break;
                }

            }

            if(actionButtonToAdd.getDeviceId() == null | actionButtonToAdd.getKeyId() == null) {
                correct = false;
            }

            if(correct) {
                actions.put(actionButtonToAdd.getName(), actionButtonToAdd);
            }

        }
        else if (actionToAdd instanceof ActionVocal) {
            Log.d(TAG, "vocal founded");

            ActionVocal actionVocalToAdd = (ActionVocal) actionToAdd;

            if(correct)
                actions.put(actionVocalToAdd.getName(), actionVocalToAdd);
            else {

                ActionVocal vocalToChange = (ActionVocal) actions.get(actionToAdd.getName());
                vocalToChange.addFiles(actionVocalToAdd.getFiles());
                actions.put(vocalToChange.getName(), vocalToChange);

            }
        }

        return correct;
    }


    /**
     * Removes the Action with the indicated name. In case that action is used in a configuration,
     * that configuration action is set to "" . If the Action removed is a ActionVocal this method deletes all the files related to it
     * and all the SVMmodel that have at least one file in common.
     * @param name the name of the action to delete
     * @return the deleted action, if any
     */
    //METODO PER RIMUOVERE UN AZIONE
    public Action removeAction(String name) {

        //set the deleted action to null
        for(Configuration conf : configurations) {

            for(Screen s : MainModel.getInstance().getScreensFromConf(conf)) {

                for(Link link : s.getLinks()) {

                    if(link.getAction() != null) {

                        if(link.getAction().getName().equals(name))
                            link.setAction(null);

                    }
                }
            }
        }

        Action thisAction = getAction(name);

        //SE L'AZIONE è DI TIPO VOCALE
        if(thisAction instanceof ActionVocal) {

            ArrayList<SVMmodel> modelsRemoved = new ArrayList<>();
            //RIMUOVO TUTTI I SUONI CHE SI RIFERISCONO AD ESSA
            ((ActionVocal) thisAction).deleteAllSounds();

            //E TUTTI GLI SVMModels CHE HANNO QUEI SUONI
            for (SVMmodel mod : svmModels.values()) {

                if (mod.containsSound(thisAction.getName())) {
                    mod.prepareForDelete();
                    modelsRemoved.add(mod);
                }

            }

            for(SVMmodel model : modelsRemoved) {

                svmModels.remove(model.getName());

                for(Configuration conf : configurations) {

                    if(conf.hasModel() && conf.getModel().equals(model)) {
                        conf.setModel(null);
                    }
                }
            }

        }

        return actions.remove(name);
    }

    public String findVocalActionFromFile(String fileName) {

        String label = "";

        List<ActionVocal> allVocalActions = getVocalActions();

        for(ActionVocal aiv : allVocalActions) {

            if(aiv.getFiles().contains(fileName)) {
                label = aiv.getName();
            }
        }

        return label;
    }

    //Questo metodo elimina tutti i nomi dei modelli che fanno riferimento all'audio eliminato in modo che
    // vengano trainingati nuovamente
    public boolean removeFileVocalAction(String actionName, String fileName) {

        boolean deleted = false;

        ActionVocal thisVocalAction = (ActionVocal) actions.get(actionName);
        assert thisVocalAction != null;
        Set<String> files = thisVocalAction.getFiles();

        for(String file : files)
            if(file.equals(fileName))
                deleted = thisVocalAction.deleteFile(fileName);

        if(thisVocalAction.getFiles().size() == 0)
            this.removeAction(thisVocalAction.getName());


        return deleted;
    }

//----------------------------------------------------------------------------------------------------------------------------------

    /** METODI PER GESTIRE I GIOCHI */

    //METODO CHE SETTA I GIOCHI NEL MAINMODEL PRENDENDOLI DAL FILE JSON
    public void setGames() {

        games = new HashMap<String, Game>();

        for(Game g : jsonManager.getGamesFromJson())
            games.put(g.getTitle(), g);

    }

    //METODO PER OTTENERE I GIOCHI PRESENTI NEL MainModel
    public ArrayList<Game> getGames() {

        if (games != null)
            return new ArrayList<>(games.values());
        else
            return new ArrayList<>();

    }

    //METODO PER OTTENERE UN GIOCO CON UN DETERMINATO NOME
    public Game getGame(String title) {

        if(games != null)
            return games.get(title);
        else
            return null;
    }

    //METODO PER OTTENERE UN GIOCO CON UN DETERMINATO BundleId
    public Game getGameFromBundleId(String bundleId) {

        for(Game game : games.values())
            if(game.getBundleId().equals(bundleId))
                return game;

        return null;
    }

    //METODO PER AGGIUNGERE UN NUOVO GIOCO AL MainModel
    public Game addNewGame(Game newGame) {

        boolean gameAdded = true;

        for(Game g : games.values()) {

            if (g.getBundleId().equals(newGame.getBundleId())) {
                gameAdded = false;
                break;
            }

        }

        if(gameAdded) {
            /*  PRIMA ALL'INSERIMENTO DI UN NUOVO GIOCO CREAVO UNA CONFIGURAZIONE DI DEFAULT
            Configuration newConf = new Configuration("ConfigurationN°1", newGame);
            newConf.setSelected();
            configurations.add(newConf);
             */
            games.put(newGame.getTitle(), newGame);
        }

        return newGame;
    }


    /**
     * Removes the Game with the indicated title and all the configuratios that refer to it
     * @param title
     * @return the removed gameItem, if any.
     */
    //METODO PER RIMUOVERE UN GIOCO //TODO DOVREBBE ESSERE void QUESTO METODO...
    public Game removeGame(String title) {

        ArrayList<Configuration> configurationsToRemove = new ArrayList<>() ;

        for(Configuration conf : configurations) {

            if(conf.getGame().getTitle().equals(title))
                configurationsToRemove.add(conf);

        }

        //RIMUOVO TUTTE LE CONFIGURAZIONI CHE FANNO PARTE DEL GIOCO
        if(configurationsToRemove.size() > 0) {

            for(Configuration conf : configurationsToRemove)
                configurations.remove(conf);

        }

        return games.remove(title);
    }


    /** It receives the event that you want to add and, if it is a new version of an existing event, also that event.
     * The method adds a new event, modifies a pre-existing one or chooses not to add according to different situations.
     * @param gameTitle, oldEvent, newEvent
     * @return true if the new event is added or if an old one is changed */
    //METODO PER SALVARE UN EVENTO IN UN GIOCO
    public boolean saveEvent(String gameTitle, Event oldEvent, Event newEvent) {

        boolean saved = false;

        Game thisGame = MainModel.getInstance().getGame(gameTitle);

        Event eventThatAlreadyExist = thisGame.getEvent(newEvent.getName());
        Log.d("saveEvent", "eventThatAlreadyExist: "+eventThatAlreadyExist.getName());
        //alreadyExistInGames E' TRUE SE ESISTE GIA' IN QUESTO GIOCO UN EVENTO CON QUEL NOME
        boolean alreadyExistInGames = (eventThatAlreadyExist.getName() != null);
        //modifyingExistingGame E' TRUE SE STO MODIFICANDO UN EVENTO GIA' ESISTENTE
        boolean modifyingExistingGame = (oldEvent != null);

        Log.d("saveEvent", "alreadyExistInGames: "+ alreadyExistInGames + "  " + newEvent.getName());
        Log.d("saveEvent", "modifyingExistingGame: "+ modifyingExistingGame);

        if(alreadyExistInGames && modifyingExistingGame) {



            thisGame.removeEvent(oldEvent);
            thisGame.addEvent(newEvent);
            //MainModel.getInstance().writeGamesJson();
            saved = true;
        }
        else if(!alreadyExistInGames && !modifyingExistingGame) {

            Log.d("saveEvent", "Added new Event");
            thisGame.addEvent(newEvent);
            //MainModel.getInstance().writeGamesJson();
            saved = true;
        }
        else if(modifyingExistingGame & !alreadyExistInGames) {

            Log.d("saveEvent", "Sto modificando un evento esistente");
            Log.d("saveEvent", "Removed old event: "+ oldEvent.getName() + ", added new Event: " + newEvent.getName());
            thisGame.removeEvent(oldEvent);
            boolean b = thisGame.addEvent(newEvent);
            if(b)
                Log.d("saveEvent", "Evento salvato");
            else
                Log.d("saveEvent", "Evento non salvato");

            saved = true;

        }
        else {
            Log.d("saveEvent", "Error saving new Event or overwritting the existing one");
        }

        return saved;
    }

//---------------------------------------------------------------------------------------------------------------------------------

    /** METODI PER GESTIRE LE CONFIGURAZIONI */

    //METODO PER SETTARE LE CONFIGURAZIONI NEL MainModel LEGGENDO IL FILE configurations.json
    public void setConfigurations() {
        configurations = jsonManager.getConfigurationsFromJson();
    }

    //METODO PER OTTENERE LE CONFIGURAZIONI DAL MainModel
    public ArrayList<Configuration> getConfigurations(){
        return configurations;
    }

    //METODO PER OTTENERE UNA CONFIGURAZIONE (DI UN DETERMINATO GIOCO, CON UN DETERMINATO NOME) DAL MainModel
    public Configuration getConfiguration(String gameName, String confName) {

        Configuration thisConf = new Configuration();

        for(Configuration conf : configurations) {

            if(conf.getGame().getTitle().equals(gameName) && conf.getConfName().equals(confName)) {
                thisConf = conf;
            }
        }

        return thisConf;
    }

    //METODO PER OTTENERE LE CONFIGURAZIONI DI UN DETERMINATO GIOCO
    public ArrayList<Configuration> getConfigurationsFromGame(String title) {

        ArrayList<Configuration> confs = new ArrayList<>();

        for(Configuration conf : configurations)
            if(conf.getGame().getTitle().equals(title))
                confs.add(conf);

        return confs;
    }

    //METODO PER OTTENERE LA CONFIGURAZIONE ATTIVA PER UN DETERMINATO GIOCO
    public Configuration getActiveConfigurationFromGame(String game_title) {

        Configuration conf = new Configuration();

        for(Configuration c : configurations)
            if(c.getGame().getTitle().equals(game_title) && c.getSelected())
                conf = c;

        return conf;

    }

    //METODO PER OTTENERE UNA CONFIGURAZIONE CON UN DETERMINATO SVMmodel
    public ArrayList<Configuration> confWithThisModel(String modelName){

        ArrayList<Configuration> thisConf = new ArrayList<>();

        for(Configuration conf : configurations)
            if(conf.getModel().getName().equals(modelName))
                thisConf.add(conf);

        return thisConf;
    }

    //METODO PER AGGIUNGERE UNA CONFIGURAZIONE AL MainModel
    public boolean addNewConfiguration(Configuration conf) {

        boolean correct = true;
        boolean alreadyExistActiveConf = false;

        for(Configuration c : configurations) {

            if (c.getConfName().equals(conf.getConfName()) && c.getGame().equals(conf.getGame())) {
                correct = false;
                break;
            }
        }

        if(correct) {

            for(Configuration c : configurations) {

                if(c.getGame().equals(conf.getGame()) && c.getSelected()) {

                    alreadyExistActiveConf = true;
                    break;
                }
            }

            if(!alreadyExistActiveConf)
                conf.setSelected();

            configurations.add(conf);
        }

        return correct;
    }

    /** Removes the Configuration with the indicated name that refers to the indicated game (appName). If the Configuration
     * is removed we remove all the links in it's screens and also all the events belonging to the game to which the configuration refers
     * @param appName the name of the app
     * @param confName the name of the configuration
     * @return true if the configuration has been deleted */
    //METODO PER RIMUOVERE UNA CONFIGURAZIONE CON UN DETERMINATO NOME DI UN DETERMINATO GIOCO
    public boolean removeConfiguration(String appName, String confName) {

        boolean removed = false;
        Configuration confToRemove = new Configuration();

        for(Configuration conf : configurations) {

            if(conf.getGame().getTitle().equals(appName) && conf.getConfName().equals(confName)) {

                confToRemove = conf;
                removed = true;
            }
        }

        if(removed) {

            ArrayList<Screen> screens = this.getScreensFromConf(confToRemove);
            ArrayList<Link> linksToRemove = new ArrayList<>();
            List<Event> events = confToRemove.getGame().getEvents();
            ArrayList<Event> eventsToRemove = new ArrayList<>();

            for(Screen s : screens)
                linksToRemove.addAll(s.getLinks());

            //List<Link> linksToRemove = confToRemove.getLinks(); PERCHè PRIMA I LINK ERANO A LIVELLO DELLE CONF ORA
            //SONO DENTRO GLI SCREEN
            for(Link l : linksToRemove)
                for(Event e : events)
                    if(l.getEvent().equals(e))
                        eventsToRemove.add(e);

            for(Event e : eventsToRemove)
                confToRemove.getGame().removeEvent(e);

            configurations.remove(confToRemove);
        }

        return removed;
    }

    //METODO PER RIMUOVERE LA CONFIGURAZIONE CHE PASSO COME PARAMETRO
    public boolean removeConfiguration(Configuration confToRemove) {

        //NON FACCIO PRIMA A FAR COSì E RICHIAMARE LA FUNZIONE SOPRA GIà DEFINITA?
        return this.removeConfiguration(confToRemove.getGame().getTitle(),confToRemove.getConfName());
        /*
        boolean remove = false;

        for(Configuration c : configurations)
            if(c.equals(confToRemove))
                remove = true;

        if(remove) {

            List<Link> linksToRemove = confToRemove.getLinks();
            List<Event> events = confToRemove.getGame().getEvents();
            ArrayList<Event> eventToRemove = new ArrayList<>();

            for(Link l : linksToRemove) {

                for(Event e : events) {

                    if(l.getEvent().equals(e))
                        eventToRemove.add(e);

                }
            }

            for(Event e : eventToRemove) {
                confToRemove.getGame().removeEvent(e);
            }

            configurations.remove(confToRemove);
        }

        return remove;

         */
    }

    //METODO PER SELEZIONARE COME ATTIVA UNA DETERMINATA CONFIGURAZIONE
    public void setSelectedConfiguration(Configuration conf) {

        conf.setSelected();

        for(Configuration c : configurations) {

            if(c.getGame().equals(conf.getGame()) && !c.getConfName().equals(conf.getConfName()))
                c.setUnselected();

        }
    }

    //METODO PER OTTENERE I LINK DI UNA CONFIGURAZIONE (SCORRO TUTTI GLI SCREEN DELLA CONF E PRENDO I LINK AL LORO INTERNO)
    public ArrayList<Link> getLinksFromConf(Configuration configuration) {

        ArrayList<Link> links = new ArrayList<>();
        ArrayList<Screen> screens = this.getScreensFromConf(configuration);

        for(Screen s : screens)
            links.addAll(s.getLinks());

        return links;

    }

//---------------------------------------------------------------------------------------------------------------------------------

    /** METODI PER GESTIRE SVMModel */

    //METODO PER SETTARE GLI SVMModel CON I DATI PRESI DAL JSON
    public void setSvmModels(){
        svmModels = new HashMap<String, SVMmodel>();
        for(SVMmodel s : jsonManager.getSVMmodelsFromJson()){
            svmModels.put(s.getName(), s);
        }
    }

    //METODO PER OTTENERE UN SVMMODEL CHE HA I SUONI VOCALI PASSATI COME ARGOMENTO
    /** @param vocalActions
     * @return the SVMmodel with all (and only) the sounds names reported
     * Note: the "noise" sound that is always part of the model should not be indicated in the parameter*/

    public SVMmodel getSVMmodel(List<ActionVocal> vocalActions) {

        for(SVMmodel model : svmModels.values()) {
            if (model.containsTheSounds( false, vocalActions))
                return model;
        }

        return null;
    }

    public SVMmodel getSVMmodel(String modelName) { return svmModels.get(modelName);  }

    public List<SVMmodel> getSvmModels(){
        return new ArrayList<>(svmModels.values());
    }


    /** Add a new model and first check that there isn't one with same sounds.
     * @param newModel
     * @return true if the model was added
     */
    public boolean addNewModel(SVMmodel newModel) {

        boolean canAdd = true;

        for(SVMmodel model : svmModels.values()) {
            if(model.containsTheSounds( true, newModel.getSounds())) {
                canAdd = false;
            }
        }

        if(canAdd) {
            svmModels.put(newModel.getName(), newModel);
        }

        return canAdd;
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

//---------------------------------------------------------------------------------------------------------------------------------

    /** METODI PER GESTIRE GLI SCREEN DI UNA CONFIGURAZIONE*/

    //METODO PER SETTARE UNO SCREEN PER UNA CONFIGURAZIONE
    public void setScreenForConf(Screen s, Configuration configuration) {

        if(s.getConfiguration() == configuration)
            screens.add(s);

    }

    //METODO PER OTTENERE LE SCHERMATE DI UNA DETERMINATA CONFIGURAZIONE
    public ArrayList<Screen> getScreensFromConf(Configuration c) {

        ArrayList<Screen> screenArrayList = new ArrayList<>();

        for(Screen s : screens)
            if(s.getConfiguration() == c)
                screenArrayList.add(s);

        return screenArrayList;

    }

    //METODO PER OTTENERE UNO SCREEN CON UN DETERMINATO NOME PER UNA DETERMINATA CONFIGURAZIONE
    public Screen getScreenFromConf(Configuration configuration, String screenName) {

        ArrayList<Screen> screenArrayList = getScreensFromConf(configuration);
        //COSì SE NON TROVA SCREEN CON QUEL NOME RITORNA UNO SCREEN VUOTO
        Screen screen = new Screen();

        for(Screen s : screenArrayList) {
            if(s.getName().equals(screenName))
                screen = s;
        }

        return screen;

    }

    /** It receives the link that you want to add and, if it is a new version of an existing link, also that link.
     * The method adds a new link, modifies a pre-existing one or chooses not to add according to different situations.
     * @param gameTitle, nameConfig, oldLink, newLink
     * @return true if the new event is added or if an old one is changed */

    public boolean saveLink(String gameTitle, String nameConfig, Link oldLink, Link newLink){  //Dovrai aggiungere le altre caratteristiche di un Link
        Log.d(TAG, "saveLink");

        boolean linkSaved = false;
        Configuration thisConfig = MainModel.getInstance().getConfiguration(gameTitle, nameConfig);

        //alreadyExistInConfig is true if an event already exists with the same name associated with the game
        //modifyingExistingLink is true if we are modifying a pre-existent event, then oldEvent! = null
        boolean alreadyExistInConfig = (thisConfig.getLink(newLink.getEvent().getName()) != null);
        boolean modifyingExistingLink = (oldLink != null);

        //if it exists and we are modifying it, we delete the previous copy to create an updated one
        if(alreadyExistInConfig && modifyingExistingLink) {

            Log.d(TAG, "deleted old link, added a new one: "+newLink.getEvent().getName());
            thisConfig.getLinks().remove(oldLink);
            thisConfig.addLink(newLink);
            MainModel.getInstance().writeConfigurationsJson();
            linkSaved = true;

        }

        //if the link does not exist in the configuration we add it without other controls
        if(!alreadyExistInConfig) {

            Log.d(TAG, "added a new Link");

            thisConfig.addLink(newLink);
            MainModel.getInstance().writeConfigurationsJson();
            linkSaved = true;
        }

        return linkSaved;
    }

    //METODO PER SALVARE IL LINK IN UNA SCHERMATA
    public boolean saveLink2(String gameTitle, String nameConfig, String screenName, Link oldLink, Link newLink) {

        Log.d(TAG, "saveLink");

        boolean linkSaved = false;
        Configuration thisConfig = MainModel.getInstance().getConfiguration(gameTitle, nameConfig);
        Screen screen = MainModel.getInstance().getScreenFromConf(thisConfig, screenName);

        //alreadyExistInConfig is true if an event already exists with the same name associated with the game
        //modifyingExistingLink is true if we are modifying a pre-existent event, then oldEvent! = null
        //boolean alreadyExistInConfig = (screen.getLink(newLink.getEvent().getName()) != null);
        boolean alreadyExistInConfig = (thisConfig.getLink(newLink.getEvent().getName()) != null);
        boolean modifyingExistingLink = (oldLink != null);

        //if it exists and we are modifying it, we delete the previous copy to create an updated one
        if(alreadyExistInConfig && modifyingExistingLink) {

            Log.d(TAG, "deleted old link, added a new one: "+newLink.getEvent().getName());
            screen.getLinks().remove(oldLink);
            screen.addLink(newLink);
            MainModel.getInstance().writeConfigurationsJson();
            linkSaved = true;

        }

        //if the link does not exist in the configuration we add it without other controls
        if(!alreadyExistInConfig) {

            Log.d(TAG, "added a new Link");

            screen.addLink(newLink);
            MainModel.getInstance().writeConfigurationsJson();
            linkSaved = true;
        }

        return linkSaved;

    }

    public boolean saveLink3(Configuration configuration, String screenName, Link oldLink, Link newLink) {

        Log.d("saveLink3", "Sono dentro la funzione saveLink3");
        boolean linkSaved = false;

        Screen screen = this.getScreenFromConf(configuration, screenName);

        //alreadyExistInConfig is true if an event already exists with the same name associated with the game
        //modifyingExistingLink is true if we are modifying a pre-existent event, then oldEvent! = null
        //boolean alreadyExistInConfig = (screen.getLink(newLink.getEvent().getName()) != null);
        boolean alreadyExistInConfig = (configuration.getLink2(newLink.getEvent().getName()).getEvent() != null);
        boolean modifyingExistingLink = (oldLink.getEvent() != null);
        Log.d("saveLink3", "alreadyExistInConfig: "+alreadyExistInConfig);
        Log.d("saveLink3", "alreadyExistInConfig Link: "+configuration.getLink2(newLink.getEvent().getName()).toString());
        Log.d("saveLink3", "modifyingExistingLink: "+ modifyingExistingLink);

        //if it exists and we are modifying it, we delete the previous copy to create an updated one
        if(alreadyExistInConfig && modifyingExistingLink) {

            Log.d("saveLink3", "deleted old link, added a new one: "+newLink.getEvent().getName());
            screen.getLinks().remove(oldLink);
            screen.addLink(newLink);
            linkSaved = true;

        }

        if(!alreadyExistInConfig && !modifyingExistingLink) { //STO CREANDO UN NUOVO LINK

            Log.d("saveLink3", "added a new Link");

            screen.addLink(newLink);
            linkSaved = true;
        }

        //STO SEMPRE MODIFICANDO UN LINK MA CAMBIANDO IL NOME DELL'EVENTO OVVIAMENTE NON TROVA LO STESSO EVENTO IN QUESTA CONFIGURAZIONE
        if(!alreadyExistInConfig && modifyingExistingLink) {

            Log.d("saveLink3", "deleted old link, added a new one: "+newLink.getEvent().getName());
            screen.getLinks().remove(oldLink);
            screen.addLink(newLink);
            linkSaved = true;
        }

        Log.d("saveLink3", "newLink: "+newLink.toString());

        return linkSaved;

    }


    public void removeScreenshot(Screen screenshot) {

        for(Screen s : screens)
            if(s == screenshot)
                screens.remove(s);

    }



    public void removeScreen(int screen_index) {

        for(Screen s : screens)
            if(screens.indexOf(s) == screen_index)
                screens.remove(s);


    }

    public ArrayList<Action> getActionsOfConf(Configuration configuration) {

        ArrayList<Action> actions = new ArrayList<>();

        for(Screen s : screens) {

            if(s.getConfiguration() == configuration) {

                for(Link l : s.getLinks()) {

                    if(l.getAction() != null) {

                        actions.add(l.getAction());

                        if(l.getActionStop()!=null)
                            actions.add(l.getActionStop());

                    }
                }

            }


        }

        return actions;

    }


//---------------------------------------------------------------------------------------------------------------------------------

    /** METODI PER SCRIVERE I DATI DEL MAINMODEL ALL'INTERNO DEI FILE JSON CORRETTI */

    public void writeActionsJson() {
        JsonManager.writeActions(this.getActions());
    }

    public void writeGamesJson(){ JsonManager.writeGames(this.games.values()); }

    public void writeConfigurationsJson() { JsonManager.writeConfigurations(configurations); }

    public void writeModelJson() { JsonManager.writeSVMmodels(this.svmModels.values()); }

    public ArrayList<Action> getActionsFromConf(Configuration configuration) {

        ArrayList<Action> actions = new ArrayList<>();
        ArrayList<Screen> screens = getScreensFromConf(configuration);

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

    public List<ActionVocal> getActionsVocalFromConf(Configuration configuration) {

        ArrayList<ActionVocal> result = new ArrayList<>();

        for(Action action : this.getActionsFromConf(configuration)){
            if(action instanceof ActionVocal){
                result.add((ActionVocal) action);
            }
        }

        return result;
    }

    public Link getLinkFromAction(Configuration configuration, String action) {

        for(Screen s:screens) {

            if(s.getConfiguration() == configuration) {

                for(Link l : s.getLinks()) {

                    if(l.getAction() != null && l.getAction().getName().equals(action))
                        return l;

                }

            }


        }

        return null;
    }

    public String getScreenNameWhereActionIsUsed(Action action, Configuration configuration) {

        String screenName = "";

        ArrayList<Screen> screens = getScreensFromConf(configuration);

        for(Screen s : screens) {

            for(Link l : s.getLinks()) {

                if(l.getAction() == action) {

                    screenName = s.getName();
                    break;

                }
            }

        }

        return screenName;
    }

    public ArrayList<ActionButton> getButtonActionsFromConf(Configuration configuration) {

        ArrayList<ActionButton> result = new ArrayList<>();

        for(Action action : this.getActionsFromConf(configuration)) {
            if(action instanceof ActionButton){
                result.add((ActionButton) action);
            }
        }

        return result;
    }

    public List<ActionVocal> getVocalActionsFromConf(Configuration configuration){

        ArrayList<ActionVocal> result = new ArrayList<>();

        for(Action action : this.getActionsFromConf(configuration)) {

            if(action instanceof ActionVocal) {

                result.add((ActionVocal) action);

            }
        }

        return result;
    }

    public Bitmap stringToBitMap(String encodedString) {

        try {
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;

        } catch(Exception e){

            e.getMessage();
            return null;
        }

    }

    public String bitmapToString(Bitmap bitmap) {

        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,5, baos);
        byte [] b=baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public String getBitmapFromDrawable(@NonNull Drawable drawable) {

        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 5, baos);
        byte[] b = baos.toByteArray();
        String encodeImage = Base64.encodeToString( b , Base64.NO_WRAP);

        return encodeImage;
    }

    /** This method returns a value which indicates if the selected configuration of a specified game has been configured correctly
     * @param title of the game to check
     * @return double between 0.0 and 1.0 */

    public double matchingUpdate(String title) {

        double matching = 0.0;
        double num = 0.0;
        double denom = 0.0;

        ArrayList<Configuration> gameConf = MainModel.getInstance().getConfigurationsFromGame(title);

        for(Configuration conf : gameConf) {

            if(conf.getSelected()) {

                if (! conf.getVocalActions().isEmpty() && conf.getModel() == null) {
                    denom++;
                }

                for (Link link : conf.getLinks()) {

                    Log.d("MATCHING_UPDATE", "Link: " + link.getEvent().getName());

                    if (link.getAction() == null) {

                        Log.d("MATCHING_UPDATE", "Dentro if di Link");
                        denom++;

                    }
                    else {

                        Log.d("MATCHING_UPDATE", "Dentro else di Link");
                        num++;
                        denom++;
                    }
                }
            }
        }

        if(denom > 0.0) {
            matching = num / denom;

        }
        else {
            matching = 0.0;
        }

        Log.d("MATCHING_UPDATE", title+" num: "+num+" denom: "+denom);
        return matching;

    }

    public double matchingUpdate2(String title) {

        double matching = 0.0;
        double num = 0.0;
        double denom = 0.0;

        ArrayList<Configuration> gameConf = MainModel.getInstance().getConfigurationsFromGame(title);

        for(Configuration conf : gameConf) {

            if(conf.getSelected()) {

                ArrayList<Screen> screens = MainModel.getInstance().getScreensFromConf(conf);

                for(Screen s :screens) {

                    for (Link link : s.getLinks()) {

                        Log.d("MATCHING_UPDATE", "Link: " + link.getEvent().getName());

                        if (link.getAction() == null) {

                            Log.d("MATCHING_UPDATE", "Dentro if di Link");
                            denom++;

                        }
                        else {

                            Log.d("MATCHING_UPDATE", "Dentro else di Link");
                            num++;
                            denom++;
                        }
                    }

                }

                //if (! conf.getVocalActions().isEmpty() && conf.getModel() == null) {
                if(! MainModel.getInstance().getVocalActionsFromConf(conf).isEmpty() && conf.getModel() == null) {
                    denom++;
                }

            }
        }

        if(denom > 0.0) {
            matching = num / denom;

        }
        else {
            matching = 0.0;
        }

        Log.d("MATCHING_UPDATE", title+" num: "+num+" denom: "+denom);
        return matching;

    }

    //VECCHI METODI NON PIù UTILIZZATI, DA CANCELLARE
    /*
    public void setActions() {
        actions = new HashMap<String, Action>();
        for (Action a: jsonManager.getActionsFromJson()) {
            actions.put(a.getName(), a);
        }
    }

    public void setGames() {
        games = new HashMap<String, Game>();
        for(Game g : jsonManager.getGamesFromJson()){
            games.put(g.getTitle(), g);
        }
    }

    public void setConfigurations() {
        configurations = (ArrayList<Configuration>) jsonManager.getConfigurationFromJson();
    }


    public void setSvmModels(){
        svmModels = new HashMap<String, SVMmodel>();
        for(SVMmodel s : jsonManager.getSVMmodelsFromJson()){
            svmModels.put(s.getName(), s);
        }
    }

    //METODO PER OTTENERE UNA CONFIGURAZIONE (DATO IL PACKAGE DEL GAME E IL NOME DELLA CONFIGURAZIONE)
    /*
    public Configuration getConfigurationFromPackage(String gamePackage, String confName){

        Configuration thisConf = new Configuration();

        for(Configuration conf : configurations){
            if(conf.getGame().getBundleId().equals(gamePackage) && conf.getConfName().equals(confName)){
                thisConf = conf;
            }
        }

        return thisConf;

    }

     */


    /** METODI CHE POTRANNO SERVIRE QUANDO VERRANO IMPLEMENTATI BENE GLI SCREEN */
    //METODO PER SALVARE IL LINK IN UNA SCHERMATA
    /*
    public boolean saveLink2(String gameTitle, String nameConfig, String screenName, Link oldLink, Link newLink) {

        Log.d(TAG, "saveLink");

        boolean linkSaved = false;
        Configuration thisConfig = MainModel.getInstance().getConfiguration(gameTitle, nameConfig);
        Screen screen = MainModel.getInstance().getScreenFromConf(thisConfig, screenName);

        //alreadyExistInConfig is true if an event already exists with the same name associated with the game
        //modifyingExistingLink is true if we are modifying a pre-existent event, then oldEvent! = null
        boolean alreadyExistInConfig = (screen.getLink(newLink.getEvent().getName()) != null);
        boolean modifyingExistingLink = (oldLink != null);

        //if it exists and we are modifying it, we delete the previous copy to create an updated one
        if(alreadyExistInConfig && modifyingExistingLink) {

            Log.d(TAG, "deleted old link, added a new one: "+newLink.getEvent().getName());
            screen.getLinks().remove(oldLink);
            screen.addLink(newLink);
            //MainModel.getInstance().writeConfigurationsJson();
            MainModel.getInstance().writeConfigurationsJson2();
            linkSaved = true;

        }

        //if the link does not exist in the configuration we add it without other controls
        if(!alreadyExistInConfig) {

            Log.d(TAG, "added a new Link");

            screen.addLink(newLink);
            //MainModel.getInstance().writeConfigurationsJson();
            MainModel.getInstance().writeConfigurationsJson2();
            linkSaved = true;
        }

        return linkSaved;

    }

    public void removeScreen(Configuration configuration, String screenName) {

        ArrayList<Screen> screenArrayList = getScreensFromConf(configuration);

        for(Screen s : screenArrayList) {

            if(s.getName().equals(screenName)) {

                screens.remove(s);

                List<Link> linksToRemove = s.getLinks();
                List<Event> events = configuration.getGame().getEvents();
                ArrayList<Event> eventToRemove = new ArrayList<>();
                //CONTROLLO QUALI EVENTI DA RIMUOVERE
                for(Link l : linksToRemove)
                    for(Event e : events)
                        if(l.getEvent().equals(e))
                            eventToRemove.add(e);
                //RIMUOVO GLI EVENTI TROVATI PRIMA
                for(Event e : eventToRemove)
                    configuration.getGame().removeEvent(e);

            }

        }

    }

     */

}

