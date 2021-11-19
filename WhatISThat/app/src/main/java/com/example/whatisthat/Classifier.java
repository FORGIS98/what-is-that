package com.example.whatisthat;

import android.content.Context;

import android.util.Log;

import com.example.whatisthat.ml.InceptionV4Quant1Metadata1;

import java.io.IOException;
import java.util.List;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class Classifier {
	private final int WIDTH = 299;
	private final int HEIGHT = 299;
	private final int NBLABELS = 1001;

	InceptionV4Quant1Metadata1 model;
	TensorBuffer inputFeature0;
	TensorImage image;

	private final float[] cumProb;
	List<Category> lastProbability;

	public Classifier(Context context) {
		//Initializes cumulated probability
		cumProb = new float[NBLABELS];

		//Load model
		try {
			model = InceptionV4Quant1Metadata1.newInstance(context);
		} catch (IOException e) {
			Log.e( "","ERROR: model cannot be loaded.");
			e.printStackTrace();
		}

		image = new TensorImage(DataType.UINT8);

		//Initialize inputs
		final int NBCHANNELS = 3;
		inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, WIDTH, HEIGHT, NBCHANNELS},
				DataType.UINT8);
	}

	private void resizeImage() {
		ImageProcessor imageProcessor =
				new ImageProcessor.Builder()
						.add(new ResizeOp(HEIGHT, WIDTH, ResizeOp.ResizeMethod.BILINEAR))
						.build();
		image = imageProcessor.process(image);
	}

	public void feed(Picture picture) {
		image.load(picture.getBitmap());
		resizeImage();
		inputFeature0.loadBuffer(image.getBuffer());
	}

	public void run() {
		InceptionV4Quant1Metadata1.Outputs outputs = model.process(image);
		lastProbability = outputs.getProbabilityAsCategoryList();
		for (int i = 0; i < NBLABELS; i++) {
			cumProb[i] = lastProbability.get(i).getScore();
		}
	}

	public String get() {
		float maxProb = 0;
		int idProb = 0;
		for (int i = 0; i < NBLABELS; i++) {
			if (cumProb[i] > maxProb) {
				maxProb = cumProb[i];
				idProb = i;
			}
		}

		return lastProbability.get(idProb).getLabel() + " " + (int) (maxProb*100) + "%";
	}

	public void close() {
		model.close();
	}
}
