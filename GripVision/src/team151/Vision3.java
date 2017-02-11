package team151;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opencv.core.*;
import org.opencv.imgproc.*;

public class Vision3 {
	//Outputs
	private Mat cvResizeOutput = new Mat();
	private Mat hsvThresholdOutput = new Mat();
	private Mat blurOutput = new Mat();
	private ArrayList<Line> findLinesOutput = new ArrayList<Line>();
	private ArrayList<Line> filterLinesOutput = new ArrayList<Line>();
	private Point smallestPoint = new Point();
	private Point largestPoint = new Point();
	private Mat rectangleOutput = new Mat();
	private Point centerPoint = new Point();
	private Mat findCenterOfRectangleOutput = new Mat();

	static {
		System.loadLibrary("opencv_java320");
	}

	/**
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 */
	public void process(Mat source0, double[] hue, double[] saturation, double[] value) {
		
		System.out.println("initial mat is " + source0);
		// Step CV_resize0:
		Mat cvResizeSrc = source0;
		Size cvResizeDsize = new Size(0, 0);
		double cvResizeFx = 0.125;
		double cvResizeFy = 0.125;
		int cvResizeInterpolation = Imgproc.INTER_LINEAR;
		cvResize(cvResizeSrc, cvResizeDsize, cvResizeFx, cvResizeFy, cvResizeInterpolation, cvResizeOutput);

		// Step HSV_Threshold0:
		Mat hsvThresholdInput = cvResizeOutput;
		hsvThreshold(hsvThresholdInput, hue, saturation, value, hsvThresholdOutput);

		// Step Blur0:
		Mat blurInput = hsvThresholdOutput;
		BlurType blurType = BlurType.get("Box Blur");
		double blurRadius = 3;
		blur(blurInput, blurType, blurRadius, blurOutput);

		// Step Find_Lines0:
		Mat findLinesInput = blurOutput;
		findLines(findLinesInput, findLinesOutput);

		// Step Filter_Lines0:
		ArrayList<Line> filterLinesLines = findLinesOutput;
		double filterLinesMinLength = 10.0;
		double[] filterLinesAngle = {0, 360};
		filterLines(filterLinesLines, filterLinesMinLength, filterLinesAngle, filterLinesOutput);
		
		//Step Rectangle:
		Mat rectangleInput = cvResizeOutput;
		rectangle(rectangleInput, findLinesOutput, rectangleOutput);
		
		//Step Find_Center_Of_Rectangle:
		Mat findCenterOfRectangleInput = rectangleOutput;
		findCenterOfRectangle(findCenterOfRectangleInput, smallestPoint, largestPoint, findCenterOfRectangleOutput);

	}

	/**
	 * This method is a generated getter for the output of a CV_resize.
	 * @return Mat output from CV_resize.
	 */
	public Mat getCvResizeOutput() {
		return cvResizeOutput;
	}

	/**
	 * This method is a generated getter for the output of a HSV_Threshold.
	 * @return Mat output from HSV_Threshold.
	 */
	public Mat getHsvThresholdOutput() {
		return hsvThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a Blur.
	 * @return Mat output from Blur.
	 */
	public Mat getBlurOutput() {
		return blurOutput;
	}

	/**
	 * This method is a generated getter for the output of a Find_Lines.
	 * @return ArrayList<Line> output from Find_Lines.
	 */
	public ArrayList<Line> getFindLinesOutput() {
		return findLinesOutput;
	}

	/**
	 * This method is a generated getter for the output of a Filter_Lines.
	 * @return ArrayList<Line> output from Filter_Lines.
	 */
	public ArrayList<Line> getFilterLinesOutput() {
		return filterLinesOutput;
	}

	public Point getSmallestPoint() {
		return smallestPoint;
	}
	
	public Point getLargestPoint() {
		return largestPoint;
	}
	
	public Mat getRectangleOutput() {
		return rectangleOutput;
	}
	
	public Point getCenterPoint() {
		return centerPoint;
	}
	
	public Mat getFindCenterOfRectangleOutput() {
		return findCenterOfRectangleOutput;
	}
	
	public Mat getFinalMat(Mat source, double[] hue, double[] saturation, double[] value) {
		process(source, hue, saturation, value);
		return findCenterOfRectangleOutput;
	}

	/**
	 * Resizes an image.
	 * @param src The image to resize.
	 * @param dSize size to set the image.
	 * @param fx scale factor along X axis.
	 * @param fy scale factor along Y axis.
	 * @param interpolation type of interpolation to use.
	 * @param dst output image.
	 */
	private void cvResize(Mat src, Size dSize, double fx, double fy, int interpolation,
			Mat dst) {
		if (dSize==null) {
			dSize = new Size(0,0);
		}
		Imgproc.resize(src, dst, dSize, fx, fy, interpolation);
	}

	/**
	 * Segment an image based on hue, saturation, and value ranges.
	 *
	 * @param input The image on which to perform the HSL threshold.
	 * @param hue The min and max hue
	 * @param sat The min and max saturation
	 * @param val The min and max value
	 * @param output The image in which to store the output.
	 */
	private void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val,
			Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
		Core.inRange(out, new Scalar(hue[0], sat[0], val[0]),
				new Scalar(hue[1], sat[1], val[1]), out);
	}

	/**
	 * An indication of which type of filter to use for a blur.
	 * Choices are BOX, GAUSSIAN, MEDIAN, and BILATERAL
	 */
	enum BlurType{
		BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"),
		BILATERAL("Bilateral Filter");

		private final String label;

		BlurType(String label) {
			this.label = label;
		}

