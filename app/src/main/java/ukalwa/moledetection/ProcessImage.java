/**
 *
 *  @Class: Class ProcessImage
 *   	Handles extraction of features and classifying the lesion
 *
 * 	@Version: 2.0
 * 	@Author: Upender Kalwa
 * 	@Created: 04/16/15
 * 	@Modified_by Upender Kalwa
 * 	@Modified: 05/16/17
 *
 */


package ukalwa.moledetection;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class ProcessImage extends Activity {

    protected static final String TAG = "ProcessImage::Activity";

    private long startTime;
    private String message;



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

    // Initialization of Android UI params
    private ViewFlipper mViewFlipper;
    private Button NextBtn;
    private Button PreviousBtn;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private TextView imageTitleText1;
    private TextView imageTitleText3;
    private TextView imageTitleText2;
    private TextView imageTitleText4;
    private ProgressDialog pDialog;
    private TextView resultView;
    private TextView resultTitle;
    private ImageView imageView5;
    private ImageView resultImage;
    private ImageView imageViewAbove;
    private TextView textViewColor;

    // Initialization params of application
    private Mat rot_mat;
    private MatOfPoint contour;
    private Mat diffHorizontal, diffVertical;
    private ArrayList<Number> featureSet;
    private double hAsym, vAsym;
    private double newWidth, newHeight;
    private double contourArea=0, contourPerimeter=0;
    private int centroid_x, centroid_y;
    private Mat originalMat,resultMat, imageMat;
    private Mat hsvMat;
    private Mat contourMask;
    private Mat segmentedImage;
    private String fileWithContour;
    private List<MatOfPoint> contours;
    private int maxAreaIdx;
    private int rows, cols;
    private RotatedRect ellipse;
    private double maxArea;

    // Active contour params
    private int[] iter_list = {75, 25};
    private double[] gaussian_list = {7, 1.0};
    private int[] energy_list = {2, 1, 1, 1, 1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Record total execution time of the application for an image
        startTime = System.currentTimeMillis();
        // Loading dialog until the processing is complete
        pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
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
        // intialize params
        ArrayList<Number> performanceMetric = new ArrayList<>();
        Log.i(TAG, "fileName : " + message);
        StringTokenizer str = new StringTokenizer(message, ".");
        originalMat = Highgui.imread(message);
        rot_mat = new Mat(); // Rotation matrix to store lesion rotation params
        Log.i(TAG, "img dims : " + originalMat.channels() + originalMat.width() +
                originalMat.height());
        Log.i(TAG, "Original Mat dims : " + this.originalMat.channels() + this.originalMat.width()
                + this.originalMat.height());

        // Number of iterations to evolve the active contour represented by different colors
        Map<Integer, Scalar> iterationsArray = new HashMap<>();
        iterationsArray.put(50, new Scalar(0,0,255));
        iterationsArray.put(100, new Scalar(0,153,0));
        iterationsArray.put(200, new Scalar(255,255,0));
        iterationsArray.put(400, new Scalar(255,0,0));

        // Different file names to save the processed images locally on the device
        String fileWithoutExtension = str.nextToken() + "_" +
                DateFormat.format("yyyy_MM_dd", new Date());
        fileWithContour = fileWithoutExtension + ".PNG";
        String segmentedImageName = fileWithoutExtension + "_segmented.PNG";
        String warpedImageName = fileWithoutExtension + "_warped.PNG";
        String hAsymFileName = fileWithoutExtension + "_roi_horizontal.PNG";
        String vAsymFileName = fileWithoutExtension + "_roi_vertical.PNG";

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

        // Call active contour function for each iteration
        SortedSet<Integer> keys = new TreeSet<>(iterationsArray.keySet());
        for (Integer key : keys){
            start = System.currentTimeMillis(); // start execution of segmentation for iteration
            getContour(key, iterationsArray.get(key));
            end = System.currentTimeMillis();
            performanceMetric.add(end-start); // end execution of segmentation for iteration
        }

        // Extract A, B, D features from the image
        start = System.currentTimeMillis(); // start execution of A, B, D features extraction
        extract_features();
        end = System.currentTimeMillis();
        performanceMetric.add(end-start); // end execution of A, B, D features extraction

        featureSet.add((int)Math.max(ellipse.size.width, ellipse.size.height));
        featureSet.add((int)Math.min(ellipse.size.width, ellipse.size.height));

        // Segmented image and warped image
        // Display image with white background
        Mat displayMat = Mat.zeros(imageMat.size(), CvType.CV_8UC3);
        Core.bitwise_not(displayMat, displayMat);
        originalMat.copyTo(displayMat, contourMask);

        Mat warpedImage = new Mat(imageMat.size(), imageMat.type());
        Imgproc.warpAffine(segmentedImage, warpedImage, rot_mat, imageMat.size());
        Log.i(TAG, "Warped mat size : " + warpedImage.size());

        ///////// Init //// Color detection /////////////////////

        int nColors;
        Mat matrixAbove = Mat.ones(imageMat.size(), CvType.CV_8UC4);
        Log.i(TAG, "Mat Above" + matrixAbove.size());

        // My additions
        ColorDetection colorProcess = new ColorDetection();
        Mat colorImage = originalMat.clone();
        Mat colorImgRGBA = new Mat();

        double tolerance = 30;
        double value_threshold = Core.mean(hsvMat).val[2] - tolerance;

        colorProcess.setContourArea(maxArea);
        colorProcess.setLabelMatrix(matrixAbove);
        colorProcess.setValueThreshold(value_threshold);
        colorProcess.setColorCentroid(centroid_x, centroid_y);
        colorProcess.getAllColorContours(segmentedImage, colorImage, featureSet);
//            Imgproc.cvtColor(colorProcess.getLabelMatrix(), matrixAbove,Imgproc.COLOR_BGR2RGBA );
        matrixAbove = colorProcess.getLabelMatrix();

        Log.i(TAG, "Mean" + String.valueOf(value_threshold));

        Highgui.imwrite(fileWithoutExtension +"_colors.PNG", colorImage);
        Imgproc.cvtColor(colorImage, colorImgRGBA,Imgproc.COLOR_BGR2RGBA );


        nColors = colorProcess.getNumberOfColors();

        // add no of colors to feature set
        featureSet.add(nColors);


        textViewColor.setText("Colors: " + nColors);

        Log.i(TAG, "Display Mat channels:" + Integer.toString(displayMat.channels()));

        //***************
        //Display 1st image with contour on the original image
        //***************
        Imgproc.cvtColor(originalMat, originalMat,Imgproc.COLOR_BGR2RGBA );
        Imgproc.drawContours(originalMat, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), 2);
        Bitmap imagebmp1 = Bitmap.createBitmap(this.originalMat.cols(), this.originalMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(originalMat, imagebmp1);
        imageView1.setImageBitmap(imagebmp1);
        imageTitleText1.setText("Original Image with Contour");

        //MatOfPoint2f mMOP2f1 = new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
        //contours.get(maxAreaIdx).convertTo(mMOP2f1, CvType.CV_32FC2);

        //***************
        //Display next image with only contour on the zeros image
        //***************
//            Core.bitwise_not(contourMask, contourMask);
        //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(240,0,63,255), 2); 	//red, cycles = 25
        //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0,255,255,255), 2);	//cyan, cycles = 50
//            Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0, 153, 0, 255), 2);    //blue, cycles = 75
        //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0,0,204,255), 2);	//violet cycles = 100
        //MinMaxLocResult minMaxPoints = Core.minMaxLoc(contourMask);
        Imgproc.cvtColor(displayMat, displayMat,Imgproc.COLOR_BGR2RGBA );
        Bitmap imagebmp2 = Bitmap.createBitmap(displayMat.cols(), displayMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(displayMat, imagebmp2);
        imageView2.setImageBitmap(imagebmp2);
        //imageTitleText2.setText("Extracted Contour");
        imageTitleText2.setText("Segmented Image");

        //***************
        //Display Vertical Asymmetry
        //***************
        //Core.line(roiVertical, new Point(r.x + r.x/2, r.y),new Point(r.x + r.x/2, 2*r.y), new Scalar(255,255,255));
        Bitmap imagebmp4 = Bitmap.createBitmap(diffVertical.cols(), diffVertical.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diffVertical, imagebmp4);
        imageView4.setImageBitmap(imagebmp4);
        imageTitleText4.setText("Vertical Asymmetry");

        //***************
        //Display Horizontal Asymmetry
        //***************
        //Core.line(roiHorizontal, new Point(r.x-r.x/2, r.y - r.y/2),new Point(2*r.x, r.y - r.y/2), new Scalar(255,255,255));
        Core.bitwise_not(diffHorizontal, diffHorizontal);
        resultMat = diffHorizontal.clone();
        Bitmap newBmp = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diffHorizontal, newBmp);
        imageView3.setImageBitmap(newBmp);
        imageTitleText3.setText("Horizontal Asymmetry");

        //***************
        //Display color image
        //***************
        Bitmap aboveBmp = Bitmap.createBitmap(matrixAbove.cols(), matrixAbove.rows(), Bitmap.Config.ARGB_8888);
        if (!matrixAbove.empty())
            matrixAbove.copyTo(matrixAbove);
        Utils.matToBitmap(matrixAbove, aboveBmp);
        imageViewAbove.setImageBitmap(aboveBmp);
        Bitmap imagebmp5 = Bitmap.createBitmap(colorImgRGBA.cols(), colorImgRGBA.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(colorImgRGBA, imagebmp5);
        imageView5.setImageBitmap(imagebmp5);

        //***************
    /*
     * Publishing results in result view
     */
        //***************
        Bitmap resultimgbmp = Bitmap.createBitmap(colorImgRGBA.cols(), colorImgRGBA.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(colorImgRGBA, resultimgbmp);
        resultImage.setImageBitmap(resultimgbmp);


        int lengthIdxStrt = 0;
        int lengthIdxEnd;
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
        content.append(Double.toString(contourPerimeter)).append("\n");

        lengthIdxStrt = content.length();
        content.append("Contour Area : ");
        lengthIdxEnd = content.length();
        content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
        content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
        content.append(Double.toString(contourArea)).append("\n");

        lengthIdxStrt = content.length();
        content.append("Vertical Asymmetrical Area : ");
        lengthIdxEnd = content.length();
        content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
        content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
        content.append(Double.toString(vAsym)).append("\n");

        lengthIdxStrt = content.length();
        content.append("Horizontal Asymmetrical Area : ");
        lengthIdxEnd = content.length();
        content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
        content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
        content.append(Double.toString(hAsym)).append("\n\n");

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
        content.append(Double.toString(hAsym / contourArea)).append("\n");

        lengthIdxStrt = content.length();
        content.append("Vertical Asymmetry ratio : ");
        lengthIdxEnd = content.length();
        content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
        content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
        content.append(Double.toString(vAsym / contourArea)).append("\n");

        lengthIdxStrt = content.length();
        content.append("Boundary Irregularity").append("\n");
        lengthIdxEnd = content.length();
        content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
        content.setSpan(new StyleSpan(Typeface.BOLD), lengthIdxStrt, lengthIdxEnd, 0);
        content.append("Boundary Irregularity factor : ").append("(P*P)/4*pi*A = ")
                .append(Double.toString((contourPerimeter * contourPerimeter * 7) / (4 * 22 * contourArea))).append("\n");

        lengthIdxStrt = content.length();
        content.append("Colors : ");
        lengthIdxEnd = content.length();
        content.setSpan(new UnderlineSpan(), lengthIdxStrt, lengthIdxEnd, 0);
        content.setSpan(new StyleSpan(Typeface.ITALIC), lengthIdxStrt, lengthIdxEnd, 0);
        content.append(Double.toString(nColors)).append("\n");
        long endTime = System.currentTimeMillis();
        long timeElapsed = (endTime - startTime);

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
        int result = (int) (2 * 1.3 + 8 * 0.1 + nColors * 0.5 + 0.5 * 5);
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

        resultView.setText(Html.fromHtml("<h3><font color = 'red'> Suspicious of Melanoma</font></h3>"));

    /*
     * Save Images & Files for further analysis
     */
        saveImage(fileWithoutExtension + "_num_colors.PNG", aboveBmp);
        saveImage(fileWithoutExtension + "_colors.PNG", imagebmp5);
        writeToFile(fileWithoutExtension, content.toString());

//            Bitmap bm2 = Bitmap.createBitmap(tempMat.cols(), tempMat.rows(), Bitmap.Config.ARGB_8888);
//
//            Utils.matToBitmap(tempMat, bm2);

        //saveImage(fileWithContour, bm2);

        Bitmap roi_horizontal_bm = Bitmap.createBitmap(diffHorizontal.cols(), diffHorizontal.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(diffHorizontal, roi_horizontal_bm);
        saveImage(hAsymFileName, roi_horizontal_bm);

        Bitmap warp_bm = Bitmap.createBitmap(warpedImage.cols(), warpedImage.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(warpedImage, warp_bm);
        saveImage(warpedImageName, warp_bm);

        Bitmap seg_bm = Bitmap.createBitmap(displayMat.cols(), displayMat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(displayMat, seg_bm);
        saveImage(segmentedImageName, seg_bm);

        Bitmap roi_vertical_bm = Bitmap.createBitmap(diffVertical.cols(), diffVertical.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(diffVertical, roi_vertical_bm);
        saveImage(vAsymFileName, roi_vertical_bm);
        Log.i(TAG, "Feature set:" + Arrays.toString(featureSet.toArray()));
        pDialog.dismiss();
        Log.i(TAG, "Execution times:" + Arrays.toString(performanceMetric.toArray()));
        return 0;
    }


    private void getContour(int iterations, Scalar contourColor) {
        //Calling Active Contour native method written in C++
        double init_height = 0.5;
        double init_width = 0.5;
        int shape = 0;
        ActiveContour(imageMat.getNativeObjAddr(), contourMask.getNativeObjAddr(),
                    iterations, shape, init_width, init_height, iter_list,
                    energy_list, gaussian_list);

        AbstractMap.SimpleEntry<Integer, List<MatOfPoint>> retVal = extractLargestContour(contourMask);
        maxAreaIdx = retVal.getKey();
        contours = retVal.getValue();

        contour = contours.get(maxAreaIdx);
        Mat contourImage = new Mat(imageMat.size(), CvType.CV_8UC3);
        Imgproc.drawContours(contourImage, contours, maxAreaIdx, contourColor, 2);

        Mat contourBinary = new Mat(imageMat.size(), CvType.CV_8UC1);
        Imgproc.drawContours(contourBinary, contours, maxAreaIdx, new Scalar(255,255,255), 2);

        segmentedImage = new Mat(imageMat.size(),CvType.CV_8UC3);
        Core.bitwise_and(originalMat, originalMat, segmentedImage, contourMask);

    }

    private AbstractMap.SimpleEntry<Integer, List<MatOfPoint>>
    extractLargestContour(Mat contourMask){

        double maxArea = -1;
        int maxAreaIdx = -1;

        contours = new ArrayList<>();
        Mat mHierarchy = new Mat();

        Imgproc.findContours(contourMask, contours, mHierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);
        Log.i(TAG, "Contours size : " + contours.size());

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

    private void extract_features() {

        MatOfPoint2f point2F = new MatOfPoint2f(contour.toArray());
        ellipse = Imgproc.fitEllipse(point2F);
        maxArea = Imgproc.contourArea(contour);

        double angle = ellipse.angle;
        Log.i(TAG, "rotation angle,width,height : " + angle + "," + ellipse.size.width + "," +
                ellipse.size.height);
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
        Log.i(TAG, "rot mat : " + rot_mat.dump());
        Log.i(TAG, "cos, sin : " + Double.toString(cos)+ Double.toString(sin));
        rot_mat.get(0,2)[0] += newWidth/2 - cols /2;
        rot_mat.get(1,2)[0] += newHeight/2 - rows /2;

        // extract Asymmetry, Border, Diameter features
        featureSet = new ArrayList<>();

        // rotate mask image
        Mat rotated_mask = rotate_mat(contourMask.clone());

        Moments moments = Imgproc.moments(contour);
        centroid_x = (int) (moments.get_m10()/moments.get_m00());
        centroid_y = (int) (moments.get_m01()/moments.get_m00());

        // Border irregularity
        contourArea = Core.countNonZero(contourMask);
        contourPerimeter = Imgproc.arcLength(point2F, true);
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
        hAsym = Core.countNonZero(diffHorizontal);
        vAsym = Core.countNonZero(diffVertical);

        flipContourHorizontal.release();
        flipContourVertical.release();

        featureSet.add(hAsym);
        featureSet.add(vAsym);
        featureSet.add(b);

    }


    private Mat rotate_mat(Mat image)
    {

        Imgproc.warpAffine(image, image, rot_mat, new Size(newWidth, newHeight));

        List<MatOfPoint> contours = new ArrayList<>();
        Mat mHierarchy = new Mat();
        double maxArea = -1;
        int maxAreaIdx = -1;
        Imgproc.findContours(image, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//            Log.i(TAG, "Contours size : " + contours2.size());

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
        resultMat.release();
        hsvMat.release();
    }

    private void saveImage(String fileName,Bitmap bm2){
        File output = new File(fileName);
        OutputStream out = null;
        try {
            Log.i(TAG,fileWithContour);
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
        try {
            FileOutputStream outputStreamWriter = new FileOutputStream(new File(fileName+".txt"));
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