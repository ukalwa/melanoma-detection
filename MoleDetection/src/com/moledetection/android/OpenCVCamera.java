package com.moledetection.android;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
@SuppressWarnings("deprecation")
public class OpenCVCamera extends Activity implements CvCameraViewListener2,OnTouchListener  {
	private static final String TAG = "OpenCVCamera::Activity";
	private static final String mDirName = "SkinCancerDetection";
	private Button button;
	private boolean buttonClicked=false;
	private Mat mHierarchy;
	private Mat mDilatedMask;
	private Mat mIntermediateMatsub;
	private Mat mHSVsub;
	private Mat mHSV;
	private Mat mPyrDownMat;
	private CustomCameraView mOpenCvCameraView;
	private Integer mCounter = 000; 
	
	private List<Camera.Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
	private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    
    private Point pt,pt1,pt2,pt3,pt4,pt5,pt6,CentrePt;
    private Rect rect,rect2;
    private Mat mResult,mSubResult,mRoi;
    private boolean onTouch = false; 
    //private Size mSize;
	

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(OpenCVCamera.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    public OpenCVCamera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_open_cvcamera);
		mOpenCvCameraView = (CustomCameraView) findViewById(R.id.opencv_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        addListenerOnButton();
        File mDirectory = new File(Environment.getExternalStorageDirectory(), "/" + mDirName);
        if(!mDirectory.exists())
        {
        	if(!mDirectory.mkdir())
        	{
        		Log.i(TAG, "Unable to create directory");
        	}
        	Log.i(TAG, "Directory Created");
        }
	}
	
