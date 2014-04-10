import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class ImageUtils {

    public static String IMAGE_TYPE_GIF = "gif";
    public static String IMAGE_TYPE_JPG = "jpg";
    public static String IMAGE_TYPE_JPG1 = "JPG";
    public static String IMAGE_TYPE_JPG2 = "JPEG";
    public static String IMAGE_TYPE_JPEG = "jpeg";
    public static String IMAGE_TYPE_BMP = "bmp";
    public static String IMAGE_TYPE_PNG = "png";
    //public static String IMAGE_TYPE_PSD = "psd";

    public static String IMAGE_TYPE[] = 
    {IMAGE_TYPE_GIF, IMAGE_TYPE_JPG, IMAGE_TYPE_JPG1, IMAGE_TYPE_JPG2, IMAGE_TYPE_JPEG, IMAGE_TYPE_BMP, IMAGE_TYPE_PNG };
    
    static boolean debug = true;
    
    private static String get_file_type(String filename)
    {
    	int dot = filename.lastIndexOf(".");
		String ext = filename.substring(dot + 1);
		
		return ext;
    }
    
    //the picture layout:
    //   the whole buffer is (2100 , 3000)
    //   the picture starts at (60, 280)
    //   the picture rectangle length (2100 - 120 = 1980) ...   y =1980 + 280 = 2260
    
    //   For text ( x=150, y = 2500 )
    //   For 2D code the gap between 2d code and 2420 - 2260 = 160. The space for the 2D code is (500, 500). End with 2920.
    //   for logo (x=2100-100 ,2420,  )  logo size  (1587 * 696)
    
    //compose the twoDcodeImage and the logo under the bottom of srcImg 
    //return the new file.
static  int bufferWidth  = 2100;
static  int bufferHeight = 3000;

//main image pos
static  int picOffsetX = 60;
static  int picOffsetY = 280;

//text pos
static int textoffsetX = 150;
static int textoffsetY = bufferHeight - 500;

//2D image pos
static int twoDImgOffsetX = picOffsetX;
static int twoDImgOffsetY = 2420;
static int twoDImgWidth = 500;
static int twoDImgHeight = 500;

//logo position

static int logoOffsetX = bufferWidth - 400;
static int logoOffsetY = twoDImgOffsetY + 300;
static int logoWidth = 300;
static int logoHeight = (int)(logoWidth / 2.28);


    public static String pictureCompose(String path, String srcImg, String twoDcodeImage)
    {
    	if(twoDcodeImage == null)
    		return srcImg;
    	
    	//System.out.println("path " + path + "srcImg " + 
    		//			srcImg + "twoDcodeimage" + twoDcodeImage);
    	
    	if(path == null)
    			path = "";
    	
    	File srcFile = new File(path + srcImg);
    	File twoDFile = new File(twoDcodeImage);
    	
    	String resultImgPath = null;

    	try {
    		BufferedImage orgBi = ImageIO.read(srcFile);
    		Graphics2D g2d = orgBi.createGraphics();
    		
    		BufferedImage twoDBi = ImageIO.read(twoDFile);
        	int twoDW = twoDBi.getWidth();
    		int twoDH = twoDBi.getHeight();

    		int x = twoDImgOffsetX;
    		int y = twoDImgOffsetY;
    		int w = twoDImgWidth;
    		int h = twoDImgHeight;
    		
    		//XXX modify the offset here!!!
    		
    		g2d.drawImage(twoDBi,
					x, y, x + w, y + h,
    				0, 0, twoDW, twoDH, null);
		
    		
    		//XXX add logo draw..  	    
    	    File srcLogoFile = new File("/usr/lib/jar/logo3.jpg");
    	    BufferedImage logoBi = ImageIO.read(srcLogoFile);
    	            
    	    int logoFilex = logoBi.getWidth();
    	    int logoFiley = logoBi.getHeight();
    	    
    	    int logox = logoOffsetX;
    	    int logoy = logoOffsetY;
    	    int logow = logoWidth;
    	    int logoh = logoHeight;
    	    
    		g2d.drawImage(logoBi,		
					logox, logoy,
					logox + logow, logoy + logoh,
    				0, 0, logoFilex, logoFiley, null);
    	
    		g2d.dispose();
    		//write file
			FileOutputStream out = null;
			resultImgPath = path + "twoD" + srcImg;
			out = new FileOutputStream(resultImgPath);
			ImageIO.write(orgBi, get_file_type(srcImg), out);
			
			out.close();
			
    	}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return "twoD" + srcImg;
    }
    
    // test case
    // path is null
    // text too long with Chinese char
    public static String text_compose(String path, String src_img_name, String text)
    {
    	//get the file's width and length
    	int old_width = 0;
    	int old_height = 0;
    	
        String fullpath = null;
        
    	try {

			if(path == null)
				path="";
			
			File img = new File(path+src_img_name);
			BufferedImage org_img = ImageIO.read(img);

			old_width = org_img.getWidth();
			old_height = org_img.getHeight();

			BufferedImage new_img = new BufferedImage(bufferWidth, bufferHeight,
					BufferedImage.TYPE_INT_BGR);

			Graphics2D g2d = new_img.createGraphics();
			
			g2d.setBackground(Color.white);
			g2d.clearRect(0, 0, bufferWidth, bufferHeight);
			
			int gap_x = picOffsetX;
			int gap_y = picOffsetY;
			
			g2d.drawImage(org_img, 
						  gap_x, gap_y,
						  bufferWidth - gap_x, 
						  (bufferWidth - 2 * gap_x) + gap_y,
						  0, 0,
						  old_width, old_height, null);
			
			// draw text
			if (text != null) {
				//g2d.setColor(Color.magenta);
				g2d.setColor(Color.BLACK);
				//g2d.setFont(new Font("黑体", Font.PLAIN, 100));
				//g2d.setFont(new Font("仿宋体", Font.PLAIN, 100));
				g2d.setFont(new Font("微软雅黑", Font.PLAIN, 100));
				
				g2d.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_ATOP, 0.5f));
				
				//(width - (getLength(text) * 110))
				g2d.drawString(text, 
						textoffsetX,
						textoffsetY);
			}

			//finish drawing.
			g2d.dispose();
			
			//write file
			FileOutputStream out = null;
			
			fullpath = "compose"+src_img_name;
			
			out = new FileOutputStream(path + fullpath);
			
			ImageIO.write(new_img, get_file_type(src_img_name), out);
			
			out.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return fullpath;
    }
    
    public static String crop_center(String path, String src_img_name)
    {
		try {
			BufferedImage bi = null;

			if (path != null) {
				bi = ImageIO.read(new File(path + src_img_name));
			} else {
				bi = ImageIO.read(new File(src_img_name));

			}

			int w = bi.getWidth();
			int h = bi.getHeight();

			//System.out.println("crop_center w: " + w + " h: " + h);
			BufferedImage newimage = null;
			
			if (w >= h) {
				newimage = bi.getSubimage(((w / 2 - h / 2)), 0, h, h);
			} else {
				//width < h
				//newimage = bi.getSubimage(0, 0, w, w)
				newimage = bi.getSubimage(0, (h-w)/2, w, w);
			}
			
			if (path != null) {

				ImageIO.write(newimage, get_file_type(src_img_name), 
						new File(path + "crop"+ src_img_name));
			} else {
				ImageIO.write(newimage, get_file_type(src_img_name), new File("crop" + src_img_name));
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "crop"+src_img_name;
    }
    
    /*
    *		rotate the picture is needed 
    * 		path: the image's folder path. may be null
    * 		src_img_name: picture 's file name
    * 	    des_img_name: rotated picture file name under the same path. if no rotaion done, it's the same as src_img_name
    * 
    *  		return value: the rotated image picture
    */
    public static String rotate(String path, String src_img_name)
    {
    	String filepath = null;	
    	//System.out.println("path : " + path + "src_img_name : " + src_img_name );
    	
    	if (path == null) 
    	{
    		path = "";
    	}

		filepath = path+src_img_name;

    	String cmd = "java -jar /usr/lib/jar/metadata-extractor-2.6.4/metadata-extractor-2.6.4.jar  " + filepath;
    	
 		String matchLine = null;

 		try {
 			Process p = Runtime.getRuntime().exec(cmd);

 			if (debug) {
 				//System.out.println("process exits normally");
 			}
 			
 			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));

 			BufferedReader stdError = new BufferedReader(new InputStreamReader(
 					p.getErrorStream()));

 	 		String s = null;
 	 		Pattern pattern = Pattern.compile("IFD0\\] Orientation");

 			// read the output from the command
 			// System.out.println("Here is the standard output of the command");
 			while ((s = stdInput.readLine()) != null) {
 				if (debug) {
 					// System.out.println(s);
 				}
 				Matcher matcher = pattern.matcher(s);
 				if (matcher.find()) {
 					// System.out.println(s);
 					matchLine = s;
 					break;
 				}

 			}

 			// read any errors from the attempted command
			if (debug) {
				//System.out
					//	.println("Here is the standard error of the command (if any):\n");

				while ((s = stdError.readLine()) != null) {
					System.out.println(s);
				}

			}
		}
 		catch (Exception e) {
 			System.out.println("meta read exception : ");
 			e.printStackTrace();
 		}

 		//no  exif meta header read. don't do rotation.
 		if(matchLine == null)
 		{
 			return src_img_name;
 		}
 		
 		// Now, get sentence like [Exif IFD0] Orientation = Right side, top
 		// (Rotate 90 CW)
 		/*
 		 * case 1: return "Top, left side (Horizontal / normal)"; case 2: return
 		 * "Top, right side (Mirror horizontal)"; case 3: return
 		 * "Bottom, right side (Rotate 180)"; case 4: return
 		 * "Bottom, left side (Mirror vertical)"; case 5: return
 		 * "Left side, top (Mirror horizontal and rotate 270 CW)"; case 6:
 		 * return "Right side, top (Rotate 90 CW)"; case 7: return
 		 * "Right side, bottom (Mirror horizontal and rotate 90 CW)"; case 8:
 		 * return "Left side, bottom (Rotate 270 CW)";
 		 */

 		// currently, support no mirror.... this should be seldom case
 		// get the digits pattern here

 	
		String degree = null;
 		int degree_int = 0;	


		Pattern p = Pattern.compile("\\d\\d+");
		Matcher m = p.matcher(matchLine);

		while (m.find()) {

			degree = m.group();

			if (debug) {
				System.out.println(degree);
			}
		}

		if (degree != null) 
		{
			degree_int = Integer.parseInt(degree);
		} 
		else 
		{
			/*no need to do rotation*/
			degree_int = 0;
			if(debug) {
				//System.out.println("rotation degree is zero. return ");
			}
			
			return src_img_name;

		}

		
		File img = new File(filepath);

		BufferedImage old_img = null;
		
		try {
			old_img = (BufferedImage) ImageIO.read(img);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int w = 0;
		int h = 0;
		
		w = old_img.getWidth();
		h = old_img.getHeight();

		BufferedImage new_img = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_BGR);

		Graphics2D g2d = new_img.createGraphics();

		AffineTransform origXform = g2d.getTransform();
		AffineTransform newXform = (AffineTransform) (origXform.clone());

		newXform.rotate(Math.toRadians(degree_int), w / 2, h / 2); 

		w = new_img.getWidth();
		h = new_img.getHeight();

		if (debug) {
			System.out.println("new image: w : " + w + "h: " + h + "rotation degree: " + degree_int);
		}

		g2d.setTransform(newXform);
		g2d.drawImage(old_img, 0, 0, null);
		g2d.setTransform(origXform);

		FileOutputStream out = null;
		try {
			if(path != null)
			{
				out = new FileOutputStream(path+"rotate"+src_img_name);
			}
			else
			{
				out = new FileOutputStream("rotate"+src_img_name);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ImageIO.write(new_img, get_file_type(src_img_name), out);
			System.out.println("rotation done");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return "rotate"+src_img_name;

	}
    
    
   
    public final static int getLength(String text) {
        int length = 0;
    
        for (int i = 0; i < text.length(); i++) {
        	
            if (new String(text.charAt(i) + "").getBytes().length > 1) {
            
            	length += 2;
            
            } else {
               
            	length += 1;
            }
        }
        
        return length > 15 ? 15 : length;
    }
}

