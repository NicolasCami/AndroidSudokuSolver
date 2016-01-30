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
	
	//private double frameWidth = VideoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
	//private int t = 0;
	private List<Integer> 	_imagesTraining;
	private OCR 			_ocr;
	private Button 			button;
	private boolean 		analyser = false;
	private int 			_tampon[][][];
	private int 			_tamponOccur[];
	private int 			_itampon;
	private int 			_itamponMax;
	private int 			_grille[][];
	private int 			_frameNum = 0;
	private long 			_lastSolve = System.currentTimeMillis();
	private Solver 			_solver;
	private Mat 			_savedMat;
    
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
    
    // contstants
    private static final String 	TAG = "OCVSample::Activity";
    private final Scalar 			COLOR_SQUARE_PATTERN = new Scalar(127,127,127,100);
    private final Scalar 			COLOR_SQUARE_FOUND = new Scalar(255,0,0,150);
	private final double 			MAT_FAST_WIDTH = 350.0;
	private final double 			MAT_FAST_HEIGHT = 350.0;
    
	static {
	    if(!OpenCVLoader.initDebug()) {
	        Log.d("ERROR", "Unable to load OpenCV");
	    } else {
	        Log.d("SUCCESS", "OpenCV loaded");
	    }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState); 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        _ocr = new OCR(getApplicationContext());
        //Log.i(TAG, ocr.findByRessource(R.drawable.training2_2).toString());
        //Log.i(TAG, ocr.findByRessource(R.drawable.training5_0).toString());
        //Log.i(TAG, ocr.findByRessource(R.drawable.training6_1).toString());

        _tampon = new int[8][9][9];
        _tamponOccur = new int[8];
        _itampon = 0;
        _itamponMax = 0;
        for(int k=0; k<8; k++) {
	    	for(int i=0; i<9; i++) {
	    		for(int j=0; j<9; j++) {
	    			_tampon[k][j][i] = 0;
	    		}
	    	}
	    	_tamponOccur[k] = 0;
        }
        _grille = new int[9][9];
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			_grille[j][i] = 0;
    		}
    	}
    	_solver = new Solver(3);
    	_solver.solve(_grille,true);
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
        
        //t = mOpenCvCameraView.getWidth();
        //Log.i(TAG, "width " + mOpenCvCameraView.getWidth());
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
    	Mat rgba = inputFrame.rgba();
    	double frameWidth = rgba.width();
    	double frameHeight = rgba.height();
    	double ratioWidth = rgba.width() / MAT_FAST_WIDTH;
	    double ratioHeight = rgba.height() / MAT_FAST_HEIGHT;
    	double patternSizeRgba = frameHeight * 0.9;
    	double patternSize = MAT_FAST_HEIGHT * 0.9;
    	
    	// resize original image smaller, to perform transformations faster
    	Mat rgbaGrid = rgba.clone();
    	ImageManager.resize(rgbaGrid, rgbaGrid, MAT_FAST_WIDTH, MAT_FAST_HEIGHT);
    	Imgproc.cvtColor(rgbaGrid, rgbaGrid, Imgproc.COLOR_RGB2GRAY);
    	
    	//Mat rgbaOCR = rgbaGrid.clone();
	    ImageManager.fastThreshold(rgbaGrid, rgbaGrid);
    	
    	Point[] gridPoints = ImageManager.findGrid(rgbaGrid, patternSize);
    	if(gridPoints != null) {
    		Log.i("ETAT", "GRILLE TROUVE");
    		
    		// draw square found
    		try {
				ImageManager.drawLines(rgba, gridPoints, ratioWidth, ratioHeight, COLOR_SQUARE_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
            /*Mat[][] matFigures = ImageManager.findFigures(rgbaGrid, gridPoints);
            String out = "";
            for(int i=0; i<9; i++) {
            	for(int j=0; j<9; j++) {
            		if(matFigures[j][i] != null) {
            			Point coordinates = new Point((frameWidth/2.0)-(patternSizeRgba/2)+(i*(patternSizeRgba/9))+10,
            					(frameHeight/2.0)-(patternSizeRgba/2)+(j*(patternSizeRgba/9))+35);
//            			ImageManager.pasteMat(matFigures[j][i], rgba, 
//            					new Point((frameWidth/2.0)-(patternSize/2)+(i*(patternSize/9)),
//            					(frameHeight/2.0)-(patternSize/2)+(j*(patternSize/9))));

            			int currentFigure = Math.round(_ocr.findByMat(matFigures[j][i]));
            			out += currentFigure + " ";
            			
            			if(currentFigure != 0) {
            				Core.putText(rgba, Integer.toString(currentFigure), coordinates, Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0,0,255,255), 3);
            			}
            		}
            		else
            			out += "0 ";
                }
            	out += "\n";
            }
            Log.i("GRILLE", out);*/
    	}
    	else {
    		Log.i("ETAT", "GRILLE NON TROUVE");
    	}
    	//ImageManager.resize(rgbaGrid, rgba, rgba.width(), rgba.height());
    	
    	// draw square pattern
    	ImageManager.drawSquare(rgba, new Point((frameWidth/2.0)-(patternSizeRgba/2),(frameHeight/2.0)-(patternSizeRgba/2)), patternSizeRgba, COLOR_SQUARE_PATTERN);

        return rgba;
    }
    
    private boolean estCarre(Mat contour) {
        MatOfPoint2f pointsList = new MatOfPoint2f();
        MatOfPoint2f approx = new MatOfPoint2f();
        double segLen;
        
    	contour.convertTo(pointsList, CvType.CV_32FC2);
		Imgproc.approxPolyDP(pointsList, approx, Imgproc.arcLength(pointsList, true)*0.02, true);
		if(Imgproc.contourArea(contour) < 500) return false;
		if(approx.toArray().length != 4) return false;
		double orderedPoints[] = getOrderedPoints(approx);
		segLen = (Math.abs(orderedPoints[0]-orderedPoints[2])+
				Math.abs(orderedPoints[3]-orderedPoints[5])+
				Math.abs(orderedPoints[4]-orderedPoints[6])+
				Math.abs(orderedPoints[1]-orderedPoints[7]))/4.0;
		if(Math.abs(orderedPoints[1]-orderedPoints[3]) > segLen/4.0) return false;
		if(Math.abs(orderedPoints[5]-orderedPoints[7]) > segLen/4.0) return false;
		if(Math.abs(orderedPoints[0]-orderedPoints[6]) > segLen/4.0) return false;
		if(Math.abs(orderedPoints[2]-orderedPoints[4]) > segLen/4.0) return false;
		
    	return true;
    }
    
    private double[] getOrderedPoints(MatOfPoint2f approx) {
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
    
    private int[][] buildTable(List<Rect> rects, List<Integer> values, Size size) {
    	int table[][] = new int[9][9];
    	int lenCase = (int)size.width/9;
    	
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			table[j][i] = 0;
    		}
    	}
		for(int k=0; k<rects.size(); k++) {
			int mx = (rects.get(k).x+rects.get(k).width)-(rects.get(k).width/2);
			int my = (rects.get(k).y+rects.get(k).height)-(rects.get(k).height/2);
			int x = mx/lenCase;
			int y = my/lenCase;
			table[y][x] = values.get(k);
		}
    	return table;
    }
    
    private boolean changerGrille() {
    	boolean change = false;
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			if(_grille[j][i] != _tampon[_itamponMax][j][i]) {
    				_grille[j][i] = _tampon[_itamponMax][j][i];
    				change = true;
    			}
    		}
    	}
    	return change;
    }
    
    private void remplirTampon(int[][] s) {
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			_tampon[_itampon][j][i] = s[j][i];
    			_tamponOccur[_itampon] = 0;
    			for(int k=0; k<8; k++) {
    				if(s[j][i] == _tampon[k][j][i]) _tamponOccur[_itampon]++;
    			}
    		}
    	}
    	_itamponMax = 0;
		for(int k=1; k<8; k++) {
			if(_tamponOccur[k] > _tamponOccur[_itamponMax]) _itamponMax = k;
		}
    	_itampon = (_itampon+1)%8;
    }
}