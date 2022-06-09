package uk.ac.imperial.isst.tcity.cpag.editor.display;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{

	private static final long serialVersionUID = -7289019003868436286L;
	private BufferedImage image;
    private int x;
    private int y;
    private int width;
    private int height;

    public ImagePanel(String imagePath, int x, int y, int width, int height) {
    	this.x = x;
    	this.y = y;
    	this.width = width;
    	this.height = height;
    	
    	File targetIconFile = new File(imagePath);
		if (!targetIconFile.exists()) {
			System.out.println("Target icon file not found");
		} else {
			try {
				this.image = ImageIO.read(targetIconFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
    }

    public void display(Graphics g) {
    	g.drawImage(image, this.x, this.y, this.width, this.height, this); // see javadoc for more info on the parameters
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters            
        g.drawImage(image, this.x, this.y, this.width, this.height, this); // see javadoc for more info on the parameters
    }

}