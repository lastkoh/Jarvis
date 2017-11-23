package com.jarvis.speech;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * Created by admin on 22-Nov-17.
 */

public class PocketSphinxSetupAsyncTask extends AsyncTask<Void, Void, Exception> {
    private static final String TAG = PocketSphinxSetupAsyncTask.class.getSimpleName();
    private final SpeechManager mSpeechRecognizerManager;

    public PocketSphinxSetupAsyncTask(SpeechManager mSpeechRecognizerManager){
        this.mSpeechRecognizerManager = mSpeechRecognizerManager;
    }

    @Override
    protected Exception doInBackground(Void... params) {
        try {
            Assets assets = new Assets(this.mSpeechRecognizerManager.getmContext());

            //Performs the synchronization of assets in the application and external storage
            File assetDir = assets.syncAssets();


            //Creates a new speech recognizer builder with default configuration
            SpeechRecognizerSetup speechRecognizerSetup = SpeechRecognizerSetup.defaultSetup();

            speechRecognizerSetup.setAcousticModel(new File(assetDir, "en-us-ptm"));
            speechRecognizerSetup.setDictionary(new File(assetDir, "cmudict-en-us.dict"));

            // Threshold to tune for keyphrase to balance between false alarms and misses
            speechRecognizerSetup .setKeywordThreshold(1e-45f);

            //Creates a new SpeechRecognizer object based on previous set up.
            mSpeechRecognizerManager.setmPocketSphinxRecognizer(speechRecognizerSetup.getRecognizer());

            // Create keyword-activation search.
            mSpeechRecognizerManager.getmPocketSphinxRecognizer().addKeyphraseSearch(
                    SpeechManager.KWS_SEARCH,
                    SpeechManager.KEYPHRASE);

            mSpeechRecognizerManager.getmPocketSphinxRecognizer()
                    .addListener(new PocketSphinxRecognitionListener(mSpeechRecognizerManager));
        } catch (IOException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception result) {
        if (result != null) {
            Log.i(TAG,result.getMessage());
        } else {
            mSpeechRecognizerManager.getmPocketSphinxRecognizer().stop();
            mSpeechRecognizerManager.getmPocketSphinxRecognizer().startListening(SpeechManager.KWS_SEARCH);
        }
    }
}