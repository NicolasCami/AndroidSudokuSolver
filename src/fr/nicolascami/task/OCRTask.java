package fr.nicolascami.task;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import android.os.AsyncTask;
import android.util.Log;
import fr.nicolascami.sudokusolver.MainActivity;
import fr.nicolascami.util.ImageManager;
import fr.nicolascami.util.OCR;

public class OCRTask extends AsyncTask<Object, Void, int[][]> {
    
    private volatile MainActivity 	_activity;
    private OCR						_ocr;

    public OCRTask(MainActivity s, OCR ocr) {
        _activity = s;
        _ocr = ocr;
    }
    
    @Override
    protected void onPreExecute() {
    	
    }

    /**
     * Return a matrix of integers.
     * @param params
     * @return
     */
    @Override
    protected int[][] doInBackground(Object... params) {
    	Mat[][] figures = (Mat[][]) params[0];
		int[][] grid = new int[9][9];
        for(int i=0; i<9; i++) {
        	for(int j=0; j<9; j++) {
        		Mat figure = figures[j][i];
        		if(figure != null) {
        			int currentFigure = Math.round(_ocr.findByMat(figure));
        			grid[i][j] = currentFigure;
        		}
            }
        }
        
        return grid;
    }

    @Override
    protected void onPostExecute(int[][] result) {
        _activity.OCRResult(result);
    }
    
}