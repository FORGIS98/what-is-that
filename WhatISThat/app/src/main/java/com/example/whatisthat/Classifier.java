package com.example.whatisthat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.util.Random;

//import org.tensorflow.Graph;
import org.tensorflow.Tensor;

public class Classifier {
	private final int WIDTH = 299;
	private final int HEIGHT = 299;
	
	private Bitmap image;
	private Tensor imageTensor;
	
	public Classifier() {
		//readImage(imageFile);
		//resizeImage();
		createTensor();
	}
	
	private void readImage(String filename) {
		String IMAGESPATH = "";
		String pathName = IMAGESPATH+filename;
		image = BitmapFactory.decodeFile(pathName);
	}
	
	private void createTensor() {
        int[] data = new int[WIDTH * HEIGHT];
        float[][][][] rgbArray = new float[1][HEIGHT][WIDTH][3];
        //image.getRGB(0, 0, WIDTH, HEIGHT, data, 0, width);
		Random rand = new Random();
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                //Color color = new Color(data[i * WIDTH + j]);
                //rgbArray[0][i][j][0] = color.getRed();
				rgbArray[0][i][j][0] = rand.nextFloat()*255;
                //rgbArray[0][i][j][1] = color.getGreen();
				rgbArray[0][i][j][1] = rand.nextFloat()*255;
                //rgbArray[0][i][j][2] = color.getBlue();
				rgbArray[0][i][j][2] = rand.nextFloat()*255;
            }
        }
        imageTensor = Tensor.create(rgbArray, Float.class);
	}
	
	private void resizeImage() {
		/*BufferedImage tempImage = new BufferedImage(WIDTH, HEIGHT, image.getType());
	    Graphics2D g = tempImage.createGraphics();  
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
	    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
	    g.drawImage(image, 0, 0, WIDTH, HEIGHT, 0, 0, image.getWidth(), image.getHeight(), null);  
	    image = tempImage;*/
	}
	
	@NonNull
	@Override
	public String toString() {
		return image.toString();
	}
	
	public void run() {
		
	}
}
