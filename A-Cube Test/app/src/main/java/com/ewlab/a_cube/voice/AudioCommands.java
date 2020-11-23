/*
AudioCommands: this class allows to populate the arraylist; the items, which are put into it, are shown in the listview.
 */

package com.ewlab.a_cube.voice;

import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.ActionVocal;

import java.util.ArrayList;
import java.util.List;

public class AudioCommands extends ArrayList<String> {

    public AudioCommands(String label) {

        List<ActionVocal> vocalActions = MainModel.getInstance().getVocalActions();

        for(ActionVocal ac : vocalActions ) {

            if(ac.getName().equals(label))
                this.addAll(ac.getFiles());

        }
    }
}
