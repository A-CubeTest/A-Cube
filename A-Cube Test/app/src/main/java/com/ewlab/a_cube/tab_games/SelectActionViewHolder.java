package com.ewlab.a_cube.tab_games;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;

public class SelectActionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

    private final NewLink newLink;
    private ImageView actionType_iv;
    private TextView actionName_tv, actionNotAvailable_tv;

    public SelectActionViewHolder(@NonNull View itemView, NewLink newLink) {

        super(itemView);

        actionName_tv = itemView.findViewById(R.id.actionName_tv);
        actionType_iv = itemView.findViewById(R.id.actionType_iv);
        actionNotAvailable_tv = itemView.findViewById(R.id.actionNotAvailable_tv);

        itemView.setOnClickListener(this);

        this.newLink = newLink;
    }

    public void setCellActionButton(String nome) {

        actionName_tv.setText(nome);
        actionType_iv.setImageDrawable(newLink.getResources().getDrawable(R.drawable.ic_buttons_white_24dp));
        actionNotAvailable_tv.setText(" ");
        actionNotAvailable_tv.setVisibility(View.GONE);

    }

    public void setCellActionVocal(String nome) {

        actionName_tv.setText(nome);
        actionType_iv.setImageDrawable(newLink.getResources().getDrawable(R.drawable.ic_mic_white_24dp));
        actionNotAvailable_tv.setText(" ");
        actionNotAvailable_tv.setVisibility(View.GONE);

    }

    public void setCellActionButtonNotAvailable(String nome, String nomeScreen) {

        actionName_tv.setText(nome);
        actionType_iv.setImageDrawable(newLink.getResources().getDrawable(R.drawable.ic_buttons_white_semitransparent_24dp));
        actionNotAvailable_tv.setText(newLink.getString(R.string.actionNotAvailable,nomeScreen));

        int color = newLink.getApplicationContext().getResources().getColor(R.color.white_semitransparent);
        actionName_tv.setTextColor(color);
        actionNotAvailable_tv.setTextColor(color);


    }

    public void setCellActionVocalNotAvailable(String nome, String nomeScreen) {

        actionName_tv.setText(nome);
        actionType_iv.setImageDrawable(newLink.getResources().getDrawable(R.drawable.ic_mic_white_semitransparent_24dp));
        actionNotAvailable_tv.setText(newLink.getString(R.string.actionNotAvailable,nomeScreen));

        int color = newLink.getApplicationContext().getResources().getColor(R.color.white_semitransparent);
        actionName_tv.setTextColor(color);
        actionNotAvailable_tv.setTextColor(color);

    }

    @Override
    public void onClick(View v) {

        String actionName = actionName_tv.getText().toString();
        Action action = MainModel.getInstance().getAction(actionName);

        if(action instanceof ActionVocal) {

            if(FragmentSelectAction.fromBtn.equals("searchAction")) {

                newLink.searchAction.setText(actionName);
                newLink.searchAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_white_24dp,0,0,0);
            }
            else if(FragmentSelectAction.fromBtn.equals("searchActionStop")) {

                newLink.searchActionStop.setText(actionName);
                newLink.searchActionStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_white_24dp,0,0,0);

            }

        }
        else if (action instanceof ActionButton) {

            if(FragmentSelectAction.fromBtn.equals("searchAction")) {

                newLink.searchAction.setText(actionName);
                newLink.searchAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_buttons_white_24dp,0,0,0);

            }
            else if(FragmentSelectAction.fromBtn.equals("searchActionStop")) {

                newLink.searchActionStop.setText(actionName);
                newLink.searchActionStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_buttons_white_24dp,0,0,0);

            }

        }

        //RIMUOVO IL FRAGMENT
        newLink.closeFragmentActions();

    }

    public ArrayList<Action> getAvailableActions(ArrayList<Action> allActions, ArrayList<Action> linkedActions) {

        ArrayList<Action> availableActions = new ArrayList<>();

        for(Action action : allActions)
            if(linkedActions.indexOf(action) == -1)
                availableActions.add(action);

        return availableActions;
    }
}
