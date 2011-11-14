/*
 * @(#) Program.java
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

import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;



/**
 * A program is simply a list of instructions created by the compiler.
 * Programs may be translated into some other text-based language or
 * interpreted by a virtual machine.
 *
 * @author Michael Horn
 * @version $Revision: 1.7 $, $Date: 2008/11/05 22:44:29 $
 */
public class Program {

   
	/** List of instructions */
	protected List<String> code;
	
	/** Symbol lookup table (maps labels to line numbers) */
	protected Map symbols;
	
	/** Used to generate unique label names */
	protected int lid;
	
	/** Rectangle that frames a program in a JPEG image */
	protected double xmin, ymin, xmax, ymax;
	
	/** Link to the original JPEG image for tangible programs */
	protected BufferedImage image;
	
	/** List of statements recognized in an image */
	protected List<Statement> statements;
	
	/** A list of statements included in the program */
	protected List<Statement> compiled;
	
	/** Used to generate statement compile-time ID numbers */
	public int COMPILE_ID = 0;
	
	
	public Program() {
		this.code       = new java.util.ArrayList<String>();
		this.symbols    = new java.util.HashMap();
		this.lid        = 0;
		this.xmin       = 1600;
		this.ymin       = 1200;
		this.xmax       = 0;
		this.ymax       = 0;
		this.image      = null;
		this.statements = null;
		this.compiled   = new java.util.ArrayList<Statement>();
	}
	
	

/**
 * Subclasses that wish to translate programs into text-based code
 * should override this function.
 */
	public void save(PrintWriter out) throws IOException {
		for (int i=0; i<code.size(); i++) {
			out.println(code.get(i));
		}
	}
	


/**
 * Save program in named file
 */
   public void save(String filename) throws IOException {
      FileWriter f = new FileWriter(filename);
      save(new PrintWriter(f));
      f.close();
   }


/**
 * Save program to XML stream
 */
	public void toXML(PrintWriter out) {
		out.println("<program>");
		for (Statement s : compiled) {
			s.toXML(out);
		}
		out.println("</program>");
	}
   

/**
 * Save the program image as a JPEG file
 */
   public void saveImage(String filename) throws IOException {
      if (image != null) {
         ImageIO.write(image, "jpeg", new File(filename));
      }
   }


/**
 * Called by individual statements in a program. Adds a line of code
 * to the program.
 */
   public void addInstruction(String instr) {
      this.code.add(instr);
   }

   

/**
 * Fetch an instruction by line number.
 */
   public String getInstruction(int line) {
      if (line < 0 || line >= code.size()) {
         return null;
      } else {
         return code.get(line).trim();
      }
   }


   
/**
 * Add a statement to the list of compiled statements in this program
 */
   public void addCompiledStatement(Statement s) {
      this.compiled.add(s);
   }



/**
 * Returns a list of compiled statements
 */
   public List<Statement> getCompiledStatements() {
      return this.compiled;
   }


   
/**
 * Called by the compiler.  Adds a label to the code and the symbol
 * lookup table.
 */
   public void addLabel(String label) {
      int num = this.code.size();
      this.code.add(label);
      this.symbols.put(label, new Integer(num));
   }

   

/**
 * Looks up a line number in the symbol table.  If the symbol
 * is not found, the function returns -1.
 */
   public int getLineNumber(String label) {
      if (symbols.containsKey(label)) {
         return ((Integer)symbols.get(label)).intValue();
      } else {
         return -1;
      }
   }

   

/**
 * Returns true if the label exists in the symbol table.
 */
   public boolean hasLabel(String label) {
      return symbols.containsKey(label);
   }
   


/**
 * Used to generate unique label names for flow-of-control structures.
 */
   public String genLabel() {
      return ("L" + (lid++));
   }

   

   public int getLineCount() {
      return this.code.size();
   }



   public String toString() {
      String s = "";
      for (String line : code) {
         s += line + "\n";
      }
      return s;
   }

   

   public BufferedImage getImage() {
      return this.image;
   }

   public void setImage(BufferedImage image) {
      this.image = image;
   }



   public List<Statement> getStatements() {
      return this.statements;
   }

   public void setStatements(List<Statement> statements) {
      this.statements = statements;
   }


/**
 * Returns a statement by compile-time ID number
 */
   public Statement getStatement(int c_id) {
      if (statements == null) return null;
      for (Statement s : statements) {
         if (s.getCompileID() == c_id) {
            return s;
         }
      }
      return null;
   }


	public boolean isEmpty() {
		return (statements == null || statements.isEmpty());
	}


   public boolean hasStartStatement() {
      if (statements == null) {
         return false;
      } else {
         for (Statement s : statements) {
            if (s instanceof StartStatement) {
               return true;
            }
         }
      }
      return false;
   }
   
   
/**
 * Called by individual statements.  Expands the bounding box that
 * frames a program in a JPEG image.
 */
   public void expandBoundingBox(double cx, double cy) {
      xmax = (cx > xmax)? cx : xmax;
      ymax = (cy > ymax)? cy : ymax;
      xmin = (cx < xmin)? cx : xmin;
      ymin = (cy < ymin)? cy : ymin;
   }


   
/**
 * Returns a bounding box around a program in a JPEG image.
 */
   public Rectangle2D getBounds() {
      if (xmin > xmax || ymin > ymax) {
         return new Rectangle2D.Double(0, 0, 1600, 1200);
      } else {
         return new Rectangle2D.Double(
            xmin - 100,
            ymin - 100,
            xmax - xmin + 200,
            ymax - ymin + 200);
      }
   }
}
