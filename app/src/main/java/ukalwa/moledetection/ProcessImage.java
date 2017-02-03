package ukalwa.moledetection;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ProcessImage extends Activity {

    protected static final String TAG = "ProcessImage::Activity";
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private ViewFlipper mViewFlipper;
    private String mDir = "MoleDetection";
    private Context mContext;
    private long startTime,timeElapsed;
    //private GestureDetector.OnGestureListener listener;
    //private final GestureDetector detector = new GestureDetector(mContext, listener);

    private String message;
    private ImageView imageView;
    private ImageView imageViewAbove;
    private TextView textViewColor;
    private Bitmap bm,newBmp;
    private Mat originalMat,tempMat,resultMat;
    private Mat grayMat,hsvMat,mIntermediateMatsub,grMat,zeroMat;
    private Mat displayMat;
    private Mat matrixAbove;
    private Mat colorLabel;
    private ColorDetection colorProcess;
    private Scalar colorsPrint[] = new Scalar[7];
    private String fileWithContour,fileWithoutExtension;
    private String colorsName[] = new String[7];
    private double contoursArea = 0,horizontalAsymmetry = 0,verticalAsymmetry = 0;
    private double contoursPerimter=0,contourLength=0;

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

    private Button NextBtn;
    private Button PreviousBtn;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private Bitmap imagebmp1;
    private Bitmap imagebmp2;
    private TextView imageTitleText1;
    private TextView imageTitleText3;
    private TextView imageTitleText2;
    private TextView imageTitleText4;
    private Bitmap imagebmp4;
    private Bitmap imagebmp5,aboveBmp;
    private RelativeLayout layout4;
    private ProgressDialog pDialog;
    private TextView resultView;
    private TextView resultTitle;
    private ImageView imageView5;
    private ImageView resultImage;
    private Bitmap resultimgbmp;
    //private TextView resultView1;
    //private TextView resultView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
        setContentView(R.layout.activity_process_image);
        mContext = this;
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.view_flipper);

        PreviousBtn = (Button) findViewById(R.id.previousbutton);
        PreviousBtn.setVisibility(Button.INVISIBLE);
        PreviousBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewFlipper.showPrevious();
                NextBtn.setVisibility(Button.VISIBLE);
                if(mViewFlipper.getDisplayedChild()==0)
                    PreviousBtn.setVisibility(Button.INVISIBLE);
				/*//Toast.makeText(mContext, "Child View count " + mViewFlipper.getDisplayedChild(), Toast.LENGTH_SHORT).show();
			      if(mViewFlipper.getDisplayedChild()==2)
			      {
			        Toast.makeText(mContext, "Horizontal Asymmetry : " + horizontalAsymmetry +"Contour Area : " + contoursArea, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Vertical Asymmetry Area : " + verticalAsymmetry +"Contour Area : " + contoursArea, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Horizontal Asymmetry/Contour Area : " + horizontalAsymmetry/contoursArea, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Vertical Asymmetry/Contour Area : " + verticalAsymmetry/contoursArea, Toast.LENGTH_SHORT).show();
			      }
			      if(mViewFlipper.getDisplayedChild()==1)
			      {
			    	  Toast.makeText(mContext, "Contour Size : " + contourLength, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Contour Area : " + contoursArea, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Contour Perimeter : " + contoursPerimter, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Irregularity Factor : " + (contoursPerimter*contoursPerimter*7)/(4*22*contoursArea), Toast.LENGTH_SHORT).show();
			      }*/
            }
        });
        NextBtn = (Button) findViewById(R.id.nextbutton);
        NextBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PreviousBtn.setVisibility(Button.VISIBLE);
                mViewFlipper.showNext();
                //Toast.makeText(mContext, "Child View count " + mViewFlipper.getDisplayedChild(), Toast.LENGTH_SHORT).show();
                if(mViewFlipper.getDisplayedChild()==mViewFlipper.getChildCount()-1)
                    NextBtn.setVisibility(Button.INVISIBLE);
			    /*if(mViewFlipper.getDisplayedChild()==2)
			    {
			    	Toast.makeText(mContext, "Horizontal Asymmetry : " + horizontalAsymmetry +"Contour Area : " + contoursArea, Toast.LENGTH_SHORT).show();
			    	Toast.makeText(mContext, "Vertical Asymmetry Area : " + verticalAsymmetry +"Contour Area : " + contoursArea, Toast.LENGTH_SHORT).show();
			    	Toast.makeText(mContext, "Horizontal Asymmetry/Contour Area : " + horizontalAsymmetry/contoursArea, Toast.LENGTH_SHORT).show();
			    	Toast.makeText(mContext, "Vertical Asymmetry/Contour Area : " + verticalAsymmetry/contoursArea, Toast.LENGTH_SHORT).show();
			    }
			    if(mViewFlipper.getDisplayedChild()==1)
			    {
			    	//Toast.makeText(mContext, "Contour Size : " + contourLength, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Contour Area : " + contoursArea, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Contour Perimeter : " + contoursPerimter, Toast.LENGTH_SHORT).show();
			        Toast.makeText(mContext, "Irregularity Factor : " + (contoursPerimter*contoursPerimter*7)/(4*22*contoursArea), Toast.LENGTH_SHORT).show();
			    }*/

            }
        });
        Intent intent = getIntent();
        message = intent.getStringExtra(GetPreviousPictures.FILEPATH);




        colorsPrint[1] = new Scalar(240,0,63,255);
        colorsPrint[2] = new Scalar(0,255,255,255);
        colorsPrint[3] = new Scalar(0,0,204,255);
        colorsPrint[4] = new Scalar(0,0,0,255);
        colorsPrint[5] = new Scalar(0,153,0,255);
        colorsPrint[6] = new Scalar(255,255,0,255);





        colorsName[1] = "BLACK";
        colorsName[2] = "LIGHT-BROWN";
        colorsName[3] = "DARK-BROWN";
        colorsName[4] = "RED";
        colorsName[5] = "GRAY";
        colorsName[6] = "WHITE";

        imageView1 = (ImageView) findViewById(R.id.image1);
        imageView2 = (ImageView) findViewById(R.id.image2);
        imageView3 = (ImageView) findViewById(R.id.image3);
        imageView4 = (ImageView) findViewById(R.id.image4);
        imageView5 = (ImageView) findViewById(R.id.color_image1);
        imageViewAbove = (ImageView) findViewById(R.id.color_image2);
        resultImage = (ImageView) findViewById(R.id.result_image);
        textViewColor = (TextView) findViewById(R.id.textViewColor);

        imageTitleText1 = (TextView) findViewById(R.id.ImageTitleText1);
        imageTitleText2 = (TextView) findViewById(R.id.ImageTitleText2);
        imageTitleText3 = (TextView) findViewById(R.id.ImageTitleText3);
        imageTitleText4 = (TextView) findViewById(R.id.ImageTitleText4);

        resultView = (TextView) findViewById(R.id.resultView);
        resultTitle = (TextView) findViewById(R.id.ResultTitle);
    }


    protected int processImage() {

        Log.i(TAG, "fileName : " + message);
        StringTokenizer str = new StringTokenizer(message, ".");
        //Log.i(TAG, "strToken1 : " + str.nextToken());
        //Log.i(TAG, "strToken2 : " + str.nextToken());
        bm = BitmapFactory.decodeFile(message); //Extract Image from the path sent in intent message
        originalMat = new Mat(bm.getHeight(),bm.getWidth(),CvType.CV_8UC3);
        Utils.bitmapToMat(bm, originalMat); //Convert Image to Mat for OpenCV operations

        fileWithoutExtension = str.nextToken() + "_" + android.text.format.DateFormat.format("yyyy_MM_dd", new java.util.Date()) ;
        fileWithContour = fileWithoutExtension + ".PNG";
        String segmentedImage = fileWithoutExtension + "_segmented.PNG";
        String warpedImage = fileWithoutExtension + "_warped.PNG";
        String roiHorizontalFileName = fileWithoutExtension + "_roi_horizontal.PNG";
        String roiVerticalFileName = fileWithoutExtension + "_roi_vertical.PNG";

        System.out.print("Original Mat created from file");

        /*
         * Active Contour Extraction process begins
         */
        //Imgproc.medianBlur(originalMat, originalMat ,5);							//Blur the image to reduce noise in the image

        //Initialize Mat objects
        grayMat = new Mat(originalMat.size(),CvType.CV_8UC1);

        hsvMat = new Mat(originalMat.size(),CvType.CV_8UC3);
        mIntermediateMatsub = new Mat(originalMat.size(),CvType.CV_8UC3);

        grMat = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
        //displayMat = originalMat.clone();
        zeroMat = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);				//Initialize gray image from original Image
