/**
 *
 *  @Class: Class ColorDetection
 *   	Function to detect color in a image
 *
 * 	@Version: 2.0
 * 	@Author: Juliana Mariana Macedo Araujo
 * 	@Created: 04/16/15
 * 	@Modified_by Upender Kalwa
 * 	@Modificated: 05/16/17
 *
 */


package ukalwa.moledetection;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

class ColorDetection{

    private double value_threshold = 150;
    private double contourArea;

    private Mat labelMatrix;
    private int nColors = 0;
    private int centroid_x, centroid_y;

    /**
	 * Construct
	 *
	 */
	ColorDetection(){

	}


    private List<MatOfPoint> getColorContour(Mat img, Scalar low_color, Scalar high_color,
                                            String color){
        Mat mHsvMat = new Mat();
        Mat mMask = Mat.zeros(img.size(), CvType.CV_8UC1);
        Mat mHierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        double cntArea;
//        double maxAreaColor = getContoursArea(cnt);
//        double maxAreaColor = getContoursArea(cnt);
//        double specContourArea = 0;
//        double percArea;
//        int rangePerc = 5;

        Imgproc.cvtColor(img, mHsvMat, Imgproc.COLOR_BGR2HSV);
        //Range of the color
        List<MatOfPoint> contours2 = new ArrayList<>();
        Core.inRange(mHsvMat, low_color, high_color, mMask);//table 2
        Imgproc.findContours(mMask, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        for(int i=0; i< contours.size(); i++){
            Mat contour = contours.get(i);
            cntArea = Imgproc.contourArea(contour);
            if(cntArea > contourArea * 0.02 &&  cntArea < contourArea){
                contours2.add(contours.get(i));
//                Log.i("INTERNAL", color + " Area" + cntArea);
            }
        }

        mHsvMat.release();
        mMask.release();
        mHierarchy.release();
        Log.i("INTERNAL", color + " contours" + contours2.size());

        return contours2;
    }

    void setContourArea(double maxAreaC){
        this.contourArea = maxAreaC;
    }

    void setValueThreshold(double value_threshold){
        this.value_threshold = value_threshold;
    }

    void getAllColorContours(Mat mask_img, Mat img, ArrayList<Double> featureSet){
        Map<String, ArrayList<Scalar>> map = new HashMap<>();
        map.put("Black",new ArrayList<Scalar>(){{
            add(new Scalar(0,0,0));
            add(new Scalar(15,140,90));
            add(new Scalar(0,0,0));
            add(new Scalar(0,0,0,255));
        }});
        map.put("Blue Gray",new ArrayList<Scalar>(){{
            add(new Scalar(15,0,0));
            add(new Scalar(179,255,value_threshold));
            add(new Scalar(0,153,0));
            add(new Scalar(0,153,0,255));
        }});
        map.put("Dark Brown",new ArrayList<Scalar>(){{
            add(new Scalar(0,80,0));
            add(new Scalar(15,255,value_threshold-3));
            add(new Scalar(0,0,204));
            add(new Scalar(204,0,0,255));
        }});
        map.put("Light Brown",new ArrayList<Scalar>(){{
            add(new Scalar(0,80,value_threshold+3));
            add(new Scalar(15,255,255));
            add(new Scalar(0,255,255));
            add(new Scalar(255,255,0,255));
        }});
        map.put("White",new ArrayList<Scalar>(){{
            add(new Scalar(0,0,145));
            add(new Scalar(15,80,value_threshold));
            add(new Scalar(255,255,0));
            add(new Scalar(0,255,255,255));
        }});


//        int noOfColors = 0;
//        int textSize = 3;
        List<String> colorsFound = new ArrayList<>();
        SortedSet<String> keys = new TreeSet<>(map.keySet());
        for (String key : keys){
            Log.i("INTERNAL", "Color" + key);
            ArrayList<Scalar> colors = map.get(key);
            List<MatOfPoint> colorContours;
            colorContours = getColorContour(mask_img,colors.get(0), colors.get(1), key);
            double dist;
            if (colorContours.size() > 0){
//                noOfColors += 1;
                colorsFound.add(key);
                dist = getColorAtrributes(colorContours);
                int thickness = 2;
                Imgproc.drawContours(img, colorContours , -1, colors.get(2), thickness);
            }
            else{
                dist = 0;
            }
            featureSet.add((double) Math.round(dist/featureSet.get(3) * 10000.0)/10000.0);

         }
        nColors = colorsFound.size();

//        for(int i=0; i < noOfColors; i++){
//            setColorLabel(map.get(colorsFound.get(i)).get(3), colorsFound.get(i), textSize);
//        }
    }

    private double getColorAtrributes(List<MatOfPoint> colorContours) {
        int centroid_mean_x = 0, centroid_mean_y = 0;
        for (MatOfPoint cnt : colorContours) {
            Moments moments = Imgproc.moments(cnt);
            centroid_mean_x += (int) (moments.get_m10()/moments.get_m00());
            centroid_mean_y += (int) (moments.get_m01()/moments.get_m00());
        }
        centroid_mean_x = centroid_mean_x/colorContours.size();
        centroid_mean_y = centroid_mean_y/colorContours.size();
        return Math.sqrt(Math.pow(centroid_mean_x-centroid_x,2) + Math.pow(centroid_mean_y-centroid_y,2));

    }

//    private void setColorLabel(Scalar colorPrint, String colorName, int textSize)
//    {
////        int sizeSquare = 40;
//        int totalColors = 5;
//        int sizeSquare = (labelMatrix.height() / totalColors) - 4;
//        Mat colorLabel = labelMatrix.submat(4 + nColors * sizeSquare, (nColors + 1) * sizeSquare, 4, sizeSquare);
//        colorLabel.setTo(colorPrint);
//        Imgproc.putText(labelMatrix, colorName, new Point( sizeSquare + 2, sizeSquare/2 +nColors*sizeSquare ), 1, textSize, new Scalar(0, 0, 0, 255), 2);
//        //Core.putText(helpMatrix, colorName, new Point( sizeSquare + 2, 25+nColors*sizeSquare ), 1, textSize, new Scalar(0, 0, 0, 255), 1);
//        nColors++;
//    }

//    void setLabelMatrix(Mat labelMatrix){
//        this.labelMatrix = labelMatrix;
//    }
//
//    Mat getLabelMatrix(){
//        return this.labelMatrix;
//    }

    int getNumberOfColors(){
        return nColors;
    }

    void setColorCentroid(int centroid_x, int centroid_y) {
        this.centroid_x = centroid_x;
        this.centroid_y = centroid_y;
    }
}