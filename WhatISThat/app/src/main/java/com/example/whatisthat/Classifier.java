import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import org.tensorflow.Graph;

import javax.imageio.ImageIO;

import org.tensorflow.Tensor;

public class Classifier {
	private final int WIDTH = 299;
	private final int HEIGHT = 299;
	
	private BufferedImage image;
	private Tensor<Float> imageTensor;
	
	public Classifier(String imageFile) {
		readImage(imageFile);
		resizeImage();
		createTensor();
	}
	
	private void readImage(String filename) {
		File inputFile = new File("src/main/resources/images/"+filename);
		try {
			image = ImageIO.read(inputFile);
		} catch (IOException e) {
			System.out.println("Image " + filename + " could not be read");
			e.printStackTrace();
            System.exit(1);
		}
	}
	
	private void createTensor() {
		int width = image.getWidth();
        int height = image.getHeight();
        int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);
        float[][][][] rgbArray = new float[1][height][width][3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color color = new Color(data[i * width + j]);
                rgbArray[0][i][j][0] = color.getRed();
                rgbArray[0][i][j][1] = color.getGreen();
                rgbArray[0][i][j][2] = color.getBlue();
            }
        }
        imageTensor = Tensor.create(rgbArray, Float.class);
	}
	
	private void resizeImage() {
		BufferedImage tempImage = new BufferedImage(WIDTH, HEIGHT, image.getType());  
	    Graphics2D g = tempImage.createGraphics();  
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
	    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
	    g.drawImage(image, 0, 0, WIDTH, HEIGHT, 0, 0, image.getWidth(), image.getHeight(), null);  
	    image = tempImage;
	}
	

	public String toString() {
		return imageTensor.toString();
	}
	
	public void run() {
		
	}
}