//
//        //Morphological operations on the images to reduce noise with 3x3 kernel
//        Imgproc.dilate(grayMat, grayMat, new Mat(3,3,CvType.CV_8UC1));
//        Imgproc.erode(grayMat, grayMat, new Mat(3,3,CvType.CV_8UC1));
//
//        //Canny with threshold multiplier of 2 and morphological operations with 5x5 kernel
//        Imgproc.Canny(grayMat, grayMat, 50, 50*2);
//        Imgproc.dilate(originalMat, mIntermediateMatsub,new Mat(5,5,CvType.CV_8UC1));
//        Imgproc.erode(mIntermediateMatsub, originalMat,new Mat(5,5,CvType.CV_8UC1));
//
        Imgproc.cvtColor(originalMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        if(new File(fileWithContour).exists()){
            Bitmap bmp_temp = BitmapFactory.decodeFile(fileWithContour);
            tempMat = new Mat(bmp_temp.getHeight(),bmp_temp.getWidth(),CvType.CV_8UC3);
            Utils.bitmapToMat(bmp_temp, tempMat);
        }
        else{
            //tempMat = new Mat(originalMat.size(), CvType.CV_8UC3);
            //Utils.bitmapToMat(bm, tempMat);
            tempMat = originalMat.clone();
        }
        //Initialize Hsv image from original Image

        /*
        //Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 3, 1);
        //Imgproc.threshold(grayMat, grayMat, 128, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);
        //Imgproc.Canny(mIntermediateMatsub, mIntermediateMatsub, 100, 100*3);
        //List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        //Mat mHierarchy = new Mat();
        //Imgproc.findContours(mIntermediateMatsub, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.drawContours(hsvMat, contours, -1, new Scalar(255,255,255,255), 4, 4, mHierarchy, 0, new Point(0,0));
        //Test active contour
        */
        /*
        List<Mat> channels = new ArrayList<Mat>();
        Core.split(hsvMat, channels );
        Mat luminance = new Mat(originalMat.size(),CvType.CV_8UC1);
        //Imgproc.equalizeHist(channels.get(2), luminance);
        Imgproc.threshold(channels.get(2), luminance, 128, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);
        luminance.copyTo(channels.get(2));
        Core.merge(channels, hsvMat);
        */

        //Calling Active Contour native method written in C++
        ActiveContour(hsvMat.getNativeObjAddr(),grMat.getNativeObjAddr(),originalMat.getNativeObjAddr());

        /*
         * 1) Extraction of contour with maximum area to eliminate contours of noises
         * 2) Finding bounding rectangle of the contour to calculate major and minor axes of the contour
         * 3) Flip the contour in Horizontal and Vertical axes and display as 2 rows
         */

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat mHierarchy = new Mat();
        double maxArea = -1;
        int maxAreaIdx = -1;
        List<Mat> contoursMat= new ArrayList<Mat>();

        RotatedRect ellipse = null;
        MatOfPoint2f point2F;
        Imgproc.findContours(grMat, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.i(TAG, "Contours size : " + contours.size());

        if (contours.size() == 0){
            Log.i(TAG, "Active contour screwed up");
            return 1;
        }
        else {
            for (int idx = 0; idx < contours.size(); idx++) {
                Mat contour = contours.get(idx);
                contoursMat.add(contour);
                double contourarea = Imgproc.contourArea(contour);
                if (contourarea > maxArea) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                }
            }

            Rect r = Imgproc.boundingRect(contours.get(maxAreaIdx));
            point2F = new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
            ellipse = Imgproc.fitEllipse(point2F);                                    //Ellipse Fitting function
            //Imgproc.warpAffine(originalMat, tempMat, rot_mat, originalMat.size());
            //////////// End ///// Contours detection ///////////////////////////////

            ///////// Init //// Color detection /////////////////////
            MatOfPoint contour = contours.get(maxAreaIdx);
            int nColors = 0;
            matrixAbove = Mat.zeros(originalMat.size(), CvType.CV_8UC3);
            Mat tempMat2 = Mat.zeros(originalMat.size(), CvType.CV_8UC3);
            Log.i(TAG, "Image View height : " + imageViewAbove.getHeight() + " Image View Width : " + imageViewAbove.getWidth());
            Log.i(TAG, "Mat Above" + matrixAbove.size() + " Help Matrix: " + tempMat2.size());
            Core.bitwise_not(tempMat2, matrixAbove);
            tempMat2.release();
            Mat helpMatrix = matrixAbove.clone();

            //remove back ground of the mole
            Mat maskZero = Mat.zeros(tempMat.size(), CvType.CV_8UC1);
            Imgproc.drawContours(maskZero, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), -1);
            Mat tempBlack = new Mat(tempMat.size(), tempMat.type());
            tempMat.copyTo(tempBlack, maskZero);

            colorProcess = new ColorDetection(originalMat.clone());
            Moments p = Imgproc.moments(contours.get(maxAreaIdx));
            int xCenter = (int) (p.get_m10() / p.get_m00());
            int yCenter = (int) (p.get_m01() / p.get_m00());
            colorProcess.insertMiddlePoint(xCenter, yCenter);
            colorProcess.setMoleWithoutBackground(tempBlack);
            colorProcess.setContourMole(contours.get(maxAreaIdx));
            colorProcess.setRect(r);
            colorProcess.setMatrixAbove(helpMatrix);

            colorProcess.colorProcessImage();
            Mat colorMat = colorProcess.getColorProcessImage();
            //originalMat = colorProcess.getColorOftheImage();
            helpMatrix = colorProcess.getMatrixAbove();
            nColors = colorProcess.getNumberOfColors();
            Imgproc.drawContours(colorMat, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), 2);


            //textViewColor.setText("No of Colors: "+nColors);

            //////////// Init ///// Abrupt Edge ////////////////////////

            //originalMat = colorProcess.getOriginalImage();
            List<MatOfPoint> cAbruptEdgeColor = colorProcess.returnCountoursAbruptColor();
            //Imgproc.drawContours(originalMat, cAbruptEdgeColor, colorProcess.maxArea(cAbruptEdgeColor), new Scalar(255,255,0,255), 1); //yellow

            List<MatOfPoint> border;
            double areaContourPart, areaColorPart;
            double taThreshold = 0.1;
            int abruptEdges = 0;
            for (int i = 1; i < 9; i++) {
                border = new ArrayList<MatOfPoint>();
                border = colorProcess.getContoursPartMole(contours, i);
                //Imgproc.drawContours(originalMat, border, colorProcess.maxArea(border), new Scalar(0,153,0,255), 1); // green
                areaContourPart = colorProcess.getContoursArea(border);

                border = new ArrayList<MatOfPoint>();
                border = colorProcess.getContoursPartMole(cAbruptEdgeColor, i);
                //Imgproc.drawContours(originalMat, border, colorProcess.maxArea(border), new Scalar(0,0,204,255), 1); //dark blue
                areaColorPart = colorProcess.getContoursArea(border);

                if (1 - (double) (areaContourPart / areaColorPart) <= taThreshold) {
                    abruptEdges++;
                }
            }
            //textViewColor.setText("Abrupt Ed: "+abruptEdges);
            //Imgproc.drawContours(originalMat, contours, maxAreaIdx, new Scalar(255,255,255,255), 1);
            //contour.release();

            //////////////// End ///////////// Abrupt Edge ////////////////
            textViewColor.setText("NColor: " + nColors + " AEdge: " + abruptEdges);

            //contourLength = contours.size();

            displayMat = Mat.zeros(originalMat.size(), CvType.CV_8UC3);
            Core.bitwise_not(displayMat, displayMat);
            tempMat.copyTo(displayMat, maskZero);
            double angle = ellipse.angle;
            Log.i(TAG, "rotation angle,width,height : " + angle + "," + ellipse.size.width + "," + ellipse.size.height);
            if (ellipse.size.width < ellipse.size.height) {
                if (angle < 90)
                    angle -= 90;
                if (angle > 90)
                    angle += 90;
            }
            RotatedRect rot = Imgproc.minAreaRect(point2F);
            Mat rot_mat = Imgproc.getRotationMatrix2D(ellipse.center, angle, 1);

            //Added to clear blank asymmetry images
            Mat rot_only_contour = Mat.zeros(displayMat.size(), CvType.CV_8UC1);
            Imgproc.drawContours(rot_only_contour, contours, maxAreaIdx, new Scalar(255, 255, 255), -1);
            Imgproc.warpAffine(rot_only_contour, rot_only_contour, rot_mat, rot_only_contour.size());
            //end


            List<MatOfPoint> contours_rot = new ArrayList<MatOfPoint>();
            Mat mHierarchy_rot = new Mat();
            Mat grImage_rot = new Mat();
            //Mat displayMatOriginal = Mat.zeros(originalMat.size(), CvType.CV_8UC3);
            //Mat test = displayMat.clone();
            //test.copyTo(displayMatOriginal.submat(r));
            Mat displayMatOriginal = displayMat.clone();
            Log.i(TAG, "displayMat.size,originalMat.size : " + displayMat.size() + "," + originalMat.size());
            Imgproc.warpAffine(displayMat, displayMat, rot_mat, displayMat.size());
            Imgproc.cvtColor(displayMat, grImage_rot, Imgproc.COLOR_BGR2GRAY);
            Imgproc.findContours(grImage_rot, contours_rot, mHierarchy_rot, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            //Imgproc.drawContours(displayMat, contours_rot, 1, new Scalar(255,0,0), 2);
            Mat warpedMat = displayMat.clone();
            Imgproc.cvtColor(displayMat, displayMat, Imgproc.COLOR_BGRA2BGR);

            Mat croppedMat = new Mat();
//        ArrayList<Mat> list = new ArrayList<Mat>();
//        ArrayList<Mat> croppedList = new ArrayList<Mat>();
//        Core.split(displayMat, list);
            Log.i(TAG, Integer.toString(displayMat.channels()));
//        for(int i =0 ; i<list.size();i++){
//        	Imgproc.getRectSubPix(list.get(i), rot.size, rot.center, croppedMat);
//        	croppedList.add(croppedMat);
//        }
//        Core.merge(croppedList, croppedMat);

            //***************
            //Display 1st image with contour on the original image
            //***************
            Imgproc.getRectSubPix(displayMat, ellipse.size, ellipse.center, croppedMat);
            Imgproc.drawContours(originalMat, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), 2);
            imagebmp1 = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(originalMat, imagebmp1);
            imageView1.setImageBitmap(imagebmp1);
            imageTitleText1.setText("Original Image with Contour");

            //MatOfPoint2f mMOP2f1 = new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
            //contours.get(maxAreaIdx).convertTo(mMOP2f1, CvType.CV_32FC2);

            //***************
            //Display next image with only contour on the zeros image
            //***************
            Core.bitwise_not(grMat, grMat);
            //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(240,0,63,255), 2); 	//red, cycles = 25
            //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0,255,255,255), 2);	//cyan, cycles = 50
            Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0, 153, 0, 255), 2);    //blue, cycles = 75
            //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0,0,204,255), 2);	//violet cycles = 100
            //MinMaxLocResult minMaxPoints = Core.minMaxLoc(grMat);

            imagebmp2 = Bitmap.createBitmap(displayMatOriginal.cols(), displayMatOriginal.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(displayMatOriginal, imagebmp2);
            imageView2.setImageBitmap(imagebmp2);
            //imageTitleText2.setText("Extracted Contour");
            imageTitleText2.setText("Segmented Image");

            Core.bitwise_not(rot_only_contour, rot_only_contour);
            Imgproc.findContours(rot_only_contour.clone(), contours_rot, mHierarchy_rot, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            double maxArea_rot = -1;
            int maxAreaIdx_rot = -1;
            for (int idx = 0; idx < contours_rot.size(); idx++) {
                Mat contour_rot = contours_rot.get(idx);
                contoursMat.add(contour_rot);
                double contourarea = Imgproc.contourArea(contour_rot);
                if (contourarea > maxArea_rot) {
                    maxArea = contourarea;
                    maxAreaIdx_rot = idx;
                }
            }

            Log.i(TAG, "Contours size : " + contours_rot.size() + "Max Area Index : " + maxAreaIdx_rot);
            r = Imgproc.boundingRect(contours_rot.get(maxAreaIdx_rot));
            //Core.rectangle(zeroMat, r.tl(), r.br(), new Scalar(255,255,255));
            //Imgproc.drawContours(zeroMat, contours_rot, maxAreaIdx_rot, new Scalar(255,255,255,255), -1);


            Rect rot_rect = Imgproc.boundingRect(contours_rot.get(maxAreaIdx_rot));
            Mat contourRegion = rot_only_contour.clone().submat(rot_rect);
            Mat flipContourHorizontal = new Mat(contourRegion.size(), CvType.CV_8UC1);
            Mat flipContourVertical = new Mat(contourRegion.size(), CvType.CV_8UC1);
            Mat diffHorizontal = new Mat(contourRegion.size(), CvType.CV_8UC1);
            Mat diffVertical = new Mat(contourRegion.size(), CvType.CV_8UC1);

            Core.flip(contourRegion, flipContourHorizontal, 1);
            Core.flip(contourRegion, flipContourVertical, 0);
            Core.compare(contourRegion, flipContourHorizontal, diffHorizontal, Core.CMP_EQ);
            Core.compare(contourRegion, flipContourVertical, diffVertical, Core.CMP_EQ);

            Mat roiHorizontal = new Mat(contourRegion.size(), CvType.CV_8UC1);
            Mat roiVertical = new Mat(contourRegion.size(), CvType.CV_8UC1);

            Core.bitwise_not(diffHorizontal, roiHorizontal);
            Core.bitwise_not(diffVertical, roiVertical);
            diffHorizontal.release();
            diffVertical.release();
            flipContourHorizontal.release();
            flipContourVertical.release();

            //Mat temp = new Mat(originalMat.size(),originalMat.type());
            //Mat mask = new Mat(originalMat.size(), CvType.CV_8UC3);
            //contourRegion.copyTo(mask.submat(r));

            //Imgproc.resize(diff, hsvMat, hsvMat.size());
            //Core.ellipse(hsvMat, r,new Scalar(255,255,0,255),2,8);

            //Imgproc.drawContours(zeroMat, contours, maxAreaIdx, new Scalar(255,255,255,255), -1);
            //Core.rectangle(zeroMat, r.br(), r.tl(), new Scalar(255,255,0,255),1);

        /*
         * Area calculation
         */
            //Calculate Contour Area

            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            contoursArea += Imgproc.contourArea(contour);
            // = Core.countNonZero(contourRegion);
            contoursPerimter = Imgproc.arcLength(curve, true);
            //contoursPerimter = Core.countNonZero(grMat);
            //contoursPerimter = Core.countNonZero(grMat);
            horizontalAsymmetry = Core.countNonZero(roiHorizontal);
            verticalAsymmetry = Core.countNonZero(roiVertical);

      /*Imgproc.findContours(roiHorizontal, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for(int a = 0; a < contours.size(); a++){
        	MatOfPoint contourPoint = contours.get(a);
        	horizontalAsymmetry += Imgproc.contourArea(contourPoint);
        }

        Imgproc.findContours(roiHorizontal, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for(int a = 0; a < contours.size(); a++){
        	MatOfPoint contourPoint = contours.get(a);
        	verticalAsymmetry += Imgproc.contourArea(contourPoint);
        }
       */
      /*
        resultMat = new Mat(2*roiHorizontal.rows(),roiHorizontal.cols(),roiHorizontal.type());
        Mat tmp = resultMat.submat(new Rect(0,0,roiHorizontal.cols(),roiHorizontal.rows()));
        roiHorizontal.copyTo(tmp);
        tmp = resultMat.submat(new Rect(0,roiVertical.rows(),roiVertical.cols(),roiVertical.rows()));
        roiVertical.copyTo(tmp);
       */
      /*
      //Display HSV Image with maximum contour bounded by rectangle
      Core.rectangle(hsvMat, r.br(), r.tl(), new Scalar(255,255,0,255),1);
      Imgproc.drawContours(hsvMat, contours, maxAreaIdx, new Scalar(255,255,255,255), 2);
      imagebmp4 = Bitmap.createBitmap(hsvMat.cols(), hsvMat.rows(), Bitmap.Config.ARGB_8888);
      Utils.matToBitmap(hsvMat, imagebmp4);
      imageView4.setImageBitmap(imagebmp4);
      imageTitleText4.setText("HSV Image");
      */


            //***************
            //Display Vertical Asymmetry
            //***************
            //Core.line(roiVertical, new Point(r.x + r.x/2, r.y),new Point(r.x + r.x/2, 2*r.y), new Scalar(255,255,255));
            Core.bitwise_not(roiVertical, roiVertical);
            imagebmp4 = Bitmap.createBitmap(roiVertical.cols(), roiVertical.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(roiVertical, imagebmp4);
            imageView4.setImageBitmap(imagebmp4);
            imageTitleText4.setText("Vertical Asymmetry");

            //***************
            //Display Horizontal Asymmetry
            //***************
            //Core.line(roiHorizontal, new Point(r.x-r.x/2, r.y - r.y/2),new Point(2*r.x, r.y - r.y/2), new Scalar(255,255,255));
            Core.bitwise_not(roiHorizontal, roiHorizontal);
            resultMat = roiHorizontal.clone();
            newBmp = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(roiHorizontal, newBmp);
            imageView3.setImageBitmap(newBmp);
            imageTitleText3.setText("Horizontal Asymmetry");

            //***************
            //Color Detection
            //***************
            aboveBmp = Bitmap.createBitmap(matrixAbove.cols(), matrixAbove.rows(), Bitmap.Config.ARGB_8888);
            if (!helpMatrix.empty())
                helpMatrix.copyTo(matrixAbove);
            Utils.matToBitmap(matrixAbove, aboveBmp);
            imageViewAbove.setImageBitmap(aboveBmp);
            imagebmp5 = Bitmap.createBitmap(colorMat.cols(), colorMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(colorMat, imagebmp5);
            imageView5.setImageBitmap(imagebmp5);
            //textViewColor.setText("Horizontal Asymmetry");

            //***************
        /*
         * Publishing results in result view
         */
            //***************
            resultimgbmp = Bitmap.createBitmap(colorMat.cols(), colorMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(colorMat, resultimgbmp);
            resultImage.setImageBitmap(resultimgbmp);


            int lengthIdxStrt = 0;
            int lengthIdxEnd = 0;
            SpannableStringBuilder content = new SpannableStringBuilder();
            //StringBuilder sb = new StringBuilder();
            content.append("Image id : ")
                    .append(message).append("\n\n");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);

            content.append("Contour Perimeter : ");
            lengthIdxStrt = lengthIdxEnd;
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(contoursPerimter)).append("\n");

            lengthIdxStrt = content.length();
            content.append("Contour Area : ");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(contoursArea)).append("\n");

            lengthIdxStrt = content.length();
            content.append("Vertical Asymmetrical Area : ");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(verticalAsymmetry)).append("\n");

            lengthIdxStrt = content.length();
            content.append("Horizontal Asymmetrical Area : ");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(horizontalAsymmetry)).append("\n\n");

            lengthIdxStrt = content.length();
            content.append("Asymmetry").append("\n");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);

            lengthIdxStrt = content.length();
            content.append("Horizontal Asymmetry ratio : ");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(horizontalAsymmetry / contoursArea)).append("\n");

            lengthIdxStrt = content.length();
            content.append("Vertical Asymmetry ratio : ");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(verticalAsymmetry / contoursArea)).append("\n");

            lengthIdxStrt = content.length();
            content.append("Boundary Irregularity").append("\n");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);
            content.append("Boundary Irregularity factor : ").append("(P*P)/4*pi*A = ")
                    .append(Double.toString((contoursPerimter * contoursPerimter * 7) / (4 * 22 * contoursArea))).append("\n");

            lengthIdxStrt = content.length();
            content.append("Colors : ");
            lengthIdxEnd = content.length();
            content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
            content.append(Double.toString(nColors)).append("\n");
            //tmp.release();
            mHierarchy_rot.release();
            grImage_rot.release();

            timeElapsed = (System.currentTimeMillis() - startTime);

            lengthIdxStrt = content.length();
            content.append("\nTime Elapsed : ").append(Long.toString(timeElapsed / 1000)).append(" seconds");
            lengthIdxEnd = content.length();
            content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);
            content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);

            resultTitle.setText("Result");
            //resultView.setText(content);


            //**************
        /*
         * Final Result
         */
            //**************
            SpannableStringBuilder left_content = new SpannableStringBuilder();
            lengthIdxStrt = 0;
            lengthIdxEnd = 0;

            left_content.append("Asymmetry : ")
                    .append("2").append("\n");
            lengthIdxEnd = left_content.length();
            //left_content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            left_content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);

            left_content.append("\n").append("Border Irregularity : ")
                    .append("8").append("\n");
            lengthIdxStrt = lengthIdxEnd;
            lengthIdxEnd = left_content.length();
            //left_content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            left_content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);

            left_content.append("\n").append("Number of Colors : ")
                    .append(Integer.toString(nColors)).append("\n");
            lengthIdxStrt = lengthIdxEnd;
            lengthIdxEnd = left_content.length();
            //left_content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            left_content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);

            left_content.append("\n").append("Diameter : ")
                    .append("5").append("\n");
            lengthIdxStrt = lengthIdxEnd;
            lengthIdxEnd = left_content.length();
            //left_content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            left_content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);

            left_content.append("\n");
            //lengthIdxStrt = lengthIdxEnd;
            //lengthIdxEnd = left_content.length();
            //left_content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
            //left_content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);
            int result = (int) (2 * 1.3 + 8 * 0.1 + nColors * 0.5 + 0.5 * 5);
            //String ResultValue = "";
            //left_content.append("TDS : ").append(Double.toString(2*1.3 + 8 * 0.1 + nColors * 0.5 + 6)).append("\n");
            left_content.append(Html.fromHtml("<h2><b><u> Result </u></b></h2>"));
            String color = result > 4.75 ? "<font color='red'>" : "<font color='green'>";
            left_content.append(Html.fromHtml("<h3><b> TDS : " + color + result + "</font> </b></h3>"));
            if (result > 4.75) {
                //ResultValue = "Suspicious of Melanoma";
                left_content.append(Html.fromHtml("<h3><font color = 'red'> Suspicious of Melanoma</font></h3>"));
            }
            if (result < 4.75) {
                //ResultValue = "Benign";
                left_content.append(Html.fromHtml("<h3><font color = 'green'> Benign</font></h3>"));
            }

            resultView.setText(left_content);

        /*
         * Save Images & Files for further analysis
         */
            saveImage(fileWithoutExtension + "_num_colors.PNG", aboveBmp);
            saveImage(fileWithoutExtension + "_colors.PNG", imagebmp5);
            writeToFile(fileWithoutExtension, content.toString());

            Bitmap bm2 = Bitmap.createBitmap(tempMat.cols(), tempMat.rows(), Bitmap.Config.ARGB_8888);
            ;
            Utils.matToBitmap(tempMat, bm2);

            //saveImage(fileWithContour, bm2);

            Bitmap roi_horizontal_bm = Bitmap.createBitmap(roiHorizontal.cols(), roiHorizontal.rows(), Bitmap.Config.ARGB_8888);
            ;
            Utils.matToBitmap(roiHorizontal, roi_horizontal_bm);
            saveImage(roiHorizontalFileName, roi_horizontal_bm);

            Bitmap warp_bm = Bitmap.createBitmap(warpedMat.cols(), warpedMat.rows(), Bitmap.Config.ARGB_8888);
            ;
            Utils.matToBitmap(warpedMat, warp_bm);
            saveImage(warpedImage, warp_bm);

            Bitmap seg_bm = Bitmap.createBitmap(displayMatOriginal.cols(), displayMatOriginal.rows(), Bitmap.Config.ARGB_8888);
            ;
            Utils.matToBitmap(displayMatOriginal, seg_bm);
            saveImage(segmentedImage, seg_bm);

            Bitmap roi_vertical_bm = Bitmap.createBitmap(roiVertical.cols(), roiVertical.rows(), Bitmap.Config.ARGB_8888);
            ;
            Utils.matToBitmap(roiVertical, roi_vertical_bm);
            saveImage(roiVerticalFileName, roi_vertical_bm);
        }
        pDialog.dismiss();
        return 0;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();


        tempMat.release();
        originalMat.release();

        grMat.release();
        resultMat.release();
        hsvMat.release();
        grayMat.release();
    }

    private void saveImage(String fileName,Bitmap bm2){
        File output = new File(fileName);
        OutputStream out = null;
        try {
            Log.i(TAG,fileWithContour);
            out = new FileOutputStream(output);
            bm2.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i(TAG,"FileNotFound");
        }
        finally{
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToFile(String fileName,String data) {
        try {
            FileOutputStream outputStreamWriter = new FileOutputStream(new File(fileName+".txt"));
            outputStreamWriter.write(data.getBytes());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public native void ActiveContour(long matAddrHsv,long matAddrGr, long matAddrRgba);
}