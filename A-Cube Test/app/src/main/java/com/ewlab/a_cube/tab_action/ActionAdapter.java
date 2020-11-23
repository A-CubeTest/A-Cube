package com.ewlab.a_cube.tab_action;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.MainModel;

public class ActionAdapter extends RecyclerView.Adapter<ActionViewHolder> {

    private final MainActivity mainActivity;
    private LayoutInflater actionInflater;

    public ActionAdapter(MainActivity mainActivity) {
        this.actionInflater = LayoutInflater.from(mainActivity);
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = actionInflater.inflate(R.layout.single_action_view, parent, false);
        return new ActionViewHolder(view, mainActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {

        Action action = MainModel.getInstance().getActions().get(position);
        if(action instanceof ActionButton) {
            Log.d("ACTION_TYPE","AZIONE BUTTON, posizione: "+ position);
            holder.setCellActionButton(action.getName());
        }
        else if(action instanceof ActionVocal) {
            Log.d("ACTION_TYPE","AZIONE VOCALE, posizione: "+ position);
            holder.setCellActionVocal(action.getName());
        }
        else
            Log.d("ACTION_TYPE","NON LO SO");

    }

    @Override
    public int getItemCount() {
        return MainModel.getInstance().getActions().size();
    }
}
