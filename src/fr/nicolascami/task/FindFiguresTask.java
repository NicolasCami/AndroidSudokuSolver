package fr.nicolascami.task;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import android.os.AsyncTask;
import fr.nicolascami.sudokusolver.MainActivity;
import fr.nicolascami.util.ImageManager;

public class FindFiguresTask extends AsyncTask<Object, Void, Mat[][]> {
    
    private volatile MainActivity _activity;

    public FindFiguresTask(MainActivity s) {
        this._activity = s;
    }
    
    @Override
    protected void onPreExecute() {
    	
    }

    /**
     * Return a matrix of images.
     * @param params
     * @return
     */
    @Override
    protected Mat[][] doInBackground(Object... params) {
    	Mat image = (Mat) params[0];
    	Point[] square = (Point[]) params[1];
    	Mat[][] figures = ImageManager.findFigures(image, square);
        
        return figures;
    }

    @Override
    protected void onPostExecute(Mat[][] result) {
        _activity.findFiguresResult(result);
    }
    
}