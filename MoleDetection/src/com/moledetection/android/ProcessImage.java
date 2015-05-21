package com.moledetection.android;
 
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
 
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
 
public class ProcessImage extends Activity {
 
    protected static final String TAG = "ProcessImage::Activity";
    private String message;
    private ImageView imageView;
    private Bitmap bm,newBmp;
    private Mat originalMat,tempMat;
    private Mat grayMat,hsvMat,mIntermediateMatsub,zeroMat;
 
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.out.print("OpenCV loaded successfully");
                    System.loadLibrary("active_contour");
                    processImage();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                    System.out.print("OpenCV loading failed");
                } break;
            }
        }
    };
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);
        Intent intent = getIntent();
        message = intent.getStringExtra(GetPreviousPictures.FILEPATH);
        imageView = (ImageView) findViewById(R.id.image1);
         
    }
     
    protected void processImage() {
        bm = BitmapFactory.decodeFile(message);
        tempMat = new Mat(bm.getHeight(),bm.getWidth(),CvType.CV_8UC3);
        originalMat = new Mat(bm.getHeight(),bm.getWidth(),CvType.CV_8UC3);
        Utils.bitmapToMat(bm, originalMat);
        zeroMat = Mat.zeros(tempMat.size(), CvType.CV_8UC1);
        //CLAHE c = null;
        //c.apply(tempMat, originalMat);
        //Imgproc.e
        //tempMat.release();
        /*tempMat = Highgui.imread(message);
        Point pt1 = new Point(tempMat.cols()/2+tempMat.cols()/4,tempMat.rows()/2-tempMat.rows()/4);
        Point pt2 = new Point(tempMat.cols()/2-tempMat.cols()/4,tempMat.rows()/2+tempMat.rows()/4);
        originalMat = tempMat.submat(new Rect(pt1,pt2));*/
        //originalMat = Highgui.imread(message);
        System.out.print("Original Mat created from file");
        grayMat = new Mat(originalMat.size(),CvType.CV_8UC1);
        hsvMat = new Mat(originalMat.size(),CvType.CV_8UC3);
        mIntermediateMatsub = new Mat(originalMat.size(),CvType.CV_8UC3);
        Imgproc.medianBlur(originalMat, originalMat ,5);
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(originalMat, hsvMat, Imgproc.COLOR_BGR2HSV);
        Imgproc.dilate(grayMat, grayMat, new Mat(3,3,CvType.CV_8UC1));
        Imgproc.erode(grayMat, grayMat, new Mat(3,3,CvType.CV_8UC1));
        Imgproc.Canny(grayMat, grayMat, 50, 50*2);
        Imgproc.dilate(hsvMat, mIntermediateMatsub,new Mat(5,5,CvType.CV_8UC1));
        Imgproc.erode(mIntermediateMatsub, hsvMat,new Mat(5,5,CvType.CV_8UC1));
        //Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 3, 1);
        //Imgproc.threshold(grayMat, grayMat, 128, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);
        //Imgproc.Canny(mIntermediateMatsub, mIntermediateMatsub, 100, 100*3);
        //List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
         
        //Mat mHierarchy = new Mat();
        //Imgproc.findContours(mIntermediateMatsub, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.drawContours(hsvMat, contours, -1, new Scalar(255,255,255,255), 4, 4, mHierarchy, 0, new Point(0,0));
        //Test active contour        
        /*
        List<Mat> channels = new ArrayList<Mat>();
        Core.split(hsvMat, channels );
        Mat luminance = new Mat(originalMat.size(),CvType.CV_8UC1);
        //Imgproc.equalizeHist(channels.get(2), luminance);
        Imgproc.threshold(channels.get(2), luminance, 128, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);
        luminance.copyTo(channels.get(2));
        Core.merge(channels, hsvMat);
        */  
        ActiveContour(hsvMat.getNativeObjAddr(),zeroMat.getNativeObjAddr(),originalMat.getNativeObjAddr());
        newBmp = Bitmap.createBitmap(hsvMat.cols(), hsvMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(zeroMat, newBmp);
        imageView.setImageBitmap(newBmp);
        hsvMat.release();
        grayMat.release();
        //tempMat.release();
         
    }
 
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }
     
    public native void ActiveContour(long matAddrHsv,long matAddrGr, long matAddrRgba);
}