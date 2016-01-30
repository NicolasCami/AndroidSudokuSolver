package fr.nicolascami.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvKNearest;

import fr.nicolascami.sudokusolver.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class OCR {
	
	private Context context = null;
	private CvKNearest knn = new CvKNearest();
	private int imageSampleSize = 16;
	private int imageSampleCols = imageSampleSize*imageSampleSize;
	private Map<Integer,Float> imagesTraining = new HashMap<Integer,Float>();
	
	public OCR(Context context) {
		this.context = context;
		this.imagesTraining.put(R.drawable.training1_0, 1f);
		this.imagesTraining.put(R.drawable.training2_0, 2f);
		this.imagesTraining.put(R.drawable.training3_0, 3f);
		this.imagesTraining.put(R.drawable.training4_0, 4f);
		this.imagesTraining.put(R.drawable.training5_0, 5f);
		this.imagesTraining.put(R.drawable.training6_0, 6f);
		this.imagesTraining.put(R.drawable.training7_0, 7f);
		this.imagesTraining.put(R.drawable.training8_0, 8f);
		this.imagesTraining.put(R.drawable.training9_0, 9f);
		this.imagesTraining.put(R.drawable.training1_1, 1f);
		this.imagesTraining.put(R.drawable.training2_1, 2f);
		this.imagesTraining.put(R.drawable.training3_1, 3f);
		this.imagesTraining.put(R.drawable.training4_1, 4f);
		this.imagesTraining.put(R.drawable.training5_1, 5f);
		this.imagesTraining.put(R.drawable.training6_1, 6f);
		this.imagesTraining.put(R.drawable.training7_1, 7f);
		this.imagesTraining.put(R.drawable.training8_1, 8f);
		this.imagesTraining.put(R.drawable.training9_1, 9f);
		this.imagesTraining.put(R.drawable.training1_2, 1f);
		this.imagesTraining.put(R.drawable.training2_2, 2f);
		this.imagesTraining.put(R.drawable.training3_2, 3f);
		this.imagesTraining.put(R.drawable.training4_2, 4f);
		this.imagesTraining.put(R.drawable.training5_2, 5f);
		this.imagesTraining.put(R.drawable.training6_2, 6f);
		this.imagesTraining.put(R.drawable.training7_2, 7f);
		this.imagesTraining.put(R.drawable.training8_2, 8f);
		this.imagesTraining.put(R.drawable.training9_2, 9f);
		this.imagesTraining.put(R.drawable.training1_3, 1f);
		this.imagesTraining.put(R.drawable.training2_3, 2f);
		this.imagesTraining.put(R.drawable.training3_3, 3f);
		this.imagesTraining.put(R.drawable.training4_3, 4f);
		this.imagesTraining.put(R.drawable.training5_3, 5f);
		this.imagesTraining.put(R.drawable.training6_3, 6f);
		this.imagesTraining.put(R.drawable.training7_3, 7f);
		this.imagesTraining.put(R.drawable.training8_3, 8f);
		this.imagesTraining.put(R.drawable.training9_3, 9f);
		this.imagesTraining.put(R.drawable.training1_4, 1f);
		this.imagesTraining.put(R.drawable.training2_4, 2f);
		this.imagesTraining.put(R.drawable.training3_4, 3f);
		this.imagesTraining.put(R.drawable.training4_4, 4f);
		this.imagesTraining.put(R.drawable.training5_4, 5f);
		this.imagesTraining.put(R.drawable.training6_4, 6f);
		this.imagesTraining.put(R.drawable.training7_4, 7f);
		this.imagesTraining.put(R.drawable.training8_4, 8f);
		this.imagesTraining.put(R.drawable.training9_4, 9f);
		this.train(this.imagesTraining);
	}
	
	/**
	 * train OCR with the samples map
	 * @param samples
	 */
	public void train(Map<Integer, Float> samples) {
		long begin = System.currentTimeMillis();
		
	    Mat trainingData = new Mat(samples.size(), this.imageSampleCols, CvType.CV_32FC1);
	    Mat trainingClasses = new Mat(samples.size(), 1, CvType.CV_32FC1);
	    int i = 0;
		for(Entry<Integer,Float> entry : samples.entrySet()) {
		    Integer imagePath = entry.getKey();
		    Float imageValue = entry.getValue();
		    Mat imageMat = ressourceToSample(imagePath);
            for(int j=0; j<this.imageSampleCols; j++) {
            	trainingData.put(i, j, imageMat.get(0, j));
            }
            trainingClasses.put(i, 0, imageValue);
            i++;
		}
		knn.train(trainingData, trainingClasses);
		
		long end = System.currentTimeMillis();
		Log.i("OCR", "training OCR : " + (end-begin));
	}
	
	/**
	 * return a Mat reshaped from a ressource ID
	 * @param Integer ressourceId
	 * @return Mat sample
	 */
	public Mat ressourceToSample(Integer ressourceId) {
    	Mat mat = new Mat();
    	Bitmap bmp = BitmapFactory.decodeResource(this.context.getResources(), ressourceId);
    	Utils.bitmapToMat(bmp, mat);
    	Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
    	Imgproc.adaptiveThreshold(mat, mat, 255, 1, 1, 9, 2);
    	mat.convertTo(mat, CvType.CV_32FC1);
    	Imgproc.resize(mat, mat, new Size(this.imageSampleSize,this.imageSampleSize), 0, 0, Imgproc.INTER_LINEAR);
    	
        return mat.reshape(1,1);
	}
	
	/**
	 * return a Mat reshaped from a Mat
	 * @param Mat src
	 * @return Mat sample
	 */
	public Mat matToSample(Mat src) {
    	Mat mat = src.clone();
    	//Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
    	//Imgproc.adaptiveThreshold(mat, mat, 255, 1, 1, 9, 2);
    	mat.convertTo(mat, CvType.CV_32FC1);
    	Imgproc.resize(mat, mat, new Size(this.imageSampleSize,this.imageSampleSize), 0, 0, Imgproc.INTER_LINEAR);
    	
        return mat.reshape(1,1);
	}
	
	/**
	 * applies the OCR on a ressource and return the corresponding figure
	 * @param Integer ressourceId
	 * @return Float
	 */
	public Float findByRessource(Integer ressourceId) {
		long begin = System.currentTimeMillis();
		
	    Mat finalMat = ressourceToSample(ressourceId);
	    Mat sample = new Mat(1, this.imageSampleCols, CvType.CV_32FC1);
        for(int j=0; j<this.imageSampleCols; j++) {
        	sample.put(0, j, finalMat.get(0, j));
        }
        Float res = this.knn.find_nearest(sample, 1, new Mat(), new Mat(), new Mat());
        
        long end = System.currentTimeMillis();
		Log.i("OCR", "finding nearest : " + (end-begin));
		
	    return res;
	}

	/**
	 * applies the OCR on a Mat and return the corresponding figure
	 * @param Mat rgba
	 * @return Float
	 */
	public Float findByMat(Mat rgba) {
		Mat finalMat = matToSample(rgba);
	    Mat sample = new Mat(1, this.imageSampleCols, CvType.CV_32FC1);
        for(int j=0; j<this.imageSampleCols; j++) {
        	sample.put(0, j, finalMat.get(0, j));
        }
        Float res = this.knn.find_nearest(sample, 1, new Mat(), new Mat(), new Mat());
        
	    return res;
	}

}
