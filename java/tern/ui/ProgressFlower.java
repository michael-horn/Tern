/*
 * @(#) ProgressFlower.java
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
package tern.ui;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import tern.Palette;


/**
 * Displays a compile progress indicator that looks like a spinning
 * flower.
 *
 * @author Michael Horn
 * @version $Revision: 1.2 $, $Date: 2007/11/14 23:46:01 $
 */
public class ProgressFlower {

	protected static final int PETAL_COUNT = 12;

	protected static final int BORDER = 25;

   /** Message to display below the flower */
   protected String message;

   /** Petal count */
   protected int count;

   /** Is the flower visible or not? */
   protected boolean visible;

	protected BufferedImage[] frames;

   
/**
 * Default constructor
 */
	public ProgressFlower() {
		this.message = "Compiling...";
		this.count   = 0;
		this.visible = false;
		this.frames  = new BufferedImage[PETAL_COUNT];
		for (int i=0; i<frames.length; i++) {
			frames[i] = Palette.createImage("/images/progress" + (i+1) + ".png");
		}
   }


	public int getWidth() {
		return BORDER * 2 + frames[0].getWidth();
	}
	
	public int getHeight() {
		return BORDER * 3 + frames[0].getHeight();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if (visible) count = 0;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void animate() {
		this.count++;
		if (count >= frames.length) count = 0;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void draw(Graphics2D g, int x, int y) {
		if (!isVisible()) return;
		int w = getWidth();
		int h = getHeight();

		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, w, h, 25, 25);
		g.setColor(Color.WHITE);
		g.fill(rect);
		g.setStroke(Palette.STROKE2);
		g.setColor(Color.GRAY);
		g.draw(rect);
		g.setStroke(Palette.STROKE1);

		g.drawImage(frames[count], x + BORDER, y + BORDER, null);
		
		g.setFont(new Font(null, Font.BOLD, 16));
		int fw = g.getFontMetrics().stringWidth(message);
		g.setColor(new Color(0x657794));
		g.drawString(message, x + w/2 - fw/2, y + h - 8);
	}
	
}
