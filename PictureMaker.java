import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import static java.nio.file.StandardCopyOption.*;


public class PictureMaker {

	boolean debug = true;

	private static boolean file_have_exif(String filename)
	{
		int dot = filename.lastIndexOf(".");
		String ext = filename.substring(dot + 1);
		
		if (ext.equals(ImageUtils.IMAGE_TYPE_JPEG) ||ext.equals(ImageUtils.IMAGE_TYPE_JPG) 
				|| ext.equals(ImageUtils.IMAGE_TYPE_JPG1)
				 || ext.equals(ImageUtils.IMAGE_TYPE_JPG2) )
			
		{
			return true;
		}
		
		return false;
	}
	
	private static boolean checkfile(String dirname , String filename)
	{
		
		int dot = filename.lastIndexOf(".");
		String ext = filename.substring(dot + 1);
		boolean typefit = false;
		
		for(int i = 0; i < ImageUtils.IMAGE_TYPE.length; i++)
		{
			if ( ext.equals(ImageUtils.IMAGE_TYPE[i]) )
			{	
				typefit = true;
				break;
			}
		}
		
		if(!typefit) {
			System.out.println("PictureMake checkfile not support this file format: " + filename);
			return false;
		}
		
		

		File img = new File(dirname+filename);
		
		
		if (img.length() >= 4 * 1024 * 1024) {
			System.out.println("PictureMake checkfile file size should less than 4MB ");
			return false;
		}

		return true;
	}

   
	// command line : C:\Users\jianming\workspace1\clipPicture\bin>java
	// PictureMaker 20131002185329.jpg
	
	//the input args[0] should be path like  ../a.jpg  or  /home/tmp/a.jpg or a.jpg
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Error: pls input mode imagepath [text] [imagepath]");
			return;
		}
		
		String Optiontext = null;
		int mode = Integer.parseInt(args[0]);
		String imgpath = null;
		String twoDcodeImage = null;
		String advertiseImage = null;
		
		switch (mode) 
		{
		case 1:
			
			imgpath = args[1];
			
			if(args.length >= 3) {
				Optiontext = ImageUtils.getsubstring(args[2], 36);							
			}
			
			
			break;
		case 2:
			
			if(args.length != 3) {
				System.out.println("Error, should input two image path");
				return;
			}
			
			imgpath = args[1];
			twoDcodeImage = args[2];	

			break;
		
		case 3:
			if(args.length != 3) {
				System.out.println("Error, should input two image path");
				return;
			}
			
			imgpath = args[1];
			advertiseImage = args[2];
			break;
			
		default:
			System.out.println("Error: currently only support mode 1 2");
			return;	
		}
		
		
		String picname = null;
		String dirname = null;
		
		int sep = imgpath.lastIndexOf(File.separator);
		
		if (sep == -1)
		{
				picname = imgpath;
				dirname = null;
				
		}
		else
		{
			 picname = imgpath.substring(sep+1);	
			 dirname = imgpath.substring(0, sep+1);
		}
		
		
		if (checkfile(dirname, picname) == false) {
			System.out.println("Error: input image file should less than 4MB, end with jpg or jpeg");
			return;
		}
		
		
		/*Step 1, do rotation is needed. */ 
		String resultimg = picname;
		
		if(file_have_exif(picname)) {
			resultimg = ImageUtils.rotate(dirname, picname);
		}
		
		/*step 2, do cut. return the new file */
		resultimg = ImageUtils.crop_center(dirname, resultimg);
		
		/*step3, do text composition. if the text is null, no text compose*/
		//text_compose will generate the whole buffer... do it any way...
		 resultimg = ImageUtils.text_compose(dirname, resultimg, Optiontext);
		
		 if(twoDcodeImage != null) {
			 resultimg = ImageUtils.voicecardCompose(dirname, resultimg, twoDcodeImage);
		 }
		 
		 if(advertiseImage != null)
			 resultimg = ImageUtils.advertiseCompose(dirname, resultimg, advertiseImage);

		 if( advertiseImage == null)
			 resultimg = ImageUtils.logoCompose(dirname, resultimg, "/usr/lib/jar/logo3.jpg");

		 resultimg = ImageUtils.copy_image(dirname, resultimg, picname);
		 
		 System.out.println(resultimg);
		
	}// main
}
