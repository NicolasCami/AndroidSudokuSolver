package fr.nicolascami.util;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import android.util.Log;

public class ImageManager {

	/**
	 * findGrid
	 * Search a square in src image.
	 * The square area in approximately size * size.
	 * @param Mat src
	 * @param double size
	 * @return Point[]
	 */
	public static Point[] findGrid(Mat src, double size) {
		long begin = System.currentTimeMillis();
		
		double minArea = (size * 0.7) * (size * 0.7);
	    double maxArea = (size * 1.1) * (size * 1.1);
	    Point[] squarePoints = null;
		
        List<MatOfPoint2f> squares = ImageManager.getSquares(src, minArea, maxArea);
        
        // return the first square that match
        // TODO : return the closest square for the given area (size * size)
        if(squares.size() > 0) {
            double[] orderedPoints = getOrderedPoints(squares.get(0));
            
            squarePoints = new Point[4];
            squarePoints[0] = new Point(orderedPoints[0],orderedPoints[1]);
            squarePoints[1] = new Point(orderedPoints[2],orderedPoints[3]);
            squarePoints[2] = new Point(orderedPoints[4],orderedPoints[5]);
            squarePoints[3] = new Point(orderedPoints[6],orderedPoints[7]);
        }
		
		long end = System.currentTimeMillis();
		Log.i("ImageManager", "findGrid : " + (end-begin));
		
		return squarePoints;
	}
	
	public static void resize(Mat src, Mat dst, double width, double height) {
    	Imgproc.resize(src, dst, new Size(width, height), 0, 0, Imgproc.INTER_LINEAR);
	}
	
