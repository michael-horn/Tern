/*
 * @(#) TangibleCompiler.java
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
package tern.compiler;


import java.util.List;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import topcodes.*;


/**
 * Compiles a tangible program.
 *
 * @author Michael Horn
 * @version $Revision: 1.8 $, $Date: 2008/11/08 16:56:57 $
 */
public class TangibleCompiler {

	/** Connection map for building control chains from JPEG images */
	protected ConnectionMap map;
	
	/** Scans JPEG image files for topcodes */
	protected Scanner scanner;
	
	/** Is a compile in progress? */
	protected boolean compiling;
	
	/** Whether or not to annotate the program with connection map info */
	protected boolean annotate;
	
	
	public TangibleCompiler() {
		this.map        = new ConnectionMap();
		this.scanner    = new Scanner();
		this.compiling  = false;
		this.annotate   = false;
		
		// need to do this in a config file somehow...
//////////////	this.scanner.setMaxCodeDiameter(56);
	}

   
	public boolean isCompiling() {
		return this.compiling;
	}


	public BufferedImage getImage() {
		return this.scanner.getImage();
	}


/**
 * Returns an image that shows the result of the binary threshold
 * filter.
 */
   public BufferedImage getBWImage() {
      return this.scanner.getPreview();
   }

   
//it was private - by Mariam	
   public Program doCompile(List<TopCode> spots)
      throws CompileException {

      try {

         //-----------------------------------------
         // 1. Get image from scanner 
         //-----------------------------------------
         BufferedImage image=  scanner.getImage();
         Graphics2D g = (Graphics2D)image.getGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        
         //-----------------------------------------
         // 2. Convert topcodes to statements
         //-----------------------------------------
         Statement s;
         Program program = new Program();
         List statements = new java.util.ArrayList();
         this.map.clear();
         
         notifyProgress("Loading statements...", 0.6);
         for (TopCode top : spots) {
            s = StatementFactory.createStatement(top);
            if (s != null) 
            {
               statements.add(s);
               System.out.println(s);
               s.registerConnections(map);
               //commented by mariam
              top.draw(g);
            }
         }
         //commented by mariam
         if (annotate) map.draw(g);

         
         //-----------------------------------------
         // 3. Figure out what each statement is connected to...
         //-----------------------------------------
         notifyProgress("Connecting statements...", 0.7);
         map.formConnections();

         
         //-----------------------------------------
         // 6. Generate program
         //-----------------------------------------
         boolean unique = false;
         notifyProgress("Generating code...", 0.9);
         for (int i=0; i<statements.size(); i++) {
            s = (Statement)statements.get(i);
            if (s instanceof StartStatement) {
               if (((StartStatement)s).isUnique()) {
                  if (!unique) {
                     unique = true;
                     s.compile(program);
                  }
               }
               else {
                  s.compile(program);
               }
            }
         }

        program.setImage(image);
         program.setStatements(statements);
         return program;
      }
      catch (Exception x) {
		  throw new CompileException(CompileException.ERR_UNKNOWN);
      }
      finally {
         this.compiling = false;
         notifyProgress("Done.", 1.0);
         notifyEnd();
      }
   }



/**
 * Tangible compile function: generate a program from a buffered image
 */
	public Program compile(BufferedImage image) throws CompileException {
		this.compiling = true;
		notifyBegin();
		
		notifyProgress("Scanning for TopCodes...", 0.0);
		List<TopCode> spots = scanner.scan(image);
                
                for(TopCode top : spots)
                    System.out.println(top.toString());
                System.out.println("I am out");
               
		return doCompile(spots);
	}      


   
/**
 * Tangible compile function: generate a program from an image file name
 */
	public Program compile(String filename) throws CompileException {
		this.compiling = true;
		notifyBegin();
		
		notifyProgress("Scanning for TopCodes...", 0.0);
		try {
                 scanner = new Scanner();
                    List<TopCode> spots =  new java.util.ArrayList<TopCode>();
                        spots= scanner.scan(filename);
                        
                     
			return doCompile(spots);
		} catch (java.io.IOException iox) {
			this.compiling = false;
			notifyProgress("Done.", 1.0);
			notifyEnd();
			throw new CompileException(CompileException.ERR_LOAD_FILE);
		}
	}


   
/**
 * Tangible compile function: generate a program from an array of pixel
 * data.
 */int i=0;
	public Program compile(int w, int h, int [] pixels) throws CompileException {
		this.compiling = true;
		notifyBegin();
		
		notifyProgress("Scanning for TopCodes...", 0.0);
		List<TopCode> spots = scanner.scan(pixels, w, h);
                System.out.println("I am here");
                for(TopCode top : spots)
                    System.out.println(top.toString());
                System.out.println("I am out");
                System.out.println(i);
                i++;
		return doCompile(spots);
	}


   
	public boolean getAnnotate() {
		return this.annotate;
	}

	public void setAnnotate(boolean annotate) {
		this.annotate = annotate;
	}
	



	protected void notifyBegin() { }
	protected void notifyEnd() { }
	protected void notifyProgress(String task, double progress) {  }
}

