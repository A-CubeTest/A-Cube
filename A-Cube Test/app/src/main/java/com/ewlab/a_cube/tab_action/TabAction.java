/*
TABACTION: fragment to show the informatios about actions
 */

package com.ewlab.a_cube.tab_action;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.external_devices.NewExternalDevice;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.voice.NewVocalCommand;


/**
 * A simple {@link Fragment} subclass.
 */

public class TabAction extends Fragment {

    private static final String TAG = TabAction.class.getName();

    public static ActionAdapter actionAdapter;
    RecyclerView recyclerView_action;

    ConstraintLayout constraintLayout1;
    ImageView image_action;
    ImageButton delete_action;
    TextView name_action, empty_actions_tv;

    View view;

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
        constraintLayout1 = view.findViewById(R.id.constraintLayout_action1);
        empty_actions_tv = view.findViewById(R.id.empty_actions_tv);

        image_action = view.findViewById(R.id.image_action);
        name_action = view.findViewById(R.id.name_action);
        delete_action = view.findViewById(R.id.delete_action);
        recyclerView_action = view.findViewById(R.id.recycle_view_action);
        recyclerView_action.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false));
        actionAdapter = new ActionAdapter((MainActivity) this.getActivity());
        recyclerView_action.setAdapter(actionAdapter);
        actionAdapter.notifyDataSetChanged();

        if(MainModel.getInstance().getActions().size() == 0) {

            empty_actions_tv.setVisibility(View.VISIBLE);
            recyclerView_action.setVisibility(View.GONE);
        }
        else {

            empty_actions_tv.setVisibility(View.GONE);
            recyclerView_action.setVisibility(View.VISIBLE);
        }

        /*
        List<Action> actions = MainModel.getInstance().getActions();
        if(actions.size()>0){

            setListView((ArrayList<Action>) actions);

            final Intent externalDevice = new Intent(view.getContext(), KeyDetails.class);
            final Intent voice = new Intent(view.getContext(), AudioCommandsList.class);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Action item = (Action)listview.getItemAtPosition(i);

                    if(item instanceof ActionButton) {

                        externalDevice.putExtra("action_name",item.getName());
                        startActivity(externalDevice);

                    }
                    else {

                        voice.putExtra("action_name" ,item.getName());
                        startActivity(voice);

                    }
                }
            });

//            setButtonNoise();

        }else{
            view = inflater.inflate(R.layout.activity_empty_listview_actions, container, false);
        }

         */

        setFloatingActionsButtons();

        return view;
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
    public void setFloatingActionsButtons() {

        final FloatingActionButton fabNewAction = view.findViewById(R.id.newAction);
        final FloatingActionButton fabNewVocal = view.findViewById(R.id.newVocal);
        final FloatingActionButton fabNewButton = view.findViewById(R.id.newButton);


        fabNewVocal.hide();
        fabNewButton.hide();

        fabNewAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(hide) {


                    constraintLayout1.setBackgroundColor(Color.parseColor("#CCFFFFFF"));
                    constraintLayout1.setElevation(7);
                    constraintLayout1.setClickable(true);

                    fabNewVocal.show();
                    fabNewButton.show();
                    fabNewAction.setImageResource(R.drawable.baseline_close_white_24dp);

                    hide = false;

                }
                else {

                    constraintLayout1.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    constraintLayout1.setElevation(0);
                    constraintLayout1.setClickable(false);

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