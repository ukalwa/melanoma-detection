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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class ProcessImage extends Activity {

    protected static final String TAG = "ProcessImage::Activity";
    private ViewFlipper mViewFlipper;
    private long startTime;
    //private GestureDetector.OnGestureListener listener;
    //private final GestureDetector detector = new GestureDetector(mContext, listener);

    private String message;
    private ImageView imageViewAbove;
    private TextView textViewColor;
    private Mat originalMat,resultMat;
    private Mat hsvMat;
    private Mat zeroMat;
    private String fileWithContour;

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
    private TextView imageTitleText1;
    private TextView imageTitleText3;
    private TextView imageTitleText2;
    private TextView imageTitleText4;
    private ProgressDialog pDialog;
    private TextView resultView;
    private TextView resultTitle;
    private ImageView imageView5;
    private ImageView resultImage;
    private Mat rot_mat;
    private MatOfPoint2f point2F;
    private MatOfPoint contour;
    private Mat diffHorizontal, diffVertical;
    private ArrayList<Number> featureSet;
    private double hAsym, vAsym;
    private double newWidth, newHeight;
    private double contourArea=0, contourPerimeter=0;
    private int centroid_x, centroid_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
        setContentView(R.layout.activity_process_image);
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
                //Toast.makeText(mContext, "Child View count " + mViewFlipper.getDisplayedChild(), Toast.LENGTH_SHORT).show();
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

        Log.i(TAG, "fileName : " + message);
        StringTokenizer str = new StringTokenizer(message, ".");
//        Bitmap bm = BitmapFactory.decodeFile(message);
//        originalMat = new Mat(bm.getHeight(), bm.getWidth(),CvType.CV_8UC3);
//        Utils.bitmapToMat(bm, originalMat); //Convert Image to Mat for OpenCV operations
        originalMat = Highgui.imread(message);
        Log.i(TAG, "img dims : " + originalMat.channels() + originalMat.width() + originalMat.height());
        Log.i(TAG, "Original Mat dims : " + this.originalMat.channels() + this.originalMat.width() + this.originalMat.height());

        String fileWithoutExtension = str.nextToken() + "_" + DateFormat.format("yyyy_MM_dd", new Date());
        fileWithContour = fileWithoutExtension + ".PNG";
        String segmentedImageName = fileWithoutExtension + "_segmented.PNG";
        String warpedImageName = fileWithoutExtension + "_warped.PNG";
        String hAsymFileName = fileWithoutExtension + "_roi_horizontal.PNG";
        String vAsymFileName = fileWithoutExtension + "_roi_vertical.PNG";

        /*
         * Active Contour Extraction process begins
         */
        //Imgproc.medianBlur(originalMat, originalMat ,5);							//Blur the image to reduce noise in the image

        //Initialize Mat objects
        int rows = this.originalMat.rows();
        int cols = this.originalMat.cols();
        rot_mat = new Mat();

        getContourMat();

        /*
         * 1) Extraction of contour with maximum area to eliminate contours of noises
         * 2) Finding bounding rectangle of the contour to calculate major and minor axes of the contour
         * 3) Flip the contour in Horizontal and Vertical axes and display as 2 rows
         */
        List<MatOfPoint> contours = new ArrayList<>();

        Mat mHierarchy = new Mat();
        double maxArea = -1;
        int maxAreaIdx = -1;

        Imgproc.findContours(zeroMat, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.i(TAG, "Contours size : " + contours.size());

        if (contours.size() == 0){
            Log.i(TAG, "Active contour screwed up");
            return 1;
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

            contour = contours.get(maxAreaIdx);
            point2F = new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
            RotatedRect ellipse = Imgproc.fitEllipse(point2F);

            double angle = ellipse.angle;
            Log.i(TAG, "rotation angle,width,height : " + angle + "," + ellipse.size.width + "," + ellipse.size.height);
            if (ellipse.size.width < ellipse.size.height) {
                if (angle < 90)
                    angle -= 90;
                if (angle > 90)
                    angle += 90;
            }
//            RotatedRect rot = Imgproc.minAreaRect(point2F);
            rot_mat = Imgproc.getRotationMatrix2D(ellipse.center, angle, 1);
            double cos = Math.abs(rot_mat.get(0,0)[0]);
            double sin = Math.abs(rot_mat.get(0,1)[0]);

            newWidth = (int) ((rows *sin)+(cols *cos));
            newHeight = (int) ((rows *cos)+(cols * sin));
            Log.i(TAG, "rot mat : " + rot_mat.dump());
            Log.i(TAG, "cos, sin : " + Double.toString(cos)+ Double.toString(sin));
            rot_mat.get(0,2)[0] += newWidth/2 - cols /2;
            rot_mat.get(1,2)[0] += newHeight/2 - rows /2;

            // create mask by drawing contours with CV_FILLED
            Mat mask = Mat.zeros(zeroMat.size(), CvType.CV_8UC1);
            Log.i(TAG, "Mask size before : " + mask.size());
            Imgproc.drawContours(mask, contours, maxAreaIdx, new Scalar(255, 255, 255, 255), -1);
            mask = mask.submat(new Rect(2,2,originalMat.width(),originalMat.height()));
            Log.i(TAG, "Mask size after : " + mask.size());
            // extract Asymmetry, Border, Diameter features
            featureSet = new ArrayList<>();
            // rotate mask image
            Mat rotated_mask = rotate_mat(mask.clone());

            extract_features(mask, rotated_mask);
            featureSet.add((int)Math.max(ellipse.size.width, ellipse.size.height));
            featureSet.add((int)Math.min(ellipse.size.width, ellipse.size.height));
            // Segmented image and warped image
            // Display image with white background
            Mat displayMat = Mat.zeros(originalMat.size(), CvType.CV_8UC3);
            Core.bitwise_not(displayMat, displayMat);
            originalMat.copyTo(displayMat, mask);

            Mat segmentedImage = new Mat(originalMat.size(), originalMat.type());
            originalMat.copyTo(segmentedImage, mask);
            Log.i(TAG, "Segmented mat size : " + segmentedImage.size());
            Mat warpedImage = new Mat(originalMat.size(), originalMat.type());
            Imgproc.warpAffine(segmentedImage, warpedImage, rot_mat, originalMat.size());
            Log.i(TAG, "Warped mat size : " + warpedImage.size());

            //////////// End ///// Contours detection ///////////////////////////////

            ///////// Init //// Color detection /////////////////////

            int nColors;
            Mat matrixAbove = Mat.ones(originalMat.size(), CvType.CV_8UC4);
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
//            Core.bitwise_not(zeroMat, zeroMat);
            //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(240,0,63,255), 2); 	//red, cycles = 25
            //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0,255,255,255), 2);	//cyan, cycles = 50
//            Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0, 153, 0, 255), 2);    //blue, cycles = 75
            //Imgproc.drawContours(tempMat, contours, maxAreaIdx, new Scalar(0,0,204,255), 2);	//violet cycles = 100
            //MinMaxLocResult minMaxPoints = Core.minMaxLoc(zeroMat);
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

            long timeElapsed = (System.currentTimeMillis() - startTime);

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
        }
        Log.i(TAG, "Feature set:" + Arrays.toString(featureSet.toArray()));
        pDialog.dismiss();
        return 0;
    }

    private void getContourMat() {
        hsvMat = new Mat(this.originalMat.size(),CvType.CV_8UC3);
        Mat img2 = new Mat(new Size(originalMat.width()+4,originalMat.height()+4),CvType.CV_8UC3);
        Imgproc.copyMakeBorder(originalMat, img2, 2, 2, 2, 2, Imgproc.BORDER_CONSTANT,new Scalar(255));
        zeroMat = Mat.zeros(img2.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(originalMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        //Calling Active Contour native method written in C++
        ActiveContour(img2.getNativeObjAddr(), zeroMat.getNativeObjAddr(),hsvMat.getNativeObjAddr());
    }

    private void extract_features(Mat mask, Mat rotated_mask) {
        Moments moments = Imgproc.moments(contour);
        centroid_x = (int) (moments.get_m10()/moments.get_m00());
        centroid_y = (int) (moments.get_m01()/moments.get_m00());

        // Border irregularity
        contourArea = Core.countNonZero(mask);
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
        zeroMat.release();
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

    public native void ActiveContour(long matAddrRgb,long matAddrZero, long matAddrHsv);
//    public native String  stringFromJNI();
}