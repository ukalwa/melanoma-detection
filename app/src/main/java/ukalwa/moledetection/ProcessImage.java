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
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ProcessImage extends Activity {

    protected static final String TAG = "ProcessImage::Activity";

    private long startTime;
    private String message;

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
//    private TextView resultView;
//    private TextView resultTitle;
//    private ImageView resultImage;
//    private ImageView imageViewAbove;
//    private TextView textViewColor;

    // Initialization params of application
    private Mat rot_mat;
    private MatOfPoint contour;
    private Mat diffHorizontal, diffVertical;
    private ArrayList<Number> featureSet;
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
    private String fileWithoutExtension;

    private ArrayList<Number> performanceMetric;


    // Active contour params
    private int[] iter_list = {75, 25};
    private double[] gaussian_list = {7, 1.0};
    private int[] energy_list = {2, 1, 1, 1, 1};

    private ProcessImage context;
    private boolean processed = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    if (!processed) {
                        // Display progress
                        pDialog = ProgressDialog.show(context, "Loading Data", "Please Wait...", true);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "OpenCV loaded successfully");
                                System.out.print("OpenCV loaded successfully");
                                System.loadLibrary("active_contour");
                                // Run our program
                                processImage();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        displayResults();
                                        pDialog.dismiss();
                                        processed = true;
                                    }
                                });
                            }
                        }).start();
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                    System.out.print("OpenCV loading failed");
                }
                break;
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
//        imageViewAbove = (ImageView) findViewById(R.id.color_image2);
//        resultImage = (ImageView) findViewById(R.id.result_image);
//        textViewColor = (TextView) findViewById(R.id.textViewColor);

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

//        resultView = (TextView) findViewById(R.id.resultView);
//        resultTitle = (TextView) findViewById(R.id.ResultTitle);
    }


    protected int processImage() {
        // initialize params
        performanceMetric = new ArrayList<>();
        Log.i(TAG, "fileName : " + message);
        StringTokenizer str = new StringTokenizer(message, ".");
        originalMat = Highgui.imread(message);
        rot_mat = new Mat(); // Rotation matrix to store lesion rotation params
//        // Log.i(TAG, "img dims : " + originalMat.channels() + originalMat.width() +
//                originalMat.height());
        // Log.i(TAG, "Original Mat dims : " + this.originalMat.channels() + "," +
//                this.originalMat.width() + "," + this.originalMat.height());

        // Number of iterations to evolve the active contour represented by different colors
        Map<Integer, Scalar> iterationsArray = new HashMap<>();
        iterationsArray.put(50, new Scalar(0,0,255));
        iterationsArray.put(100, new Scalar(0,153,0));
        iterationsArray.put(200, new Scalar(255,255,0));
        iterationsArray.put(400, new Scalar(255,0,0));

        // filename without extension to save processed images locally on the device
        fileWithoutExtension = str.nextToken();

        /*******************************************************************************************
         * Preprocessing Stage
         ******************************************************************************************/
        long start = System.currentTimeMillis(); // start execution of preprocessing
        //Blur the image to reduce noise in the image
        imageMat = new Mat(originalMat.size(),CvType.CV_8UC3);
        Imgproc.medianBlur(originalMat, imageMat ,9);

        // Convert image to HSV for further processing
        hsvMat = new Mat(imageMat.size(),CvType.CV_8UC3);
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
        performanceMetric.add(end - start); // end execution of color feature extraction

        return 0;
    }

    private void displayResults() {
        /*******************************************************************************************
         * Display results in Android views
         ******************************************************************************************/
        // Main image (Contour overlaid image)
        Imgproc.drawContours(originalMat, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), 2);
        displayImageView(originalMat, imageView1, imageTitleText1, resultText1,
                "Original Image with Contour", "<h3><font color = 'red'> " +
                        "Suspicious of Melanoma</font></h3>",
                "final", true);

        // Segmented image with white background
        displayImageView(segmentedImage, imageView2, imageTitleText2, resultText2,
                "Segmented Image", "",
                "segmented", false);

        // Vertical Asymmetry
        displayImageView(diffVertical, imageView3, imageTitleText3, resultText3,
                "Vertical Asymmetry", "<h4>" + "A_V :" + featureSet.get(0).toString() + "," +
                        " B :" + featureSet.get(2).toString() + "</h4>",
                "roi_vertical", false);

        // Horizontal Asymmetry
        displayImageView(diffHorizontal, imageView4, imageTitleText4, resultText4,
                "Horizontal Asymmetry", "<h4>" + "A_H :" + featureSet.get(1).toString() + "," +
                        " D :" + featureSet.get(3).toString() + "</h4>",
                "roi_horizontal", false);

        // Color Image
//        Bitmap aboveBmp = Bitmap.createBitmap(matrixAbove.cols(), matrixAbove.rows(), Bitmap.Config.ARGB_8888);
//        if (!matrixAbove.empty())
//            matrixAbove.copyTo(matrixAbove);
//        Utils.matToBitmap(matrixAbove, aboveBmp);
//        imageViewAbove.setImageBitmap(aboveBmp);
        displayImageView(colorImage, imageView5, imageTitleText5, resultText5,
                "Number of colors", "<h4>" + "C :" + featureSet.get(10).toString() + "</h4>",
                "colors", false);

//        // Result image
//        displayImageView(colorImage,resultImage,resultTitle,"Result",
//                "display", false);
//        resultView.setText(Html.fromHtml("<h3><font color = 'red'> Suspicious of Melanoma</font></h3>"));

        // Log results to console