		public static BlurType get(String type) {
			if (BILATERAL.label.equals(type)) {
				return BILATERAL;
			}
			else if (GAUSSIAN.label.equals(type)) {
				return GAUSSIAN;
			}
			else if (MEDIAN.label.equals(type)) {
				return MEDIAN;
			}
			else {
				return BOX;
			}
		}

		@Override
		public String toString() {
			return this.label;
		}
	}

	/**
	 * Softens an image using one of several filters.
	 * @param input The image on which to perform the blur.
	 * @param type The blurType to perform.
	 * @param doubleRadius The radius for the blur.
	 * @param output The image in which to store the output.
	 */
	private void blur(Mat input, BlurType type, double doubleRadius,
			Mat output) {
		int radius = (int)(doubleRadius + 0.5);
		int kernelSize;
		switch(type){
		case BOX:
			kernelSize = 2 * radius + 1;
			Imgproc.blur(input, output, new Size(kernelSize, kernelSize));
			break;
		case GAUSSIAN:
			kernelSize = 6 * radius + 1;
			Imgproc.GaussianBlur(input,output, new Size(kernelSize, kernelSize), radius);
			break;
		case MEDIAN:
			kernelSize = 2 * radius + 1;
			Imgproc.medianBlur(input, output, kernelSize);
			break;
		case BILATERAL:
			Imgproc.bilateralFilter(input, output, -1, radius, radius);
			break;
		}
	}

	public static class Line {
		public final double x1, y1, x2, y2;
		public Line(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		public double getX1() {
			return this.x1;
		}
		public double getX2() {
			return this.x2;
		}
		public double getY1() {
			return this.y1;
		}
		public double getY2() {
			return this.y2;
		}
		public double lengthSquared() {
			return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
		}
		public double length() {
			return Math.sqrt(lengthSquared());
		}
		public double angle() {
			return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
		}
	}
	/**
	 * Finds all line segments in an image.
	 * @param input The image on which to perform the find lines.
	 * @param lineList The output where the lines are stored.
	 */
	private void findLines(Mat input, ArrayList<Line> lineList) {
		final LineSegmentDetector lsd = Imgproc.createLineSegmentDetector();
		final Mat lines = new Mat();
		lineList.clear();
		if (input.channels() == 1) {
			lsd.detect(input, lines);
		} else {
			final Mat tmp = new Mat();
			Imgproc.cvtColor(input, tmp, Imgproc.COLOR_BGR2GRAY);
			lsd.detect(tmp, lines);
		}
		if (!lines.empty()) {
			for (int i = 0; i < lines.rows(); i++) {
				lineList.add(new Line(lines.get(i, 0)[0], lines.get(i, 0)[1],
						lines.get(i, 0)[2], lines.get(i, 0)[3]));
			}
		}
		
		System.out.println(lines.size());
	}

	/**
	 * Filters out lines that do not meet certain criteria.
	 * @param inputs The lines that will be filtered.
	 * @param minLength The minimum length of a line to be kept.
	 * @param angle The minimum and maximum angle of a line to be kept.
	 * @param outputs The output lines after the filter.
	 */
	private void filterLines(List<Line> inputs, double minLength, double[] angle, List<Line> outputs) {
		outputs = inputs.stream().filter(line -> line.lengthSquared() >= Math.pow(minLength,2))
													.filter(line -> (line.angle() >= angle[0] && line.angle() <= angle[1])
						|| (line.angle() + 180.0 >= angle[0] && line.angle() + 180.0 <= angle[1]))
				.collect(Collectors.toList());
	}
	
	/**
	 * Based on the line segments found in an image, draws a rectangle surrounding all the lines.
	 * @param input The image on which the lines were found.
	 * @param lines The ArrayList containing all of the lines found on <code>input</code>.
	 * @param output The Mat on which to draw the rectangle.
	 */
	private void rectangle(Mat input, ArrayList<Line> lines, Mat output) {
		
		System.out.println(lines.size());
		double smallestX = 10000, smallestY = 10000, largestX = 0, largestY = 0;
		double currentX1, currentY1, currentX2, currentY2;
		for(int i = 0; i < lines.size(); i++) {
			System.out.println(lines.get(i).length());
			currentX1 = lines.get(i).getX1();
			currentY1 = lines.get(i).getY1();
			currentX2 = lines.get(i).getX2();
			currentY2 = lines.get(i).getY2();
			smallestX = (currentX1 < smallestX) ? currentX1 : smallestX;
			smallestY = (currentY1 < smallestY) ? currentY1 : smallestY;
			largestX = (currentX2 > largestX) ? currentX2 : largestX;
			largestY = (currentY2 > largestY) ? currentY2 : largestY;
		}
		
		smallestPoint = new Point(smallestX, smallestY);
		largestPoint = new Point(largestX, largestY);
		Imgproc.rectangle(input, smallestPoint,  largestPoint,  new Scalar(0, 255, 0), 2);
		input.copyTo(output);
	}

	/**
	 * Finds the center of the rectangle drawn in the rectangle() method.
	 * @param input The Mat to add the center circle to.
	 * @param smallest A Point that contains the top-left coordinates of the rectangle.
	 * @param largest A Point that contains the bottom-right coordinates of the rectangle.
	 * @param output The Mat to which the center circle will be added.
	 */
	private void findCenterOfRectangle(Mat input, Point smallest, Point largest, Mat output) {
		double length = largest.x - smallest.x;
		double height = largest.y - smallest.y;
		
		centerPoint = new Point(smallest.x + length/2, smallest.y + height/2);
		Imgproc.circle(input, centerPoint, 5, new Scalar(0, 255, 0), 2);
		input.copyTo(output);
	}

}
