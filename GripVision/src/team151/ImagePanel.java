package team151;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;  
	private BufferedImage image;  
	// Create a constructor method  
	public ImagePanel(){  
		super();  
	}  
	private BufferedImage getImage(){  
		return image;  
	}  
	void setImage(BufferedImage  temp){  
		image = temp;  
		return;  
	}  
	/**  
	 * Converts/writes a Mat into a BufferedImage.  
	 *  
	 * @param matrix Mat of type CV_8UC3 or CV_8UC1  
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
	 */  
	public static BufferedImage matToBufferedImage(Mat matrix) {  
		BufferedImage bufImage = null;
		try {
			MatOfByte matOfByte = new MatOfByte();
			Imgcodecs.imencode(".jpg", matrix, matOfByte); 
			byte[] byteArray = matOfByte.toArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			return bufImage;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}  

	@Override
	public void paintComponent(Graphics g){  
		BufferedImage temp = getImage();  
		if(temp != null) {
			g.drawImage(temp, 10, 10, temp.getWidth(), temp.getHeight(), this);  
		}
	} 
}