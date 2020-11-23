package com.ewlab.a_cube.tab_games;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;

public class FragmentSelectAction extends Fragment {

    TextView actionsTitle, actionsEmpty_tv;
    ImageButton cancel_btn;
    RecyclerView recyclerView_actions;
    SelectActionAdapter selectActionAdapter;
    //VARIABILE PER CAPIRE SE HO SELEZIONATO searchAction o searchActionStop
    public static String fromBtn;
    //VARIABILE PER CAPIRE SE HO GIà SELEZIONATO UN'AZIONE IN UN ALTRO CAMPO DELL'EVENTO
    String nameAction, nameActionStop;

    public FragmentSelectAction() {  }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFE_CYCLE", "OnCreateView FragmentEditScreen");
        View v = inflater.inflate(R.layout.fragment_select_action, container, false);

        actionsTitle = v.findViewById(R.id.actionsTitle_tv);
        actionsEmpty_tv = v.findViewById(R.id.actionsEmpty_tv);
        //CONTROLLO LE AZIONI ME MOSTRARE TEXTVIEW DI AVVISO NEL CASO NON CE NE SIANO DISPONIBILI
        ArrayList<Action> allActions = (ArrayList<Action>) MainModel.getInstance().getActions();
        ArrayList<Action> linkedActions = NewLink.screen.getActions();

        if(getAvailableActions(allActions,linkedActions).size()==0)
            actionsEmpty_tv.setVisibility(View.VISIBLE);
        else
            actionsEmpty_tv.setVisibility(View.GONE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            fromBtn = bundle.getString("fromBtn");

            assert fromBtn != null;
            if (fromBtn.equals("searchAction")) {

                actionsTitle.setText("Azioni");
                //Prendo il nome dell'azione già inserita nel campo Seleziona Azione Stop (se già inserita)
                if (bundle.getString("searchActionStop") != null)
                    nameActionStop = bundle.getString("searchActionStop");

                Toast.makeText(getContext(), "Nome azione: "+nameActionStop, Toast.LENGTH_SHORT).show();
            }
            else {

                actionsTitle.setText("Azioni stop");
                //Prendo il nome dell'azione già inserita nel campo Seleziona Azione (se già inserita)
                if (bundle.getString("searchAction") != null)
                    nameAction = bundle.getString("searchAction");

                Toast.makeText(getContext(), "Nome azione: "+nameAction, Toast.LENGTH_SHORT).show();
            }
        }
        //TODO RECYCLER VIEW ACTIONS
        recyclerView_actions = v.findViewById(R.id.recycler_view_actions);
        recyclerView_actions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false));
        selectActionAdapter = new SelectActionAdapter((NewLink) this.getActivity());
        recyclerView_actions.setAdapter(selectActionAdapter);
        selectActionAdapter.notifyDataSetChanged();

        cancel_btn = v.findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RIMUOVO IL FRAGMENT
                Fragment fragment = getFragmentManager().findFragmentByTag("fragmentActions");
                FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
                transaction2.remove(fragment).commit();
            }
        });

        return v;
    }

    public ArrayList<Action> getAvailableActions(ArrayList<Action> allActions, ArrayList<Action> linkedActions) {

        ArrayList<Action> availableActions = new ArrayList<>();

        for(Action action : allActions)
            if(linkedActions.indexOf(action) == -1)
                availableActions.add(action);

        return availableActions;
    }

}



