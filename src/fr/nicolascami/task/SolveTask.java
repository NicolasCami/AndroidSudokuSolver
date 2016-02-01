package fr.nicolascami.task;

import android.os.AsyncTask;
import fr.nicolascami.sudokusolver.MainActivity;
import fr.nicolascami.util.Grid;
import fr.nicolascami.util.Solver;

public class SolveTask extends AsyncTask<Object, Void, Grid> {
    
    private volatile MainActivity 	_activity;

    public SolveTask(MainActivity s) {
        _activity = s;
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
    protected Grid doInBackground(Object... params) {
    	Grid grid = (Grid) params[0];
    	Solver solver = new Solver(3);
		int[][] gridArray = grid.toArray();
        boolean result = solver.solve(gridArray, true);
        
        if(result) {
        	return new Grid(solver.getSolution());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Grid result) {
        _activity.SolveResult(result);
    }
    
}