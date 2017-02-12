package team151;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class TestProgram {

	public static void main(String[] args) {
		
/*
		JFrame frame = new JFrame();
		frame.setLayout(new FlowLayout());
		
		JLabel gearLabel;
		JLabel boilerLabel;
		JLabel boiler2Label;
		ImageIcon gearImgIcon;
		ImageIcon boilerImgIcon;
		ImageIcon boiler2ImgIcon;
		
		Vision3 vis = new Vision3();
		
		Mat gear, finalGear, boiler, finalBoiler, boiler2, finalBoiler2;
		Image gearImg, boilerImg, boiler2Img;
		double[] hsvHue, hsvSaturation, hsvValue;
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter 'red' or 'blue'");
		String color = scan.nextLine();
		String basePath = new File("").getAbsolutePath();
		
		if(color.equalsIgnoreCase("red")) {
			gear = Imgcodecs.imread(basePath + "\\red-images\\gear-red.png");
//			boiler = Imgcodecs.imread("C:/Users/Shannon/Downloads/bright-edited/boiler-red.png");
			boiler2 = Imgcodecs.imread(basePath + "\\red-images\\boiler2-red.png");
			
			hsvHue = new double[]{0.0, 15.0};
			hsvSaturation = new double[]{0.0, 25.0};
			hsvValue = new double[]{245.0, 255.0};
			
			finalGear = vis.getFinalMat(gear, hsvHue, hsvSaturation, hsvValue);
			gearImg = toBufferedImage(finalGear);
			gearImgIcon = new ImageIcon(gearImg);
			gearLabel = new JLabel(gearImgIcon);
			
//			Mat finalBoiler = vis.getFinalMat(boiler);
//			boilerImg = toBufferedImage(finalBoiler);
//			boilerImgIcon = new ImageIcon(boilerImg);
//			boilerLabel = new JLabel(boilerImgIcon);
			boilerLabel = new JLabel();
			
			finalBoiler2 = vis.getFinalMat(boiler2, hsvHue, hsvValue, hsvSaturation);
			boiler2Img = toBufferedImage(finalBoiler2);
			boiler2ImgIcon = new ImageIcon(boiler2Img);
			boiler2Label = new JLabel(boiler2ImgIcon);
			
		} else {
			String gearpath = basePath + "\\blue-images\\gear-blue.png";
			gear = Imgcodecs.imread(basePath + "\\blue-images\\gear-blue.png");
			boiler = Imgcodecs.imread(basePath + "\\blue-images\\boiler-blue.png");
			boiler2 = Imgcodecs.imread(basePath + "\\blue-images\\boiler2-blue.png");
			
			hsvHue = new double[]{80.0, 95.0};
			hsvSaturation = new double[]{0.0, 25.0};
			hsvValue = new double[]{245.0, 255.0};
			
			finalGear = vis.getFinalMat(gear, hsvHue, hsvSaturation, hsvValue);
			gearImg = toBufferedImage(finalGear);
			gearImgIcon = new ImageIcon(gearImg);
			gearLabel = new JLabel(gearImgIcon);
			
			finalBoiler = vis.getFinalMat(boiler, hsvHue, hsvSaturation, hsvValue);
			boilerImg = toBufferedImage(finalBoiler);
			boilerImgIcon = new ImageIcon(boilerImg);
			boilerLabel = new JLabel(boilerImgIcon);
			
			finalBoiler2 = vis.getFinalMat(boiler2, hsvHue, hsvSaturation, hsvValue);
			boiler2Img = toBufferedImage(finalBoiler2);
			boiler2ImgIcon = new ImageIcon(boiler2Img);
			boiler2Label = new JLabel(boiler2ImgIcon);
		}
		
		frame.add(gearLabel);
		frame.add(boilerLabel);
		frame.add(boiler2Label);
		
		frame.pack();
		frame.setVisible(true);
		
		
*/
		
		
		
		
		
//		Vision3 vis = new Vision3();
		GripPipeline vis = new GripPipeline();
		//frame.setVisible(true);
		JFrame vidContainer = new JFrame();
		ImagePanel vidPanel = new ImagePanel();
		vidContainer.add(vidPanel);
		VideoCapture cap = new VideoCapture(1);
		Mat vidFrame = new Mat();
		BufferedImage temp;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
//			 TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		vidContainer.setVisible(true);

		if(cap.isOpened())  {  
			System.out.println("yay");
			while(true)  {  
				cap.read(vidFrame);  
				if(!vidFrame.empty() )  {  
					vidContainer.setSize(vidFrame.width()+40, vidFrame.height()+60);  
					temp = vidPanel.matToBufferedImage(vis.process(vidFrame)); 
//					temp = vidPanel.matToBufferedImage(vidFrame);
					vidPanel.setImage(temp);  
					vidPanel.repaint();  
					System.out.println(vis.getDistance());
				}  else  {  
					System.out.println(" --(!) No captured frame -- Break!");  
					break;  
				}  
			}  
		} else {
			System.out.println("no");
		}
		return;  
	}  


	public static BufferedImage imshow(Mat src){
		BufferedImage bufImage = null;
		try {
			MatOfByte matOfByte = new MatOfByte();
			Imgcodecs.imencode(".jpg", src, matOfByte); 
			byte[] byteArray = matOfByte.toArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			return bufImage;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Image toBufferedImage(Mat m){
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if ( m.channels() > 1 ) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels()*m.cols()*m.rows();
		byte [] b = new byte[bufferSize];
		m.get(0,0,b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);  
		return image;

	}
}
