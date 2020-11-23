package com.ewlab.a_cube.model;

import android.os.Environment;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ActionVocal extends Action {

    private Set<String> fileNames = new HashSet<>();


    public ActionVocal(String name) {
        this.name = name;
    }

    public ActionVocal(String name, Set<String> files) {
        this.name = name;
        this.fileNames = files;
    }

    public Set<String> getFiles() {
        return fileNames;
    }

    public void setFiles(Set<String> files) {
        this.fileNames = files;
        return;
    }

    /**
     * This method returns true if the deletion of the specified file was successful. This method eliminates both the file reference
     * within the action and the file itself in the Download / A-Cube / Sounds folder
     * @param fileName of the file to delete
     * @return true if deleted
     */
    public boolean deleteFile(String fileName){
        boolean deleted = false;

        for (Iterator<String> iterator = fileNames.iterator(); iterator.hasNext();) {
            String s =  iterator.next();
            if (s.equals(fileName)) {
                iterator.remove();

                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Sounds/"+fileName;
                File fdelete = new File(filePath);

                deleted = fdelete.delete();
            }
        }

        return deleted;
    }

    public void addFile(String fileName){
        this.fileNames.add(fileName);
    }

    public void addFiles(Set<String> newFiles){
        fileNames.addAll(newFiles);
    }

    public boolean deleteAllSounds(){
        boolean deleted = false;

        for(String file : fileNames){
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Sounds/"+file;
            File fdelete = new File(filePath);

            deleted = fdelete.delete();

        }

        fileNames.clear();

        return deleted;
    }

}
