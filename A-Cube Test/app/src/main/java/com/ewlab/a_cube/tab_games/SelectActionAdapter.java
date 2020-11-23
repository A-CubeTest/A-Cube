package com.ewlab.a_cube.tab_games;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;

public class SelectActionAdapter extends RecyclerView.Adapter<SelectActionViewHolder> {

    private final NewLink newLink;
    private LayoutInflater selectActionInflater;

    public SelectActionAdapter(NewLink newLink) {

        this.selectActionInflater = LayoutInflater.from(newLink);
        this.newLink = newLink;
    }

    @NonNull
    @Override
    public SelectActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = selectActionInflater.inflate(R.layout.single_select_action_view, parent, false);
        return new SelectActionViewHolder(view, newLink);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectActionViewHolder holder, int position) {

        //PRENDO L'AZIONE
        Action action = MainModel.getInstance().getActions().get(position);
        //LISTE DI SUPPORTO PER FARE IL CONTROLLO
        ArrayList<Action> allActions = (ArrayList<Action>) MainModel.getInstance().getActions();
        //ArrayList<Action> linkedActions = NewLink.screen.getActions();
        ArrayList<Action> linkedActions = MainModel.getInstance().getActionsFromConf(NewLink.configuration);
        Log.d("LINKED_ACTIONS","linked actions: "+ linkedActions.size());
        //LISTA CON LE AZIONI CHE NON SONO UTILIZZATE DA ALTRE PARTI
        ArrayList<Action> availableActions = getAvailableActions(allActions,linkedActions);
        //ALLA LISTA DI PRIMA DEVO TOGLIERE NEL CASO IN CUI CI FOSSE L'AZIONE GIà INSERITA NEL CAMPO selectAction O selectActionStop
        Action toRemove;

        if(FragmentSelectAction.fromBtn.equals("searchAction"))
            toRemove = MainModel.getInstance().getAction(newLink.searchActionStop.getText().toString());
        else
            toRemove = MainModel.getInstance().getAction(newLink.searchAction.getText().toString());

        if(toRemove != null)
            availableActions.remove(toRemove);

        if(availableActions.indexOf(action) >= 0) {

            if(action instanceof ActionButton) {

                Log.d("ACTION_TYPE","AZIONE BUTTON");
                holder.setCellActionButton(action.getName());

            }
            else if(action instanceof ActionVocal) {

                Log.d("ACTION_TYPE","AZIONE VOCALE");
                holder.setCellActionVocal(action.getName());
            }
            else
                Log.d("ACTION_TYPE","NON LO SO");

        }
        else {

            String nomeScreen = MainModel.getInstance().getScreenNameWhereActionIsUsed(action,NewLink.configuration);

            if(action instanceof ActionButton) {

                holder.setCellActionButtonNotAvailable(action.getName(),nomeScreen);
                //TOLGO LA POSSIBILITà DI FARE TAP SULL'AZIONE VISTO CHE NON è DISPONIBILE
                holder.itemView.setOnClickListener(null);
            }
            else if(action instanceof ActionVocal) {

                holder.setCellActionVocalNotAvailable(action.getName(),nomeScreen);
                //TOLGO LA POSSIBILITà DI FARE TAP SULL'AZIONE VISTO CHE NON è DISPONIBILE
                holder.itemView.setOnClickListener(null);
            }
            else
                Log.d("ACTION_TYPE","NON LO SO");

        }

    }

    public ArrayList<Action> getAvailableActions(ArrayList<Action> allActions, ArrayList<Action> linkedActions) {

        ArrayList<Action> availableActions = new ArrayList<>();

        for(Action action : allActions)
            if(linkedActions.indexOf(action) == -1)
                availableActions.add(action);

        return availableActions;
    }



    @Override
    public int getItemCount() {
        return MainModel.getInstance().getActions().size();
    }


}