	public void addListenerOnButton() {
		 
		button = (Button) findViewById(R.id.button1);
		final String  cs = button.getText().toString();
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!onTouch)
					Toast.makeText(OpenCVCamera.this, "Please select atleast one picture before saving it", Toast.LENGTH_SHORT).show();
				else if(cs.equals("Proceed"))
				{
					
				}
				else
				{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",Locale.US);
		        String currentDateandTime = sdf.format(new Date());
		        String fileName = Environment.getExternalStorageDirectory().getPath() +
		        		"/" + mDirName +
		                               "/SDA_" + currentDateandTime + "_" + mCounter.toString() + ".jpg";
		        Imgproc.cvtColor(mSubResult, mSubResult, Imgproc.COLOR_RGB2BGRA);
		        Highgui.imwrite(fileName, mSubResult);
		        mOpenCvCameraView.disableView();
		        button.setText("Proceed");
		        //mOpenCvCameraView.takePicture(fileName);
		        Log.i(TAG,fileName);
		        Toast.makeText(OpenCVCamera.this, fileName + " saved", Toast.LENGTH_SHORT).show();
		        buttonClicked = true;
				}
			}
 
		});
 
	}

	@Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		//mRgba = new Mat(height, width, CvType.CV_8UC1);
		mHSV = new Mat();
		mResult = new Mat();
		mSubResult = new Mat((width+8)/2-width/4, (height-8)/2+height/4, CvType.CV_8UC4);
	    mDilatedMask = new Mat();
	    mHierarchy = new Mat();
	    mHSVsub = new Mat();
	    mIntermediateMatsub = new Mat();
	    mPyrDownMat = new Mat();
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        //mSize = new Size(3,3);
        CONTOUR_COLOR = new Scalar(255,255,255,255);
        CentrePt = new Point(width/2,height/2);
        pt = new Point((width+8)/2-width/8,(height+8)/2-height/8);
        pt1 = new Point(width/2+width/8,height/2-height/8);
		pt2 = new Point(width/2-width/8,height/2+height/8);
		pt3 = new Point((width-8)/2+width/8,(height+8)/2-height/8);
		pt4 = new Point((width+8)/2-width/8,(height-8)/2+height/8);
		//For displaying enlarged image
		pt5 = new Point (0,0);
		pt6 = new Point((width-8)/2+width/4,(height+8)/2-height/4);
		rect = new Rect(pt3,pt4);
		rect2 = new Rect(pt5,mSubResult.size());
		
	}
	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mHSV.release();
		mResult.release();
		mSubResult.release();
	    mDilatedMask.release();
	    mHierarchy.release();
	    mHSVsub.release();
	    mIntermediateMatsub.release();
	    mPyrDownMat.release();
        mSpectrum.release();
	}
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		//mRgba = inputFrame.gray();
		//Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL);
		mRoi = mRgba.submat(rect);
		if(onTouch)
			mSubResult.copyTo(mRgba.submat(rect2));
		//Mat mSubGray = mRgba.submat(rect);
		//Imgproc.cvtColor(mRoi, mSubGray, Imgproc.COLOR_RGB2HSV_FULL);
		//Mat mBlur = new Mat();
		/*Imgproc.GaussianBlur(mSubGray, mSubGray ,new Size(3,3) , 0, 0, Imgproc.BORDER_DEFAULT);
		Imgproc.dilate(mSubGray, mSubGray, new Mat());
        Imgproc.erode(mSubGray, mSubGray, new Mat());
        Scalar sc = Core.mean(mSubGray);
        Log.i(TAG, "Avg pixel value : " + sc.val[0]);
        //Imgproc.Canny(mSubGray, mIntermediateMatsub, 180, 250);
        Imgproc.threshold(mSubGray, mIntermediateMatsub, sc.val[0], 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);
        //Imgproc.adaptiveThreshold(mSubGray, mIntermediateMatsub, sc.val[0], Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 3, 1);
        */
		Mat mSubHsv = new Mat(mRoi.size(), CvType.CV_8UC4);
		//Scalar sc = Core.mean(mSubHsv);
		
		Imgproc.GaussianBlur(mRoi, mRoi ,new Size(5,5) , 0, 0, Imgproc.BORDER_DEFAULT);
        
        Log.i(TAG, "Avg pixel value : " + mBlobColorHsv.val[0]);
       //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
		Imgproc.cvtColor(mRoi, mSubHsv, Imgproc.COLOR_RGB2HSV_FULL);
		Imgproc.dilate(mSubHsv, mSubHsv, new Mat());
        Imgproc.erode(mSubHsv, mSubHsv, new Mat());
        Imgproc.Canny(mSubHsv, mIntermediateMatsub, 180, 180*2);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mIntermediateMatsub, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(mRgba, contours, -1, new Scalar(255,255,255,255), 4, 8, mHierarchy, 0, pt);
        //mRoi = mRgba.submat(rect);

		//Imgproc.resize(mRoi, mRgba, mRgba.size());
		//mSubHsv.copyTo(mRoi);
		//mSubHsv.copyTo(inputFrame.rgba().submat(rect));
		//Imgproc.resize(mSubHsv, mRgba, mRgba.size());
		//mRoi.release();
        mSubHsv.release();
		/*Mat mGray = new Mat(mRoi.size(), CvType.CV_8UC1);
		Mat mBlur = new Mat();
		//Imgproc.GaussianBlur(mRoi, mBlur , mSize , 0, 0, Imgproc.BORDER_DEFAULT);
		Imgproc.cvtColor(mRoi, mGray, Imgproc.COLOR_RGB2GRAY);
		//Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 3, 1);
		Imgproc.threshold(mGray, mGray, 128, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mGray, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(mRgba, contours, -1, new Scalar(255,255,255,255), 4, 8, mHierarchy, 0, pt);
		mGray.release();
		mBlur.release();*/
		//Imgproc.resize(mIntermediateMatsub, mResult, mRgba.size());
		Core.rectangle(mRgba, pt1, pt2, CONTOUR_COLOR, 2);
		//Core.circle(mRgba, CentrePt, 4, CONTOUR_COLOR,-2,4,0);
		//Core.circle(mRgba, CentrePt, (int) ((CentrePt.x)*0.125), CONTOUR_COLOR);
		mIntermediateMatsub.release();
		return mRgba;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
           String element = effectItr.next();
           mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
           idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];
        ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Camera.Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Camera.Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint({ "SimpleDateFormat", "ClickableViewAccessibility" })
    @Override
    public boolean onTouch(View v, MotionEvent event) {
		if(buttonClicked)
    		Toast.makeText(this, "Press Proceed button to process selected image", Toast.LENGTH_LONG).show();
		else
		{
        Log.i(TAG,"onTouch event");
        Imgproc.resize(mRoi, mSubResult, mSubResult.size());
        onTouch = true;
        mCounter++;
		}
        return false;
    }
}
