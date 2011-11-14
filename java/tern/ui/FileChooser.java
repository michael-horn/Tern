/*
 * @(#) FileChooser.java
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;



/**
 * A thin wrapper around JFileChooser
 *
 * @author Michael Horn
 * @version $Revision: 1.1 $, $Date: 2008/10/18 20:19:50 $
 */
public class FileChooser extends FileFilter {


   /** Singleton static instance to support static methods */
   protected static FileChooser instance = new FileChooser();
   
   protected JFileChooser chooser;
   protected String ext;
   protected JComponent parent;

   
  
   public FileChooser() {
      this.chooser = new JFileChooser(new File("."));
      this.ext     = null;
      this.parent  = null;
   }

   

/**
 * Lets the user choose a file to open with no restrictions on the
 * file extension.
 */
   public static String openFile() {
      return instance._openFile(null);
   }

   

/**
 * Lets the user choose a file to open.  The file must match the
 * given file extension (e.g. "lisp" or "txt").
 */
   public static String openFile(String ext) {
      return instance._openFile(ext);
   }



   protected String _openFile(String ext) {
      if (ext == null) {
         chooser.resetChoosableFileFilters();
      } else {
         this.ext = ext.toLowerCase();
         chooser.addChoosableFileFilter(this);
      }
      chooser.setCurrentDirectory(new File("."));
      chooser.setSelectedFile(new File(""));
      int result = chooser.showOpenDialog(parent);
      if (result == JFileChooser.APPROVE_OPTION) {
         return chooser.getSelectedFile().getAbsolutePath();
      } else {
         return null;
      }
   }      

   
   
/**
 * Lets the user choose a file to save.  The file must match the
 * given file extension.
 */
   public static String saveFile() {
      return instance._saveFile(null);
   }



   public static String saveFile(String ext) {
      return instance._saveFile(ext);
   }



   protected String _saveFile(String ext) {
      if (ext == null) {
         chooser.resetChoosableFileFilters();
      } else {
         this.ext = ext.toLowerCase();
         chooser.addChoosableFileFilter(this);
      }
      chooser.setCurrentDirectory(new File("."));
      chooser.setSelectedFile(new File(""));
      int result = chooser.showSaveDialog(parent);
      if (result == JFileChooser.APPROVE_OPTION) {
         String file = chooser.getSelectedFile().getAbsolutePath();
         if (ext != null &&
             !file.toLowerCase().endsWith("." + this.ext)) {
            file += "." + ext;
         }
         return file;
      } else {
         return null;
      }
   }
   


/**
 * FileFilter method.  Determine whether our current file extension
 * matches the given file.
 */
   public boolean accept(File file) {
      if (ext == null) return true;
      
      return (file.isDirectory() ||
              file.getName().toLowerCase().endsWith("." + ext));
   }

   

/**
 * FileFilter method.
 */
   public String getDescription() {
      if (ext == null) {
         return "All Files";
      } else {
         return (ext.toUpperCase() + " Files");
      }
   }
}
