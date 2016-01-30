package fr.nicolascami.task;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import android.os.AsyncTask;
import fr.nicolascami.sudokusolver.MainActivity;
import fr.nicolascami.util.ImageManager;

public class FindSquareTask extends AsyncTask<Object, Void, Point[]> {
    
    private volatile MainActivity _activity;

    public FindSquareTask(MainActivity s) {
        this._activity = s;
    }
    
    @Override
    protected void onPreExecute() {
    	
    }

    /**
     * Return square points if found, or null otherwise.
     * @param params
     * @return
     */
    @Override
    protected Point[] doInBackground(Object... params) {
    	Mat image = (Mat) params[0];
    	Double squareSize = (Double) params[1];
    	Point[] square = ImageManager.findGrid(image, squareSize);
        
        return square;
    }

    @Override
    protected void onPostExecute(Point[] result) {
        _activity.findGridResult(result);
    }
    
}