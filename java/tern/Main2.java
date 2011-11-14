/*
 * @(#) Main.java
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
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.*;
import java.applet.AudioClip;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import tern.ui.*;
import tern.nqc.*;
import tern.compiler.*;
import tern.language.*;
import webcam.*;
import topcodes.*;


/**
 * Main application frame for Tern
 *
 * @author Michael Horn
 * @version $Revision: 1.2 $, $Date: 2007/03/07 17:43:07 $
 */
public class Main2 extends JPanel implements
Runnable,
KeyListener,
ActionListener,
MouseListener,
MouseMotionListener,
WindowListener
{

	protected static final int CMD_COMPILE = 0;
	protected static final int CMD_FIRMWARE = 1;

	private static final int BORDER = 30;

	/** Single instance of the application */
	protected static Main2 instance;
	
	/** Main app window */
	protected JFrame frame;

	/** Controls current view of the program (zoom / pan) */
	protected AffineTransform tform;
	
	/** Used to compile JPEG images into robot programs */
	protected TangibleCompiler compiler;
	
	/** Sends compiled programs to the RCX */
//	protected Transmitter nqc;
        
        /**Send compiled programms to the NXT - Mariam */
        
        protected NXCTransmitter nxc;
	
	/** Configuration settings */
	protected Properties props;
	
	/** Most recently compiled program */
	protected Program program;
	
	/** Whether or not we're in the middle of a compile */
	protected boolean compiling;
	
	/** Shows a progress dialog */
	protected ProgressFlower progress;
	
	/** Webcam interface */
	protected WebCam webcam;
	
	/** Animates display */
	protected Timer animator;

	/** Maintains the system log */
	protected Logger logger;
	
	/** Compile error message */
	protected int error;

	/** Action command (firmware or compile) */
	protected int command;

	public Main2() {
		super(true);
		Main2.instance=this;

		//--------------------------------------------------
		// Load the configuration properties
		//--------------------------------------------------
		try {
			this.props = new Properties();
			this.props.load(new java.io.FileInputStream("config.properties"));
		} catch (IOException iox) {
			iox.printStackTrace();
			System.exit(1);
		}
		
		this.frame     = new JFrame("Tern Tangible Programming");
		this.tform     = new AffineTransform();
		this.compiler  = new TangibleCompiler();
		//this.nqc       = new Transmitter(props);
                this.nxc= new NXCTransmitter(props);
		this.program   = null;
		this.compiling = false;
		this.progress  = new ProgressFlower();
		this.webcam    = new WebCam();
		this.animator  = new Timer(300, this);
		this.command   = CMD_COMPILE;
		this.error     = CompileException.ERR_NONE;

		
		//--------------------------------------------------
		// Add event listeners
		//--------------------------------------------------
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		frame.addWindowListener(this);

		
		//--------------------------------------------------
		// Set up the frame.
		//--------------------------------------------------
		setOpaque(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setContentPane(this);
		frame.setUndecorated(getBooleanProperty("app.fullscreen"));
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setIconImage(Palette.ICON_LG);

		
		//--------------------------------------------------
		// Register statements for the compiler
		//--------------------------------------------------
		{
			Backward.register();
			Beep.register();
			End.register();
			Forward.register();
			Go.register();
			Growl.register();
			Left.register();
			Num.register();
			Repeat.register();
			Right.register();
			Sensor.register();
			Shake.register();
			Shuffle.register();
			Song.register();
			Spin.register();
			Start.register();
			Stop.register();
			Wait.register();
			Whistle.register();
			Wiggle.register();
		}
		
		
		//--------------------------------------------------
		// Create the log directory and start logging
		//--------------------------------------------------
		this.logger = new Logger(getProperty("log.dir"));
		if (getBooleanProperty("log.enabled")) {
			this.logger.start();
		}
		
		
		//--------------------------------------------------
		// Start the camera and connect to the camera (this
		// might fail if the camera isn't connected yet).
		//--------------------------------------------------
		try {
			this.webcam.initialize();
			int w = getIntProperty("webcam.width");
			int h = getIntProperty("webcam.height");
                        //modified by mariam
			this.webcam.openCamera(640, 480);
		} catch (Exception x) {
			log(x);
		}
		
		requestFocusInWindow();
	}
		

	protected void paintComponent(java.awt.Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						   RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		
		//--------------------------------------------------
		// Background texture
		//--------------------------------------------------
		GradientPaint gp = new GradientPaint(
			0, h + 100, Color.BLACK,
			0, h/2, new Color(0x657794), 
			true);
		g.setPaint(gp);
		g.fillRect(0, 0, w, h);
		
		
		//----------------------------------------------------
		// Main screen with logo in background
		//----------------------------------------------------
		gp = new GradientPaint(
			0, BORDER, Color.WHITE,
			0, h + h/2, Color.GRAY);

		RoundRectangle2D rect = new RoundRectangle2D.Double(
			BORDER, BORDER, w - BORDER * 2, h - BORDER * 2, BORDER, BORDER);
		g.setPaint(gp);
		g.fill(rect);

		int iw = Palette.LOGO.getWidth();
		int ih = Palette.LOGO.getHeight();
		g.drawImage(Palette.LOGO, w/2 - iw/2, h/2 - ih/2, null);

		
		//----------------------------------------------------
		// Copyright
		//----------------------------------------------------
		g.setFont(Palette.FONT12);
		String copy = "Copyright (c) 2009 Michael S. Horn";
		int fw = g.getFontMetrics().stringWidth(copy);
		g.setColor(Color.GRAY);
		g.drawString(copy, w - fw - BORDER, h - 8);

		
		//----------------------------------------------------
		// Captured image
		//----------------------------------------------------
		java.awt.Shape oldc = g.getClip();
		g.setClip(rect);
		if (program != null) {
			BufferedImage image = program.getImage();
			if (image != null) {
				g.drawRenderedImage(image, this.tform);
			}
		}
		

		//----------------------------------------------------
		// TopCodes & statement names
		//----------------------------------------------------
		if (program != null) {
			AffineTransform oldt = g.getTransform();
			g.transform(tform);
			List<Statement> statements = program.getStatements();
			for (Statement s : statements) 
                        {
				s.getTopCode().draw(g);
                                
			}
			g.setTransform(oldt);
		}
		g.setClip(oldc);
		g.setColor(Color.DARK_GRAY);
		g.setStroke(Palette.STROKE3);
		g.draw(rect);
		g.setStroke(Palette.STROKE1);

		
		//----------------------------------------------------
      // Status buttons
      //----------------------------------------------------
		int ix = w - BORDER - 20;
		int iy = h - BORDER - 10;
		BufferedImage icon;

		icon = webcam.isCameraOpen() ? Palette.CAMERA_ON : Palette.CAMERA_OFF;
		g.drawImage(icon, ix - icon.getWidth(), iy - icon.getHeight(), null);
		ix -= icon.getWidth() + 20;

		icon = Palette.NXT_SMALL;
		g.drawImage(icon, ix - icon.getWidth(), iy - icon.getHeight() + 5, null);

		icon = onPlayButton() ? Palette.PLAY_DN : Palette.PLAY_UP;
		ix = BORDER + 20;
		g.drawImage(icon, ix, iy - icon.getHeight(), null);
		
	  
		//----------------------------------------------------
		// Progress indicator
		//----------------------------------------------------
		if (progress.isVisible()) {
			progress.draw(g, w - BORDER - progress.getWidth() - 20, BORDER + 20);
		}
		
		
		//----------------------------------------------------
		// Generate user error message
		//----------------------------------------------------
		if (this.error != CompileException.ERR_NONE) {
			g.setFont(Palette.FONT20B);
			
			String message = "";
			icon = null;
			switch (error) {
			case CompileException.ERR_NO_BLOCKS:
				break;
			case CompileException.ERR_NO_BEGIN:
				message = "Every program needs a Begin block.";
				icon = Palette.ERR_NO_BEGIN;
				break;
			case CompileException.ERR_CAMERA:
				message = "Uh oh! Make sure the camera is plugged in.";
				break;
			case CompileException.ERR_NO_RCX:
				message = "Uh oh! Make sure the RCX is turned on.";
				icon = Palette.ERR_NO_RCX;
				break;
                        case CompileException.ERR_NO_NXT:
                                message ="Uh oh! Make sure the NXT is connected and turned on.";
			case CompileException.ERR_NO_TOWER:
				message = "Uh oh! Make sure the Tower is plugged in.";
				icon = Palette.ERR_NO_TOWER;
				break;
			case CompileException.ERR_NO_NQC:
				message = "No NQC compiler found.";
				break;
			case CompileException.ERR_SAVE_FILE:
				message = "Error saving program file.";
				break;
			case CompileException.ERR_LOAD_FILE:
				message = "Error loading JPG image.";
				break;
			case CompileException.ERR_FIRMWARE:
				message = "Uh oh! RCX Firmware needs to be installed.";
				break;
			case CompileException.ERR_UNKNOWN:
				message = "Unknown compile error.";
				break;
			}
			
			// Draw error box
			int ew = 500;
			int eh = icon == null ? 65 : 300;
			int ex = w - BORDER - ew - 20;
			int ey = BORDER + 20;
			
			RoundRectangle2D box = new RoundRectangle2D.Double(ex, ey, ew, eh, 25, 25);
			g.setPaint(Color.WHITE);
			g.fill(box);
			g.setStroke(Palette.STROKE3);
			g.setPaint(Color.DARK_GRAY);
			g.draw(box);

			if (icon != null) {
				g.drawImage(icon, ex + ew/2 - icon.getWidth()/2, ey + 10, null);
			}
			
			fw = g.getFontMetrics().stringWidth(message);
			g.setColor(Color.BLACK);
			g.drawString(message, ex + ew/2 - fw/2, ey + eh - 15);
		}
	}

	
	/**
	 * Perform tangible compiles in a separate thread...
	 */
	public void run() {
		if (command == CMD_COMPILE) {
			compile();
		} else {
			loadFirmware();
		}
	}


	protected void startFirmware() {
		if (!compiling) {
			this.compiling = true;
			this.command = CMD_FIRMWARE;
			this.error = CompileException.ERR_NONE;
			repaint();
			(new Thread(this)).start();
		}
	}


	protected void startCompile() {
		if (!compiling) {
			this.compiling = true;
			this.command = CMD_COMPILE;
			this.error = CompileException.ERR_NONE;
			Palette.SOUND_SHUTTER.play();
			repaint();
			(new Thread(this)).start();
		}
	}

	
	/**
	 * Begin a compile process in a separate thread.
	 */
	protected void compile() {
		try {
			this.program = null;
			
			//-------------------------------------------------
			// Start the progress indicator
			//-------------------------------------------------
			log ("Starting compile");
			progress.setMessage("Compiling...");
			progress.setVisible(true);
			animator.start();
			
			Repeat.NEST = 0;
			Repeat.VAR = 0;
		 
			//-------------------------------------------------
			// If the camera is connected, compile a picture
			// from the webcam
			//-------------------------------------------------
         /*temp comments for */               
			if (webcam.isCameraOpen()) {
				webcam.captureFrame();
				log("Captured image from webcam");
                               
                               webcam.saveFrameImage("cam2.png");
                                this.program= compiler.compile("cam2.png") ;
                                //commented by Mariam
                              
				/*this.program = compiler.compile(
					webcam.getFrameWidth(),
					webcam.getFrameHeight(),
					webcam.getFrameData());*/
			}
			
			//-------------------------------------------------
			// Otherwise, let the user select a JPG file to compile
			//-------------------------------------------------
			else {
				String filename = FileChooser.openFile("jpg");
				if (filename == null) {
					return;
				} else {
					log("Captured image from file system");
					this.program = compiler.compile(filename);
				}
			}
			
			repaint();
			
			//-------------------------------------------------
			// Zoom in on the program
			//-------------------------------------------------
			focus(program.getBounds());
			
			
			//-------------------------------------------------
			// Save and log the program
			//-------------------------------------------------
			System.out.println(program);
			//program.save("program.nqc");
                        program.save("program.nxc");
			
			
			//-------------------------------------------------
			// Send the program to the RCX
			//-------------------------------------------------
			if (program.hasStartStatement()) {
				progress.setMessage("Downloading program...");
				nxc.Run("program.nxc");
                                //nqc.send("program.nqc");
			} else if (program.isEmpty()) {
				setErrorCode(CompileException.ERR_NONE);
			} else {
				setErrorCode(CompileException.ERR_NO_BEGIN);
			}
			
			log("Running program");
			this.logger.log(program);
		}
		catch (IOException iox) {
			setErrorCode(CompileException.ERR_SAVE_FILE);
		}
		catch (CompileException cx) {
			setErrorCode(cx.getErrorCode());
		}
		catch (WebCamException wcx) {
			setErrorCode(CompileException.ERR_CAMERA);
		}
		finally {
			animator.stop();
			progress.setVisible(false);
			compiling = false;
			repaint();
			log("Compile complete");
		}
	}


	protected void loadFirmware() {
		log ("Loading firmware");
		progress.setMessage("Loading firmware...");
		progress.setVisible(true);
		animator.start();
		/*try {
			nqc.loadFirmware();
		} catch (CompileException cx) {
			setErrorCode(cx.getErrorCode());
		}*/
		
		animator.stop();
		progress.setVisible(false);
		compiling = false;
		repaint();
	}


	protected boolean onPlayButton() {
		int bx = BORDER + 20;
		int bw = 50;
		int by = getHeight() - BORDER - 10 - bw;
		return (
			mouseX >= bx &&
			mouseX <= bx + bw &&
			mouseY >= by &&
			mouseY <= by + bw);
	}
	
	
	public void zoom(double factor) {
		double dx = getWidth()/2.0;
		double dy = getHeight()/2.0;
		tform.preConcatenate(AffineTransform.getTranslateInstance(-dx, -dy));
		tform.preConcatenate(AffineTransform.getScaleInstance(factor, factor));
		tform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		repaint();
	}


	public void pan(int dx, int dy) {
		tform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		repaint();
	}

	
	public void rotate(double angle) {
		double dx = getWidth()/2.0;
		double dy = getHeight()/2.0;
		tform.preConcatenate(AffineTransform.getTranslateInstance(-dx, -dy));
		tform.preConcatenate(AffineTransform.getRotateInstance(angle));
		tform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		repaint();
	}
   

	public void focus(Rectangle2D bounds) {
		double pw = bounds.getWidth() + 40;
		double ph = bounds.getHeight() + 40;
		double px = bounds.getX() - 20;
		double py = bounds.getY() - 20;
		
		double sw = getWidth() - BORDER * 2.0;
		double sh = getHeight() - BORDER * 2.0;
		double sar = (sw / sh);
		double par = (pw / ph);
		
		double cx = sw / 2.0 + BORDER;
		double cy = sh / 2.0 + BORDER;
		double pcx = pw / 2.0 + px;
		double pcy = ph / 2.0 + py;
		
		this.tform.setToTranslation(cx - pcx, cy - pcy);
		double z = 1;
		
		if (par > sar) {
			z = sw / pw;
		} else {
			z = sh / ph;
		}
		
		// Limit zoom in factor so we don't get too close
		if (z > 1.5) z = 1.5;
		zoom(z * 0.95);
	}
   

	public void setErrorCode(int code) {
		this.error = code;
		repaint();
	}
	
	public String getProperty(String key) {
		return this.props.getProperty(key);
	}
	
	public boolean getBooleanProperty(String key) {
		String p = props.getProperty(key);
		return ("true".equalsIgnoreCase(p));
	}

	public int getIntProperty(String key) {
		String p = props.getProperty(key);
		return Integer.parseInt(p);
	}
	
	public void log(String line) {
		this.logger.log(line);
	}
	
	public void log(Exception x) {
		this.logger.log(x);
	}

   
/******************************************************************/
/*                        ACTION EVENTS                           */
/******************************************************************/
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.animator) {
			this.progress.animate();
			if (program != null) {
				List<Statement> pops = program.getCompiledStatements();
				if (pops.size() > 0) {
					Palette.SOUND_POP.play();
					Statement s = pops.remove(0);
					TopCode top = s.getTopCode();
					top.setDiameter( top.getDiameter() * 1.65f );
				}
			}
			repaint();
		}
	}

   
