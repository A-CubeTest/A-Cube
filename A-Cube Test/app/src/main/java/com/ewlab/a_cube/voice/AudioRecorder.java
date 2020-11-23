/*
AudioRecorder: asynctask to record the audio and save it as wav file
 */

package com.ewlab.a_cube.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.SVMmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder extends AsyncTask<Void, Void, Void> {

    //parameters for recording voice
    private static final String TAG = AudioRecorder.class.getName();

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 44100;
    private int bufferSize = 0;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    //temporary variable to store the audio. This contains only the bytes, not the header of the wav type, indeed it's not a real wav file
    private static final String FILE_NAME_TEMP = "audio_temp";

    private static final String DIRECTORY_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube/Sounds/";

    private AudioRecord recorder = null;

    private String fileName, timestamp;
    private Context context;

    private boolean isRecording = false;


    public AudioRecorder(Context c, String fileName) {

        context = c;
        this.fileName = fileName;

    }

    @Override
    protected Void doInBackground(Void... objects) {

        isRecording = true;
        //definizione della dimensione del buffer con i parametri definiti inizialmente
        //the variable buffersize defines the dimension of the picked data from the microphone, before that are processed
        bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,RECORDER_AUDIO_ENCODING);
        //this variable is used to get the audio from microphone
        //POSSO ANCHE USARE MIC, INVECE DI VOICE_COMMUNICATION, SE USO DIRETTAMENTE IL MICROFONO DEL DISPOSITIVO
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);

        recorder.startRecording();

        //buffer to store the data from microphone
        byte[] data = new byte[bufferSize];

        String audioTemp = DIRECTORY_PATH + FILE_NAME_TEMP +".wav";

        //steam to write the data into temporary audio file
        FileOutputStream ows = null;
        try {
            ows = new FileOutputStream(audioTemp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        // the while cycle continues recording and writting into the temporary audio file,
        // until the asynctask isn't stopped
        while(isRecording) {

            read = recorder.read(data, 0, bufferSize);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    ows.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(isCancelled()){
                isRecording = false;
            }

        }

        recorder.stop();
        recorder.release();

        try {
            ows.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean audioAdded = false;

        timestamp = Long.toString(System.currentTimeMillis() / 1000);

        if(fileName.equals(SVMmodel.NOISE_NAME)){
            copyWaveFile(FILE_NAME_TEMP + ".wav",fileName + ".wav");
            audioAdded = true;

        }
        else {

            String file = fileName + "_" + timestamp + ".wav";
            copyWaveFile(FILE_NAME_TEMP + ".wav", file);
            ActionVocal newVocal = new ActionVocal(fileName);
            newVocal.addFile(file);


            audioAdded = MainModel.getInstance().addAction(newVocal);

            MainModel.getInstance().removeModelWithThisSound(fileName);

        }

        deleteTempFile();

        if(audioAdded) {

            Log.d(TAG, fileName);
            MainModel.getInstance().writeActionsJson();
            MainModel.getInstance().writeModelJson();
            MainModel.getInstance().writeConfigurationsJson();
            Toast.makeText(context, R.string.voice_command_saved, Toast.LENGTH_LONG).show();

        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    //this method allows to copy the temporary audio file data into the wav file, adding the information of the header
    private void copyWaveFile(String s, String s1) {
        //streams for the reading and the writing of the data
        FileInputStream in = null;
        FileOutputStream out = null;

        //wav header parameters
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = (RECORDER_BPP * RECORDER_SAMPLERATE * channels) / 8;
        byte[] data = new byte[bufferSize];

        try {

            in = new FileInputStream(DIRECTORY_PATH + s);
            out = new FileOutputStream(DIRECTORY_PATH + s1);
            totalAudioLen = new File(DIRECTORY_PATH + s).length();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1) {
                out.write(data);
            }


            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //removal of the temporary audio file
    private void deleteTempFile() {
        File file = new File(DIRECTORY_PATH + FILE_NAME_TEMP + ".wav");
        file.delete();
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen,
                                     long longSampleRate, int channels, long byteRate) {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * RECORDER_BPP / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        try {
            out.write(header, 0, 44);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
