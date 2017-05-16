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


package ukalwa.moledetection;

//import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.graphics.Point;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class ColorDetection{

	private Mat	originalImage;
	private Mat	img;
    private double value_threshold;
	private Mat	notBackground;
	private List<Point> colorPoints = new ArrayList<Point>();
	private List<Double> areaContoursPoints = new ArrayList<Double>();
	private Rect rectCheck;

	private int halfOfMoleX;
	private int halfOfMoleY;

	private Mat	imageHelpColor;
	private Mat	countoursImage;
	private MatOfPoint countourMole;
    private double contourArea;
    private int thickness = 2;


	private Mat helpMatrix;
	private Mat colorLabel;
	private int nColors = 0;

	private List<MatOfPoint> countoursAbruptColor;

	private Mat	imageEggs;

	/**
	 * Construct
	 * @param
	 * 	mRgba-> the image that will be process
	 */
	ColorDetection(Mat	mRgba){
		originalImage = mRgba.clone();
		//imageHelpColor = mRgba.clone();
	}

	ColorDetection(){

	}

    public void setImg(Mat img){
        this.img = img;
    }

	/**
	 * public void newImage(Mat	mRgba)
	 * 	Take a new image to be process
	 * @param
	 * 	mRgba-> the image that will be process
	 */
	public void newImage(Mat mRgba){
		originalImage = mRgba.clone();
		//imageHelpColor = mRgba.clone();
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
	 * Set the Mat image without a skin, with just the mole image
	 */
	public void setMoleWithoutBackground(Mat nBlackground)
	{
		notBackground = nBlackground.clone();
		imageHelpColor = nBlackground.clone();
	}
	/**
	 * Get the Mat image without a skin, with just the mole image
	 */
	public Mat getMoleWithoutBackground()
	{
		return notBackground.clone();
	}
	/**
	 * Set the rectangule with the mole region
	 */
	public void setRect(Rect regionCheck){
		rectCheck = regionCheck;
	}

	/**
	 * 	Get the rect where it is the mole
	 */
	public Rect getRect(){
		return rectCheck;
	}
	/**
	 * Set contour mole
	 */
	public void setContourMole(MatOfPoint contourC){
		countourMole = contourC;
	}

	/**
	 * Get contour mole
	 */
	public List<MatOfPoint> getCoutourMole(){
		List<MatOfPoint> countourList = new ArrayList<MatOfPoint>();
		countourList.add(countourMole);
		return countourList;
	}


	/**
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
	 * public Color getColorPoint(int x, int y)
	 * 	Get the color in the determined point x and y
	 * @param
	 * 	x -> position x in the image where want the color
	 *  y -> position y in the image where want the color
	 *
	 * @return
	 * 	The original image that it is processed
	 */
	public int getColorPoint(Bitmap bitmap, int x, int y){
		int Range = 1;


		int[] pixels = new int [50];
		int i = 0;
		if( x-Range> 0 && y-Range>0){
			for(int yR = y-Range; yR <= y+Range && yR < bitmap.getHeight(); yR++){
				for(int xR = x-Range; xR <= x+Range && xR < bitmap.getWidth(); xR++){
					pixels[i] = bitmap.getPixel(xR,yR);
					i++;
				}
			}
		}
		else{
			for(int yR = y; yR <= y+4 && yR < bitmap.getHeight(); yR++){
				for(int xR = x; xR <= x+4 && xR < bitmap.getWidth(); xR++){
					pixels[i] = bitmap.getPixel(xR,yR);
					i++;
				}
			}
		}

		int red = 0;
		int blue = 0;
		int green = 0;
		int alpha = 0;
		int a;
		for(a = 0; a < i;a++){
			red += Color.red(pixels[a]);
			blue += Color.blue(pixels[a]);
			green += Color.green(pixels[a]);
			alpha += Color.alpha(pixels[a]);
		}

		red = red/a;
		blue = blue/a;
		green = green/a;
		alpha = alpha/a;

		int pixel = Color.rgb(red, green, blue);
		//int pixel = bitmap.getPixel(x,y);
		return pixel;
	}

	/**
	 * public int getFastColorPoint(int x, int y)
	 * 	Get the color in the determined point x and y
	 * @param
	 * 	x -> position x in the image where want the color
	 *  y -> position y in the image where want the color
	 *
	 * @return
	 * 	The original image that it is processed
	 */
	public int getFastColorPoint(Bitmap bitmap, int x, int y){
		int pixel = bitmap.getPixel(x,y);
		return pixel;
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

		ColorBlobDetector	matDetector = new ColorBlobDetector();
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
	 * public List<MatOfPoint> getContourScalarColor(Mat image, Scalar color)
	 * 	Get the contours for a specific color
	 * @param
	 *	image -> The Mat of the image that will be process
	 * 	color -> Color to check in the image
	 * @return
	 * 	contours of the color
	 */
	public List<MatOfPoint> getContourScalarColor(Mat image, Scalar color){

		ColorBlobDetector colorDetector = new ColorBlobDetector();
		Scalar hsv = converScalarRgba2Hsv(color);

		colorDetector.setHsvColor(hsv);

		colorDetector.process(image);
		List<MatOfPoint> contours = colorDetector.getContours();

		return contours;
	}

	/**
	 * public Double getContourScalarColor(Mat image, Scalar color)
	 * 	Get the contours for a specific color
	 * @return
	 * 	contours of the color
	 */
	public List<MatOfPoint> getContourScalarColor(Bitmap bm, Scalar color){

		Mat image = new Mat(bm.getHeight(),bm.getWidth(),CvType.CV_8UC3);
		List<MatOfPoint> contours = getContourScalarColor(image, color);
		return contours;
	}

	/**
	 * Scalar converScalarHsv2Rgba(Scalar hsvColor)
	 * 	Convert a Hsv color to Rgb color
	 * @param
	 * 	hsvColor -> the Hsv color that will be convert
	 * @return
	 * 	The Rgb color
	 */
	public Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

		return new Scalar(pointMatRgba.get(0, 0));
	}

	/**
	 * public Scalar converScalarRgba2Hsv(Scalar rgbColor)
	 * 	Convert a Scalar Rgb color to Scalar Hsv color
	 * @param
	 * 	rgbColor -> the Rgb color that will be convert
	 * @return
	 * 	The Hsv color
	 */
	public Scalar converScalarRgba2Hsv(Scalar rgbColor) {
		Mat pointMatHsv = new Mat();
		Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC3, rgbColor);
		Imgproc.cvtColor(pointMatRgba, pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL);

		return new Scalar(pointMatHsv.get(0, 0));
	}

	/**
	 * public Double getContoursColor(Rect regionCheck)
	 * 	Get the contours area of a input contour
	 * @param
	 *	contours -> The contours matrix
	 * @return
	 * 	the area of the contours for that color
	 */
	public double getContoursArea(List<MatOfPoint> contours){
		double contoursArea = 0;

		for(int a = 0; a < contours.size(); a++){
			MatOfPoint contour = contours.get(a);
			contoursArea += Imgproc.contourArea(contour);
		}

		return contoursArea;
	}

	/**
	 * 	Get the contours area of the specific contour area of a input contour
	 * @param
	 *	contours -> The contours matrix
	 *	wantedContours -> The specific contours that want the area
	 * @return
	 * 	the area of the contours for that color
	 */
	public double getContoursArea(List<MatOfPoint> contours, int wantedContours){
		double contoursArea = 0;

		MatOfPoint contour = contours.get(wantedContours);
		contoursArea += Imgproc.contourArea(contour);


		return contoursArea;
	}

	/**
	 * public Double getContoursColor(Rect regionCheck)
	 * 	Get the contours area of a input contour
	 * @return
	 * 	the area of the contours for that color
	 */
	public double getContoursArea(Scalar color){

		Rect moleRect = new Rect();
		moleRect.x = 5;
		moleRect.y = 5;
		moleRect.width = originalImage.width()-5;
		moleRect.height = originalImage.height()-5;


		Mat touchedRegionRgba = originalImage.submat(moleRect);
		double contoursArea = 0;

		ColorBlobDetector	matDetector = new ColorBlobDetector();
		matDetector.setHsvColor(color);
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
	 * Return the "distance" between two colors. The rgb entries are taken
	 * to be coordinates in a 3D space [0.0-1.0], and this method returnes
	 * the distance between the coordinates for the first and second color.
	 *
	 * @param   r1, g1, b1  First color.
	 * @param   r2, g2, b2  Second color.
	 * @return  Distance bwetween colors.
	 */
	public double colorDistance (double r1, double g1, double b1,
								 double r2, double g2, double b2)
	{
		double a = r2 - r1;
		double b = g2 - g1;
		double c = b2 - b1;

		return Math.sqrt (a*a + b*b + c*c);

	}



	/**
	 * Return the "distance" between two colors.
	 *
	 * @param color1  First color [r,g,b].
	 * @param color2  Second color [r,g,b].
	 * @return        Distance bwetween colors.
	 */
	public double colorDistanceVector (double[] color1, double[] color2)
	{
		return colorDistance (color1[0], color1[1], color1[2],
				color2[0], color2[1], color2[2]);
	}



	/**
	 * Return the "distance" between two colors.
	 *
	 * @param color1  First color.
	 * @param color2  Second color.
	 * @return        Distance between colors.
	 */
	public double colorDistance (int color1, int color2)
	{
		float rgb1[] = new float[3];
		float rgb2[] = new float[3];

		rgb1[0] = Color.red(color1);// red
		rgb1[1] = Color.green(color1);// green
		rgb1[2] = Color.blue(color1);// blue

		rgb2[0] = Color.red(color2);// red
		rgb2[1] = Color.green(color2);// green
		rgb2[2] = Color.blue(color2);// blue
		//color1.getColorComponents (rgb1);
		//color2.getColorComponents (rgb2);

		return colorDistance (rgb1[0], rgb1[1], rgb1[2],
				rgb2[0], rgb2[1], rgb2[2]);
	}

	/**
	 * Check if a color is more dark than light. Useful if an entity of
	 * this color is to be labeled: Use white label on a "dark" color and
	 * black label on a "light" color.
	 *
	 * @param r,g,b  Color to check.
	 * @return       True if this is a "dark" color, false otherwise.
	 */
	public boolean isDark (double r, double g, double b)
	{
		// Measure distance to white and black respectively
		double dWhite = colorDistance (r, g, b, 1.0, 1.0, 1.0);
		double dBlack = colorDistance (r, g, b, 0.0, 0.0, 0.0);

		return dBlack < dWhite;
	}



	/**
	 * Check if a color is more dark than light. Useful if an entity of
	 * this color is to be labeled: Use white label on a "dark" color and
	 * black label on a "light" color.
	 *
	 * @param color  Color to check.
	 * @return       True if this is a "dark" color, false otherwise.
	 */
	public boolean isDark (int color)
	{
				  /*
			    float r = color.getRed()   / 255.0f;
			    float g = color.getGreen() / 255.0f;
			    float b = color.getBlue()  / 255.0f;
				   */
		float r = Color.red(color) / 255.0f;
		float g = Color.green(color) / 255.0f;
		float b = Color.blue(color)  / 255.0f;

		return isDark (r, g, b);
	}

	/**
	 * Check if one color1 is more dark than color2.
	 * @param color1  Color to check.
	 * @param color2  Color to check.
	 * @return       True if color1 it is darker than color2, false otherwise.
	 */
	public boolean isDarker (int color1, int color2)
	{
		float rgb1[] = new float[3];
		float rgb2[] = new float[3];

		//color1.getColorComponents (rgb1);
		//color2.getColorComponents (rgb2);

		//return colorDistance (rgb1[0], rgb1[1], rgb1[2],rgb2[0], rgb2[1], rgb2[2]);

		//double dWhite = colorDistance (r, g, b, 1.0, 1.0, 1.0);

		rgb1[0] = Color.red(color1);// red
		rgb1[1] = Color.green(color1);// green
		rgb1[2] = Color.blue(color1);// blue

		rgb2[0] = Color.red(color2);// red
		rgb2[1] = Color.green(color2);// green
		rgb2[2] = Color.blue(color2);// blue
		double dBlack1 = colorDistance (rgb1[0], rgb1[1], rgb1[2], 0.0, 0.0, 0.0);
		double dBlack2 = colorDistance (rgb2[0], rgb2[1], rgb2[2], 0.0, 0.0, 0.0);
		//return dBlack < dWhite;
		return dBlack1 < dBlack2;
	}

	/**
	 * Check if one color1 is more dark than color2.
	 * @param color1  Color to check.
	 * @param color2  Color to check.
	 * @return       True if color1 it is darker than color2, false otherwise.
	 */
	public boolean isDarkerAlpha (int color1, int color2)
	{
		float rgb1[] = new float[3];
		float rgb2[] = new float[3];

		int dBlack1 = Color.alpha(color1);
		int dBlack2 = Color.alpha(color2);
		//return dBlack < dWhite;
		return dBlack1 > dBlack2;
	}

	/**
	 * Check if one color1 is more dark than color2.
	 * @return       True if color1 it is darker than color2, false otherwise.
	 */
	public boolean isDarkerHSV (float[] hsv1, float[] hsv2)
	{
		return hsv1[2] < hsv2[2];
	}

	/**
	 * Convert Scalar to Color type.
	 * @return colorColor		return the the color as a Color type
	 */

	public Scalar convertColorToScalar (int color)
	{
		//return new Scalar(Color.red(color), Color.green(color), Color.blue(color));
		Scalar	scalar;
		scalar = new Scalar(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
		return scalar;
	}

	/**
	 * Convert Scalar to Color type.
	 * @return colorColor		return the the color as a Color type
	 */
	public float[]  convertColorToHsv(int color)
	{
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		float[] hsv = new float[3];
		Color.RGBToHSV(red, green, blue, hsv);

		return hsv;
	}


	/**
	 * Return the lightest color in the image based in the points that means the extreme if the mole.
	 * @param bm  The bitmap image(need to be the image in black and the extreme of the mole in white)
	 * @return     the lightest color in the image
	 */
	public List<Point> pointsMole (Bitmap bm)
	{

		List<Point> cPoint = new ArrayList<Point>();

		for(int y = 0; y < bm.getHeight();y++){ //comprimento  y
			for(int x = 0; x < bm.getWidth();x++){ //largura  x
				int colorPointCheck = getFastColorPoint(bm, x, y);
				if(colorPointCheck != Color.BLACK){
					cPoint.add(new Point(x,y));
				}
			}
		}
		return cPoint;
	}



	/**
	 * Return the points of the extreme mole.
	 * @param bm  The bitmap image(need to be the image in black and the extreme of the mole in white)
	 * @param n each of the four parts it want(there is 4 parts)
	 * @return	the points for the part that want
	 *
	 * 	1	2
	 * 	3	4
	 */
	public List<Point> pointsMolePart (Bitmap bm, int n)
	{
		int halfMoleX;
		int halfMoleY;

		List<Point> cPoint = new ArrayList<Point>();
		///Y
		int minY=0;
		int maxY= bm.getHeight();
		///X
		int minX=0;
		int maxX=bm.getWidth();

		for(int y = 0; y < bm.getHeight();y++){ //comprimento  y
			for(int x = 0; x < bm.getWidth();x++){ //largura  x
				int colorPointCheck = getFastColorPoint(bm, x, y);
				if(colorPointCheck != Color.BLACK){
					cPoint.add(new Point(x,y));

					if(minX > x)	minX = x;
					if(maxX < x)	maxX = x;

					if(minY > x)	minY = x;
					if(maxY < x)	maxY = x;
				}
			}
		}

		halfMoleX = ((maxX - minX)/2) + minX;
		halfMoleY = ((maxY - minY)/2) + minY;

		if(n == 1){
			for(int i=1; i < cPoint.size();i+=2){
				if((int)cPoint.get(i).x > halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));

				if((int)cPoint.get(i).y > halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		else if(n == 2){
			for(int i=0; i < cPoint.size()-1;i+=2){
				if((int)cPoint.get(i).x < halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));

				if((int)cPoint.get(i+1).y > halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		if(n == 3){
			for(int i=0; i < cPoint.size()-1;i+=2){
				if((int)cPoint.get(i+1).x > halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));

				if((int)cPoint.get(i).y < halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		if(n == 4){
			for(int i=0; i < cPoint.size();i+=2){
				if((int)cPoint.get(i).x < halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));
				if((int)cPoint.get(i).y < halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		return cPoint;
	}


	/**
	 * Return the points of the extreme mole.
	 * @param bm  The bitmap image(need to be the image in black and the extreme of the mole in white)
	 * @param n each of the four parts it want(there is 4 parts)
	 * @return	the points for the part that want
	 *
	 * 	1	2
	 * 	3	4
	 */
	public List<Point> pointsMolePerPart (Bitmap bm, int n)
	{
		int halfMoleX;
		int halfMoleY;

		List<Point> cPoint = new ArrayList<Point>();
		///Y
		int minY=0;
		int maxY= bm.getHeight();
		///X
		int minX=0;
		int maxX=bm.getWidth();

		for(int y = 0; y < bm.getHeight();y++){ //comprimento  y
			for(int x = 0; x < bm.getWidth();x++){ //largura  x
				int colorPointCheck = getFastColorPoint(bm, x, y);
				if(colorPointCheck != Color.BLACK){
					cPoint.add(new Point(x,y));

					if(minX > x)	minX = x;
					if(maxX < x)	maxX = x;

					if(minY > x)	minY = x;
					if(maxY < x)	maxY = x;
				}
			}
		}




		halfMoleX = ((maxX - minX)/2) + minX;
		halfMoleY = ((maxY - minY)/2) + minY;

		List<Point> cPoint2 = new ArrayList<Point>();

		int x,y;
		if(n == 1){
			List<Point> cPoint3 = new ArrayList<Point>();
			for(int i=0; i < cPoint.size()-1;i++){
				if(cPoint.get(i).y == cPoint.get(i+1).y){
					int x1 = Math.min((int)cPoint.get(i).x, (int)cPoint.get(i+1).x);
					int x2 = Math.max((int)cPoint.get(i).x, (int)cPoint.get(i+1).x);

					cPoint3.add(new Point(x1, cPoint.get(i).y));
					cPoint3.add(new Point(x2, cPoint.get(i).y));
				}
			}

			for(int i=1; i < cPoint3.size();i++){
				if((int)cPoint3.get(i).x > halfMoleX)
					x = halfMoleX;
				else
					x = (int)cPoint3.get(i).x;
				//cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));

				if((int)cPoint3.get(i).y > halfMoleY)
					y = halfMoleY;
				else
					y = (int)cPoint3.get(i).y;
				//cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
				cPoint2.add(new Point(x,y));
			}


			return cPoint2;
		}
		else if(n == 2){
			for(int i=0; i < cPoint.size()-1;i+=2){
				if((int)cPoint.get(i).x < halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));

				if((int)cPoint.get(i+1).y > halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		if(n == 3){
			for(int i=0; i < cPoint.size()-1;i+=2){
				if((int)cPoint.get(i+1).x > halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));

				if((int)cPoint.get(i).y < halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		if(n == 4){
			for(int i=0; i < cPoint.size();i+=2){
				if((int)cPoint.get(i).x < halfMoleX)
					cPoint.set(i, new Point(halfMoleX, (int)cPoint.get(i).y));
				if((int)cPoint.get(i).y < halfMoleY)
					cPoint.set(i, new Point((int)cPoint.get(i).x, halfMoleY));
			}
		}
		return cPoint;
	}

	/**
	 * Draw the List of points in a bitmap image
	 * @param bm  The bitmap image
	 */
	public void drawPointsBitmap (Bitmap bm, List<Point> points)
	{
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);

		Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);

		Canvas canvas = new Canvas(bm);

		for(int i=0; i<points.size();i++){
			canvas.drawPoint((float)points.get(i).x, (float)points.get(i).y, paint);
		}
	}



	/**
	 * Return the Point of the middle of the mole.
	 * @param bm  The bitmap image(need to be the image in black and the extreme of the mole in white)
	 * @return	 Point of the middle of the mole
	 */
	public Point middlePointMole (Bitmap bm)
	{
		int halfMoleX;
		int halfMoleY;

		///Y
		int minY=0;
		int maxY= bm.getHeight();
		///X
		int minX=0;
		int maxX=bm.getWidth();

		for(int y = 0; y < bm.getHeight();y++){ //comprimento  y
			for(int x = 0; x < bm.getWidth();x++){ //largura  x
				int colorPointCheck = getFastColorPoint(bm, x, y);
				if(colorPointCheck != Color.BLACK){

					if(minX > x)	minX = x;
					if(maxX < x)	maxX = x;

					if(minY > x)	minY = x;
					if(maxY < x)	maxY = x;
				}
			}
		}

		halfMoleX = ((maxX - minX)/2) + minX;
		halfMoleY = ((maxY - minY)/2) + minY;

		return new Point(halfMoleX, halfMoleY);
	}


	/**
	 * Return the darkest color in the image.
	 * @param bm  The bitmap image
	 * @return     the darkest color in the image
	 */
	public Scalar darkestColorImage (Bitmap bm)
	{
		int colorA = getColorPoint(bm, 0, 0);
		float[] darkColor = new float[3];
		darkColor = convertColorToHsv(colorA);
		for(int a = 0; a < bm.getWidth();a+=2){
			for(int b = 0; b < bm.getHeight();b+=2)
			{
				int colorPointCheck = getColorPoint(bm, a, b);
				float[] hsv = new float[3];
				hsv = convertColorToHsv(colorPointCheck);
				if( isDarkerHSV (hsv, darkColor)){
					darkColor = hsv;
				}
			}
		}

		int darkOne = Color.HSVToColor (darkColor);
		Scalar darkestColor = convertColorToScalar(darkOne);

		return darkestColor;
	}


	/**
	 * Return the lightest color in the image.
	 * @param bm  The bitmap image
	 * @return     the lightest color in the image
	 */
	public Scalar lightestColorImage (Bitmap bm)
	{
		int colorA = getColorPoint(bm, 0, 0);
		float[] lightColor = new float[3];
		lightColor = convertColorToHsv(colorA);
		for(int a = 0; a < bm.getWidth();a+=2){
			for(int b = 0; b < bm.getHeight();b+=2)
			{
				int colorPointCheck = getColorPoint(bm, a, b);
				float[] hsv = new float[3];
				hsv = convertColorToHsv(colorPointCheck);
				if( !isDarkerHSV (hsv, lightColor)){
					lightColor = hsv;
				}
			}
		}

		int lightOne = Color.HSVToColor (lightColor);
		Scalar lightestColor = convertColorToScalar(lightOne);

		return lightestColor;
	}

	/**
	 * Return the lightest color in the image based in the points that means the extreme if the mole.
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @return     the lightest color in the image
	 */
	public Scalar darkestColorImage (Bitmap bm, List<Point> contoursMole)
	{
		int colorA = getColorPoint(bm, 0, 0);
		float[] lightColor = new float[3];
		lightColor = convertColorToHsv(colorA);

		for(int a = 0; a < contoursMole.size()-1; a+=2){
			for(int x= (int)(contoursMole.get(a).x); x < (int)(contoursMole.get(a+1).x);x++)
			{
				int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
				float[] hsv = new float[3];
				hsv = convertColorToHsv(colorPointCheck);
				if( isDarkerHSV (hsv, lightColor)){
					lightColor = hsv;
				}
			}
		}

		int lightOne = Color.HSVToColor (lightColor);
		Scalar lightestColor = convertColorToScalar(lightOne);

		return lightestColor;
	}

	/**
	 * Return the lightest color in the image based in the points that means the extreme if the mole.
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @return     the lightest color in the image
	 */
	public Scalar lightestColorImage (Bitmap bm, List<Point> contoursMole)
	{
		int colorA = getColorPoint(bm, 0, 0);
		float[] lightColor = new float[3];
		lightColor = convertColorToHsv(colorA);

		for(int a = 0; a < contoursMole.size()-1; a+=2){
			for(int x= (int)(contoursMole.get(a).x); x < (int)(contoursMole.get(a+1).x);x++)
			{
				int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
				float[] hsv = new float[3];
				hsv = convertColorToHsv(colorPointCheck);
				if( !isDarkerHSV (hsv, lightColor)){
					lightColor = hsv;
				}
			}
		}

		int lightOne = Color.HSVToColor (lightColor);
		Scalar lightestColor = convertColorToScalar(lightOne);

		return lightestColor;
	}

	/**
	 * Return the lightest color in a vector input.
	 * @param colorPart	The vector with the different colors
	 * @return     the lightest color in the vector
	 */
	public int lightestColorPart (int colorPart[])
	{
		int lightestColor;
		lightestColor = colorPart[0];
		for(int i=1; i<colorPart.length; i++){
			if(!isDarker (colorPart[i], lightestColor)){
				lightestColor = colorPart[i];
			}
		}

		return lightestColor;
	}

	/**
	 * Return the darkest color in a vector input.
	 * @param colorPart	The vector with the different colors
	 * @return     the darkest color in the vector
	 */
	public int darkestColorPart (int colorPart[])
	{
		int darkestColor;
		darkestColor = colorPart[0];
		for(int i=1; i<colorPart.length; i++){
			if(isDarker (colorPart[i], darkestColor)){
				darkestColor = colorPart[i];
			}
		}

		return darkestColor;
	}
	/**
	 * Return the median color inside a contours
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @return     the lightest color in the image
	 */
	public Scalar takeMedianColor (Bitmap bm, List<Point> contoursMole)
	{
		int red=0;
		int green = 0;
		int blue = 0;
		int alpha = 0;
		int size = 0;

		for(int a = 0; a < contoursMole.size()-1; a+=2){
			for(int x= (int)(contoursMole.get(a).x); x < (int)(contoursMole.get(a+1).x);x++)
			{
				int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
				red += Color.red(colorPointCheck);
				blue += Color.blue(colorPointCheck);
				green += Color.green(colorPointCheck);
				alpha += Color.alpha(colorPointCheck);
				size++;
			}
		}

		red = red/size;
		blue = blue/size;
		green = green/size;
		alpha = alpha/size;

		int meadianOne = Color.rgb(red, green, blue);

		Scalar medianColor = convertColorToScalar(meadianOne);

		return medianColor;
	}

	/**
	 * Return the median color inside a part of the contours
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @param	n	the part that want the media
	 * 		1	2
	 * 		3	4
	 * @return     the media color of the part n
	 */
	public int takeMedianColorPartColor (Bitmap bm, List<Point> contoursMole, int n)
	{
		int red=0;
		int green = 0;
		int blue = 0;
		int alpha = 0;
		int size = 0;


		if(n==1){
			for(int a = 0; a < contoursMole.size()-1; a++){
				for(int x= (int)(contoursMole.get(a).x); x < halfOfMoleX;x++)
				{
					if((int)(contoursMole.get(a).y) < halfOfMoleY){
						int colorPointCheck = getFastColorPoint(bm, x, (int)(contoursMole.get(a).y));
						red += Color.red(colorPointCheck);
						blue += Color.blue(colorPointCheck);
						green += Color.green(colorPointCheck);
						alpha += Color.alpha(colorPointCheck);
						size++;
					}
				}
			}
		}

		if(n==2){
			for(int a = 0; a < contoursMole.size(); a++){
				for(int x= halfOfMoleX; x < (int)(contoursMole.get(a).x);x++)
				{
					if((int)(contoursMole.get(a).y) < halfOfMoleY){
						int colorPointCheck = getFastColorPoint(bm, x, (int)(contoursMole.get(a).y));
						red += Color.red(colorPointCheck);
						blue += Color.blue(colorPointCheck);
						green += Color.green(colorPointCheck);
						alpha += Color.alpha(colorPointCheck);
						size++;
					}
				}
			}
		}

		if(n==3){
			for(int a = 0; a < contoursMole.size()-1; a++){
				for(int x= (int)(contoursMole.get(a).x); x < halfOfMoleX;x++)
				{
					if((int)(contoursMole.get(a).y) > halfOfMoleY){
						int colorPointCheck = getFastColorPoint(bm, x, (int)(contoursMole.get(a).y));
						red += Color.red(colorPointCheck);
						blue += Color.blue(colorPointCheck);
						green += Color.green(colorPointCheck);
						alpha += Color.alpha(colorPointCheck);
						size++;
					}
				}
			}
		}
		if(n==4){
			for(int a = 0; a < contoursMole.size(); a++){
				for(int x= halfOfMoleX; x < (int)(contoursMole.get(a).x);x++)
				{
					if((int)(contoursMole.get(a).y) > halfOfMoleY){
						int colorPointCheck = getFastColorPoint(bm, x, (int)(contoursMole.get(a).y));
						red += Color.red(colorPointCheck);
						blue += Color.blue(colorPointCheck);
						green += Color.green(colorPointCheck);
						alpha += Color.alpha(colorPointCheck);
						size++;
					}
				}
			}
		}

		int meadianOne;

		if(size >0){
			red = red/size;
			blue = blue/size;
			green = green/size;
			alpha = alpha/size;

			meadianOne = Color.rgb(red, green, blue);

		}
		else{
			meadianOne = Color.rgb(0, 0, 0);
		}

		return meadianOne;
	}

	/**
	 * Return the median color inside a part of the contours
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @return     the media color of the part n
	 */
	public List<Integer> takeDiferentsColorInsideContours (Bitmap bm, List<Point> contoursMole)
	{
		int offset = 5;
		List<Integer> colors = new ArrayList<Integer>();

		for(int a = 0; a < contoursMole.size()-1; a++){
			for(int x= (int)(contoursMole.get(a).x + offset); x < halfOfMoleX;x++)
			{
				if((int)(contoursMole.get(a).y + offset) < halfOfMoleY){
					int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
					//int colorPointCheck = getFastColorPoint(bm, x, (int)(contoursMole.get(a).y));
					float hsv[] = new float [3];
					Color.colorToHSV(colorPointCheck, hsv);

					int colorCheck = checkColor(hsv);
					if(!colors.contains(colorCheck))
						colors.add(colorCheck);

				}
			}
		}
		for(int a = 0; a < contoursMole.size(); a++)
		{
			for(int x= halfOfMoleX; x < (int)(contoursMole.get(a).x)-offset;x++)
			{
				if((int)(contoursMole.get(a).y)+offset < halfOfMoleY){
					int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
					float hsv[] = new float [3];
					Color.colorToHSV(colorPointCheck, hsv);

					int colorCheck = checkColor(hsv);
					if(!colors.contains(colorCheck))
						colors.add(colorCheck);

				}
			}
		}
		for(int a = 0; a < contoursMole.size()-1; a++){
			for(int x= (int)(contoursMole.get(a).x)+offset; x < halfOfMoleX;x++)
			{
				if((int)(contoursMole.get(a).y)+offset > halfOfMoleY){
					int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
					float hsv[] = new float [3];
					Color.colorToHSV(colorPointCheck, hsv);

					int colorCheck = checkColor(hsv);
					if(!colors.contains(colorCheck))
						colors.add(colorCheck);

				}
			}
		}

		for(int a = 0; a < contoursMole.size(); a++)
		{
			for(int x= halfOfMoleX; x < (int)(contoursMole.get(a).x) - offset;x++)
			{
				if((int)(contoursMole.get(a).y) + offset> halfOfMoleY){
					int colorPointCheck = getColorPoint(bm, x, (int)(contoursMole.get(a).y));
					float hsv[] = new float [3];
					Color.colorToHSV(colorPointCheck, hsv);

					int colorCheck = checkColor(hsv);
					if(!colors.contains(colorCheck))
						colors.add(colorCheck);
				}
			}
		}

		// colors.add(Color.WHITE);
		return colors;
	}

	/**
	 * Check if the hsv color it is one of the are important for the mole
	 * @param hsv  The hsv color
	 * @return     the color of the range if it is one the are searching, -1 otherwise
	 */
	private int checkColor(float hsv[]){


		//Check White
		if((hsv[0] >= 0 && hsv[0] <= 360) && (hsv[1] < 0.15) && (hsv[2] > 0.65)){
			return Color.WHITE;
		}
		//Check Brown
		if((hsv[0] >11 && hsv[0] <45) && (hsv[1] > 0.15) && (hsv[2] >0.1 && hsv[2] <0.75)){
			//light brown
			if(hsv[0] <28)
				return Color.rgb(176, 93, 12);
				//dark brown
			else
				return Color.rgb(102, 51, 0);
		}
		//Check Black
		if((hsv[0] > 0 && hsv[0] < 360) && (hsv[1] > 0 && hsv[1] < 1) && hsv[2] <0.1){
			return Color.BLACK;
		}
		//Check Red
		if((hsv[0] < 11 || hsv[0] >351) && (hsv[1] > 0.7) && (hsv[2] >0.1)){
			return Color.rgb(255, 153, 153);//return Color.RED;
		}

		//Check Gray
		if((hsv[0] > 0 && hsv[0] < 360) && (hsv[1] < 0.15) && (hsv[2] > 0.1 && hsv[2] < 0.65)){
			return Color.GRAY;
		}
		//Check Red
		if((hsv[0] < 11 || hsv[0] >351) && (hsv[1] > 0.7) && (hsv[2] >0.1)){
			return Color.MAGENTA;
		}

		return -1;

	}

	/**
	 * Return the median color inside a part of the contours
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @param	n	the part that want the media
	 * 		1	2
	 * 		3	4
	 * @return     the media color of the part n
	 */
	public Scalar takeMedianColorPart (Bitmap bm, List<Point> contoursMole, int n)
	{
		int color = takeMedianColorPartColor (bm, contoursMole, n);
		Scalar medianColor = convertColorToScalar(color);
		return medianColor;
	}

	/**
	 * Return the median color inside a part of the contours
	 * @param bm  The bitmap image
	 * @param contoursMole	The points of the extreme of the mole
	 * @return     the media color of the part n
	 */
	public Scalar[] takeAllMedianColorPart (Bitmap bm, List<Point> contoursMole)
	{
		Scalar[] colors = new Scalar [3];
		colors[0] = convertColorToScalar(takeMedianColorPartColor (bm, contoursMole, 1));
		colors[1] = convertColorToScalar(takeMedianColorPartColor (bm, contoursMole, 2));
		colors[2] = convertColorToScalar(takeMedianColorPartColor (bm, contoursMole, 3));
		colors[3] = convertColorToScalar(takeMedianColorPartColor (bm, contoursMole, 4));


		return colors;
	}


	/**
	 * Insent the middle point of the mole
	 * @param x  The bitmap image
	 * @param y	The points of the extreme of the mole
	 * @return     the lightest color in the image
	 */
	public void insertMiddlePoint (int x, int y)
	{
		halfOfMoleX = x;
		halfOfMoleY = y;
	}



	/**
	 * Return the value of how much the color mole it is different
	 * @param colorPart  The bitmap image
	 * @return     true if it is normal, false otherwise
	 */
	public int percentageCancerColor(int colorPart[])
	{
		int media = 0;
		int size = colorPart.length;
		for(int i=0; i < colorPart.length; i++){
			media += colorPart[i];
			size++;
		}
		media = media/size;

		double diffSquare=0;
		double StandardDeviation = 0;

		for (int i=0; i < colorPart.length; i++){
			diffSquare += Math.pow(colorPart[i] - media,2);
		}

		if (size > 0){
			StandardDeviation = Math.sqrt(diffSquare / size);
		}

		double cVariation[] = new double[colorPart.length];

		for (int i=0; i < colorPart.length; i++){
			cVariation[i] = colorPart[i]/StandardDeviation;
		}

		int pDiference[] = new int[colorPart.length];
		for(int a=0;a < colorPart.length;a++){
			pDiference[a] = 100;
			for(int b=0; b < colorPart.length; b++){

				double cVa = Math.abs(cVariation[a]);
				double cVB = Math.abs(cVariation[b]);

				double max = Math.max(cVa, cVB);
				double min = Math.min(cVa, cVB);

				int perc = (int)Math.round((min*100)/max);

				if(pDiference[a] > perc)
					pDiference[a] = perc;
			}
		}

		int small = 100;
		for(int a=0; a < colorPart.length;a++){
			if(small > pDiference[a])
				small = pDiference[a];
		}


		return small;
	}


	/**
	 */

	public List<MatOfPoint> removeSkin(List<MatOfPoint> contoursColor, Rect r)
	{

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat contourC = new Mat();
		for(int idx = 0; idx < contoursColor.size(); idx++){
			contourC = contoursColor.get(idx);

			MatOfPoint contMat = contoursColor.get(idx);
			Point points[] = contMat.toArray();

			double maxX = 0;
			double maxY = 0;
			double minX = (int)points[0].x;
			double minY = (int)points[0].y;
			for(int a=0; a < points.length;a++){
				double xA = (double)points[a].x;
				double yA = (double)points[a].y;

				if(xA > maxX)
					maxX = xA;
				if(yA > maxY)
					maxY = yA;

				if(xA < minX)
					minX = xA;
				if(yA < minY)
					minY = yA;
			}

			if(minX > r.x && maxX < r.x + r.width-100 && minY > r.y && maxY < r.y + r.height-100 ){
				contours.add(contoursColor.get(idx));
			}

		}
		contourC.release();
		return contours;
	}




	/**
	 *
	 */
	public  List<MatOfPoint> deleteSmallContours(List<MatOfPoint> contoursColor){

		List<MatOfPoint> contoursOut = new ArrayList<MatOfPoint>();
		Mat contourC = new Mat();
		double contourTotalArea = getContoursArea(contoursColor);

		for(int idx = 0; idx < contoursColor.size(); idx++){
			contourC = contoursColor.get(idx);

			double contourArea = Imgproc.contourArea(contourC);

			if( ((contourArea*100) / contourTotalArea) > 7)
				contoursOut.add(contoursColor.get(idx));
		}
		contourC.release();

		return contoursOut;
	}


	/**
	 * Get the countours in the mole by color version 1.0
	 */
	public List<MatOfPoint> findContoursColor(Rect r, int n)
	{
		//Mat originalMat = notBackground.clone();
		Mat originalMat;
		if(n ==1)
			originalMat = originalImage.clone();
		else
			originalMat = notBackground.clone();

		Mat mask = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
		// Cache
		Mat mPyrDownMat = new Mat();
		Mat mHsvMat = new Mat();
		//Mat mMask = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
		Mat mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
		Mat mDilatedMask = new Mat();
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();
		Log.i("INTERNAL", "Rect" + r.area());
		Log.i("INTERNAL", "originalMat" + originalMat.size());

		// convert input-image to HSV-image
		Imgproc.cvtColor(originalMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

		//Black
		if(n == 1){
			Log.i("INTERNAL", "Entered" + n);
			Core.inRange(mHsvMat.submat(r), new Scalar(0, 0, 0, 0), new Scalar(360, 255, 50, 255), mMask);// table 1
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			Log.i("INTERNAL", "contoursColor" + contoursColor.size());

			mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
			Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}
		//Light-Blown
		if(n == 2){
			//Core.inRange(mHsvMat.submat(r), new Scalar(11, 38.25, 25.5, 0), new Scalar(45, 255, 191, 0), mMask);// table 1


			List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(11, 120, 120, 0), new Scalar(60, 210, 210, 255), mMask);//table 2
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0,50,50, 0), new Scalar(11,180,180, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0,72,163, 0), new Scalar(11,50,74, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0,77,128, 0), new Scalar(11,153,163, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);
			//contoursColor = removeSkin(contoursColor,r);

			mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
			Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			Log.i("INTERNAL", "contoursColor" + contoursColor.size());
		}
		//Dark-Blown
		if(n == 3){
			List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
			//Core.inRange(mHsvMat.submat(r), new Scalar(11, 38.25, 25.5, 0), new Scalar(45, 255, 191, 0), mMask);// table 1

			//Core.inRange(mHsvMat.submat(r), new Scalar(11, 0, 50, 0), new Scalar(300, 190, 190, 0), mMask);//table 2
			Core.inRange(mHsvMat.submat(r), new Scalar(45, 50, 50, 0), new Scalar(130, 190, 190, 255), mMask);//table 2
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0, 191, 140, 0), new Scalar(0,219,165,255), mMask);
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);


			///
			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0, 50, 50, 0), new Scalar(130, 200, 100, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);
			Log.i("INTERNAL", "contoursColor" + contoursColor.size());


			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(280, 50, 50, 0), new Scalar(360, 200, 100, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);
			Log.i("INTERNAL", "contoursColor" + contoursColor.size());
			///

			mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
			Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		}

		//Red, take a range between red and purple, image test
		if(n == 4){
			List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();

			//Core.inRange(mHsvMat.submat(r), new Scalar(0,100,127, 0), new Scalar(11,225,255, 255), mMask);//table 2
			///// 0 - 11
			Core.inRange(mHsvMat.submat(r), new Scalar(0,90,170, 0), new Scalar(11,225,255, 255), mMask);//table 2
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0,128,163, 0), new Scalar(11,225,225, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(0,140,120, 0), new Scalar(11,225,225, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);


			///// 270 - 360
			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(270,90,170, 0), new Scalar(360,225,255, 255), mMask);//table 2
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(270,128,163, 0), new Scalar(360,225,225, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);

			contours2 = new ArrayList<MatOfPoint>();
			Core.inRange(mHsvMat.submat(r), new Scalar(270,140,110, 0), new Scalar(360,225,225, 255), mMask);//table 2
			Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			addCoutoursListToOther(contoursColor, contours2);

			mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
			Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		}

		//Gray
		if(n == 5){
			//Core.inRange(mHsvMat.submat(r), new Scalar(0, 0, 25.5, 0), new Scalar(360, 38.25, 165.75, 0), mMask);// table 1

			Core.inRange(mHsvMat.submat(r), new Scalar(115,21.2,50, 0), new Scalar(270,240,120, 255), mMask);// table 2
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


			Log.i("INTERNAL", "contoursColor" + contoursColor.size());

			mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
			Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}

		//White
		if(n == 6){
			Core.inRange(mHsvMat.submat(r), new Scalar(0, 0, 200, 0), new Scalar(360, 150, 255, 255), mMask);   //
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			Log.i("INTERNAL", "contoursColor" + contoursColor.size());
			contoursColor = removeSkin(contoursColor,r);
			Log.i("INTERNAL", "remove skin contoursColor" + contoursColor.size());

			mMask = Mat.zeros(originalMat.submat(r).size(), CvType.CV_8UC1);
			Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
			Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}

		mask.release();

		// Cache
		mPyrDownMat.release();
		mHsvMat.release();
		//Mat mMask = Mat.zeros(originalMat.size(), CvType.CV_8UC1);
		mMask.release();
		mDilatedMask.release();
		mHierarchy.release();
		originalMat.release();
		return contoursColor;
	}


	/**
	 * Get the countours of black color in the mole
	 */
	public List<MatOfPoint> getBlackContour(Mat imageProcess)
	{
		//Mat originalMat= originalImage.clone();
		Rect r = getRect();
		Mat mHsvMat = new Mat();
		Mat mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();

		Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);  // convert input-image to HSV-image


		//Range of the color
		Core.inRange(mHsvMat.submat(r), new Scalar(0, 0, 0, 0), new Scalar(360, 255, 50, 255), mMask);// table 1
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.i("INTERNAL", "contoursColor" + contoursColor.size());
		//


		mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		mHsvMat.release();		mMask.release();	mHierarchy.release();	imageProcess.release();

		return contoursColor;
	}

    /**
	 * Get the countours of light brown color in the mole
	 */
	public List<MatOfPoint> getLightBrownContour(Mat imageProcess)
	{
		//Mat originalMat= notBackground.clone();
		Rect r = getRect();
		Mat mHsvMat = new Mat();
		Mat mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();

        //		Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);  // convert input-image to HSV-image
        Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_BGR2HSV);




				    /*
		        	List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
		        	Core.inRange(mHsvMat.submat(r), new Scalar(11, 5, 5, 0), new Scalar(60, 210, 210, 255), mMask);//table 2
		        	Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		        	contours2 = new ArrayList<MatOfPoint>();
			        Core.inRange(mHsvMat.submat(r), new Scalar(0,5,5, 0), new Scalar(11,210,210, 255), mMask);//table 2
		        	Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		        	addCoutoursListToOther(contoursColor, contours2);
		        	*/


        //Range of the color
        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Core.inRange(mHsvMat.submat(r), new Scalar(0,80,166), new Scalar(15,255,255), mMask);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

//		contours2 = new ArrayList<MatOfPoint>();
//		Core.inRange(mHsvMat.submat(r), new Scalar(13,5,5, 0), new Scalar(250,225,225, 255), mMask);//table 2
//		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//		addCoutoursListToOther(contoursColor, contours2);
//
//		contours2 = new ArrayList<MatOfPoint>();
//		Core.inRange(mHsvMat.submat(r), new Scalar(250,5,5, 0), new Scalar(360,160,160, 255), mMask);//table 2
//		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//		addCoutoursListToOther(contoursColor, contours2);
//

		mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.i("INTERNAL", "Light Brown contoursColor" + contoursColor.size());

		mHsvMat.release();		mMask.release();	mHierarchy.release();	imageProcess.release();

		return contoursColor;
	}

    /**
	 * Get the countours of dark brown color in the mole
	 */
	public List<MatOfPoint> getDarkBrownContour(Mat imageProcess)
	{
		//Mat originalMat= notBackground.clone();
		Rect r = getRect();
		Mat mHsvMat = new Mat();
		Mat mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();

//		Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);  // convert input-image to HSV-image
        Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_BGR2HSV);


		//Range of the color
		List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
		Core.inRange(mHsvMat.submat(r), new Scalar(0,80,0), new Scalar(15,255,160), mMask);//table 2
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//
//		contours2 = new ArrayList<MatOfPoint>();
//		Core.inRange(mHsvMat.submat(r), new Scalar(130, 5, 5, 0), new Scalar(280, 225, 50, 255), mMask);//table 2
//		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//		addCoutoursListToOther(contoursColor, contours2);
//
//
//		contours2 = new ArrayList<MatOfPoint>();
//		Core.inRange(mHsvMat.submat(r), new Scalar(280, 5, 5, 0), new Scalar(360, 225, 100, 255), mMask);//table 2
//		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//		addCoutoursListToOther(contoursColor, contours2);
//		///

		mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		mHsvMat.release();		mMask.release();	mHierarchy.release();	imageProcess.release();
        Log.i("INTERNAL", "Dark Brown contoursColor" + contoursColor.size());

		return contoursColor;
	}

    /**
	 * Get the countours of red color in the mole
	 */
	public List<MatOfPoint> getRedContour(Mat imageProcess)
	{
		Rect r = getRect();
		Mat mHsvMat = new Mat();
		Mat mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();

		Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);  // convert input-image to HSV-image

		//Range of the color
		List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();

		        	/*
		        	Core.inRange(mHsvMat.submat(r), new Scalar(0,20,5, 0), new Scalar(360,225,255, 255), mMask);//table 2
			        Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			        	*/
		Core.inRange(mHsvMat.submat(r), new Scalar(0,20,170, 0), new Scalar(13,225,255, 255), mMask);//table 2
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		contours2 = new ArrayList<MatOfPoint>();
		Core.inRange(mHsvMat.submat(r), new Scalar(0,128,163, 0), new Scalar(13,225,225, 255), mMask);//table 2
		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		addCoutoursListToOther(contoursColor, contours2);

		contours2 = new ArrayList<MatOfPoint>();
		Core.inRange(mHsvMat.submat(r), new Scalar(0,140,120, 0), new Scalar(13,225,225, 255), mMask);//table 2
		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		addCoutoursListToOther(contoursColor, contours2);


		///// 270 - 360
		contours2 = new ArrayList<MatOfPoint>();
		Core.inRange(mHsvMat.submat(r), new Scalar(180,20,170, 0), new Scalar(360,225,255, 255), mMask);//table 2
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		contours2 = new ArrayList<MatOfPoint>();
		Core.inRange(mHsvMat.submat(r), new Scalar(180,128,163, 0), new Scalar(360,225,225, 255), mMask);//table 2
		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		addCoutoursListToOther(contoursColor, contours2);

		contours2 = new ArrayList<MatOfPoint>();
		Core.inRange(mHsvMat.submat(r), new Scalar(180,140,110, 0), new Scalar(360,225,225, 255), mMask);//table 2
		Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		addCoutoursListToOther(contoursColor, contours2);

		mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		mHsvMat.release();		mMask.release();	mHierarchy.release();	imageProcess.release();

		return contoursColor;
	}

    /**
	 * Get the countours of blue gray color in the mole
	 */
	public List<MatOfPoint> getBlueContour(Mat imageProcess)
	{
		//Mat originalMat= notBackground.clone();
		Rect r = getRect();
		Mat mHsvMat = new Mat();
		Mat mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();

		Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);  // convert input-image to HSV-image

		Core.inRange(mHsvMat.submat(r), new Scalar(100,5,5, 0), new Scalar(230,255,255, 255), mMask);
		//Core.inRange(mHsvMat.submat(r), new Scalar(115,21.2,50, 0), new Scalar(270,240,120, 255), mMask);// table 2
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


		mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		mHsvMat.release();		mMask.release();	mHierarchy.release();	imageProcess.release();

		return contoursColor;
	}

    /**
	 * Get the countours of white color in the mole
	 */
	public List<MatOfPoint> getWhiteContour(Mat imageProcess)
	{
		//Mat originalMat= notBackground.clone();
		Rect r = getRect();
		Mat mHsvMat = new Mat();
		Mat mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();

		Imgproc.cvtColor(imageProcess, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);  // convert input-image to HSV-image


		//Range of the color
		Core.inRange(mHsvMat.submat(r), new Scalar(0, 1, 70, 255), new Scalar(360, 90, 255, 255), mMask);   //
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


				    /*
				    List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
				    Core.inRange(mHsvMat.submat(r), new Scalar(30, 0, 165, 0), new Scalar(50, 30, 255, 255), mMask);
				    Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

				    contours2 = new ArrayList<MatOfPoint>();
				    Core.inRange(mHsvMat.submat(r), new Scalar(240, 0, 165, 0), new Scalar(360, 150, 255, 255), mMask);
		        	Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		        	addCoutoursListToOther(contoursColor, contours2);


		        	contours2 = new ArrayList<MatOfPoint>();
				    Core.inRange(mHsvMat.submat(r), new Scalar(50, 0, 165, 0), new Scalar(240, 30, 255, 255), mMask);
				    Imgproc.findContours(mMask, contours2, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
				    addCoutoursListToOther(contoursColor, contours2);

				    */
		mMask = Mat.zeros(imageProcess.submat(r).size(), CvType.CV_8UC1);
		Imgproc.drawContours(mMask, contoursColor, -1, new Scalar(255,255,255,255) ,-1);
		Imgproc.findContours(mMask, contoursColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


		mHsvMat.release();		mMask.release();	mHierarchy.release();	imageProcess.release();

		return contoursColor;
	}


    /**
	 * Get the countours of white color in the mole
	 */
	public void colorProcessImage()
	{
		List<MatOfPoint> contoursColor = new ArrayList<MatOfPoint>();
		Scalar toBlackColor = new Scalar(240,0,63,255);   // red color
		Scalar toLightBrownColor  = new Scalar(0,255,255,255);   // cyan
		Scalar toDarkBrownColor  = new Scalar(0,0,204,255); // dark blue
		Scalar toRedColor  = new Scalar(0,0,0,255);  // black
		Scalar toBlueColor  = new Scalar(0,153,0,255); // green
		Scalar toWhiteColor  = new Scalar(255,255,0,255); // yellow

		Scalar colorsPrint[] = new Scalar[7];
		String colorsName[] = new String[7];
		int nCls = 0;
		int sizeLineC = 2;
		countoursImage = originalImage.clone();
		countoursAbruptColor = new ArrayList<MatOfPoint>();

		resetColorOftheImage();
		contoursColor = getBlackContour(originalImage.clone());
		if(setColorFind(contoursColor)){
			removeTotalColorOftheImage(contoursColor);//removeColorOftheImage(contoursColor);
			drawBiggestsCountours(countoursImage, contoursColor, toBlackColor, sizeLineC);//Imgproc.drawContours(countoursImage.submat(r), contoursColor, maxArea, toBlackColor,1);
			colorsPrint[nCls] = toBlackColor;	  colorsName[nCls] = "BLACK"; nCls++;//setColorLabeMatrixAbove(toBlackColor, "BLACK");
			addCoutoursListToOther(countoursAbruptColor, contoursColor);
		}

		contoursColor = getDarkBrownContour(img.clone());
		if(setColorFind(contoursColor)){
			removeTotalColorOftheImage(contoursColor);//removeColorOftheImage(contoursColor);
			drawBiggestsCountours(countoursImage, contoursColor, toDarkBrownColor,sizeLineC);//Imgproc.drawContours(countoursImage.submat(r), contoursColor, maxArea, toDarkBrownColor,1);
			colorsPrint[nCls] = toDarkBrownColor;	  colorsName[nCls] = "DARK BROWN"; nCls++;//setColorLabeMatrixAbove(toDarkBrownColor, "DARK BROWN");
			addCoutoursListToOther(countoursAbruptColor, contoursColor);
		}


		contoursColor = getBlueContour(imageHelpColor.clone());
		if(setColorFind(contoursColor)){
			removeTotalColorOftheImage(contoursColor);//removeColorOftheImage(contoursColor);
			drawBiggestsCountours(countoursImage, contoursColor, toBlueColor,sizeLineC);//Imgproc.drawContours(countoursImage.submat(r), contoursColor, maxArea, toGrayColor,1);
			colorsPrint[nCls] = toBlueColor;	  colorsName[nCls] = "BLUE-WHITE"; nCls++;//setColorLabeMatrixAbove(toGrayColor, "GRAY BLUE");
			addCoutoursListToOther(countoursAbruptColor, contoursColor);
		}


		/// detectando a cor rosa nas pintas, e nao detectando o mais branco
		contoursColor = getWhiteContour(imageHelpColor.clone());
		if(setColorFind(contoursColor)){
			//removeTotalColorOftheImage(contoursColor);//removeColorOftheImage(contoursColor);
			drawBiggestsCountours(countoursImage, contoursColor, toWhiteColor,sizeLineC);//Imgproc.drawContours(countoursImage.submat(r), contoursColor, maxArea, toWhiteColor,1);
			colorsPrint[nCls] = toWhiteColor;	  colorsName[nCls] = "WHITE"; nCls++;//setColorLabeMatrixAbove(toWhiteColor, "WHITE");
			addCoutoursListToOther(countoursAbruptColor, contoursColor);
		}


		contoursColor = getLightBrownContour(img.clone());
		if(setColorFindWithoutRemove(contoursColor)){
			//removeTotalColorOftheImage(contoursColor);//removeColorOftheImage(contoursColor);
			drawBiggestsCountours(countoursImage, contoursColor, toLightBrownColor,sizeLineC);//Imgproc.drawContours(countoursImage.submat(r), contoursColor, maxArea, toLightBrownColor,1);
			colorsPrint[nCls] = toLightBrownColor;	  colorsName[nCls] = "LIGHT BROWN"; nCls++;//setColorLabeMatrixAbove(toLightBrownColor, "LIGHT BROWN");
			addCoutoursListToOther(countoursAbruptColor, contoursColor);
		}

		contoursColor = getRedContour(imageHelpColor.clone());
		if(setColorFind(contoursColor)){
			removeTotalColorOftheImage(contoursColor);//removeColorOftheImage(contoursColor);
			drawBiggestsCountours(countoursImage, contoursColor, toRedColor,sizeLineC);//Imgproc.drawContours(countoursImage.submat(r), contoursColor, maxArea, toRedColor,1);
			colorsPrint[nCls] = toRedColor;	  colorsName[nCls] = "RED"; nCls++;//setColorLabeMatrixAbove(toRedColor, "RED");
			addCoutoursListToOther(countoursAbruptColor, contoursColor);
		}


		/**
		 *  Get the contours of the mole by color
		 */

		Mat mMask = Mat.zeros(originalImage.size(), CvType.CV_8UC1);
		Mat mHierarchy = new Mat();
		mMask = Mat.zeros(originalImage.size(), CvType.CV_8UC1);
		Rect r = getRect();

		for(int i=0; i < countoursAbruptColor.size(); i++){
			Imgproc.drawContours(mMask.submat(r), countoursAbruptColor, i, new Scalar(255,255,255,255) ,-1);
			//Imgproc.drawContours(mMask, countoursAbruptColor, i, new Scalar(255,255,255,255) ,-1);
		}

		//Imgproc.findContours(mMask, countoursAbruptColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		countoursAbruptColor = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mMask, countoursAbruptColor, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		/**
		 *
		 */


		int sizeSquare;
		if(nCls < 4)
			sizeSquare = (helpMatrix.height() / 3) - 4;
		else
			sizeSquare = (helpMatrix.height() / nCls) - 4;

		int textSize;
		if(sizeSquare > 40)
			textSize = 2;
		else if(sizeSquare > 100)
			textSize = 3;
		else if(sizeSquare > 200)
			textSize = 4;
		else
			textSize = 1;

		nColors = 0;
		for(int i=0; i < nCls; i++){
			setColorLabeMatrixAbove(colorsPrint[i], colorsName[i], sizeSquare, textSize);
		}

	}
	/**
	 * Get the contour of the mole by the colors
	 * @return countoursAbruptColor -> the contour of the mole by the colors
	 */
	public List<MatOfPoint> returnCountoursAbruptColor(){
		return countoursAbruptColor;
	}

	/**
	 * Get the part wanted part of the mole, like in the schematic below
	 *
	 *           \1|2/
	 *          8_\|/_3
	 *           7/|\4
	 *           /6|5\
	 *
	 * @param wantedPart -> the part the wanted the contours, like shown in the schematic above
	 * @return border -> the contours for the wanted part
	 */
	public List<MatOfPoint> getContoursPartMole(List<MatOfPoint> cAbruptEdgeColor, int wantedPart){
		List<MatOfPoint> border = new ArrayList<MatOfPoint>();

		Mat originalMat = getOriginalImage();
		Mat abruptEdge = new Mat(originalMat.size(),CvType.CV_8UC3);

		abruptEdge = getMoleWithoutBackground();
		Mat maskZero =  Mat.zeros(originalMat.size(), CvType.CV_8UC1);
		Imgproc.drawContours(maskZero, cAbruptEdgeColor, maxArea(cAbruptEdgeColor), new Scalar(255,255,255,255),-1);
		Mat tempBlack = new Mat(abruptEdge.size(), abruptEdge.type());
		abruptEdge.copyTo(tempBlack, maskZero);
		abruptEdge = tempBlack.clone();

		Point rook_points[][] = new Point [1][4];
		if(wantedPart == 1){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(0, 0 );
			rook_points[0][1] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][2] = new Point(0, halfOfMoleY);
			rook_points[0][3] = new Point(0, 0);
		}
		else if(wantedPart == 2){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(halfOfMoleX, 0 );
			rook_points[0][1] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][2] = new Point(originalMat.size().width, 0);
			rook_points[0][3] = new Point(halfOfMoleX, 0 );
		}
		else if(wantedPart == 3){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(originalMat.size().width, 0);
			rook_points[0][1] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][2] = new Point(originalMat.size().width, halfOfMoleY);
			rook_points[0][3] = new Point(originalMat.size().width, 0);
		}
		else if(wantedPart == 4){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][1] = new Point(originalMat.size().width, halfOfMoleY);
			rook_points[0][2] = new Point(originalMat.size().width, originalMat.size().height);
			rook_points[0][3] = new Point(halfOfMoleX, halfOfMoleY);
		}
		else if(wantedPart == 5){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][1] = new Point(originalMat.size().width, originalMat.size().height);
			rook_points[0][2] = new Point(halfOfMoleX, originalMat.size().height);
			rook_points[0][3] = new Point(halfOfMoleX, halfOfMoleY);
		}
		else if(wantedPart == 6){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][1] = new Point(0, originalMat.size().height);
			rook_points[0][2] = new Point(halfOfMoleX, originalMat.size().height);
			rook_points[0][3] = new Point(halfOfMoleX, halfOfMoleY);
		}
		else if(wantedPart == 7){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][1] = new Point(0, originalMat.size().height);
			rook_points[0][2] = new Point(0, halfOfMoleY);
			rook_points[0][3] = new Point(halfOfMoleX, halfOfMoleY);
		}
		else if(wantedPart == 8){
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(halfOfMoleX, halfOfMoleY);
			rook_points[0][1] = new Point(0, halfOfMoleY);
			rook_points[0][2] = new Point(0, 0);
			rook_points[0][3] = new Point(halfOfMoleX, halfOfMoleY);
		}
		else{
			rook_points = new Point [1][4];
			rook_points[0][0] = new Point(0, 0);
			rook_points[0][1] = new Point(0, 0);
			rook_points[0][2] = new Point(0, 0);
			rook_points[0][3] = new Point(0, 0);
		}

		border = new ArrayList<MatOfPoint>();
		border.add(new MatOfPoint(rook_points[0][0], rook_points[0][1], rook_points[0][2], rook_points[0][3]));

		maskZero =  Mat.zeros(originalMat.size(), CvType.CV_8UC1);
		Imgproc.drawContours(maskZero, border, -1, new Scalar(255,255,255,255),-1);
		tempBlack = new Mat(abruptEdge.size(), abruptEdge.type());
		abruptEdge.copyTo(tempBlack, maskZero);
		abruptEdge = tempBlack.clone();

		Mat grayMatAbrupt = new Mat(abruptEdge.size(),CvType.CV_8UC1);
		Imgproc.cvtColor(abruptEdge, grayMatAbrupt, Imgproc.COLOR_BGR2GRAY);
		List<MatOfPoint> cAbruptEdgeColorPart = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(grayMatAbrupt, cAbruptEdgeColorPart , hierarchy , Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);


		return cAbruptEdgeColorPart;
	}


	/**
	 * Draw the contours for the biggest countours of the mole
	 */
	public void drawBiggestsCountours(Mat	countoursImage, List<MatOfPoint> contoursColor, Scalar color, int size)
	{
		Rect r = getRect();
		double maxAreaColor = getContoursArea(contoursColor);
		double specContourArea = 0;
		double percArea;
		int rangePerc = 5;

		for(int i=0; i< contoursColor.size(); i++){
			specContourArea = getContoursArea(contoursColor, i);
			percArea = (specContourArea*100) / maxAreaColor;
			if( percArea > rangePerc)
				Imgproc.drawContours(countoursImage.submat(r), contoursColor, i, color,size);
		}
	}


	/**
	 * Set a certain color, after check.
	 */
	public boolean setColorFind(List<MatOfPoint> contoursColor){
		List<MatOfPoint> moleContour	=  getCoutourMole();
		double maxAreaMole = getContoursArea(moleContour);
		double maxAreaColor = getContoursArea(contoursColor);
		double percArea;
		int rangePerc = 20;

		if(maxAreaColor >= maxAreaMole)
			percArea = 90;
		else
			percArea = (maxAreaColor*100) / maxAreaMole;

		if(percArea > rangePerc){
			removeColorOftheImage(contoursColor);
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Set a certain color, after check.
	 */
	public boolean setColorFindWithoutRemove(List<MatOfPoint> contoursColor){
		List<MatOfPoint> moleContour	=  getCoutourMole();
		double maxAreaMole = getContoursArea(moleContour);
		double maxAreaColor = getContoursArea(contoursColor);
		double percArea;
		int rangePerc = 20;

		if(maxAreaColor >= maxAreaMole)
			percArea = 90;
		else
			percArea = (maxAreaColor*100) / maxAreaMole;

		if(percArea > rangePerc){
			// removeColorOftheImage(contoursColor);
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * Get the countours of white color in the mole
	 */
	public Mat getColorProcessImage()
	{
		return countoursImage;
	}



	/**
	 * Set the matrix above size
	 * @param
	 * 	matrixAbove ->  receive the matrix above that should be empty
	 */
	public void setMatrixAbove( Mat matrixAbove)
	{
		helpMatrix = matrixAbove.clone();
		nColors = 0;
	}

	/**
	 * Set the square of colors label
	 * @param
	 * 	colorPrint ->  the Scalar color that will be painting the label
	 * 	colorName-> the name the will be printed in front of the label
	 */
	private void setColorLabeMatrixAbove(Scalar colorPrint, String colorName, int sizeSquare, int textSize)
	{
		//int sizeSquare = 40;
		colorLabel = helpMatrix.submat(4+nColors*sizeSquare, (nColors+1)*sizeSquare, 4, sizeSquare);
		colorLabel.setTo(colorPrint);
		Core.putText(helpMatrix, colorName, new Point( sizeSquare + 2, sizeSquare/2 +nColors*sizeSquare ), 1, textSize, new Scalar(0, 0, 0, 255), 1);
		//Core.putText(helpMatrix, colorName, new Point( sizeSquare + 2, 25+nColors*sizeSquare ), 1, textSize, new Scalar(0, 0, 0, 255), 1);
		nColors++;
	}

	/**
	 * Receive the matriz above after the process, with the labels of the mole
	 */
	public Mat getMatrixAbove()
	{
		return helpMatrix.clone();
	}

	/**
	 * Receive the number of colors in the mole
	 */
	public int getNumberOfColors()
	{
		return nColors;
	}

	/**
	 * Get the countours of white color in the mole
	 * @param
	 * 	coutoursRemove ->  the contours that want to remove the biggest area of the image help
	 */
	private void removeColorOftheImage(List<MatOfPoint> coutoursRemove)
	{
		Rect r = getRect();
		Mat maskZero =  Mat.ones(imageHelpColor.size(), CvType.CV_8UC1);
		int maxArea = maxArea(coutoursRemove);
		Imgproc.drawContours(maskZero.submat(r), coutoursRemove, maxArea, new Scalar(0,0,0,0),-1);
		Mat tempBlack = new Mat(imageHelpColor.size(), imageHelpColor.type());
		imageHelpColor.copyTo(tempBlack, maskZero);
		imageHelpColor = tempBlack.clone();
		//Imgproc.drawContours(imageHelpColor.submat(r), coutoursRemove, maxArea, new Scalar(0,0,204,255),1);
	}

	/**
	 * Get the countours of white color in the mole
	 * @param
	 * 	coutoursRemove ->  the contours that want to remove all the countours
	 */
	private void removeTotalColorOftheImage(List<MatOfPoint> coutoursRemove)
	{
		Rect r = getRect();
		Mat maskZero =  Mat.ones(imageHelpColor.size(), CvType.CV_8UC1);

		for(int i=0; i<coutoursRemove.size();i++ ){
			Imgproc.drawContours(maskZero.submat(r), coutoursRemove, i, new Scalar(0,0,0,0),-1);
		}

		Mat tempBlack = new Mat(imageHelpColor.size(), imageHelpColor.type());
		imageHelpColor.copyTo(tempBlack, maskZero);
		imageHelpColor = tempBlack.clone();

	}


    /**
	 * 	Return the position of the max area
	 */
	public int maxArea(List<MatOfPoint> contoursColor){
		Mat contourC = new Mat();
		double maxAreaC = -1;
		int maxAreaIdxC = -1;
		for(int idx = 0; idx < contoursColor.size(); idx++){
			contourC = contoursColor.get(idx);
			//contoursMat.add(contourC);
			double contourarea = Imgproc.contourArea(contourC);

			if(contourarea > maxAreaC){
				maxAreaC = contourarea;
				maxAreaIdxC = idx;
			}
		}
		return maxAreaIdxC;
	}

    /**
	 * 	Reset imageOfTakeColor
	 */
	public void resetColorOftheImage()
	{
		imageHelpColor = notBackground.clone();
	}


    /**
	 *
	 *
	 */
	public static void addCoutoursListToOther(List<MatOfPoint> countoursA, List<MatOfPoint> countoursB){

		for(int i=0; i < countoursB.size();i++){
			countoursA.add(countoursB.get(i));
		}

	}


    public List<MatOfPoint> getColorContour(Mat img, Scalar low_color, Scalar high_color,
                                            String color){
        Mat mHsvMat = new Mat();
        Mat mMask = Mat.zeros(img.size(), CvType.CV_8UC1);
        Mat mHierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        double cntArea = 0;
//        double maxAreaColor = getContoursArea(cnt);
//        double maxAreaColor = getContoursArea(cnt);
//        double specContourArea = 0;
//        double percArea;
//        int rangePerc = 5;

        Imgproc.cvtColor(img, mHsvMat, Imgproc.COLOR_BGR2HSV);
        //Range of the color
        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Core.inRange(mHsvMat, low_color, high_color, mMask);//table 2
        Imgproc.findContours(mMask, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
//        Double[] areas = new Double[contours.size()];
        for(int i=0; i< contours.size(); i++){
            Mat contour = contours.get(i);
            cntArea = Imgproc.contourArea(contour);
            if(cntArea > contourArea * 0.02 && !color.equals("Black")){
                contours2.add(contours.get(i));
//                Log.i("INTERNAL", color + " Area" + cntArea);
            }
            if(cntArea > contourArea * 0.02 && color.equals("Black") && cntArea < contourArea){
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

    public void setContourArea(double maxAreaC){
        this.contourArea = maxAreaC;
    }

    public void setValueThreshold(double value_threshold){
        this.value_threshold = value_threshold;
    }

    public void getAllColorContours(Mat mask_img, Mat img){
        Map<String, ArrayList<Scalar>> map = new HashMap<String, ArrayList<Scalar>>();
        Scalar color = new Scalar(15,0,0);
        map.put("Blue Gray",new ArrayList<Scalar>(){{
            add(new Scalar(15,0,0));
            add(new Scalar(179,255,value_threshold));
            add(new Scalar(0,153,0));
        }});
        map.put("White",new ArrayList<Scalar>(){{
            add(new Scalar(0,0,145));
            add(new Scalar(15,80,value_threshold));
            add(new Scalar(255,255,0));
        }});
        map.put("Light Brown",new ArrayList<Scalar>(){{
            add(new Scalar(0,80,value_threshold+3));
            add(new Scalar(15,255,255));
            add(new Scalar(0,255,255));
        }});
        map.put("Dark Brown",new ArrayList<Scalar>(){{
            add(new Scalar(0,80,0));
            add(new Scalar(15,255,value_threshold-3));
            add(new Scalar(0,0,204));
        }});
        map.put("Black",new ArrayList<Scalar>(){{
            add(new Scalar(0,0,0));
            add(new Scalar(15,140,90));
            add(new Scalar(0,0,0));
        }});

        Log.i("INTERNAL", "Contour Area" + contourArea);
        for (String key : map.keySet()){
            ArrayList<Scalar> colors = map.get(key);
            List<MatOfPoint> colorContours;
            colorContours = getColorContour(mask_img,colors.get(0), colors.get(1), key);
            Imgproc.drawContours(img, colorContours , -1, colors.get(2),thickness);
         }
    }

}