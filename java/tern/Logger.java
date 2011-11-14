/*
 * @(#) Logger.java
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

import tern.compiler.Program;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Calendar;


public class Logger {

	/** Log file */
	protected PrintWriter log;
	
	protected String logdir;
	
	protected String basedir;
	
	
	public Logger(String basedir) {
		this.log     = null;
		this.basedir = basedir;
		this.logdir  = (
			basedir + File.separator +
			String.format("%1$tY_%1$tm_%1$td", Calendar.getInstance()) );
	}
	
	
	public void start() {
		if (isLogging()) return;
		try {
			
			// Create log directory
			(new File(logdir)).mkdirs();
			
			// Create log stream writer
			this.log = new PrintWriter (
				new java.io.FileWriter(
					logdir + File.separator + "log.txt", true),
				true );
			log("Start Logging");
		} catch (IOException iox) {
			System.err.println(iox);
			this.log = null;
		}
	}
	
	
	public void stop() {
		if (this.log != null) {
			this.log.close();
			this.log = null;
		}
	}
	
	
	public boolean isLogging() {
		return this.log != null;
	}
	
	
	protected void checkDate(Calendar c) {
		if (isLogging()) {
			String ld = (
				basedir + File.separator +
				String.format("%1$tY_%1$tm_%1$td", c) );
			if (! ld.equals(logdir)) {
				stop();
				start();
			}
		}
	}
	
	
	public void log(String line) {
		Calendar c = Calendar.getInstance();
		checkDate(c);
		String s = String.format("[%1$tH:%1$tM:%1$tS] " + line, c);
		if (this.log != null) log.println(s);
	}
	
	
	public void log(Exception x) {
		log("EXCEPTION: " + x.toString());
	}
	
	
	public void log(Program p) {
		if (!isLogging()) return;
		try {
			String file_prefix = (
				this.logdir + File.separator +
				String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS",
							  Calendar.getInstance() ) );
			p.save(file_prefix + ".pcode");
			p.saveImage(file_prefix + ".jpg");
		} catch (IOException iox) {
			log(iox);
		}
	}
}
