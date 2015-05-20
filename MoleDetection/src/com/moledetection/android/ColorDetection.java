/**
 *  
 *  @Class: Class ColorDetection
 *   	Function to detect color in a image
 * 	
 * 	@Version: 1.0
 * 	@Author: Juliana Mariana Macedo Araujo
 * 	@Created: 04/16/15
 * 	@Modificated: 04/20/15
 *
 */


package com.moledetection.android;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

//import android.graphics.Point;

public class ColorDetection {
	
	private Mat	originalImage;
	private List<Point> colorPoints = new ArrayList<Point>();
	private List<Double> areaContoursPoints = new ArrayList<Double>();
	private Rect rectCheck;
	/**
	 * Construct
	 * @param 
	 * 	mRgba-> the image that will be process
	 */
	ColorDetection(Mat	mRgba){
		originalImage = mRgba.clone();
	}
	
	/**
	 * public void newImage(Mat	mRgba)
	 * 	Take a new image to be process
	 * @param 
	 * 	mRgba-> the image that will be process
	 */
	public void newImage(Mat mRgba){
		originalImage = mRgba.clone();
	}
	
	/**
	 * public Mat getOriginalImage()
	 * 	Get the original image that the processing are making
	 * @return	
	 * 	The original image that it is processed
	 */
		public Mat getOriginalImage(){
			return originalImage.clone();
		}
	/**
	 * 
	 */
		public void setRect(Rect regionCheck){
			rectCheck = regionCheck;
		}
	/**
	 * 	
	 */
		public Rect getRect(){
			return rectCheck;
		}
		
	/**
	 * public Scalar getColorPoint(int x, int y)
	 * 	Get the color in the determined poin x and y
	 * @param 
	 * 	x -> position x in the image where want the color
	 *  y -> position y in the image where want the color
	 * 
	 * @return	
	 * 	The original image that it is processed
	 */
	    public Scalar getColorPoint(int x, int y){
	    	Scalar	mBlobColorRgba;
	        Scalar	mBlobColorHsv;
	
	    	int cols = originalImage.cols();
	        int rows = originalImage.rows();
	    	Rect touchedRect = new Rect();
	
	        touchedRect.x = (x>4) ? x-4 : 0;
	        touchedRect.y = (y>4) ? y-4 : 0;
	
	        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
	        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
	
	        Mat touchedRegionRgba = originalImage.submat(touchedRect);
	
	        Mat touchedRegionHsv = new Mat();
	        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
	
	        // Calculate average color of touched region
	        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
	        int pointCount = touchedRect.width*touchedRect.height;
	        for (int i = 0; i < mBlobColorHsv.val.length; i++){
	            mBlobColorHsv.val[i] /= pointCount;
	        }
	
	        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
	
	    	return mBlobColorRgba;
	    }
	  
	/**
	 * public void addPointColor(int x, int y)
	 * 	Add a point where wanted the color in the original image
	 *  Point that already have are not considered 
	 * @param 
	 * 	x -> position x in the image where want the color
	 *  y -> position y in the image where want the color
	 * 
	 */
	    public void addPointColor(int x, int y){   
	    	
	    	if(!colorPoints.contains(new Point(x,y))){
	    		colorPoints.add(new Point(x,y));
	    	}
	    
	    }
    /**
	 * public int getSizePointColor()
	 * 	Get the number of points that it is saved
	 * 
	 */
	    public int getSizePointColor(){   
	    	return	colorPoints.size();
	    } 
	    
    /**
	 * public Point getPoint(int n)
	 * 	Get the Point n, with coordanates x,y
	 * @param 
	 * 	n -> Point n
	 * 
	 * @return
	 * 	the Point
	 */
	    public Point getPoint(int n){   
	    	return	colorPoints.get(n);
	    }	
	    
    /**
	 * public void clearPointColor()
	 * 	Erase all the points 
	 */
	    public void clearPointColor(){   
	    	colorPoints.clear();
	    }   
	    
    /**
	 * public Scalar getPointColor(int n)
	 * 	Get the color in the Point n
	 * @param 
	 * 	n -> Point n
	 * 
	 * @return
	 * 	the color
	 */
	    public Scalar getPointColor(int n){
	    	
	    	int x = (int)colorPoints.get(n).x;
	    	int y = (int)colorPoints.get(n).y;
	    	return	getColorPoint(x,y);
	    }  
	    
    /**
	 * public Double getContoursColor(Rect regionCheck)
	 * 	Get the contours area of some color in a specific region
	 * @param 
	 *	regionCheck -> The region of the image where will be check
	 * 	n -> Point n	 * 
	 * @return
	 * 	the area of the contours for that color
	 */
	    public double getContoursAreaColor(Rect regionCheck, int n){
	    	
	    	Mat touchedRegionRgba = originalImage.submat(regionCheck);
        	double contoursArea = 0;
        	
        	ColorBlobDetector	matDetector = new ColorBlobDetector();;
        	matDetector.setHsvColor(getPointColor(n));
        	matDetector.process(touchedRegionRgba);//mDetector.process(mRgba);
            List<MatOfPoint> contours = matDetector.getContours();

            for(int a = 0; a < contours.size(); a++){
            	MatOfPoint contour = contours.get(a);
            	contoursArea += Imgproc.contourArea(contour);
            }

        	return contoursArea;
	    } 
	    
	/**
	 * public void calcContoursAreaPointsSaved(Rect regionCheck)
	 * 	Calculate and store in a array a area of the Points store in the class
	 * @param 
	 *	regionCheck -> The region of the image where will be check
	 */
	    public void calcContoursAreaPointsSaved(Rect regionCheck){   
        	for(int i=0; i < getSizePointColor(); i++){
        		areaContoursPoints.add(getContoursAreaColor(regionCheck, i));	           
        	}
	    }
    
	/**
	 * public double getContoursAreaPointsSaved(int n)
	 * 	Get the value of contours that are store
	 * @param 
	 *	n -> The contours of the image that want to get
	 * @return
	 * 	the area of the contours for that point
	 */
	    public double getContoursAreaPointsSaved(int n){   
	    	return areaContoursPoints.get(n);
	    }
	/**
	 * private Scalar converScalarHsv2Rgba(Scalar hsvColor)
	 * 	Convert a Hsv color to Rgba color
	 * @param 
	 * 	hsvColor -> the Hsv color that will be convert
	 * 
	 * @return	
	 * 	The Rgba color
	 */	  	    
		private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
	        Mat pointMatRgba = new Mat();
	        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
	        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
	
	        return new Scalar(pointMatRgba.get(0, 0));
	    }
}