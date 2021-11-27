package com.example.whatisthat;

import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Locale;
import android.os.Handler;

public class EnglishTranslator {

    Translator englishToDeviceLang;
    DownloadConditions conditions = new DownloadConditions.Builder()
        .requireWifi()
        .build();
    boolean isReady = false;

    public EnglishTranslator() {
        String lang = Locale.getDefault().getLanguage();
        TranslatorOptions options = new TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(lang)
            .build();

        englishToDeviceLang = Translation.getClient(options);
        downloadModel();
    }

    public boolean isReadyToTranslate() {
        return isReady;
    }

    public void downloadModel() {
        englishToDeviceLang.downloadModelIfNeeded(conditions)
            .addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(this.getClass().getSimpleName(), "Download model succeed");
                        isReady = true;
                    }
                }
            )
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(this.getClass().getSimpleName(), "Downloading model failed.");
                        e.printStackTrace();
                    }
                }
            );
    }

    public void translateWord(String word, Handler responseHandler) {
        englishToDeviceLang.translate(word)
            .addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.i(this.getClass().getSimpleName(), "Translation succeed.");
                        Message msg = new Message();
                        msg.obj = s;

                        responseHandler.sendMessage(msg);
                    }
                }
            )
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(this.getClass().getSimpleName(), "Translation failed.");
                        e.printStackTrace();
                    }
                }
            );
    }

}
