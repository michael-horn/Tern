/*
 * @(#) Transmitter.java
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
package tern.nqc;

import java.io.BufferedInputStream;
import tern.compiler.CompileException;


/**
 * Transmits compiled programs and firmware to the RCX
 * processor via the NQC compiler.
 *
 * @author Michael Horn
 * @version $Revision: 1.4 $, $Date: 2009/02/12 23:06:27 $
 */
public class Transmitter {

	/** The Lego Mindstorms firmware binary. */
   private String firmware = "firm0328.lgo";

	/** NQC Compiler binary (nqc.exe) */	
	private String compiler;

	/** LEGO tower port */
	private String port;

	/** Compiler process */
	private Process process;

	/** NQC command to execute */
	private String [] command;


	
	public Transmitter(java.util.Properties props) {
		this.firmware = props.getProperty("nqc.firmware");
		this.compiler = props.getProperty("nqc.compiler");
		this.port     = props.getProperty("nqc.port");
		this.process  = null;
		this.command  = new String[4];
   }


	/**
	 * Sends a program to the RCX.
	 */
	public void send(String filename) throws CompileException {
         
		/*
		 * Format nqc.exe command line:
		 *
		 *     nqc -S<port> -d <program>
		 */
		command[0] = compiler;
		command[1] = "-S" + port;
		command[2] = "-d";
		command[3] = filename;
		
		exec(command);
	}


/**
 * Loads firmware onto the RCX.
 */
	public void loadFirmware() throws CompileException {
		
		/*
		 * Format NQC command
		 */
		command[0] = compiler;
		command[1] = "-S" + port;
		command[2] = "-firmware";
		command[3] = firmware;
		
		exec(command);
	}
	

	/**
	 * Asynchronous process monitoring...
	 */
	protected void exec(String [] command) throws CompileException {
		try {
			
			int b;
			String sout = "", serr = "";
			BufferedInputStream in;
			int result;

			
			//---------------------------------------------
			// Exec process
			//---------------------------------------------
			this.process = Runtime.getRuntime().exec(command);
			
			
			//---------------------------------------------
			// Read NQC output
			//---------------------------------------------
			in = new BufferedInputStream(process.getInputStream());			
			while ((b = in.read()) > 0) { sout += (char)b; }

			
			//---------------------------------------------
			// Read NQC error
			//---------------------------------------------
			in = new BufferedInputStream(process.getErrorStream());
			while ((b = in.read()) > 0) { serr += (char)b; }

			
			//---------------------------------------------
			// Wait for process to complete
			//---------------------------------------------
			try { result = process.waitFor(); }
			catch (InterruptedException ix) { result = 0; }

			if (result != 0) {
				generateError(sout + serr);
			}
		}
		catch (java.io.IOException iox) {
			throw new CompileException(CompileException.ERR_NO_NQC);
		}
	}


	private void generateError(String err) throws CompileException {
		if (err.indexOf("No reply from RCX") >= 0) {
			throw new CompileException(CompileException.ERR_NO_RCX);
		}
		else if (err.indexOf("Could not open serial port or USB device") >= 0) {
			throw new CompileException(CompileException.ERR_NO_TOWER);
		}
		else if (err.indexOf("No firmware") >= 0) {
			throw new CompileException(CompileException.ERR_FIRMWARE);
		}
		else {
			System.out.println(err);
			throw new CompileException(CompileException.ERR_UNKNOWN);
		}
	}
}