//         Log.i(TAG, "Feature set:" + Arrays.toString(featureSet.toArray()));
        Log.i(TAG, "Individual Execution times:" + Arrays.toString(performanceMetric.toArray()));
        Log.i(TAG, "Total Execution time:" + Long.toString(System.currentTimeMillis() - startTime));

        // Save results to text file
        writeToFile(fileWithoutExtension + ".txt", "Feature set:" +
                Arrays.toString(featureSet.toArray()) + "\n" +
                "Individual Execution times:" + Arrays.toString(performanceMetric.toArray()) +
                "\n" + "Total Execution time:" +
                Long.toString(System.currentTimeMillis() - startTime));
    }


    private void getContour(int iterations, Scalar contourColor, Mat contourBinary,
                            Mat contourImage) {
        //Calling Active Contour native method written in C++
        double init_height = 0.5;
        double init_width = 0.5;
        int shape = 0;
        ActiveContour(imageMat.getNativeObjAddr(), contourMask.getNativeObjAddr(),
                    iterations, shape, init_width, init_height, iter_list,
                    energy_list, gaussian_list);

        AbstractMap.SimpleEntry<Integer, List<MatOfPoint>> retVal =
                extractLargestContour(contourMask);
        maxAreaIdx = retVal.getKey();
        contours = retVal.getValue();

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

        Imgproc.findContours(contourMask, contours, mHierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);
//        Log.i(TAG, "Contours size : " + contours.size());

        if (contours.size() == 0){
            Log.i(TAG, "Active contour screwed up");
        }
        else {
            for (int idx = 0; idx < contours.size(); idx++) {
                Mat cnt = contours.get(idx);
                double contourarea = Imgproc.contourArea(cnt);
                if (contourarea > maxArea) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(maxAreaIdx, contours);
    }

    private void extractFeaturesABD() {

        MatOfPoint2f point2F = new MatOfPoint2f(contour.toArray());
        RotatedRect ellipse = Imgproc.fitEllipse(point2F);
        maxArea = Imgproc.contourArea(contour);

        double angle = ellipse.angle;
        // Log.i(TAG, "rotation angle,width,height : " + angle + "," + ellipse.size.width + "," +
//                ellipse.size.height);
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
        // Log.i(TAG, "rot mat : " + rot_mat.dump());
        // Log.i(TAG, "cos, sin : " + Double.toString(cos)+ Double.toString(sin));
        rot_mat.get(0,2)[0] += newWidth/2 - cols /2;
        rot_mat.get(1,2)[0] += newHeight/2 - rows /2;

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
        diffHorizontal = new Mat(contourRegion.size(), CvType.CV_8UC1);
        diffVertical = new Mat(contourRegion.size(), CvType.CV_8UC1);

        Core.flip(contourRegion, flipContourHorizontal, 1);
        Core.flip(contourRegion, flipContourVertical, 0);
        Core.compare(contourRegion, flipContourHorizontal, diffHorizontal, Core.CMP_EQ);
        Core.compare(contourRegion, flipContourVertical, diffVertical, Core.CMP_EQ);

        Core.bitwise_not(diffHorizontal, diffHorizontal);
        Core.bitwise_not(diffVertical, diffVertical);
        double hAsym = Core.countNonZero(diffHorizontal);
        double vAsym = Core.countNonZero(diffVertical);

        flipContourHorizontal.release();
        flipContourVertical.release();

        featureSet.add(hAsym);
        featureSet.add(vAsym);
        featureSet.add(b);
        featureSet.add((int) Math.max(ellipse.size.width, ellipse.size.height));
        featureSet.add((int) Math.min(ellipse.size.width, ellipse.size.height));

    }

    private void extractFeaturesColor() {
//        matrixAbove = Mat.ones(imageMat.size(), CvType.CV_8UC4);
//        Log.i(TAG, "Mat Above" + matrixAbove.size());

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
        Imgproc.cvtColor(colorImage, colorImgRGBA, Imgproc.COLOR_BGR2RGBA);

        int nColors = colorProcess.getNumberOfColors();
        // Log.i(TAG, "Colors" + String.valueOf(nColors));
        // add no of colors to feature set
        featureSet.add(nColors);
//        textViewColor.setText("Colors: " + nColors);

    }

    private Mat rotateMatImage(Mat image)
    {

        Imgproc.warpAffine(image, image, rot_mat, new Size(newWidth, newHeight));

        List<MatOfPoint> contours = new ArrayList<>();
        Mat mHierarchy = new Mat();
        double maxArea = -1;
        int maxAreaIdx = -1;
        Imgproc.findContours(image, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contour);
            if (contourarea > maxArea) {
                maxArea = contourarea;
                maxAreaIdx = idx;
            }
        }

        MatOfPoint warpedContour = contours.get(maxAreaIdx);
        Rect warpRect = Imgproc.boundingRect(warpedContour);
        image = image.submat(warpRect);
        return image;
    }

    private void displayImageView(Mat resultMat, ImageView imageView, TextView textView,
                                  TextView resultText, String text, String result,
                                  String fileExtension, boolean save) {
        // Log.i(TAG,fileExtension + "," + resultMat.dims());
        if (resultMat.channels() != 1 && resultMat.channels() != 4) {
            Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_BGR2RGBA);
        }
        Bitmap bmp = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultMat, bmp);
        imageView.setImageBitmap(bmp);
        textView.setText(text);
        resultText.setText(Html.fromHtml(result));
        if (save) {
            saveImage(fileWithoutExtension + "_" + fileExtension + ".PNG", bmp);
        }
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
        originalMat.release();
        contourMask.release();
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
            // Log.i(TAG,"FileNotFound");
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
}