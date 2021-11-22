package com.example.whatisthat;

import android.content.Context;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.whatisthat.ml.InceptionV4Quant1Metadata1;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class Classifier {
	private final int WIDTH = 299;
	private final int HEIGHT = 299;
	// private final int NBLABELS = 1001;

	InceptionV4Quant1Metadata1 model;
	TensorBuffer inputFeature0;
	TensorImage image;

	List<Category> lastProbability;

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
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public String get() {
		Optional<Category> max = lastProbability.stream().max(Comparator.comparing(Category::getScore));
		return max.map(category -> category.getLabel() + " " + (int) (category.getScore() * 100) + "%").orElse("");
	}

	public void close() {
		model.close();
	}
}
