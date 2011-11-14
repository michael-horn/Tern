/*
 * @(#) WebCam.java
 * 
 * Tangible Object Placement Codes (TopCodes)
 * Copyright (c) 2007 Michael S. Horn
 * 
 *           Michael S. Horn (michael.horn@tufts.edu)
 *           Tufts University Computer Science
 *           161 College Ave.
 *           Medford, MA 02155
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2) as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package webcam;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;


public class WebCam {

   protected int width;
   protected int height;
   protected int [] rgba;

   static {
       String libPath= System.getProperty("user.dir")+"\\lib\\JavaWebCam.dll";
   //  System.loadLibrary("JavaWebCam");
       System.load(libPath);
   }      

   public WebCam() { }

   public native void initialize() throws WebCamException;

   public native void uninitialize();

   public void openCamera(int width, int height) throws WebCamException {
      this.width  = width;
      this.height = height;
      this.rgba   = new int [width * height];
      NopenCamera(width, height);
   }

   private native void NopenCamera(int w, int h) throws WebCamException;

   public native void closeCamera();

   public native void capture(String filename) throws WebCamException;

   public native void captureFrame() throws WebCamException;

   public native boolean isCameraOpen();

   public int getFrameWidth() { return this.width; }

   public int getFrameHeight() { return this.height; }

   public BufferedImage getFrameImage() {
      BufferedImage image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      image.setRGB(0, 0, width, height, rgba, 0, width);
      return image;
   }
   
   public void saveFrameImage(String FileName)
   {
         BufferedImage image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      image.setRGB(0, 0, width, height, rgba, 0, width);
      File f =new File(FileName) ;
      try{
    ImageIO.write(image, "png",f);
      }
      catch (Exception ex){System.out.print(ex);
      }
   }

   public int [] getFrameData() {
      return this.rgba;
   }

   
   private void callback(byte [] data) {
      int rgb, sindex = 0, dindex;
      if (data.length != rgba.length * 3) return;
      for (int j=0; j<height; j++) {
         dindex = (height - j - 1) * width;
         for (int i=0; i<width; i++) {
            rgb = (((255 & 0xff) << 24) |
                   (((int)data[sindex+2] & 0xff) << 16) |
                   (((int)data[sindex+1] & 0xff) << 8) |
                   ((int)data[sindex] & 0xff ));
            rgba[dindex] = rgb;
            dindex += 1;
            sindex += 3;
         }
      }
   }
}
