package com.ewlab.a_cube.model;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SVMmodel {

    private String modelName = "";
    private ArrayList<ActionVocal> sounds = null;
    public static final String NOISE_NAME = "Noise";
    

    public SVMmodel(){
        sounds = new ArrayList<>();
    }

    public SVMmodel (String model_name, ArrayList<ActionVocal> sounds) {

        this.modelName = model_name;
        this.sounds = new ArrayList<>();
        this.sounds = sounds;

    }

    public String getName() { return modelName; }

    public ArrayList<ActionVocal> getSounds(){ return sounds;}

    public void setName(String model_name){
        this.modelName = model_name;
    }

    public boolean containsSound(String sound){

        for(ActionVocal vocal : this.getSounds()){
            if(vocal.getName().equals(sound)){
                return true;
            }
        }

        return false;
    }

    /**
     * This method returns true if the model includes all and only the vocal actions given as a parameter
     * @param noiseIncluded a Boolean value that defines whether the actionItemVocal list contains the noise value or not
     * @param actionItemVocals the list of ActionVocal to check, excluded the "noise" sound
     * @return true if this SVMmodel contains all (and only) the sounds passed as argument
     */
    boolean containsTheSounds(boolean noiseIncluded, List<ActionVocal> actionItemVocals) {

        if(noiseIncluded){

            if (sounds.size() != actionItemVocals.size())
                return false;

        }
        else {

            if (sounds.size() != actionItemVocals.size()+1) return false;

        }

        for (ActionVocal item : actionItemVocals) {
            if (!sounds.contains(item)) return false;
        }

        return true;
    }

    /**
     * This method deletes the svmModel file associated with this svmModel
     * @return true if the deletion was successful
     */
    boolean prepareForDelete(){
        boolean deleted = false;
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Models/"+ modelName;
        File fdelete = new File(filePath);

        deleted = fdelete.delete();

        return deleted;
    }


    public boolean equals(Object other){
        if(!(other instanceof SVMmodel)){
            return false;
        }

        SVMmodel otherSVMmodel = (SVMmodel) other;
        return this.getName().equals(otherSVMmodel.getName());
    }

    @Override
    public String toString() {
        return "SVMmodel{" +
                "modelName='" + modelName + '\'' +
                ", sounds=" + sounds +
                '}';
    }
}