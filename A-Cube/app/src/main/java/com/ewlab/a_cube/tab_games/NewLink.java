/*
NewLink: this activity allows the user to link the event with an action
 */
package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.SVMmodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

public class NewLink extends AppCompatActivity {

    private static final String TAG = NewLink.class.getName();

    double x;
    double y;

    int markerSize = 50;
    int markerColor = -65536;

    boolean OnOff = false;
    boolean timed = false;

    public boolean portrait;

    String title;
    String nameConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_link);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        seekbar();

        final Context context = getApplicationContext();

        //prendo tutti gli elementi che mi servono da Intent
        title = getIntent().getStringExtra("title");

        this.setTitle(title + " - " + getString(R.string.new_link));

        nameConfig = getIntent().getStringExtra("name");
        final String event = getIntent().getStringExtra("event");


        //se event esiste vuol dire che sto modificando un link preesistente, se event invece è nullo vuol dire che ne stiamo aggiungendo uno nuovo
        final Configuration thisConfig = MainModel.getInstance().getConfiguration(title, nameConfig);
        final Link oldLink = thisConfig.getLink(event);
        final Event oldEvent = oldLink.getEvent();


        //prendo i riferimenti di tutti gli elementi grafici del layout
        final EditText eventName = findViewById(R.id.eventName);
        final EditText eventTypeBox = findViewById(R.id.eventType);
        final Button searchEventTypeButton = findViewById(R.id.searchEventType);
        final ImageView screenshot = findViewById(R.id.screenshot);
        screenshot.setClickable(true);
        final TextView errorCoordinate = findViewById(R.id.ErrorCordinate);
        final ImageView marker = findViewById(R.id.marker);
        SeekBar seek = findViewById(R.id.markerSizeBar);
        LinearLayout colorTest = findViewById(R.id.layoutColorTest);
        colorTest.setClickable(true);
        final EditText actionsBox = findViewById(R.id.actionBox);
        final Button searchActionButton = findViewById(R.id.searchAction);
        final EditText actionStopBox = findViewById(R.id.actionStopBox);
        final Button searchActionStopButton = findViewById(R.id.searchActionStop);
        final EditText durationTime = findViewById(R.id.durationTime);

        FloatingActionButton buttonSave = findViewById(R.id.floatButtonSave);
        //buttonSave.setBackgroundResource(R.drawable.rounded_button_true);

        //inserisco nel popUp menu le tipologie di evento possibili salvati in R.array.spinnerEventType
        searchEventTypeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(NewLink.this, searchEventTypeButton);
                String[] eventType = getResources().getStringArray(R.array.spinnerEventType);
                for (String event : eventType) {
                    popup.getMenu().add(event);
                }

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        eventTypeBox.setText(item.getTitle());
                        return true;
                    }
                });

                popup.show();
            }
        });

        eventTypeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //aggiorno il layout in base alla tipologia di evento selezionata
            @Override
            public void afterTextChanged(Editable s) {
                final String eventType = String.valueOf(eventTypeBox.getText());

                LinearLayout layoutOnOff = findViewById(R.id.LayoutOnOff);
                LinearLayout layoutTimed = findViewById(R.id.layoutTimed);

                if (eventType.equals(Event.LONG_TAP_ON_OFF_TYPE)) {
                    layoutOnOff.setVisibility(View.VISIBLE);
                    layoutTimed.setVisibility(View.GONE);

                    OnOff = true;
                    timed = false;
                }

                if (eventType.equals(Event.LONG_TAP_TIMED_TYPE)) {
                    layoutOnOff.setVisibility(View.GONE);
                    layoutTimed.setVisibility(View.VISIBLE);
                    OnOff = false;
                    timed = true;
                }

                if (!eventType.equals(Event.LONG_TAP_ON_OFF_TYPE) && !eventType.equals(Event.LONG_TAP_TIMED_TYPE)) {
                    layoutOnOff.setVisibility(View.GONE);
                    layoutTimed.setVisibility(View.GONE);
                    OnOff = false;
                    timed = false;
                }
            }
        });

        //inserisco in una lista tutte le azioni selezionabili, cioè quelle non presenti in altri link
        ArrayList<Action> linkedActions = thisConfig.getActions();
        final ArrayList<Action> allActions = (ArrayList<Action>) MainModel.getInstance().getActions();

        if (oldEvent != null) {
            linkedActions.remove(oldLink.getAction());
            this.setTitle(title + " - " + getString(R.string.event) + " - " + event);

        }

        final ArrayList<String> actions = getAvailableActions(allActions, linkedActions);

        //inserisco nel popUp menu le azioni scremate in precedenza
        searchActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(NewLink.this, searchActionButton);
                for (String action : actions) {
                    if (!action.equals(actionStopBox.getText().toString())) {
                        popup.getMenu().add(action);
                    }
                }

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        actionsBox.setText(item.getTitle());
                        return true;
                    }
                });

                popup.show();
            }
        });


        ArrayAdapter<String> actionStopAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, actions);

        //inserisco nel popUp menu delle actionStop le azioni scremate in precedenza
        searchActionStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(NewLink.this, searchActionStopButton);
                for (String action : actions) {
                    Action thisAction = MainModel.getInstance().getAction(action);
                    if (!action.equals(actionsBox.getText().toString())) { //thisAction instanceof ActionButton
                        popup.getMenu().add(action);
                    }
                }

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        actionStopBox.setText(item.getTitle());
                        return true;
                    }
                });

                popup.show();
            }
        });


        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.image_not_found);

        Log.d("dimensioni", getScreenDimension()[0]+" "+getScreenDimension()[1]);

        //se il link che ho selezionato esiste già popolo i campi con i suoi dati
        if (oldEvent != null) {
            Log.d(TAG, "you are modifying an existing link");

            //nome evento
            eventName.setText(oldEvent.getName());

            //tipo evento
            eventTypeBox.setText(oldEvent.getType());


            //coordinate
            x = oldEvent.getX();
            y = oldEvent.getY();

            //screenshot
            bm = MainModel.getInstance().StringToBitMap(oldEvent.getScreenshot());
            setBitmapPosition(bm);

            portrait = oldEvent.getPortrait();

            //grandezza del puntatore
            seek.setProgress(oldLink.getMarkerSize());
            markerSize = oldLink.getMarkerSize();
            setMarker(markerSize);
            marker.setVisibility(View.VISIBLE);


            //colore del puntatore
            markerColor = oldLink.getMarkerColor();
            colorTest.setBackgroundColor(markerColor);
            marker.setImageTintList(ColorStateList.valueOf(markerColor));


            //action adapter
            if (oldLink.getAction() != null) {
                actionsBox.setText(oldLink.getAction().getName());
            }

            //action stop adapter
            if (oldLink.getActionStop() != null) {
                actionStopBox.setText(oldLink.getActionStop().getName());
            }

            if (oldEvent.getType().equals(Event.LONG_TAP_TIMED_TYPE)) {
                durationTime.setText(String.valueOf(oldLink.getDuration()));
            }

            FloatingActionButton deleteLinkButton = findViewById(R.id.floatButtonDeleteLink);
            deleteLinkButton.setVisibility(View.VISIBLE);
            //deleteLinkButton.setBackgroundResource(R.drawable.rounded_button_false);
            deleteLinkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Action action = oldLink.getAction();

                    boolean removed = thisConfig.removeLink(oldLink);

                    //se viene rimosso il link cerco se esiste un svmModel che va bene per la nostra configurazione
                    if (removed) {
                        if (action instanceof ActionVocal) {
                            SVMmodel newModel = MainModel.getInstance().getSVMmodel(thisConfig.getVocalActions());
                            thisConfig.setModel(newModel);
                        }

                        MainModel.getInstance().writeConfigurationsJson();
                        MainModel.getInstance().writeGamesJson();

                        Toast.makeText(getApplicationContext(), R.string.link_deleted, Toast.LENGTH_LONG).show();


                        Intent intent = new Intent(getApplicationContext(), ConfigurationDetail.class);
                        intent.putExtra("title", title);
                        intent.putExtra("name", nameConfig);
                        startActivity(intent);
                    }
                }
            });

        } else {
            String provisionalName = getIntent().getStringExtra("provisionalName");
            String provisionalEventType = getIntent().getStringExtra("provisionalEventType");
            String provisionalAction = getIntent().getStringExtra("provisionalAction");
            String provisionalActionStop = getIntent().getStringExtra("provisionalActionStop");
            String provisionalDurationTime = getIntent().getStringExtra("provisionalDurationTime");

            if (provisionalName != null) {
                eventName.setText(provisionalName);
            }

            if (provisionalEventType != null) {
                eventTypeBox.setText(provisionalEventType);
            }

            if (provisionalAction != null) {
                actionsBox.setText(provisionalAction);
            }

            if (provisionalActionStop != null) {
                actionStopBox.setText(provisionalActionStop);
            }

            if (provisionalDurationTime != null) {
                durationTime.setText(provisionalDurationTime);
            }
        }

        //al tap sull'imageview vado a scegliere l'immagine e le coordinate dove verrà eseguita l'azione
        screenshot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ScreenPosition.class);

                intent.putExtra("title", title);
                intent.putExtra("name", nameConfig);
                intent.putExtra("event", event);

                String eventS = eventName.getText().toString();
                if (eventS.length() > 0) {
                    intent.putExtra("provisionalName", eventS);
                }

                String eventTypeS = eventTypeBox.getText().toString();
                if (eventTypeS.length() > 0) {
                    intent.putExtra("provisionalEventType", eventTypeS);
                }

                String actionS = actionsBox.getText().toString();
                if (actionS.length() > 0) {
                    intent.putExtra("provisionalAction", actionS);
                }

                String actionStopS = actionStopBox.getText().toString();
                if (actionStopS.length() > 0) {
                    intent.putExtra("provisionalActionStop", actionStopS);
                }

                String durationS = durationTime.getText().toString();
                if (durationS.length() > 0) {
                    intent.putExtra("provisionalDurationTime", durationS);
                }

                startActivity(intent);
            }
        });

        //se tramite intent arriva "x" vuol dire che ho selezionato un nuovo screen quindi aggiorno lo screenshot
        if (getIntent().getStringExtra("x") != null) {

            x = Float.parseFloat(getIntent().getStringExtra("x"));
            y = Float.parseFloat(getIntent().getStringExtra("y"));

            String img = getIntent().getStringExtra("img");
            Uri uriImg = Uri.parse(img);

            try {
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImg);
            } catch (IOException e) {
                e.printStackTrace();

            }

            ViewGroup.LayoutParams lpScreen = screenshot.getLayoutParams();

            marker.setVisibility(View.VISIBLE);
            setMarker(markerSize);
            seek.setProgress(markerSize);
            lpScreen.width = bm.getWidth();
            lpScreen.height = bm.getHeight();

            setBitmapPosition(bm);

        }

        colorTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });


        //alla pressione di "Save" verifico se è possibile salvare il nuovo evento e il nuovo link associato ad esso
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String eventS = eventName.getText().toString();
                String eventType = String.valueOf(eventTypeBox.getText());//spinnerEventType.getSelectedItem().toString();
                String actionName = String.valueOf(actionsBox.getText());
                String actionStopName = "";
                double durationT = 0.0;

                if (actionStopBox.getText() != null) {//spinnerActionStop.getSelectedItem() != null) {
                    actionStopName = String.valueOf(actionStopBox.getText()); //spinnerActionStop.getSelectedItem().toString();
                }

                if (durationTime.getText().length() > 0) {
                    durationT = Double.parseDouble(durationTime.getText().toString());
                }


                int problemCounter = 0;

                if (eventS.length() == 0) {
                    eventName.setError(getString(R.string.no_name));
                    problemCounter++;
                }

                if (eventType.length() == 0) {
                    eventTypeBox.setError(getString(R.string.no_event_type));
                    problemCounter++;
                }

                if (x == 0 | y == 0) {
                    errorCoordinate.setVisibility(View.VISIBLE);
                    errorCoordinate.setError(getString(R.string.no_screenshot));
                    problemCounter++;
                }

                if (OnOff && actionStopName.equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.no_stop_action, Toast.LENGTH_SHORT).show();
                    problemCounter++;
                }

                if (timed && durationT <= 0) {
                    durationTime.setError(getString(R.string.no_duration));
                    problemCounter++;
                }

                Log.d(TAG, problemCounter + " problems accurred saving the new link");


                if (problemCounter == 0) {

                    Bitmap bitmap = ((BitmapDrawable) screenshot.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                    byte[] b = baos.toByteArray();
                    String img = Base64.encodeToString(b, Base64.NO_WRAP);

                    Log.d(TAG, "x - y " + x + " " + y);

                    Event newEvent = new Event(eventS, eventType, x, y, img, portrait);
                    Log.d(TAG, "event type " + newEvent.getType());

                    boolean eventSaved = MainModel.getInstance().saveEvent(title, oldEvent, newEvent);

                    if (!eventSaved) {
                        Toast.makeText(getApplicationContext(), R.string.event_exists, Toast.LENGTH_SHORT).show();
                    }


                    Action actionItem = MainModel.getInstance().getAction(actionName);
                    Link newLink = new Link(newEvent, actionItem, markerColor, markerSize);

                    //se l'utente ha scelto modalità on/off e ha definito un'azione-stop la aggiungiamo al link appena salvato
                    if (actionName.length() > 0 && OnOff && actionStopName.length() > 0) {
                        Action actionStop = MainModel.getInstance().getAction(actionStopName);
                        newLink.setActionStop(actionStop);
                    }

                    //se l'utente ha scelto modalità timed e ha definito una durata la aggiungiamo al link appena salvato
                    if (actionName.length() > 0 && timed && durationT > 0) {
                        newLink.setDuration(durationT);
                    }


                    boolean linkSaved = MainModel.getInstance().saveLink(title, nameConfig, oldLink, newLink);

                    if (!linkSaved) {
                        Toast.makeText(getApplicationContext(), R.string.link_exists, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.link_added, Toast.LENGTH_SHORT).show();


                        Intent intent = new Intent(context, ConfigurationDetail.class);
                        intent.putExtra("title", title);
                        intent.putExtra("name", thisConfig.getConfName());
                        startActivity(intent);
                    }
                }
            }
        });
    }

    //this method returns all the actions that are avvailable, without considering action that has been already linked
    // to an event of the game
    private ArrayList<String> getAvailableActions(ArrayList<Action> allActions, ArrayList<Action> linkedActions) {

        ArrayList<String> usableActions = new ArrayList<>();

        for (Action action : allActions) {
            usableActions.add(action.getName());
            for (Action linkedAction : linkedActions) {

                if (action.equals(linkedAction)) {
                    usableActions.remove(action.getName());
                    Log.d("avaiable", action.getName());
                }
            }
        }

        return usableActions;
    }

    //this method manages the seekbar operation
    private void seekbar() {
        SeekBar seekBar = findViewById(R.id.markerSizeBar);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setMarker(progress);

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        setMarker(seekBar.getProgress());
                    }
                }
        );
    }

    //this method set the dimensions and the position of the marker
    public void setMarker(int progress) {
        ImageView marker = findViewById(R.id.marker);

        marker.requestLayout();
        marker.getLayoutParams().height = progress * 4;
        marker.getLayoutParams().width = progress * 4;
        marker.setScaleType(ImageView.ScaleType.FIT_XY);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        marker.setX((int) (width * x) - (progress * 2));
        marker.setY((int) (height * y) - (progress * 2));
        markerSize = progress;
    }

    public void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, markerColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                markerColor = color;
                Log.d(TAG, "colore settato" + color);
                LinearLayout layoutColorTest = findViewById(R.id.layoutColorTest);
                layoutColorTest.setBackgroundColor(color);
                ImageView marker = findViewById(R.id.marker);
                marker.setImageTintList(ColorStateList.valueOf(markerColor));
            }
        });

        colorPicker.show();
    }

    private double[] getScreenDimension() {
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        Point point = new Point (1000, 1000);
        display.getRealSize(point);
        int width = point.x;
        int height = point.y;
        Log.d(TAG, width + " detected width");
        Log.d(TAG, height + " detected height");


        double[] screenInformation = new double[2];
        screenInformation[0] = width;
        screenInformation[1] = height;
        return screenInformation;
    }

    @Override
    public void onBackPressed() {
        Context context = getApplicationContext();

        Intent intent = new Intent(context, ConfigurationDetail.class);
        intent.putExtra("title", title);
        intent.putExtra("name", nameConfig);
        startActivity(intent);
    }

    public void setBitmapPosition(Bitmap bm) {
        ImageView screenshot = findViewById(R.id.screenshot);

        if (bm.getWidth() > bm.getHeight()) {
            Log.d(TAG, "più larga che alta " + bm.getWidth() + " " + bm.getHeight());

            portrait = false;
            Bitmap bOutput;
            float degrees = 90;//rotation degree
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees);
            bOutput = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            screenshot.getLayoutParams().width = bm.getHeight();
            screenshot.getLayoutParams().height = bm.getWidth();
            screenshot.setImageBitmap(bOutput);


        } else {
            Log.d(TAG, "più alta che larga");
            portrait = true;

            screenshot.getLayoutParams().width = bm.getWidth();
            screenshot.getLayoutParams().height = bm.getHeight();

            screenshot.setImageBitmap(bm);

        }
    }

}

