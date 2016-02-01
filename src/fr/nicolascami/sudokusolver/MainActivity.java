package fr.nicolascami.sudokusolver;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import fr.nicolascami.task.FindFiguresTask;
import fr.nicolascami.task.FindSquareTask;
import fr.nicolascami.task.OCRTask;
import fr.nicolascami.task.SolveTask;
import fr.nicolascami.util.Grid;
import fr.nicolascami.util.GridBuffer;
import fr.nicolascami.util.ImageManager;
import fr.nicolascami.util.OCR;
import fr.nicolascami.util.Solver;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	
	// Attributes
	//private double frameWidth = VideoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
	//private int t = 0;
	private List<Integer> 	_imagesTraining;
	private OCR 			_ocr;
	private Button 			button;
	private boolean 		analyser = false;
	private long 			_lastSolve = System.currentTimeMillis();
	private Solver 			_solver;
	private Point[] 		_lastSquareFound = null;
	private long			_lastSquareFoundFrameNumber = 0;
	private long			_lastFiguresFoundFrameNumber = 0;
	private long			_previousFrameNumber = 0;
	private boolean			_processingFindSquare = false;
	private boolean			_processingFindFigures = false;
	private boolean			_processingSolve = false;
	private long			_currentFrameNumber = 0;
	private Mat				_lastImageForProcessing;
	private double			_patternSizeRgba = 0.0;
	private double 			_frameWidth = 0.0;
	private double 			_frameHeight = 0.0;
	private GridBuffer		_gridBuffer;
	private Grid			_lastSolvedGrid = null;
    
    private CameraBridgeViewBase 	_mOpenCvCameraView;
    private boolean              	_mIsJavaCamera = true;
    private Mat 					_mIntermediateMat;
    private BaseLoaderCallback 		_mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    _mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    // Constant
    private static final String 	TAG = "SudikuSolver::MainActivity";
    private final Scalar 			COLOR_SQUARE_PATTERN = new Scalar(127,127,127,100);
    private final Scalar 			COLOR_SQUARE_FOUND = new Scalar(255,0,0,150);
	private final double 			MAT_FAST_WIDTH = 350.0;
	private final double 			MAT_FAST_HEIGHT = 350.0;
    
	// OpenCV loader
	static {
	    if(!OpenCVLoader.initDebug()) {
	        Log.d("ERROR", "Unable to load OpenCV");
	    } else {
	        Log.d("SUCCESS", "OpenCV loaded");
	    }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState); 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        _ocr = new OCR(getApplicationContext());
        _gridBuffer = new GridBuffer(10);
        
    	//_solver = new Solver(3);
    	//_solver.solve(_grille,true);
        setContentView(R.layout.tutorial1_surface_view);
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(analyser==false) {
					analyser = true;
					_lastSolve = System.currentTimeMillis();
				}
				else analyser = false;
				Toast.makeText(getApplicationContext(), 
			    		Boolean.toString(analyser), Toast.LENGTH_LONG).show();
			}
		});

        if (_mIsJavaCamera)
            _mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        else
            _mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);

        _mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        _mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (_mOpenCvCameraView != null)
            _mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        _mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    public void onDestroy() {
        super.onDestroy();
        if (_mOpenCvCameraView != null)
            _mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	_mIntermediateMat = new Mat();
    }

    public void onCameraViewStopped() {
        if(_mIntermediateMat != null)
            _mIntermediateMat.release();
        _mIntermediateMat = null;
    }

    /**
     * Called for each camera frame
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        /*
         * traitement de l'image
         */
    	//Log.i(TAG, "width " + mOpenCvCameraView.getWidth());
    	_currentFrameNumber = _previousFrameNumber + 1;
    	Mat rgba = inputFrame.rgba();
    	if(_frameWidth < 1.0) {
    		_frameWidth = rgba.width();
    	}
    	if(_frameHeight < 1.0) {
    		_frameHeight = rgba.height();
    	}
    	if(_patternSizeRgba < 1.0) {
    		_patternSizeRgba = _frameHeight * 0.9;
    	}
    	double ratioWidth = rgba.width() / MAT_FAST_WIDTH;
	    double ratioHeight = rgba.height() / MAT_FAST_HEIGHT;
    	double patternSize = MAT_FAST_HEIGHT * 0.9;
    	
    	// resize original image smaller, to perform transformations faster
    	Mat rgbaGrid = rgba.clone();
    	ImageManager.resize(rgbaGrid, rgbaGrid, MAT_FAST_WIDTH, MAT_FAST_HEIGHT);
    	Imgproc.cvtColor(rgbaGrid, rgbaGrid, Imgproc.COLOR_RGB2GRAY);
    	
    	//Mat rgbaOCR = rgbaGrid.clone();
	    ImageManager.fastThreshold(rgbaGrid, rgbaGrid);
	    
	    if(_processingFindSquare) {
	    	Log.i(TAG, "Processing finding square...");
	    }
	    else {
	    	Log.i(TAG, "Start finding square...");
	    	findGridStart(rgbaGrid, patternSize);
	    }
	    
    	if(_lastSquareFound != null) {
    		// draw square found
    		try {
				ImageManager.drawLines(rgba, _lastSquareFound, ratioWidth, ratioHeight, COLOR_SQUARE_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	// draw square pattern
    	ImageManager.drawSquare(rgba, new Point((_frameWidth/2.0)-(_patternSizeRgba/2),(_frameHeight/2.0)-(_patternSizeRgba/2)), _patternSizeRgba, COLOR_SQUARE_PATTERN);
    	
    	// draw grid
    	Grid grid = _gridBuffer.getReliableGrid();
    	if(_lastSolvedGrid != null) {
            for(int i=0; i<9; i++) {
            	for(int j=0; j<9; j++) {
            		int value = _lastSolvedGrid.get(i, j);
            		if(value != 0) {
            			Point coordinates = new Point((_frameWidth/2.0)-(_patternSizeRgba/2)+(i*(_patternSizeRgba/9))+10,
    					(_frameHeight/2.0)-(_patternSizeRgba/2)+(j*(_patternSizeRgba/9))+35);
            			Core.putText(rgba, Integer.toString(value), coordinates, Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0,0,255,255), 3);
            		}
                }
            }
    	}
    	else {
            for(int i=0; i<9; i++) {
            	for(int j=0; j<9; j++) {
            		int value = grid.get(i, j);
            		if(value != 0) {
            			Point coordinates = new Point((_frameWidth/2.0)-(_patternSizeRgba/2)+(i*(_patternSizeRgba/9))+10,
    					(_frameHeight/2.0)-(_patternSizeRgba/2)+(j*(_patternSizeRgba/9))+35);
            			Core.putText(rgba, Integer.toString(value), coordinates, Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0,0,255,255), 3);
            		}
                }
            }
    	}
    	
    	_previousFrameNumber = _currentFrameNumber;

        return rgba;
    }

    private void findGridStart(Mat image, double squareSize) {
    	new FindSquareTask(this).execute((Object) image, (Object) squareSize);
    	_lastImageForProcessing = image;
		_processingFindSquare = true;
	}
    
	public void findGridResult(Point[] result) {
		Log.i(TAG, "Grid search result. It tooks " + (_currentFrameNumber - _lastSquareFoundFrameNumber) + " frames.");
		if(result != null) {
			Log.i(TAG, "Grid found !");
			if(!_processingFindFigures) {
				findFiguresStart(_lastImageForProcessing, result);
			}
		}
		else {
			Log.i(TAG, "Grid not found.");
		}
		_lastSquareFound = result;
		_lastSquareFoundFrameNumber = _currentFrameNumber;
		_processingFindSquare = false;
	}
	
	private void findFiguresStart(Mat image, Point[] square) {
    	new FindFiguresTask(this).execute((Object) image, (Object) square);
    	_processingFindFigures = true;
	}

	public void findFiguresResult(Mat[][] result) {
		Log.i(TAG, "Figures search result. It tooks " + (_currentFrameNumber - _lastFiguresFoundFrameNumber) + " frames.");
		OCRStart(result);
		_processingFindFigures = false;
	}
	
	private void OCRStart(Mat[][] figures) {
    	new OCRTask(this, _ocr).execute((Object) figures);
	}

	public void OCRResult(int[][] result) {
        _gridBuffer.fillBuffer(result);
        Grid grid = _gridBuffer.getReliableGrid();
        if(grid != null && !_processingSolve) {
        	SolveStart(grid);
        }
	}
	
	private void SolveStart(Grid grid) {
    	new SolveTask(this).execute((Object) grid);
    	_processingSolve = true;
	}

	public void SolveResult(Grid result) {
		_lastSolvedGrid = result;
		_processingSolve = false;
	}
}