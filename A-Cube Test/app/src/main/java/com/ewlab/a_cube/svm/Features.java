package com.ewlab.a_cube.svm;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.ewlab.a_cube.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.mfcc.MFCC;

public class Features {

    //campiono la voce a 44100 Hz (dipende da come ho campionato il segnale quando lo acquisito)
    private int sampleRate;
    //campioni per frame
    private int sampleForFrame; //(1024 -> 23 ms)
    //overlapping dei frame del 50%
    private int bufferOverlap;
    //dimensione di ogni campione in termini di bits
    private int bits;
    //audio mono-channel
    private int channel;
    //numero di features estratte da ogni frame
    private int melCoefficients;
    //numero di filtri da applicare per estrarre le features
    private int melFilterBank;
    //minima frequenza di interesse
    private int lowFilter;
    //massima frequenza di interesse
    private int highFilter;
    //range di valori che puÃ² assumere ogni campione (0-255 -> unsigned; -127-+128 -> signed)
    private boolean signed;
    //modo in cui vengono memorizzati i bits
    private boolean big_endian;
    //dimensione dei vettori
    private int vectorDim;

    private String soundName;
    private Context context;

    public Features(Context cx){
        super();
        context = cx;
        sampleRate = 44100;
        sampleForFrame= 1024;
        bufferOverlap = 512;
        bits = 16;
        channel = 2;
        melCoefficients = 21; //13
        melFilterBank = 32;
        lowFilter = 30;
        highFilter = 3000;
        signed = true;
        big_endian = false;
        vectorDim = 20; //12
    }

    public HashMap<String, HashSet<float[]>> getClassifiedFeatures(TreeMap<String, ArrayList<String>> map){

        final HashMap<String, HashSet<float[]>> classifiedFeatures = new HashMap<>();
        final HashSet<float[]> noiseList = new HashSet<>();
        final ArrayList<float[]> audio_list = new ArrayList<>();

        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Sounds";

            //recupero gli audio
            for(Map.Entry<String, ArrayList<String>> m : map.entrySet()) {
                soundName = m.getKey();

                for (String s : m.getValue()) {
                    File sound = new File(filePath, s);

                    InputStream inStream = new FileInputStream(sound);
                    final AudioDispatcher dispatcher = new AudioDispatcher(new UniversalAudioInputStream(inStream, new TarsosDSPAudioFormat(sampleRate, bits, channel, signed, big_endian)), sampleForFrame, bufferOverlap);//, bufferSize, bufferOverlap);
                    final MFCC mfcc = new MFCC(sampleForFrame, sampleRate, melCoefficients, melFilterBank, lowFilter, highFilter);
                    dispatcher.addAudioProcessor(mfcc);
                    dispatcher.addAudioProcessor(new AudioProcessor() {

                        @Override
                        public boolean process(AudioEvent audioEvent) {
                            float[] audio_float;
                            mfcc.process(audioEvent);
                            audio_float = mfcc.getMFCC();

                            float power = audio_float[0];
                            float[] temp = new float[vectorDim];
                            //rimuovo il primo coefficiente della window perchÃ¨ rappresenta l'RMS (= info sulla potenza della finestra)
                            for (int i = 1, k = 0; i < audio_float.length; i++, k++) {
                                temp[k] = audio_float[i];
                            }

                            if (power <= 0) {

                                if (!classifiedFeatures.containsKey("Noise")) {

                                    noiseList.add(normalize(temp));
                                    classifiedFeatures.put("Noise", noiseList);

                                }
                                else {

                                    HashSet<float[]> treeTemp = classifiedFeatures.get("Noise");
                                    treeTemp.add(normalize(temp));
                                    classifiedFeatures.put("Noise", treeTemp);

                                }

                            }
                            else {

                                if (!classifiedFeatures.containsKey(soundName)) {

                                    HashSet<float[]> soundList = new HashSet<>();
                                    soundList.add(normalize(temp));
                                    classifiedFeatures.put(soundName, soundList);

                                } else {
                                    HashSet<float[]> treeTemp = classifiedFeatures.get(soundName);
                                    treeTemp.add(normalize(temp));
                                    classifiedFeatures.put(soundName, treeTemp);
                                }

                                audio_list.add(audio_float);
                            }

                            return true;
                        }

                        @Override
                        public void processingFinished() {

                        }
                    });
                    dispatcher.run();
                }
            }

            String[] noises = new String[]{"Noise.wav"};
            Context con = null;

            for(String noise : noises){
                InputStream inStream = context.getResources().openRawResource(R.raw.noise);

                //aggiungo rumore classificato per ridurre il numero di false positive
//                File sound = new File(filePath, noise);
//                InputStream inStream = new FileInputStream(sound);//R.raw.noise);
                final AudioDispatcher dispatcher = new AudioDispatcher(new UniversalAudioInputStream(inStream, new TarsosDSPAudioFormat(sampleRate, bits, channel, signed, big_endian)), sampleForFrame, bufferOverlap);//, bufferSize, bufferOverlap);
                final MFCC mfcc = new MFCC(sampleForFrame, sampleRate, melCoefficients, melFilterBank, lowFilter, highFilter);
                dispatcher.addAudioProcessor(mfcc);
                dispatcher.addAudioProcessor(new AudioProcessor() {

                    @Override
                    public boolean process(AudioEvent audioEvent) {
                        float[] audio_float;
                        mfcc.process(audioEvent);
                        audio_float = mfcc.getMFCC();
                        float[] temp = new float[vectorDim];
                        //rimuovo il primo coefficiente della window perchÃ¨ rappresenta l'RMS (= info sulla potenza della finestra)
                        for (int i = 1, k = 0; i < audio_float.length; i++, k++) {
                            temp[k] = audio_float[i];
                        }

                        HashSet<float[]> treeTemp = classifiedFeatures.get("Noise");
                        treeTemp.add(normalize(temp));
                        classifiedFeatures.put("Noise", treeTemp);
                        return true;
                    }

                    @Override
                    public void processingFinished() {

                    }
                });
                dispatcher.run();
            }




        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return classifiedFeatures;

    }

    private float[] normalize(float[] f){
        float[] normArray = new float[f.length];

        float sum = 0;

        for(int i = 0; i < f.length; i++){
            sum += Math.pow(f[i],2);
        }

        for(int i = 0; i < f.length; i++){
            normArray[i] = f[i] / (float) Math.sqrt(sum);
        }

        return normArray;
    }

}