/******************************************************************/
/*                       MOUSE EVENTS                             */
/******************************************************************/
	protected int mouseX;
	protected int mouseY;
   public void mouseClicked(MouseEvent e) { }
   public void mouseMoved(MouseEvent e) { }
   public void mouseEntered(MouseEvent e) { }
   public void mouseExited(MouseEvent e) { }
   public void mousePressed(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();
      repaint();
   }
   public void mouseReleased(MouseEvent e) {
	   if (onPlayButton()) {
		   startCompile();
	   }
	   mouseX = -1;
	   mouseY = -1;
	   repaint();
   }
   public void mouseDragged(MouseEvent e) {
	   int dx = e.getX() - mouseX;
	   int dy = e.getY() - mouseY;
	   pan(dx, dy);
      mouseX = e.getX();
      mouseY = e.getY();
	   repaint();
   }
	

/******************************************************************/
/*	                      KEYBOARD EVENTS                          */
/******************************************************************/
	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		boolean ctrl = e.isControlDown();
		
		switch (e.getKeyCode()) {
			
		case KeyEvent.VK_SPACE:
		case KeyEvent.VK_F5:
			startCompile();
			break;

		case KeyEvent.VK_F6:
			startFirmware();
			break;
			
		case KeyEvent.VK_A:
			if (ctrl) {
				compiler.setAnnotate(!compiler.getAnnotate());
			}
			break;
			
		case KeyEvent.VK_R:
			rotate(Math.PI / 2);
			break;
			
		case KeyEvent.VK_MINUS:
			zoom(0.95);
			break;
			
		case KeyEvent.VK_EQUALS:
			zoom(1/0.95);
			break;
			
		default:
			this.error = CompileException.ERR_NONE;
			repaint();
			break;
		}
	}
	
	public void keyReleased(KeyEvent e) { }
	public void keyTyped(KeyEvent e) { }
	

/******************************************************************/
/*                          WINDOW EVENTS                         */
/******************************************************************/
	public void windowClosing(WindowEvent e) {
		this.webcam.closeCamera();
		this.webcam.uninitialize();
		this.logger.stop();
		frame.setVisible(false);
		frame.dispose();
		System.exit(0);
	}
	public void windowActivated(WindowEvent e) { } 
	public void windowClosed(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { } 
	public void windowIconified(WindowEvent e) { } 
	public void windowOpened(WindowEvent e) { }


/**
 * main entry point
 */
	public static void main(String[] args) {
		
		//--------------------------------------------------
		// Fix cursor flicker problem (sort of :( )
		//--------------------------------------------------
		System.setProperty("sun.java2d.noddraw", "");
		
		//--------------------------------------------------
		// Use standard Windows look and feel
		//--------------------------------------------------
		try { 
			javax.swing.UIManager.setLookAndFeel(
				javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception x) { ; }

		//--------------------------------------------------
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		//--------------------------------------------------
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new Main2();
				}
			});
	}
}


	
