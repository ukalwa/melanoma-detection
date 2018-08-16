/**
 *
 *  @Class: Class ProcessImage
 *   	Handles extraction of features and classifying the lesion
 *
 * 	@Version: 2.2
 * 	@Author: Upender Kalwa
 * 	@Created: 04/16/15
 * 	@Modified_by Upender Kalwa
 * 	@Modified: 08/30/17
 *
 */


package ukalwa.moledetection;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.ml.SVM;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ProcessImage extends Activity {
    private static final String classifierFile = "opencv_svm.xml";
    protected static final String TAG = "ProcessImage::Activity";
    private static final String mDirName = "MoleDetection";

    private static String mDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mDirName;
    private static String classifierXml = mDirectory + "/" + classifierFile;

    private long startTime;
    private String message;
    private int isMelanoma = 0;

    // Initialization of Android UI params
    private ViewFlipper mViewFlipper;
    private Button NextBtn;
    private Button PreviousBtn;

    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ImageView imageView5;
    private TextView imageTitleText1;
    private TextView imageTitleText3;
    private TextView imageTitleText2;
    private TextView imageTitleText4;
    private TextView imageTitleText5;
    private TextView resultText1;
    private TextView resultText2;
    private TextView resultText3;
    private TextView resultText4;
    private TextView resultText5;


    private ProgressDialog pDialog;

    // Initialization params of application
    private Mat rot_mat;
    private MatOfPoint contour;
    private Mat diffHorizontal, diffVertical;
    private ArrayList<Double> featureSet;
    private double newWidth, newHeight;
    private int centroid_x, centroid_y;
    private Mat originalMat, imageMat;
    private Mat hsvMat;
    private Mat contourMask;

    private Mat colorImage;
    private Mat segmentedImage;
    private List<MatOfPoint> contours;
    private int maxAreaIdx;
    private int rows, cols;
    private double maxArea;
    Rect warpRect;
    private String fileWithoutExtension;

    private ArrayList<Number> performanceMetric;


    // Active contour params
    private int[] iter_list = {75, 25};
    private double[] gaussian_list = {7, 1.0};
    private int[] energy_list = {2, 1, 1, 1, 1};

    private ProcessImage context;
    private boolean processed = false;
    static{
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    if(!processed){
                        // Display progress
                        pDialog = ProgressDialog.show(context, "Loading Data", "Please Wait...", true);
                        new Thread(new Runnable() {
                            @Override
                            public void run()
                            {
                                Log.i(TAG, "OpenCV loaded successfully");
                                System.out.print("OpenCV loaded successfully");
                                System.loadLibrary("active_contour");
                                // Run our program
                                processImage();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        displayResults();
                                        pDialog.dismiss();
                                        processed = true;
                                    }
                                });
                            }
                        }).start();
                    }

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
        /*******************************************************************************************
         * Android App initialization params
         ******************************************************************************************/
        super.onCreate(savedInstanceState);
        // Record total execution time of the application for an image
        startTime = System.currentTimeMillis();
        // Loading dialog until the processing is complete
        context = this;
        setContentView(R.layout.activity_process_image);
        // View flipper (carousel) with previous and next buttons to flip through different views
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
            }
        });
        NextBtn = (Button) findViewById(R.id.nextbutton);
        NextBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PreviousBtn.setVisibility(Button.VISIBLE);
                mViewFlipper.showNext();
                if(mViewFlipper.getDisplayedChild()==mViewFlipper.getChildCount()-1)
                    NextBtn.setVisibility(Button.INVISIBLE);
            }
        });
        Intent intent = getIntent();
        message = intent.getStringExtra(GetPreviousPictures.FILEPATH);

        imageView1 = (ImageView) findViewById(R.id.image1);
        imageView2 = (ImageView) findViewById(R.id.image2);
        imageView3 = (ImageView) findViewById(R.id.image3);
        imageView4 = (ImageView) findViewById(R.id.image4);
        imageView5 = (ImageView) findViewById(R.id.image5);

        imageTitleText1 = (TextView) findViewById(R.id.ImageTitleText1);
        imageTitleText2 = (TextView) findViewById(R.id.ImageTitleText2);
        imageTitleText3 = (TextView) findViewById(R.id.ImageTitleText3);
        imageTitleText4 = (TextView) findViewById(R.id.ImageTitleText4);
        imageTitleText5 = (TextView) findViewById(R.id.ImageTitleText5);

        resultText1 = (TextView) findViewById(R.id.resultText1);
        resultText2 = (TextView) findViewById(R.id.resultText2);
        resultText3 = (TextView) findViewById(R.id.resultText3);
        resultText4 = (TextView) findViewById(R.id.resultText4);
        resultText5 = (TextView) findViewById(R.id.resultText5);

    }


    protected void processImage() {
        // initialize params
        performanceMetric = new ArrayList<>();
        Log.i(TAG, "fileName : " + message);
        StringTokenizer str = new StringTokenizer(message, ".");
        originalMat = Imgcodecs.imread(message);
        int max_dim = Math.max(originalMat.rows(), originalMat.cols());
        if (max_dim > 1024){
            double ratio = max_dim/1024;
            double nHeight = originalMat.rows() / ratio ;
            double nWidth = originalMat.cols() / ratio ;
            Imgproc.resize(originalMat, originalMat, new Size(nWidth, nHeight));
        }
        rot_mat = new Mat(); // Rotation matrix to store lesion rotation params
        Log.i(TAG, "img dims : " + originalMat.channels() + originalMat.width() +
                originalMat.height());
        Log.i(TAG, "Original Mat dims : " + this.originalMat.channels() + "," +
                this.originalMat.width() + "," + this.originalMat.height());

        // Number of iterations to evolve the active contour represented by different colors
        Map<Integer, Scalar> iterationsArray = new HashMap<>();
//        iterationsArray.put(50, new Scalar(0,0,255));
//        iterationsArray.put(100, new Scalar(0,153,0));
//        iterationsArray.put(200, new Scalar(255,255,0));
        iterationsArray.put(400, new Scalar(255,0,0));

        // filename without extension to save processed images locally on the device
        fileWithoutExtension = str.nextToken();
        /*******************************************************************************************
         * Preprocessing Stage
         ******************************************************************************************/
        long start = System.currentTimeMillis(); // start execution of preprocessing

        imageMat = originalMat.clone();
        //Blur the image to reduce noise in the image
        Imgproc.medianBlur(imageMat, imageMat ,5);

        // Applying CLAHE to resolve uneven illumination
        hsvMat = new Mat(imageMat.size(),CvType.CV_8UC3);
        Imgproc.cvtColor(imageMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        CLAHE clahe = Imgproc.createCLAHE(3.0, new Size(8,8));
        List<Mat> splitMats = new ArrayList<>(3);
        Core.split(hsvMat, splitMats);
        Log.i(TAG, "Before Clahe and morph close" + Core.mean(splitMats.get(2)));
        Mat illuminationCorrectedChannel = new Mat();
        clahe.apply(splitMats.get(2), illuminationCorrectedChannel);
        Log.i(TAG, "clahe result" + Core.mean(illuminationCorrectedChannel));
        splitMats.set(2, illuminationCorrectedChannel);
        Mat newHsv = new Mat();
        Core.merge(splitMats, newHsv);
        Imgproc.cvtColor(newHsv, imageMat, Imgproc.COLOR_HSV2BGR);

        // Apply Morphological Closing operation
        Core.split(imageMat, splitMats);
        Mat kernel = Mat.ones(8,8,CvType.CV_8UC1);
        for (int i=0; i < splitMats.size();i++){
            Imgproc.morphologyEx(splitMats.get(i), illuminationCorrectedChannel, Imgproc.MORPH_CLOSE,
                    kernel);
            splitMats.set(i, illuminationCorrectedChannel);
        }
        Core.merge(splitMats, imageMat);

        // Convert image to HSV for further processing
        Imgproc.cvtColor(imageMat, hsvMat, Imgproc.COLOR_BGR2HSV);
        contourMask = Mat.zeros(imageMat.size(), CvType.CV_8UC1);

        //store rows and cols of the image
        rows = this.imageMat.rows();
        cols = this.imageMat.cols();
        long end = System.currentTimeMillis();
        performanceMetric.add(end-start); // end execution of preprocessing


        /*******************************************************************************************
         * Segmentation Stage
         ******************************************************************************************/
        // Call active contour function for each iteration
        SortedSet<Integer> keys = new TreeSet<>(iterationsArray.keySet());
        Mat contourImage = new Mat(imageMat.size(), CvType.CV_8UC3);
        Mat contourBinary = new Mat(imageMat.size(), CvType.CV_8UC1);
        segmentedImage = new Mat(imageMat.size(), CvType.CV_8UC3);
        for (Integer key : keys){
            start = System.currentTimeMillis(); // start execution of segmentation for iteration
            getContour(key, iterationsArray.get(key), contourBinary, contourImage);
            end = System.currentTimeMillis();
            performanceMetric.add(end-start); // end execution of segmentation for iteration
        }
//        getContour(400, new Scalar(255,0,0), contourBinary, contourImage);
        contourBinary.release();
        contourImage.release();

        /*******************************************************************************************
         * Asymmetry (A), Border Irregularity (B), Diameter (D) features extraction stage
         ******************************************************************************************/
        // Extract A, B, D features from the image
        start = System.currentTimeMillis(); // start execution of A, B, D features extraction
        extractFeaturesABD();
        end = System.currentTimeMillis();
        performanceMetric.add(end-start); // end execution of A, B, D features extraction


        /*******************************************************************************************
         * Color feature extraction stage
         ******************************************************************************************/
        start = System.currentTimeMillis(); // start execution of color feature extraction
//        int nColors=0;
        extractFeaturesColor();
        end = System.currentTimeMillis();
        performanceMetric.add(end-start); // end execution of color feature extraction

        /*******************************************************************************************
         * Feature classification stage
         ******************************************************************************************/
        start = System.currentTimeMillis(); // start execution of color feature extraction
//      int nColors=0;
        // convert diameter in pixels to mm
        int real_diamter_pixels_mm = 72;
        featureSet.set(3, (double) Math.round(featureSet.get(3)/ real_diamter_pixels_mm));
        featureSet.set(4, (double) Math.round(featureSet.get(4)/ real_diamter_pixels_mm));
        isMelanoma = classifyLesion();
        end = System.currentTimeMillis();
        performanceMetric.add(end-start); // end execution of color feature extraction

    }

    private void displayResults(){
        /*******************************************************************************************
         * Display results in Android views
         ******************************************************************************************/
        // Main image (Contour overlaid image)
        Imgproc.drawContours(originalMat, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), 2);
        if(isMelanoma>0){
            displayImageView(originalMat,imageView1,imageTitleText1,resultText1,
                    "Result", "<h3><font color = 'red'> " +
                            "Suspicious of Melanoma</font></h3>",
                    "final", false);
        }
        else{
            displayImageView(originalMat,imageView1,imageTitleText1,resultText1,
                    "Result", "<h3><font color = 'green'> " +
                            "Benign</font></h3>",
                    "final", false);
        }


        // Segmented image with white background
        displayImageView(segmentedImage,imageView2,imageTitleText2,resultText2,
                "Segmented Image", "",
                "segmented", false);

        // Vertical Asymmetry
        displayImageView(diffVertical,imageView3,imageTitleText3,resultText3,
                "Vertical Asymmetry", "<h4>" + "A_V :" + featureSet.get(0).toString() + "," +
                        " B :" + featureSet.get(2).toString() + "</h4>",
                "roi_vertical", false);

        // Horizontal Asymmetry
        displayImageView(diffHorizontal,imageView4,imageTitleText4,resultText4,
                "Horizontal Asymmetry", "<h4>" + "A_H :" + featureSet.get(1).toString() + "," +
                        " D :" + featureSet.get(3).toString() + "</h4>",
                "roi_horizontal", false);

        // Color Image
//        Bitmap aboveBmp = Bitmap.createBitmap(matrixAbove.cols(), matrixAbove.rows(), Bitmap.Config.ARGB_8888);
//        if (!matrixAbove.empty())
//            matrixAbove.copyTo(matrixAbove);
//        Utils.matToBitmap(matrixAbove, aboveBmp);
//        imageViewAbove.setImageBitmap(aboveBmp);
        displayImageView(colorImage,imageView5,imageTitleText5, resultText5,
                "Number of colors", "<h4>" + "C :" + featureSet.get(10).toString() + "</h4>",
                "colors", false);

//        // Result image
//        displayImageView(colorImage,resultImage,resultTitle,"Result",
//                "display", false);
//        resultView.setText(Html.fromHtml("<h3><font color = 'red'> Suspicious of Melanoma</font></h3>"));

        // Log results to console
        Log.i(TAG, "Original Feature set:" + Arrays.toString(featureSet.toArray()));
        Log.i(TAG, "Individual Execution times:" + Arrays.toString(performanceMetric.toArray()));
        Log.i(TAG, "Total Execution time:" + Long.toString(System.currentTimeMillis() - startTime));

        // Save results to text file
        writeToFile(fileWithoutExtension+".txt","Feature set:" +
                Arrays.toString(featureSet.toArray()) + "\n" +
                "Individual Execution times:" + Arrays.toString(performanceMetric.toArray()) +
                "\n" + "Total Execution time:" +
                Long.toString(System.currentTimeMillis() - startTime));
    }


    private void getContour(int iterations, Scalar contourColor, Mat contourBinary,
                            Mat contourImage) {
        //Calling Active Contour native method written in C++
        double init_height = 0.6;
        double init_width = 0.6;
        int shape = 0;

        Log.i(TAG, "Calling Active contours method \n");
        ActiveContour(imageMat.getNativeObjAddr(), contourMask.getNativeObjAddr(),
                iterations, shape, init_width, init_height, iter_list,
                energy_list, gaussian_list);

        AbstractMap.SimpleEntry<Integer, List<MatOfPoint>> retVal =
                extractLargestContour(contourMask);
        maxAreaIdx = retVal.getKey();
        contours = retVal.getValue();
        if (contours.size() <= 0){
            Toast.makeText(this, "Application could not find any lesion ", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            return;
        }
        contour = contours.get(maxAreaIdx);
        contourImage.setTo(new Scalar(0));
        Imgproc.drawContours(contourImage, contours, maxAreaIdx, contourColor, 2);

        contourBinary.setTo(new Scalar(0));
        Imgproc.drawContours(contourBinary, contours, maxAreaIdx, new Scalar(255,255,255), 2);

        segmentedImage.setTo(new Scalar(0));
        Core.bitwise_not(segmentedImage, segmentedImage);
        originalMat.copyTo(segmentedImage, contourMask);

    }

    private AbstractMap.SimpleEntry<Integer, List<MatOfPoint>>
    extractLargestContour(Mat contourMask){

        double maxArea = -1;
        int maxAreaIdx = -1;

        contours = new ArrayList<>();
        Mat mHierarchy = new Mat();
        Mat cnt = new Mat();
        double contourarea=0;

        Imgproc.findContours(contourMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE);
        Log.i(TAG, "Contours size : " + contours.size());

        if (contours.size() == 0){
            Log.i(TAG, "Active contour screwed up");
        }
        else {
            for (int idx = 0; idx < contours.size(); idx++) {
                cnt = contours.get(idx);
                contourarea = Imgproc.contourArea(cnt);
                if (contourarea > maxArea) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                }
            }
        }
        Log.i(TAG, "Max area, idx, size : " + contourarea + maxAreaIdx + contours.get(maxAreaIdx).size());
        return new AbstractMap.SimpleEntry<>(maxAreaIdx, contours);
    }

    private void extractFeaturesABD() {

        MatOfPoint2f point2F = new MatOfPoint2f(contour.toArray());
        RotatedRect ellipse = Imgproc.fitEllipse(point2F);
        maxArea = Core.countNonZero(contourMask);
        Log.i(TAG, "Non zero pixel count:" + maxArea);
        Log.i(TAG, "contour count:" + contour.size());

        double angle = ellipse.angle;
        Log.i(TAG, "ellipse params : " + ellipse.toString());
        if (ellipse.size.width < ellipse.size.height) {
            if (angle < 90)
                angle -= 90;
            if (angle > 90)
                angle += 90;
        }

        rot_mat = Imgproc.getRotationMatrix2D(ellipse.center, angle, 1);
        double cos = Math.abs(rot_mat.get(0,0)[0]);
        double sin = Math.abs(rot_mat.get(0,1)[0]);

        newWidth = (int) ((rows *sin)+(cols *cos));
        newHeight = (int) ((rows *cos)+(cols * sin));
        Log.i(TAG, "old rot mat : " + rot_mat.dump());
        Log.i(TAG, "cos, sin : " + Double.toString(cos)+ Double.toString(sin));
        double[] updatedWidth = rot_mat.get(0,2);
        updatedWidth[0] += newWidth/2 - cols /2;
        rot_mat.put(0,2, updatedWidth);
        double[] updatedHeight = rot_mat.get(1,2);
        updatedHeight[0] += newHeight/2 - rows /2;
        rot_mat.put(1,2, updatedHeight);
        Log.i(TAG, "new rot mat : " + rot_mat.dump());

        // extract Asymmetry, Border, Diameter features
        featureSet = new ArrayList<>();

        // rotate mask image
        Mat rotated_mask = rotateMatImage(contourMask.clone());

        Moments moments = Imgproc.moments(contour);
        centroid_x = (int) (moments.get_m10()/moments.get_m00());
        centroid_y = (int) (moments.get_m01()/moments.get_m00());

        // Border irregularity
        double contourArea = Core.countNonZero(contourMask);
        double contourPerimeter = Imgproc.arcLength(point2F, true);
        double b = Math.pow(contourPerimeter, 2) / (4 * Math.PI * contourArea);

        // Asymmetry calculation
        Mat contourRegion = rotated_mask.clone();
        Mat flipContourHorizontal = new Mat(contourRegion.size(), CvType.CV_8UC1);
        Mat flipContourVertical = new Mat(contourRegion.size(), CvType.CV_8UC1);

        Core.flip(contourRegion, flipContourHorizontal, 1);
        Core.flip(contourRegion, flipContourVertical, 0);
        diffHorizontal = Mat.zeros(contourRegion.size(),contourRegion.type());
        diffVertical = Mat.zeros(contourRegion.size(),contourRegion.type());

//        Core.compare(contourRegion, flipContourHorizontal, diffHorizontal, Core.CMP_EQ);
//        Core.compare(contourRegion, flipContourVertical, diffVertical, Core.CMP_EQ);
        Compare(contourRegion.getNativeObjAddr(), flipContourHorizontal.getNativeObjAddr(),
                diffHorizontal.getNativeObjAddr());
        Compare(contourRegion.getNativeObjAddr(), flipContourVertical.getNativeObjAddr(),
                diffVertical.getNativeObjAddr());

        Core.bitwise_not(diffHorizontal, diffHorizontal);
        Core.bitwise_not(diffVertical, diffVertical);
        double hAsym = Core.countNonZero(diffHorizontal);
        double vAsym = Core.countNonZero(diffVertical);

        flipContourHorizontal.release();
        flipContourVertical.release();

        featureSet.add((double) Math.round (hAsym/maxArea * 100.0) / 100.0);
        featureSet.add((double) Math.round (vAsym/maxArea * 100.0) / 100.0);
        featureSet.add((double) Math.round (b * 100.0) / 100.0);
        featureSet.add((double)Math.max(warpRect.width, warpRect.height));
        featureSet.add((double)Math.min(warpRect.width, warpRect.height));
    }

    private void extractFeaturesColor(){
//        matrixAbove = Mat.ones(imageMat.size(), CvType.CV_8UC4);
        Log.i(TAG, "Colors execution started");

        // My additions
        ColorDetection colorProcess = new ColorDetection();
        colorImage = originalMat.clone();
        Mat colorImgRGBA = new Mat();

        double tolerance = 30;
        double value_threshold = Core.mean(hsvMat).val[2] - tolerance;

        colorProcess.setContourArea(maxArea);
//        colorProcess.setLabelMatrix(matrixAbove);
        colorProcess.setValueThreshold(value_threshold);
        colorProcess.setColorCentroid(centroid_x, centroid_y);
        colorProcess.getAllColorContours(segmentedImage, colorImage, featureSet);
//        matrixAbove = colorProcess.getLabelMatrix();

        // Log.i(TAG, "Mean" + String.valueOf(value_threshold));
        Imgproc.cvtColor(colorImage, colorImgRGBA,Imgproc.COLOR_BGR2RGBA );

        int nColors = colorProcess.getNumberOfColors();
        Log.i(TAG, "Colors" + String.valueOf(nColors));
        // add no of colors to feature set
        featureSet.add((double)nColors);
//        textViewColor.setText("Colors: " + nColors);

    }

    private Mat rotateMatImage(Mat image)
    {
//        Mat image_copy = image.clone();
        Imgproc.warpAffine(image, image, rot_mat, new Size(newWidth, newHeight));

        List<MatOfPoint> contours = new ArrayList<>();
        Mat mHierarchy = new Mat();
        double maxArea = -1;
        int maxAreaIdx = -1;
        Imgproc.findContours(image, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        if (contours.size() > 0){
            for (int idx = 0; idx < contours.size(); idx++) {
                Mat contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(contour);
                if (contourarea > maxArea) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                }
            }

            MatOfPoint warpedContour = contours.get(maxAreaIdx);
            warpRect = Imgproc.boundingRect(warpedContour);
            image = image.submat(warpRect);
        }

        return image;
    }

    private void displayImageView(Mat resultMat, ImageView imageView, TextView textView,
                                  TextView resultText, String text, String result,
                                  String fileExtension, boolean save){
        Log.i(TAG,fileExtension + "," + resultMat.dims());
        if(resultMat.channels() != 1 && resultMat.channels() != 4){
            Imgproc.cvtColor(resultMat, resultMat,Imgproc.COLOR_BGR2RGBA );
        }
        Bitmap bmp = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultMat, bmp);
        imageView.setImageBitmap(bmp);
        textView.setText(text);
        resultText.setText(Html.fromHtml(result));
        if(save){
            saveImage(fileWithoutExtension + "_" + fileExtension + ".PNG", bmp);
        }
    }

    private int classifyLesion(){
        SVM svm = SVM.load(classifierXml);
        svm.setC(5.0); svm.setGamma(0.2); svm.setKernel(SVM.RBF); svm.setDegree(5);
        Mat sample = Mat.zeros(new Size(11,1),CvType.CV_32FC1);
        sample.put(0, 0, featureSet.get(0)); // A1
        sample.put(0, 1, featureSet.get(1)); // A2
        sample.put(0, 2, featureSet.get(2)); // B
        sample.put(0, 3, featureSet.get(10)); // C
        sample.put(0, 4, featureSet.get(5)); // A_B
        sample.put(0, 5, featureSet.get(6)); // A_BG
        sample.put(0, 6, featureSet.get(7)); // A_DB
        sample.put(0, 7, featureSet.get(8)); // A_LB
        sample.put(0, 8, featureSet.get(9)); // A_W
        sample.put(0, 9, featureSet.get(3)); // D1
        sample.put(0, 10,featureSet.get(4)); // D2

        Log.i(TAG, "features passed : " + sample.dump() + sample.cols());
        Log.i(TAG, "SVM params :" + svm.getC() + "," + svm.getDegree() +
                "," + svm.getGamma() + "," + svm.getVarCount());
        int result = (int)svm.predict(sample);
        Log.i(TAG, "Classification result : " + Integer.toString(result));
        return result;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        originalMat.release();
        contourMask.release();
        pDialog.dismiss();
//        resultMat.release();
        hsvMat.release();
    }

    private void saveImage(String fileName,Bitmap bm2){
        File output = new File(fileName);
        OutputStream out = null;
        try {
//            Log.i(TAG,fileWithContour);
            out = new FileOutputStream(output);
            bm2.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
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
        Log.i(TAG, "text Filename : " + fileName);
        try {
            FileOutputStream outputStreamWriter = new FileOutputStream(new File(fileName));
            outputStreamWriter.write(data.getBytes());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public native void ActiveContour(long matAddrRgb,long matAddrZero, int iterations,
                                     int init_contour, double init_width_java,
                                     double init_height_java, int[] iter_list,
                                     int[] energy_list, double[] gaussian_list);

    public native void Compare(long matAddrOrig,long matAddrFlip,long matAddrCompared);
}