/*
JSONMANAGER: allows to manage the json files about player, games anc links.
 */
package com.ewlab.a_cube.model;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class JsonManager {

    private static final String TAG = JsonManager.class.getName();

    private static final String FILE_ACTIONS = "actions.json";
    private static final String FILE_GAMES = "games.json";
    private static final String FILE_CONFIGURATIONS = "configurations.json";
    private static final String FILE_MODELS = "models.json";
    private MainModel mainModel;

    public JsonManager(MainModel mainModel){
        this.mainModel = mainModel;
    }

    public List<Action> getActionsFromJson() {

        ArrayList<Action> result = new ArrayList<>();

        JSONObject jsonfileA = this.readActionsJson();
        if (jsonfileA == null) {
            Log.e(TAG, "critical error reading actions.json");
            return result;
        }

        JSONArray jsonActions = new JSONArray();


        try {

            jsonActions = jsonfileA.getJSONArray("actions");

            for (int i = 0; i < jsonActions.length(); i++) {
                JSONObject jsonAction = jsonActions.getJSONObject(i);
                Action newAction = null;

                String acName = jsonAction.getString("action_name");
                String acType = jsonAction.getString("action_type");

                if (acType.equals(Action.VOCAL_TYPE)) {
                    JSONArray files = jsonAction.getJSONArray("files");
                    Set<String> acFiles = new HashSet<>();

                    for (int j = 0; j < files.length(); j++) {
                        acFiles.add(files.getJSONObject(j).getString("file_name"));
                    }

                    newAction = new ActionVocal(acName, acFiles);
                    result.add(newAction);

                }else if (acType.equals(Action.BUTTON_TYPE)) {
                    String acDeviceId = jsonAction.getString("device_id");
                    String acKeyId = jsonAction.getString("key_id");

                    newAction = new ActionButton(acName, acDeviceId, acKeyId);

                }else{
                    Log.e(TAG, "critical error while reading actions: unrecognised action type");

                }

                if(newAction!=null) {
                    result.add(newAction);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Game> getGamesFromJson(){
        JSONObject jsonfileG = this.readGamesJson();
        JSONArray gamesJa;

        ArrayList<Game> games = new ArrayList<>();

        try{
            if(jsonfileG!=null) {
                gamesJa = jsonfileG.getJSONArray("games");

                for (int i = 0; i < gamesJa.length(); i++) {
                    String gaBundleId = gamesJa.getJSONObject(i).getString("bundle_id");
                    String gaTitle = gamesJa.getJSONObject(i).getString("title");
                    String gaIcon = gamesJa.getJSONObject(i).getString("icon");

                    JSONArray eventsJa = gamesJa.getJSONObject(i).getJSONArray("events");
                    ArrayList<Event> events = new ArrayList<>();

                    for (int j = 0; j < eventsJa.length(); j++) {
                        String evName = eventsJa.getJSONObject(j).getString("name");
                        String evType = eventsJa.getJSONObject(j).getString("type");
                        double evX = eventsJa.getJSONObject(j).getDouble("X");
                        double evY = eventsJa.getJSONObject(j).getDouble("Y");
                        String evScreenshot = eventsJa.getJSONObject(j).getString("screenshot");
                        boolean evPortrait = Boolean.parseBoolean(eventsJa.getJSONObject(j).getString("portrait"));

                        Event newEvent = new Event(evName, evType, evX, evY, evScreenshot, evPortrait);
                        events.add(newEvent);
                    }

                    Game newGame = new Game(gaBundleId, gaTitle, gaIcon, events);
                    games.add(newGame);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return games;
    }

    public List<SVMmodel> getSVMmodelsFromJson(){
        JSONObject jsonfileM = this.readModelsJson();
        JSONArray models;

        ArrayList<SVMmodel> SVMmodels = new ArrayList<>();

        try{
            if(jsonfileM!=null) {
                models = jsonfileM.getJSONArray("models");

                for (int i = 0; i < models.length(); i++) {
                    String model_name = models.getJSONObject(i).getString("model_name");
                    JSONArray sounds = models.getJSONObject(i).getJSONArray("sounds");
                    ArrayList<ActionVocal> moSounds = new ArrayList<>();

                    for (int j = 0; j < sounds.length(); j++) {
                        String soundName = sounds.getJSONObject(j).getString("sound");

                        if(soundName.equals(SVMmodel.NOISE_NAME)){
                            ActionVocal Noise = new ActionVocal("Noise");
                            moSounds.add(Noise);

                        }else{

                            Action action = mainModel.getAction(soundName);

                            if(action instanceof ActionVocal) {
                                moSounds.add((ActionVocal)action);

                            }else{
                                Log.e(TAG, "inconsistant Json");
                            }
                        }


                    }

                    SVMmodel newModel = new SVMmodel(model_name, moSounds);
                    SVMmodels.add(newModel);
                }
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return SVMmodels;
    }

    public List<Configuration> getConfigurationFromJson(){
        JSONObject jsonfileC = this.readConfigurationsJson();
        JSONArray configurationsJa;

        ArrayList<Configuration> configurations = new ArrayList<>();

        try{
            if(jsonfileC!=null) {
                configurationsJa = jsonfileC.getJSONArray("configurations");

                for (int i = 0; i < configurationsJa.length(); i++) {
                    Configuration newConf;

                    String confName = configurationsJa.getJSONObject(i).getString("conf_name");
                    String gamePackage = configurationsJa.getJSONObject(i).getString("game_package");

                    boolean selected = Boolean.parseBoolean(configurationsJa.getJSONObject(i).getString("selected"));

                    JSONArray links = configurationsJa.getJSONObject(i).getJSONArray("links");
                    ArrayList<Link> coLinks = new ArrayList<>();

                    for (int j = 0; j < links.length(); j++) {
                        String eventName = links.getJSONObject(j).getString("event_name");
                        String actionName = null;
                        int markerColor = 0;
                        int marker = 0;

                        String actionStopName = "";
                        double duration = 0.0;

                        if (links.getJSONObject(j).has("action_name")) {
                            actionName = links.getJSONObject(j).getString("action_name");
                        }
                        if (links.getJSONObject(j).has("marker_color")) {
                            markerColor = links.getJSONObject(j).getInt("marker_color");
                        }
                        if (links.getJSONObject(j).has("marker_color")) {
                            marker = links.getJSONObject(j).getInt("marker_size");
                        }
                        if (links.getJSONObject(j).has("action_stop_name")) {
                            actionStopName = links.getJSONObject(j).getString("action_stop_name");
                        }
                        if (links.getJSONObject(j).has("duration")) {
                            duration = links.getJSONObject(j).getDouble("duration");
                        }


                        Link newLink = new Link();

                        if(actionName!=null) {
                            Event event = MainModel.getInstance().getGameFromBundleId(gamePackage).getEvent(eventName);

                            Action action = MainModel.getInstance().getAction(actionName);
                            newLink = new Link(event, action, markerColor, marker);
                        }else{
                            Event event = MainModel.getInstance().getGameFromBundleId(gamePackage).getEvent(eventName);

                            newLink = new Link(event, markerColor, marker);
                        }

                        //se duration o actionStopName ci sono li aggiungo al link
                        if (duration != 0.0) {
                            newLink.setDuration(duration);
                        }
                        if (!actionStopName.equals("")) {
                            Action actionStop = MainModel.getInstance().getAction(actionStopName);
                            newLink.setActionStop(actionStop);
                        }


                        coLinks.add(newLink);
                    }

                    Game game = MainModel.getInstance().getGameFromBundleId(gamePackage);

                    if (configurationsJa.getJSONObject(i).has("model_name")) {
                        String coModelName = configurationsJa.getJSONObject(i).getString("model_name");
                        SVMmodel svmModel = MainModel.getInstance().getSVMmodel(coModelName);
                        newConf = new Configuration(confName, game, svmModel, coLinks);

                    } else {
                        newConf = new Configuration(confName, game, coLinks);
                    }

                    if(selected){
                        newConf.setSelected();
                    }else{
                        newConf.setUnselected();
                    }

                    configurations.add(newConf);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return configurations;
    }



    public static void writeActions(Collection<Action> values) {
        JSONObject jsonfileA = new JSONObject();
        JSONArray actions = new JSONArray();

        try{
            for(Action a : values) {
                JSONObject action = new JSONObject();

                Log.d(TAG, a.getName());
                action.put("action_name", a.getName());


                if (a instanceof ActionButton) {
                    Log.d(TAG, "buttonFinded");
                    ActionButton a1 = (ActionButton) a;

                    action.put("action_type", Action.BUTTON_TYPE);
                    action.put("device_id", a1.getDeviceId());
                    action.put("key_id", a1.getKeyId());
                }

                if (a instanceof ActionVocal) {
                    Log.d(TAG, "vocalFinded");
                    ActionVocal a2 = (ActionVocal) a;

                    action.put("action_type", Action.VOCAL_TYPE
                    );

                    Set<String> filesV = (a2.getFiles());
                    JSONArray files = new JSONArray();

                    Iterator<String> it = filesV.iterator();
                    while(it.hasNext()){
                        JSONObject newFile = new JSONObject();
                        newFile.put("file_name", it.next());
                        files.put(newFile);
                    }

                    action.put("files", files);
                }

                actions.put(action);
            }

            jsonfileA.put("actions", actions);

        }catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("actions", jsonfileA);
    }

    public static void writeGames(Collection<Game> values){
        JSONObject jsonfileG = new JSONObject();
        JSONArray games = new JSONArray();

        try{

            for(Game g : values){
                JSONObject newGame = new JSONObject();

                newGame.put("bundle_id", g.getBundleId());
                newGame.put("title", g.getTitle());
                newGame.put("icon", g.getIcon());

                ArrayList<Event> newEvents = g.getEvents();
                JSONArray events = new JSONArray();

                if(newEvents!=null) {
                    for(Event event : newEvents) {
                        JSONObject newEvent = new JSONObject();

                        newEvent.put("name", event.getName());
                        newEvent.put("type", event.getType());
                        newEvent.put("X", event.getX());
                        newEvent.put("Y", event.getY());
                        newEvent.put("screenshot", event.getScreenshot());
                        newEvent.put("portrait", event.getPortrait());

                        events.put(newEvent);
                    }
                }

                newGame.put("events", events);
                games.put(newGame);
            }

            jsonfileG.put("games", games);

        }catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("games", jsonfileG);
    }

    public static void writeConfigurations(ArrayList<Configuration> conf){
        JSONObject jsonfileC = new JSONObject();
        JSONArray configurations = new JSONArray();

        try{

            for(Configuration c : conf){
                JSONObject newConf = new JSONObject();

                newConf.put("conf_name", c.getConfName());
                newConf.put("game_package", c.getGame().getBundleId());

                newConf.put("selected", c.getSelected());

                if(c.getModel()!=null) {
                    newConf.put("model_name", c.getModel().getName());
                }

                ArrayList<Link> links = c.getLinks();
                JSONArray JaLinks = new JSONArray();

                for(Link l : links) {
                    JSONObject newLink = new JSONObject();

                    newLink.put("event_name", l.getEvent().getName());
                    if(l.getAction() != null) {
                        newLink.put("action_name", l.getAction().getName());
                    }
                    newLink.put("marker_color", l.getMarkerColor());
                    newLink.put("marker_size", l.getMarkerSize());

                    if(l.getActionStop()!=null) {
                        newLink.put("action_stop_name", l.getActionStop().getName());
                    }

                    if (l.getDuration() > 0) {
                        newLink.put("duration", l.getDuration());
                    }

                    JaLinks.put(newLink);
                }

                newConf.put("links", JaLinks);
                configurations.put(newConf);
            }

            jsonfileC.put("configurations", configurations);

        }catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("configurations", jsonfileC);
    }

    public static void writeSVMmodels(Collection<SVMmodel> values){
        JSONObject jsonfileM = new JSONObject();
        JSONArray models = new JSONArray();

        try{

            for(SVMmodel m : values){
                JSONObject newSvmModel = new JSONObject();

                newSvmModel.put("model_name", m.getName());

                ArrayList<String> vocalActionsNames = new ArrayList<>();
                for(ActionVocal vocalAction : m.getSounds()) {
                    if (vocalAction != null) {
                        vocalActionsNames.add(vocalAction.getName());
                    }
                }

                JSONArray sounds = new JSONArray();

                for(String vocalActionName : vocalActionsNames){
                    JSONObject newSound = new JSONObject();
                    newSound.put("sound", vocalActionName);
                    sounds.put(newSound);
                }

                newSvmModel.put("sounds", sounds);
                models.put(newSvmModel);
            }

            jsonfileM.put("models", models);
        }catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("models", jsonfileM);
    }


    //reads actions.json and returns the contents in a jsonobject
    private JSONObject readActionsJson(){
        String jsonPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube";
        File jsonFile = new File(jsonPath, FILE_ACTIONS);
        JSONObject jObject = null;
        if(jsonFile.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(jsonFile));
                String s = br.readLine();
                jObject = (JSONObject) new JSONTokener(s).nextValue();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jObject;

    }

    //reads configurations.json and returns the contents in a jsonobject
    private JSONObject readConfigurationsJson(){
        String jsonPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube";
        File jsonFile = new File(jsonPath,FILE_CONFIGURATIONS);
        JSONObject jObject = null;
        if(jsonFile.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(jsonFile));
                String s = br.readLine();
                jObject = (JSONObject) new JSONTokener(s).nextValue();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jObject;
    }

    //reads models.json and returns the contents in a jsonobject
    private JSONObject readModelsJson(){
        String jsonPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube";
        File jsonFile = new File(jsonPath, FILE_MODELS);
        JSONObject jObject = null;
        if(jsonFile.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(jsonFile));
                String s = br.readLine();
                jObject = (JSONObject) new JSONTokener(s).nextValue();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jObject;
    }

    //the method reads games.json and returns the file as jsonobject
    private JSONObject readGamesJson(){
        String jsonPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube";
        File jsonFile = new File(jsonPath, FILE_GAMES);

        JSONObject jObject = null;
        if(jsonFile.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(jsonFile));
                String s, json = "";
                //the use of the while cycle depends upon how the games.json has been written
                //if the file has not been written in only one row you need the while cycle to parse it
                while( (s = br.readLine()) != null){
                    json += s;
                }
                jObject = new JSONObject(new JSONTokener(json));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jObject;
    }


    //receives as parameters the file name and the json object (it represents the contents of the file) and stores the file
    private static void writeJson(String fileName, JSONObject json){
        String jsonPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube";
        String file = fileName+".json";
        File jsonFile = new File(jsonPath, file);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFile));
            bw.write(json.toString());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } //BUONO


}
