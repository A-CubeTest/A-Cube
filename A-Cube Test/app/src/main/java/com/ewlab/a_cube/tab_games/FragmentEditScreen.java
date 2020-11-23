package com.ewlab.a_cube.tab_games;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FragmentEditScreen extends Fragment {

    private static final int RESULT_LOAD_IMAGE = 1;

    EditText screenName_et;
    ImageView screenImage_iv;
    TextView screenImageTitle_tv;
    Button change_btn, cancel_btn, save_btn;

    public String screenName, screenImage;
    public boolean portrait;
    public static int screen_index;

    ArrayList<Screen> screens;
    Screen screen;
    Configuration configuration;

    public FragmentEditScreen(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFE_CYCLE","OnCreateView FragmentEditScreen");
        View v = inflater.inflate(R.layout.fragment_edit_screen, container, false);

        screenName_et = v.findViewById(R.id.screenName_et);
        screenImage_iv = v.findViewById(R.id.screenImage_iv);
        screenImageTitle_tv = v.findViewById(R.id.screenImageTitle_tv);
        //GESTISCO I BOTTONI E AGGIUNGO LISTENER
        change_btn = v.findViewById(R.id.changeImage_btn);
        cancel_btn = v.findViewById(R.id.cancel_btn);
        save_btn = v.findViewById(R.id.save_btn);

        change_btn.setOnClickListener(buttonListener);
        cancel_btn.setOnClickListener(buttonListener);
        save_btn.setOnClickListener(buttonListener);

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            screenName = bundle.getString("screenName");
            screenName_et.setHint(screenName);

            screenImage = bundle.getString("img");
            screenImage_iv.setImageBitmap(MainModel.getInstance().stringToBitMap(screenImage));

            portrait = bundle.getBoolean("portrait");
            if(!portrait)
                setBitmapPosition(MainModel.getInstance().stringToBitMap(screenImage));

        }

        configuration = MainModel.getInstance().getConfiguration(ScreenActivity.game_name, ScreenActivity.conf_name);
        screens = MainModel.getInstance().getScreensFromConf(configuration);
        screen = MainModel.getInstance().getScreenFromConf(configuration,screenName);

        return v;
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            assert getFragmentManager() != null;
            Fragment fragment = getFragmentManager().findFragmentByTag("fragmentEditScreen");
            FragmentTransaction transaction2 = getFragmentManager().beginTransaction();

            switch(view.getId()) {

                case R.id.changeImage_btn:

                    screenName = screenName_et.getText().toString().equals("") ?
                            screenName_et.getHint().toString() : screenName_et.getText().toString();

                    Intent intentImg = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentImg, RESULT_LOAD_IMAGE);

                    break;

                case R.id.cancel_btn:

                    transaction2.remove(fragment).commit();

                    break;

                case R.id.save_btn:

                    //OPPURE POSSO CONTROLLARE SE è UNO SCREEN ESISTENTE CON screen_index != 0
                    int problemCounter = 0;
                    //SE screen.getName() != null VUOL DIRE CHE STO MODIFICANDO UNO SCREEN ESISTENTE
                    if(screen.getName() != null) {

                        screenName = screenName_et.getText().toString().equals("") ?
                                screenName_et.getHint().toString() : screenName_et.getText().toString();

                        //PRIMA DI MODIFICARE LO SCREEN CONTROLLO CHE NON VOGLIO SETTARE LA STESSA IMG O NOME DI UNO SCREEN GIà ESISTENTE
                        for(Screen s : screens) {
                            //ESCLUDO DAL CICLO LO SCREEN CHE STO MODIFICANDO
                            if(s != screen) {

                                if(screenName.equals(s.getName()))
                                    problemCounter++;
                                if(screenImage.equals(s.getImage()))
                                    problemCounter++;

                            }
                        }
                        //SE NON CI SONO PROBLEMI MODIFICO LO SCREEN IN QUESTIONE
                        if(problemCounter == 0) {

                            screen.setName(screenName);
                            screen.setImage(screenImage);
                            screen.setPortrait(portrait);
                            screen.setConfiguration(configuration);

                            //AGGIORNO DIRETTAMENTE DA QUI LE VARIABILI NELLA ScreenActivity
                            ScreenActivity.screenName = screenName;
                            ScreenActivity.img = screenImage;
                            ScreenActivity.portrait = portrait;
                            //SCRIVO I DATI NEL json
                            MainModel.getInstance().writeConfigurationsJson();
                            //CONTROLLO ORIENTAMENTO IMMAGINE E POI TORNO NELLA SCREEN ACTIVITY
                            if(!portrait)
                                ((ScreenActivity) getActivity()).changeViewsForExistingLandscapeScreen();
                            else
                                ((ScreenActivity) getActivity()).changeViewsForExistingPortraitScreen();

                            transaction2.remove(fragment).commit();

                        }
                        else {
                            Toast.makeText(getContext(), "Errore nel salvataggio dello screen, controlla che img" +
                                    "e nome non siano già utilizzati in un altro screen", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else  { //ALTRIMENTI STO AGGIUNGENDO UN NUOVO SCREEN

                        screenName = screenName_et.getText().toString().equals("") ?
                                screenName_et.getHint().toString() : screenName_et.getText().toString();

                        //PRIMA DI AGGIUNGERE LO SCREEN CONTROLLO CHE NON VOGLIO SETTARE LA STESSA IMG O NOME DI UNO SCREEN GIà ESISTENTE
                        for(Screen s : screens) {

                            if(screenName.equals(s.getName()))
                                problemCounter++;
                            if(screenImage.equals(s.getImage()))
                                problemCounter++;

                        }
                        //SE NON CI SONO PROBLEMI AGGIUNGO LO SCREEN IN QUESTIONE
                        if(problemCounter == 0) {

                            screen = new Screen(screenName,screenImage,portrait,configuration);
                            //AGGIORNO DIRETTAMENTE DA QUI LE VARIABILI NELLA ScreenActivity
                            ScreenActivity.screenName = screenName;
                            ScreenActivity.img = screenImage;
                            ScreenActivity.portrait = portrait;
                            //SETTO LO SCREEN PER QUESTA CONF, SCRIVO I DATI NEL json
                            MainModel.getInstance().setScreenForConf(screen, configuration);
                            MainModel.getInstance().writeConfigurationsJson();
                            //CONTROLLO ORIENTAMENTO IMMAGINE E POI TORNO NELLA SCREEN ACTIVITY
                            if(!portrait)
                                ((ScreenActivity) getActivity()).changeViewsForExistingLandscapeScreen();
                            else
                                ((ScreenActivity) getActivity()).changeViewsForExistingPortraitScreen();

                            transaction2.remove(fragment).commit();
                        }
                        else {
                            Toast.makeText(getContext(), "Errore nel salvataggio dello screen, controlla che img" +
                                    " e nome non siano già utilizzati in un altro screen", Toast.LENGTH_SHORT).show();
                        }
                        
                    }

                    break;

            }

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Bitmap myBitmap = null;
        int orientation = -1;
        Matrix matrix = new Matrix();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            String a = String.valueOf(selectedImage);

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            try {
                orientation = new ExifInterface(new File(cursor.getString(cursor.getColumnIndex(filePathColumn[0]))).getAbsolutePath())
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:

                        matrix.postRotate(90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:

                        matrix.postRotate(180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:

                        matrix.postRotate(270);
                        break;

                    default:
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            String filePath = cursor.getString(columnIndex);
            myBitmap = BitmapFactory.decodeFile(filePath);
            cursor.close();

            if (myBitmap != null) {

                final String img = MainModel.getInstance().bitmapToString(myBitmap);

                screenImage = img;
                screenImage_iv.setImageBitmap(MainModel.getInstance().stringToBitMap(screenImage));

                Toast.makeText(getContext(),"Immagine caricata correttamente",Toast.LENGTH_LONG).show();

            }
            else {

                Toast.makeText(getContext(),"Errore nel caricare l'immagine",Toast.LENGTH_LONG).show();

            }

        }
    }

    public void setBitmapPosition(Bitmap bm) {

        if (bm.getWidth() > bm.getHeight()) {

            Log.d("POSIZIONE_BITMAP", "più larga che alta " + bm.getWidth() + " " + bm.getHeight());

            screenImage_iv.getLayoutParams().width = screenImage_iv.getLayoutParams().height;
            screenImage_iv.getLayoutParams().height = screenImage_iv.getLayoutParams().width;

        }

    }
}
