package com.example.whatisthat;

import android.content.Context;

import android.util.Log;

import com.example.whatisthat.ml.InceptionV4299Quant;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class Classifier {
	private final int WIDTH = 299;
	private final int HEIGHT = 299;
	private final int NBLABELS = 1000;
	private final int NBCHANNELS = 3;

	InceptionV4299Quant model;
	TensorBuffer inputFeature0;
	TensorBuffer outputFeature0;

	private float[] cumProb;

	public Classifier(Context context) {
		//Initializes cumulated probability
		cumProb = new float[NBLABELS];

		//Load model
		try {
			model = InceptionV4299Quant.newInstance(context);
		} catch (IOException e) {
			Log.e( "","ERROR: model cannot be loaded.");
			e.printStackTrace();
		}

		//Initialize inputs
		inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, WIDTH, HEIGHT, NBCHANNELS}, DataType.UINT8);

		model.close();

	}

	public void run(ByteBuffer byteBuffer) {
		inputFeature0.loadBuffer(byteBuffer);
		InceptionV4299Quant.Outputs outputs = model.process(inputFeature0);
		outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
	}

	public void close() {
		model.close();
	}

}
