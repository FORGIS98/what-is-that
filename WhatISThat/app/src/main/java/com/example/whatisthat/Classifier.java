package com.example.whatisthat;

import android.content.Context;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.whatisthat.ml.InceptionV4Quant1Metadata1;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

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

	List<Category> lastProbability;

	LinkedList<List<Category>> analyzerMemory;
	float[] probabilities;

	public Classifier(Context context) {
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

		probabilities = new float[NBLABELS];
		analyzerMemory = new LinkedList<>();
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

	private void updateProbabilities() {
		final int PROBBUFFER = 5;
		analyzerMemory.add(lastProbability);
		if (analyzerMemory.size() > PROBBUFFER) {
			List<Category> ancientProbability = analyzerMemory.poll();
			int j = 0;
			assert ancientProbability != null;
			for (Category c : ancientProbability) {
				probabilities[j] -= c.getScore()/PROBBUFFER;
				j++;
			}
		}

		int i = 0;
		for (Category c : lastProbability) {
			probabilities[i] += c.getScore()/ PROBBUFFER;
			i++;
		}
	}

	public void run() {
		InceptionV4Quant1Metadata1.Outputs outputs = model.process(image);
		lastProbability = outputs.getProbabilityAsCategoryList();
		updateProbabilities();
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public String get() {
		int maxAt = 0;
		for (int i = 0; i < NBLABELS; i++) {
			maxAt = probabilities[i] > probabilities[maxAt] ? i : maxAt;
		}
		//Optional<Category> max = probabilities.stream().max(Comparator.comparing(Category::getScore));
		//return max.map(category -> category.getLabel() + " " + (int) (category.getScore() * 100) + "%").orElse("");
		return lastProbability.get(maxAt).getLabel() + " " + (int) (probabilities[maxAt]*100) + "%";
	}

	public void close() {
		model.close();
	}
}
