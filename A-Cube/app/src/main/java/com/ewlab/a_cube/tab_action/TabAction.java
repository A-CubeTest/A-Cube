/*
TABACTION: fragment to show the informatios about actions
 */

package com.ewlab.a_cube.tab_action;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.ewlab.a_cube.external_devices.KeyDetails;
import com.ewlab.a_cube.external_devices.NewExternalDevice;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.voice.AudioCommandsList;
import com.ewlab.a_cube.voice.NewVocalCommand;
import com.ewlab.a_cube.voice.VoiceRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */

public class TabAction extends Fragment {

    private static final String TAG = TabAction.class.getName();

    private ActionAdapter adapter;

    private ListView listview;

    private View view;

    boolean hide = true;

    public TabAction() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tab_action, container, false);
        listview = view.findViewById(R.id.listView);

        List<Action> actions = MainModel.getInstance().getActions();
        if(actions.size()>0){

            setListView((ArrayList<Action>) actions);

            final Intent externalDevice = new Intent(view.getContext(), KeyDetails.class);
            final Intent voice = new Intent(view.getContext(), AudioCommandsList.class);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Action item = (Action)listview.getItemAtPosition(i);

                    if(item instanceof ActionButton){
                        externalDevice.putExtra("action_name",item.getName());
                        startActivity(externalDevice);

                    }else{
                        voice.putExtra("action_name" ,item.getName());
                        startActivity(voice);
                    }
                }
            });

//            setButtonNoise();

        }else{
            view = inflater.inflate(R.layout.activity_empty_listview_actions, container, false);
        }

        setFloatingActionsButtons();


        return view;
    }


    public void setListView(ArrayList<Action> actions){

        adapter = new ActionAdapter(view.getContext(),android.R.layout.list_content, actions);

        listview.setAdapter(adapter);
    }

//    //manages the operations and the visualization of the buttonNoise
//    public void setButtonNoise(){
//
//        Button noiseButton = view.findViewById(R.id.noiseButton);
//
//        List<ActionVocal> vocals = MainModel.getInstance().getVocalActions();
//        Log.d(TAG, " "+vocals.size());
//
//        if(vocals.size()>0){
//
//            boolean noiseExists = false;
//
//            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Sounds/noise.wav";
//            File noise = new File (filePath);
//            if(noise.exists()){
//                noiseExists = true;
//                Log.d(TAG, "noise sound exists");
//            }
//
//            if(noiseExists){
//                noiseButton.setBackgroundResource(R.drawable.rounded_button_true);
//                noiseButton.setVisibility(View.VISIBLE);
//
//
//            }else{
//                noiseButton.setBackgroundResource(R.drawable.rounded_button_false);
//                noiseButton.setVisibility(View.VISIBLE);
//            }
//
//        }
//
//        noiseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), VoiceRecorder.class);
//                intent.putExtra("noiseRecording", "true");
//                startActivity(intent);
//            }
//        });
//
//
//    }

    //manages the operations and the visualization of the buttons addNewAction, addNewVocal and addNewButton
    public void setFloatingActionsButtons(){
        final FloatingActionButton fabNewAction = view.findViewById(R.id.newAction);
        final FloatingActionButton fabNewVocal = view.findViewById(R.id.newVocal);
        final FloatingActionButton fabNewButton = view.findViewById(R.id.newButton);

        fabNewVocal.hide();
        fabNewButton.hide();

        fabNewAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hide) {
                    fabNewVocal.show();
                    fabNewButton.show();
                    fabNewAction.setImageResource(R.drawable.baseline_close_white_24dp);
                    hide = false;

                }else{
                    fabNewVocal.hide();
                    fabNewButton.hide();
                    fabNewAction.setImageResource(R.drawable.ic_add_black_24dp);
                    hide = true;
                }
            }
        });

        fabNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addButtonAction = new Intent(view.getContext(), NewExternalDevice.class);
                startActivity(addButtonAction);
            }
        });

        fabNewVocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addVocalAction = new Intent(view.getContext(), NewVocalCommand.class);
                startActivity(addVocalAction);
            }
        });

    }
}