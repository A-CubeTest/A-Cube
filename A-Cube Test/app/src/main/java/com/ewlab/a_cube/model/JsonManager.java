/*
JSONMANAGER: allows to manage the json files about player, games anc links.
 */
package com.ewlab.a_cube.model;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JsonManager {

    private static final String TAG = JsonManager.class.getName();

    private static final String JSON_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube";
    private static final String FILE_ACTIONS = "actions.json";
    private static final String FILE_GAMES = "games.json";
    private static final String FILE_CONFIGURATIONS = "configurations.json";
    private static final String FILE_MODELS = "models.json";
    private MainModel mainModel;

    public JsonManager(MainModel mainModel){
        this.mainModel = mainModel;
    }

    /** METODI PER LEGGERE E SCRIVERE AZIONI DA E PER FILE actions.json */

    //METODO PER OTTENERE LE AZIONI PRESENTI NEL FILE actions.json
    public List<Action> getActionsFromJson() {

        ArrayList<Action> result = new ArrayList<>();

        JSONObject jsonfileA = this.readJsonFile(FILE_ACTIONS);

        if (jsonfileA == null) {
            Log.e(TAG, "critical error reading actions.json");
            return result;
        }

        JSONArray jsonActions;

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

                }
                else if (acType.equals(Action.BUTTON_TYPE)) {

                    String acDeviceId = jsonAction.getString("device_id");
                    String acKeyId = jsonAction.getString("key_id");

                    newAction = new ActionButton(acName, acDeviceId, acKeyId);
                    result.add(newAction);

                }
                else {
                    Log.e(TAG, "critical error while reading actions: unrecognised action type");
                }

                if(newAction != null) {
                    result.add(newAction);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;

    }

    public static void writeActions(Collection<Action> values) {

        JSONObject jsonfileA = new JSONObject();
        JSONArray actions = new JSONArray();

        try {

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
                else if (a instanceof ActionVocal) {

                    Log.d(TAG, "vocalFinded");
                    ActionVocal a2 = (ActionVocal) a;

                    action.put("action_type", Action.VOCAL_TYPE);

                    Set<String> filesV = (a2.getFiles());
                    JSONArray files = new JSONArray();

                    Iterator<String> it = filesV.iterator();

                    while(it.hasNext()) {

                        JSONObject newFile = new JSONObject();
                        newFile.put("file_name", it.next());
                        files.put(newFile);

                    }

                    action.put("files", files);
                }

                actions.put(action);
            }

            jsonfileA.put("actions", actions);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("actions", jsonfileA);

    }

    //----------------------------------------------------------------------------------------------
    /** METODI PER LEGGERE E SCRIVERE GIOCHI DA E PER FILE games.json */

    public List<Game> getGamesFromJson() {

        JSONObject jsonfileG = this.readJsonFile(FILE_GAMES);
        JSONArray gamesJa;

        ArrayList<Game> games = new ArrayList<>();

        try {

            if(jsonfileG != null) {

                gamesJa = jsonfileG.getJSONArray("games");

                for (int i = 0; i < gamesJa.length(); i++) {

                    String gaBundleId = gamesJa.getJSONObject(i).getString("bundle_id");
                    String gaTitle = gamesJa.getJSONObject(i).getString("title");

                    JSONArray eventsJa = gamesJa.getJSONObject(i).getJSONArray("events");
                    ArrayList<Event> events = new ArrayList<>();

                    for (int j = 0; j < eventsJa.length(); j++) {

                        String evName = eventsJa.getJSONObject(j).getString("name");
                        String evType = eventsJa.getJSONObject(j).getString("type");
                        double evX = eventsJa.getJSONObject(j).getDouble("X");
                        double evY = eventsJa.getJSONObject(j).getDouble("Y");
                        //String evScreenshot = eventsJa.getJSONObject(j).getString("screenshot");
                        String evScreen = eventsJa.getJSONObject(j).getString("screen");
                        boolean evPortrait = Boolean.parseBoolean(eventsJa.getJSONObject(j).getString("portrait"));

                        Event newEvent = new Event(evName, evType, evX, evY, evScreen, evPortrait);
                        events.add(newEvent);

                    }

                    //Game newGame = new Game(gaBundleId, gaTitle, gaIcon, events);
                    Game newGame = new Game(gaBundleId, gaTitle, events);
                    //Game newGame = new Game(gaBundleId, gaTitle, gaIcon);
                    games.add(newGame);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return games;
    }

    public static void writeGames(Collection<Game> values) {

        JSONObject jsonfileG = new JSONObject();
        JSONArray games = new JSONArray();

        try {

            for(Game g : values) {

                JSONObject newGame = new JSONObject();

                newGame.put("bundle_id", g.getBundleId());
                newGame.put("title", g.getTitle());
                //newGame.put("icon", g.getIcon());

                ArrayList<Event> newEvents = g.getEvents();
                JSONArray events = new JSONArray();

                if(newEvents != null) {

                    for(Event event : newEvents) {

                        JSONObject newEvent = new JSONObject();

                        newEvent.put("name", event.getName());
                        newEvent.put("type", event.getType());
                        newEvent.put("X", event.getX());
                        newEvent.put("Y", event.getY());
                        newEvent.put("screen", event.getScreenImage());
                        newEvent.put("portrait", event.getPortrait());

                        events.put(newEvent);
                    }
                }

                newGame.put("events", events);

                games.put(newGame);

            }

            jsonfileG.put("games", games);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("games", jsonfileG);

    }

    //_____________________________________________________________________________________________________
    /** METODI PER LEGGERE E SCRIVERE AZIONI DA E PER FILE models.json */

    public List<SVMmodel> getSVMmodelsFromJson() {

        JSONObject jsonfileM = this.readJsonFile(FILE_MODELS);
        JSONArray models;

        ArrayList<SVMmodel> SVMmodels = new ArrayList<>();

        try {

            if(jsonfileM != null) {

                models = jsonfileM.getJSONArray("models");

                for (int i = 0; i < models.length(); i++) {

                    String model_name = models.getJSONObject(i).getString("model_name");
                    JSONArray sounds = models.getJSONObject(i).getJSONArray("sounds");

                    ArrayList<ActionVocal> moSounds = new ArrayList<>();

                    for (int j = 0; j < sounds.length(); j++) {

                        String soundName = sounds.getJSONObject(j).getString("sound");

                        if(soundName.equals(SVMmodel.NOISE_NAME)) {

                            ActionVocal Noise = new ActionVocal("Noise");
                            moSounds.add(Noise);

                        }
                        else {

                            Action action = mainModel.getAction(soundName);

                            if(action instanceof ActionVocal)
                                moSounds.add((ActionVocal)action);
                            else
                                Log.e(TAG, "inconsistant Json");


                        }

                    }

                    SVMmodel newModel = new SVMmodel(model_name, moSounds);
                    SVMmodels.add(newModel);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return SVMmodels;

    }

    public static void writeSVMmodels(Collection<SVMmodel> values) {

        JSONObject jsonfileM = new JSONObject();
        JSONArray models = new JSONArray();

        try {

            for(SVMmodel m : values) {

                JSONObject newSvmModel = new JSONObject();

                newSvmModel.put("model_name", m.getName());

                ArrayList<String> vocalActionsNames = new ArrayList<>();

                for(ActionVocal vocalAction : m.getSounds()) {

                    if (vocalAction != null)
                        vocalActionsNames.add(vocalAction.getName());

                }

                JSONArray sounds = new JSONArray();

                for(String vocalActionName : vocalActionsNames) {

                    JSONObject newSound = new JSONObject();
                    newSound.put("sound", vocalActionName);
                    sounds.put(newSound);

                }

                newSvmModel.put("sounds", sounds);
                models.put(newSvmModel);
            }

            jsonfileM.put("models", models);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("models", jsonfileM);

    }

    //______________________________________________________________________________________________________________________________
    /** METODI PER LEGGERE E SCRIVERE CONFIGURAZIONI DA E PER FILE configurations.json*/

    public ArrayList<Configuration> getConfigurationsFromJson() {

        JSONObject jsonfileC = this.readJsonFile(FILE_CONFIGURATIONS);
        JSONArray configurationsJa;

        ArrayList<Configuration> configurations = new ArrayList<>();

        try {

            if(jsonfileC != null) {

                configurationsJa = jsonfileC.getJSONArray("configurations");

                for (int i = 0; i < configurationsJa.length(); i++) {

                    Configuration newConf;
                    String confName = configurationsJa.getJSONObject(i).getString("conf_name");
                    String gamePackage = configurationsJa.getJSONObject(i).getString("game_package");
                    boolean selected = Boolean.parseBoolean(configurationsJa.getJSONObject(i).getString("selected"));

                    Game game = MainModel.getInstance().getGameFromBundleId(gamePackage);
                    //TODO PROBABILMENTE QUESTA PARTE ANDRA' SPOSTATA SOTTO (ALL'INTERNO DELLE SCHERMATE) QUANDO CI SARA'
                    // UN SISTEMA DI RICONOSCIMENTO AUTOMATICO DELLE SCHERMATE
                    if (configurationsJa.getJSONObject(i).has("model_name")) {

                        String coModelName = configurationsJa.getJSONObject(i).getString("model_name");
                        SVMmodel svmModel = MainModel.getInstance().getSVMmodel(coModelName);
                        newConf = new Configuration(confName, game, svmModel);

                    }
                    else {
                        newConf = new Configuration(confName, game);
                    }

                    if(selected)
                        newConf.setSelected();
                    else
                        newConf.setUnselected();

                    //configurations.add(newConf);
                    JSONArray screensJa = configurationsJa.getJSONObject(i).getJSONArray("screens");

                    for (int j = 0; j < screensJa.length(); j++) {

                        String screenName = screensJa.getJSONObject(j).getString("screen_name");
                        String screenImage = screensJa.getJSONObject(j).getString("screen_image");
                        boolean portrait = screensJa.getJSONObject(j).getBoolean("portrait");

                        JSONArray links = screensJa.getJSONObject(j).getJSONArray("links");
                        ArrayList<Link> coLinks = new ArrayList<>();

                        for (int k = 0; k < links.length(); k++) {

                            String eventName = links.getJSONObject(k).getString("event_name");
                            String actionName = null;
                            int markerColor = 0;
                            int marker = 0;

                            String actionStopName = "";
                            double duration = 0.0;

                            if (links.getJSONObject(k).has("action_name"))
                                actionName = links.getJSONObject(k).getString("action_name");
                            if (links.getJSONObject(k).has("marker_color"))
                                markerColor = links.getJSONObject(k).getInt("marker_color");
                            if (links.getJSONObject(k).has("marker_color"))
                                marker = links.getJSONObject(k).getInt("marker_size");
                            if (links.getJSONObject(k).has("action_stop_name"))
                                actionStopName = links.getJSONObject(k).getString("action_stop_name");
                            if (links.getJSONObject(k).has("duration"))
                                duration = links.getJSONObject(k).getDouble("duration");

                            Link newLink = new Link();

                            if(actionName != null) {

                                Event event = new Event();
                                if(MainModel.getInstance().getGameFromBundleId(gamePackage) != null) {
                                    event = MainModel.getInstance().getGameFromBundleId(gamePackage).getEvent(eventName);
                                }

                                Action action = MainModel.getInstance().getAction(actionName);
                                newLink = new Link(event, action, markerColor, marker);
                            }
                            else {

                                Event event;
                                if(MainModel.getInstance().getGameFromBundleId(gamePackage) != null) {
                                    event = MainModel.getInstance().getGameFromBundleId(gamePackage).getEvent(eventName);
                                    newLink = new Link(event, markerColor, marker);
                                }


                            }

                            //se duration o actionStopName ci sono li aggiungo al link
                            if (duration != 0.0)
                                newLink.setDuration(duration);
                            if (!actionStopName.equals("")) {
                                Action actionStop = MainModel.getInstance().getAction(actionStopName);
                                newLink.setActionStop(actionStop);
                            }

                            coLinks.add(newLink);

                        }

                        Screen screen = new Screen(screenName, screenImage, portrait, newConf, coLinks);
                        MainModel.getInstance().setScreenForConf(screen,newConf);

                    }

                    configurations.add(newConf);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return configurations;
    }

    public static void writeConfigurations(ArrayList<Configuration> conf) {

        JSONObject jsonfileC = new JSONObject();
        JSONArray configurations = new JSONArray();

        try{

            for(Configuration c : conf) {

                JSONObject newConf = new JSONObject();

                newConf.put("conf_name", c.getConfName());
                if(c.getGame() != null)
                    newConf.put("game_package", c.getGame().getBundleId());

                newConf.put("selected", c.getSelected());

                if(c.getModel() != null) {
                    newConf.put("model_name", c.getModel().getName());
                }

                ArrayList<Screen> screens = MainModel.getInstance().getScreensFromConf(c);
                JSONArray JaScreenshots = new JSONArray();

                for(Screen s : screens) {

                    JSONObject newScreen = new JSONObject();

                    if(s.getName() != null)
                        newScreen.put("screen_name", s.getName());

                    if(s.getImage() != null)
                        newScreen.put("screen_image", s.getImage());

                    newScreen.put("portrait", s.isPortrait());

                    //METTO I LINK QUA DENTRO COSì PER OGNI SCHERMATA HO I LINK DEFINITI SU ESSA
                    ArrayList<Link> links = s.getLinks();
                    JSONArray JaLinks = new JSONArray();

                    if(links != null) {

                        for(Link l : links) {

                            JSONObject newLink = new JSONObject();

                            if(l.getEvent() != null)
                                newLink.put("event_name", l.getEvent().getName());

                            if(l.getAction() != null)
                                newLink.put("action_name", l.getAction().getName());

                            newLink.put("marker_color", l.getMarkerColor());
                            newLink.put("marker_size", l.getMarkerSize());

                            if(l.getActionStop() != null)
                                newLink.put("action_stop_name", l.getActionStop().getName());

                            if (l.getDuration() > 0)
                                newLink.put("duration", l.getDuration());

                            JaLinks.put(newLink);

                        }


                    }

                    newScreen.put("links", JaLinks);
                    JaScreenshots.put(newScreen);

                }

                newConf.put("screens", JaScreenshots);

                configurations.put(newConf);

            }

            jsonfileC.put("configurations", configurations);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonManager.writeJson("configurations", jsonfileC);

    }

    //______________________________________________________________________________________________________________________________
    /** METODI GENERALI PER GESTIRE I JSON*/

    //METODO PER LEGGERE UN FILE JSON, CI PASSO DENTRO LA STRINGA CON IL TIPO DI FILE CHE VOGLIO LEGGERE (ES.
    //FILE_CONFIGURATIONS
    private JSONObject readJsonFile(String file) {

        JSONObject jObject = null;
        String text = "";

        //Make an InputStream with your File in the constructor
        try {

            File jsonFile = new File(JSON_PATH, file);

            InputStream inputStream = new FileInputStream(jsonFile);
            StringBuilder stringBuilder = new StringBuilder();

            if (inputStream != null) {

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                //Use a while loop to append the lines from the Buffered reader
                while ((receiveString = bufferedReader.readLine()) != null){
                    stringBuilder.append(receiveString);
                }
                //Close your InputStream and save stringBuilder as a String
                inputStream.close();
                text = stringBuilder.toString();

                jObject = new JSONObject(new JSONTokener(text));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        Log.d("READ_CONFIGURATIONS",""+text);

        return jObject;
    }

    //METODO CHE RICEVE COME PARAMETRO IL JSONOBJECT DA SALVARE E IL FILE IN CUI SALVARE IL JSONOBJECT
    private static void writeJson(String fileName, JSONObject json) {

        String file = fileName+".json";
        File jsonFile = new File(JSON_PATH, file);

        String prettyJson = JsonUtil.toPrettyFormat(json.toString());

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFile));
            bw.write(prettyJson);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //CLASSE CON METODO PER SCRIVERE IN MANIERA CORRETTA I FILE JSON
    public static class JsonUtil {

        public static String toPrettyFormat(String jsonString) {

            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonString).getAsJsonObject();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            return gson.toJson(json);

        }
    }

}