	public static void fastThreshold(Mat src, Mat dst) {
		Imgproc.threshold(src, dst, -1, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
	}
	
	public static void drawLine(Mat dst, Point p1, Point p2, double ratioWidth, double ratioHeight, Scalar color) {
		Core.line(dst, new Point(p1.x*ratioWidth,p1.y*ratioHeight), new Point(p2.x*ratioWidth,p2.y*ratioHeight), color, 2);
	}
	
	public static void drawLines(Mat dst, Point[] points, double ratioWidth, double ratioHeight, Scalar color) throws Exception {
		if(points.length < 2) {
			throw new Exception("Not enought points to draw lines : got " + points.length + ", need at least 2.");
		}
		
		for(int i=0; i<points.length; i++) {
			Point p1 = points[i];
			Point p2 = points[(i+1)%points.length];
			
			ImageManager.drawLine(dst, new Point(p1.x*ratioWidth,p1.y*ratioHeight), new Point(p2.x*ratioWidth,p2.y*ratioHeight), 1, 1, color);
		}
	}
	
	public static List<MatOfPoint2f> getSquares(Mat src, double minArea, double maxArea) {
		long begin = System.currentTimeMillis();
		//Log.i("ImageManager", "min : " + minArea + " / max : " + maxArea);
		
        List<MatOfPoint> shapes = new ArrayList<MatOfPoint>();
        List<MatOfPoint2f> squares = new ArrayList<MatOfPoint2f>();
        
        shapes.clear();
        Imgproc.findContours(src.clone(), shapes, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for(int i=0; i<shapes.size(); i++) {
            MatOfPoint2f pointsList = new MatOfPoint2f();
            MatOfPoint2f approxPoly = new MatOfPoint2f();
            shapes.get(i).convertTo(pointsList, CvType.CV_32FC2);
    		Imgproc.approxPolyDP(pointsList, approxPoly, Imgproc.arcLength(pointsList, true)*0.02, true);
        	if(ImageManager.isSquare(approxPoly)) {
            	double area = ImageManager.squareArea(approxPoly);
            	//Log.i("ImageManager", "square area : " + area);
            	// ignore small or large areas
            	if(area < minArea || area > maxArea) {
            		continue;
            	}
                squares.add(approxPoly);
                //Log.i("ImageManager", "square OK : " + area);
        	}
        }
        
        long end = System.currentTimeMillis();
		Log.i("ImageManager", "getSquares : " + (end-begin));
        
        return squares;
	}
	
    private static boolean isSquare(MatOfPoint2f approxPoly) {
        //double segLen;

		if(approxPoly.toArray().length != 4) return false;
		/*double orderedPoints[] = ImageManager.getOrderedPoints(approxPoly);
		segLen = (Math.abs(orderedPoints[0]-orderedPoints[2])+
				Math.abs(orderedPoints[3]-orderedPoints[5])+
				Math.abs(orderedPoints[4]-orderedPoints[6])+
				Math.abs(orderedPoints[1]-orderedPoints[7]))/4.0;
		if(Math.abs(orderedPoints[1]-orderedPoints[3]) > segLen/4.0) return false;
		if(Math.abs(orderedPoints[5]-orderedPoints[7]) > segLen/4.0) return false;
		if(Math.abs(orderedPoints[0]-orderedPoints[6]) > segLen/4.0) return false;
		if(Math.abs(orderedPoints[2]-orderedPoints[4]) > segLen/4.0) return false;*/
		
    	return true;
    }
    
    private static double squareArea(MatOfPoint2f approxPoly) {
        double segLen;
        double area;

		double orderedPoints[] = ImageManager.getOrderedPoints(approxPoly);
		segLen = (Math.abs(orderedPoints[0]-orderedPoints[2])+
				Math.abs(orderedPoints[3]-orderedPoints[5])+
				Math.abs(orderedPoints[4]-orderedPoints[6])+
				Math.abs(orderedPoints[1]-orderedPoints[7]))/4.0;
		area = segLen * segLen;
		
    	return area;
    }
    
    // TODO : clean this fonction
    private static double[] getOrderedPoints(MatOfPoint2f approx) {
    	double points[] = new double[8];
    	
    	int min = 0;
    	int max = 0;
    	double sum[] = new double[4];
    	for(int i=0; i<4; i++) {
    		sum[i] = approx.toArray()[i].x+approx.toArray()[i].y;
    	}
    	/*
    	 * haut gauche
    	 * et
    	 * bas droit
    	 */
    	for(int i=1; i<4; i++) {
    		if(sum[i]<sum[min]) min = i;
    		if(sum[i]>sum[max]) max = i;
    	}
    	points[0] = approx.toArray()[min].x;
    	points[1] = approx.toArray()[min].y;
    	points[4] = approx.toArray()[max].x;
    	points[5] = approx.toArray()[max].y;
    	/*
    	 * haut droit
    	 * et
    	 * bas gauche
    	 */
    	int rest0 = -1;
    	int rest1 = -1;
    	for(int i=0; i<4; i++) {
    		if(i!=min && i!=max) {
        		if(rest0 < 0) rest0=i;
        		else rest1=i;
    		}
    	}
    	if(approx.toArray()[rest0].x<approx.toArray()[rest1].x) {
    		min=rest0;
    		max=rest1;
    	}
    	else {
    		min=rest1;
    		max=rest0;
    	}
    	points[6] = approx.toArray()[min].x;
    	points[7] = approx.toArray()[min].y;
    	points[2] = approx.toArray()[max].x;
    	points[3] = approx.toArray()[max].y;
    	
    	return points;
    }

	public static void drawSquare(Mat dst, Point point, double size, Scalar color) {
        ImageManager.drawLine(dst, point, new Point(point.x+size,point.y), 1, 1, color);
        ImageManager.drawLine(dst, point, new Point(point.x,point.y+size), 1, 1, color);
        ImageManager.drawLine(dst, new Point(point.x+size,point.y+size), new Point(point.x+size,point.y), 1, 1, color);
        ImageManager.drawLine(dst, new Point(point.x+size,point.y+size), new Point(point.x,point.y+size), 1, 1, color);
	}
	
	public static void pasteMat(Mat src, Mat dst, Point p) {
		Rect roi = new Rect(p, src.size());
		Mat dstRoi = new Mat(dst, roi);
		src.copyTo(dstRoi);
	}

	public static Mat[][] findFigures(Mat src, Point[] gridPoints) {
		long begin = System.currentTimeMillis();
		
		//double fastWidth = 400.0;
	    //double fastHeight = 400.0;
	    //double lenCase = fastWidth/9;
		double lenCase = (src.height())/9;
	    Mat[][] figures = new Mat[9][9];
        Mat srcMat = new Mat(4,1,CvType.CV_32FC2);
        Mat dstMat = new Mat(4,1,CvType.CV_32FC2);
        double[] orderedPoints = {
        		gridPoints[0].x,gridPoints[0].y,
        		gridPoints[1].x,gridPoints[1].y,
        		gridPoints[2].x,gridPoints[2].y,
        		gridPoints[3].x,gridPoints[3].y};
        Mat mat = src.clone();
        //ImageManager.resize(mat, mat, fastWidth, fastHeight);
        
        srcMat.put(0,0,orderedPoints);
        dstMat.put(0,0,
        		0,0,
        		mat.width(),0,
        		mat.width(),mat.height(),
        		0,mat.height());

        // warp grid into a perfect square
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat);
        Imgproc.warpPerspective(src, mat, perspectiveTransform, mat.size());
        Mat matClean = mat.clone();

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    contours.clear();
        //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.GaussianBlur(mat, mat, new Size(3,3), 0);
        //Imgproc.adaptiveThreshold(mat, mat, 255,1,1,11,2);
        
        int maxWidth = (mat.width())/12;
        int maxHeight = (mat.height())/12;
        int minWidth = (mat.width())/45;
        int minHeight = (mat.height())/20;
        
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			figures[j][i] = null;
    		}
    	}
        //ImageManager.pasteMat(mat, src, gridPoints[0]);
    	Imgproc.findContours(mat.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for(int i=0; i<contours.size(); i++) {
        	Rect rect = Imgproc.boundingRect(contours.get(i));
        	if(rect.width<maxWidth && rect.height<maxHeight &&
        			rect.width>minWidth && rect.height>minHeight) {
		        Rect roi = new Rect(rect.x, rect.y, rect.width, rect.height);
		        Mat cropped = new Mat(matClean, roi);
		        
				int mx = (rect.x+rect.width)-(rect.width/2);
				int my = (rect.y+rect.height)-(rect.height/2);
				int x = (int) (mx/lenCase);
				int y = (int) (my/lenCase);
				figures[y][x] = cropped;
        	}
        }
        
        long end = System.currentTimeMillis();
		Log.i("ImageManager", "findFigures : " + (end-begin));
		
		return figures;
	}

}
