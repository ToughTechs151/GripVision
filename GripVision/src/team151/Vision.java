package team151;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

public class Vision {

	static {
		System.loadLibrary("opencv_java320");
	}

	public Vision() {
		
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
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 * @param source
	 */
	public static Mat process(Mat source, double resizeValue, double[] rgbThresholdRed, double[] rgbThresholdGreen, double[] rgbThresholdBlue) {
		Mat sizeOutput = new Mat();
		//Step Resize:
		Imgproc.resize(source, sizeOutput, new Size(), resizeValue, resizeValue, Imgproc.INTER_CUBIC);

		Mat rgbThresholdOutput = new Mat();
		// Step RGB_Threshold:
		Mat rgbThresholdInput  = sizeOutput;
		rgbThreshold(rgbThresholdInput, rgbThresholdRed, rgbThresholdGreen, rgbThresholdBlue, rgbThresholdOutput);

		Mat blurOutput = new Mat();
		// Step Blur:
		Mat blurInput = rgbThresholdOutput;
		BlurType blurType = BlurType.get("Box Blur");
		double blurRadius = 5.4054054054054;
		blur(blurInput, blurType, blurRadius, blurOutput);

		ArrayList<Line> findLinesOutput = new ArrayList<Line>();
		// Step Find_Lines:
		Mat findLinesInput = blurOutput;
		findLines(findLinesInput, findLinesOutput);

		Point smallest = smallestPoint(findLinesOutput);
		Point largest = largestPoint(findLinesOutput);
		System.out.println("smallestpoint is " + smallest + " and largestpoint is " + largest);
		Mat rectOutput = new Mat();
		//Step Draw_Rectangle:
		drawRectangle(sizeOutput, smallest, largest, rectOutput);
		System.out.println(rectOutput);

		Mat centerOutput = new Mat();
		//Step Draw_Center:
		centerOfRect(rectOutput, smallest, largest, centerOutput);
		return centerOutput;
	}
	
	public Mat getFinalMat(Mat source, double resizeValue, double[] rgbThresholdRed, double[] rgbThresholdGreen, double[] rgbThresholdBlue) {
		return process(source, resizeValue, rgbThresholdRed, rgbThresholdGreen, rgbThresholdBlue);
	}

	/**
	 * Segment an image based on color ranges.
	 * @param input The image on which to perform the RGB threshold.
	 * @param red The min and max red.
	 * @param green The min and max green.
	 * @param blue The min and max blue.
	 * @param output The image in which to store the output.
	 */
	public static void rgbThreshold(Mat input, double[] red, double[] green, double[] blue, Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2RGB);
		Core.inRange(out, new Scalar(red[0], green[0], blue[0]), new Scalar(red[1], green[1], blue[1]), out);
	}

	/**
	 * Softens an image using one of several filters.
	 * @param input The image on which to perform the blur.
	 * @param type The blurType to perform.
	 * @param doubleRadius The radius for the blur.
	 * @param output The image in which to store the output.
	 */
	private static void blur(Mat input, BlurType type, double doubleRadius, Mat output) {
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

	/**
	 * Finds all line segments in an image.
	 * @param input The image on which to perform the find lines.
	 * @param lineList The output where the lines are stored.
	 */
	public static void findLines(Mat input, ArrayList<Line> lineList) {
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
	}
	
	public static Point smallestPoint(ArrayList<Line> lineList) {
		double smallestX = 1000;
		double smallestY = 1000;
		for(Line line : lineList) {
			if(line.getX1() < smallestX) {
				smallestX = line.getX1();
			}
			if(line.getX2() < smallestX) {
				smallestX = line.getX2();
			}
			if(line.getY1() < smallestY) {
				smallestY = line.getY1();
			}
			if(line.getY2() < smallestY) {
				smallestY = line.getY1();
			}
		}

		Point smallest = new Point(smallestX, smallestY);
		return smallest;
	}
	
	public static Point largestPoint(ArrayList<Line> lineList) {
		double largestX = 0;
		double largestY = 0;
		for(Line line : lineList) {
			if(line.getX1() > largestX) {
				largestX = line.getX1();
			}
			if(line.getX2() > largestX) {
				largestX = line.getX2();
			}
			if(line.getY1() > largestY) {
				largestY = line.getY1();
			}
			if(line.getY1() > largestY) {
				largestY = line.getY2();
			}
		}

		Point largest = new Point(largestX, largestY);
		return largest;
	}

	public static void drawRectangle(Mat input, Point smallest, Point largest, Mat output) {
		Imgproc.rectangle(input, smallest, largest, new Scalar(255, 0, 0, 1), 5);
		input.copyTo(output);
	}

	public static void centerOfRect(Mat input, Point smallest, Point largest, Mat output) {
		double height = largest.y - smallest.y;
		double width = largest.x - smallest.x;

		double centerY = smallest.y + height/2;
		double centerX = smallest.x + width/2;
		Imgproc.circle(input, new Point(centerX, centerY), 5, new Scalar(0, 255, 0, 1), 2);
		input.copyTo(output);
	}

}
