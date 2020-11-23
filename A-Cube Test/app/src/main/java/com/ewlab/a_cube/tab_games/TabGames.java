/*
TABGAMES: fragment to show the informatios about games
 */

package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.tab_action.ActionAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabGames extends Fragment  {

  public static GamesAdapter gamesAdapter;
  RecyclerView recyclerView_games;

  ImageView image_game;
  ImageButton delete_game;
  TextView name_game, empty_games_tv;

  private ListView listview;

  View view;

  public TabGames() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d("LIFE_CYCLE","OnCreateView TabGames");
    // Inflate the layout for this fragment
    view = inflater.inflate(R.layout.fragment_tab_games, container, false);

    empty_games_tv = view.findViewById(R.id.empty_games_tv);

    ArrayList<Game> games = MainModel.getInstance().getGames();

    if(games.size() == 0)
      empty_games_tv.setVisibility(View.VISIBLE);
    else
      empty_games_tv.setVisibility(View.GONE);

    //TODO RecycleView games
    image_game = view.findViewById(R.id.image_game);
    name_game = view.findViewById(R.id.name_game);
    delete_game = view.findViewById(R.id.delete_game);
    //delete_game.setOnClickListener(delete_gameListener);

    recyclerView_games = view.findViewById(R.id.recycle_view_game);

    if(recyclerView_games != null) {

      recyclerView_games.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
              false));
      gamesAdapter = new GamesAdapter((MainActivity) this.getActivity());
      recyclerView_games.setAdapter(gamesAdapter);
      gamesAdapter.notifyDataSetChanged();

    }


  /*
    if (games.size() > 0) {
      view = inflater.inflate(R.layout.fragment_tab_games, container, false);
      listview = view.findViewById(R.id.tab_games_listview);
      setListView(games);

      final Intent confList = new Intent(view.getContext(), ConfigurationList.class);

      listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
          Game gi = (Game)listview.getItemAtPosition(i);
          confList.putExtra("title",gi.getTitle());
          startActivity(confList);
        }

      });

      listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

          Game gameToRemove = (Game)listview.getItemAtPosition(position);
          FragmentManager fm = getFragmentManager();
          DialogGame alertDialog = new DialogGame();
          alertDialog.show(fm, "fragment_alert");
          alertDialog.getData(gameToRemove.getTitle());

          //TODO ESEMPIO DI CREAZIONE DI UN ALERT DIALOG SENZA ANDARE IN UNA NUOVA CLASSE
          /*
          AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
          LayoutInflater inflater = getActivity().getLayoutInflater();
          View dialogView = inflater.inflate(R.layout.game_title_template, null);
          dialogBuilder.setView(dialogView);
          AlertDialog alertDialog = dialogBuilder.create();
          alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
          alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
          alertDialog.show();

          Và poi messo qui un listener per gestire il tap sui bottoni dell'alert dialog


          return true;

        }
      });



    }
    else{

      view = inflater.inflate(R.layout.empty_listview_games, container, false);
    }
        */

    FloatingActionButton fab = view.findViewById(R.id.newGame);

    final Intent addGame = new Intent(view.getContext(), GameList.class);

    fab.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        startActivity(addGame);
      }

    });

    return view;

  }
/*
  private void setListView(ArrayList<Game> games){
    gamesAdapter = new GamesAdapter(view.getContext(),android.R.layout.list_content, games);

    listview.setAdapter(gamesAdapter);
  }


//Listener che al click del pulsante elimina di fianco ad un gioco lo elimina
public View.OnClickListener delete_gameListener = new View.OnClickListener() {

  @Override
  public void onClick(View v) {

    Game gameToRemove = MainModel.getInstance().getGames().get();
    final String game_name = gameToRemove.getTitle();

    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity.getApplicationContext());
    LayoutInflater inflater = mainActivity.getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.alert_dialog_remove_game, null);

    Button delete = dialogView.findViewById(R.id.deleteGame_btn);
    Button back = dialogView.findViewById(R.id.backGame_btn);

    dialogBuilder.setView(dialogView);
    final AlertDialog alertDialog = dialogBuilder.create();

    if(alertDialog.getWindow() != null)
      Log.d("ALERT_DIALOG","no null");
    else
      Log.d("ALERT_DIALOG","null");

    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    alertDialog.show();

    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MainModel.getInstance().removeGame(game_name);
        MainModel.getInstance().writeConfigurationsJson();
        MainModel.getInstance().writeGamesJson();
        alertDialog.dismiss();
        TabGames.gamesAdapter.notifyDataSetChanged();
      }
    });

  }
};

 */

}
