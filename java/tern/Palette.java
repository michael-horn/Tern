/*
 * @(#) Palette.java
 * 
 * Tern Tangible Programming System
 * Copyright (C) 2009 Michael S. Horn 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tern;

import java.awt.Font;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.applet.AudioClip;
import javax.imageio.ImageIO;


public class Palette {


	public static Stroke STROKE1 = new BasicStroke(1);
	public static Stroke STROKE2 = new BasicStroke(2);
	public static Stroke STROKE3 = new BasicStroke(3);

	public static Font FONT12 = new Font(null, 0, 12);
	public static Font FONT20B = new Font(null, Font.BOLD, 20);


	public static AudioClip SOUND_POP = createAudio("/sounds/pop.wav");
	public static AudioClip SOUND_SHUTTER = createAudio("/sounds/camera.wav");

	public static BufferedImage LOGO = createImage("/images/logo.png");
	public static BufferedImage CAMERA_ON = createImage("/images/camera_on.png");
	public static BufferedImage CAMERA_OFF = createImage("/images/camera_off.png");
	//public static BufferedImage RCX_SMALL = createImage("/images/nxtBrick_small.png");
        public static BufferedImage NXT_SMALL=createImage("/images/nxtBrick_small.png");
	public static BufferedImage PLAY_UP = createImage("/images/play_button_up.png");
	public static BufferedImage PLAY_DN = createImage("/images/play_button_dn.png");
	public static BufferedImage ICON_LG = createImage("/images/icon.png");

	public static BufferedImage ERR_NO_RCX =
		createImage("/images/error_rcx.png");
	public static BufferedImage ERR_NO_TOWER =
		createImage("/images/error_tower.png");
	public static BufferedImage ERR_NO_BEGIN =
		createImage("/images/error_begin.png");

	/**
	 * Returns an audio clip, or null if the path was invalid.
	 */
	public static AudioClip createAudio(String path) {
		java.net.URL audioURL = Main2.class.getResource(path);
		if (audioURL != null) {
			return java.applet.Applet.newAudioClip(audioURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

   
	/**
	 * Returns a buffered image from the given path.
	 */
	public static BufferedImage createImage(String path) {
		try {
			java.net.URL url = Main2.class.getResource(path);
			if (url != null) {
				return ImageIO.read(url);
			} else {
				System.err.println("Couldn't find file: " + path);
				return null;
			}
		} catch (java.io.IOException iox) {
			System.err.println("Unable to read image file: " + path);
			return null;
		}
	}
}
	
	
