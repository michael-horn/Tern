/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tern.nqc;
import java.io.BufferedInputStream;
import java.io.IOException;
import tern.compiler.CompileException;

/**
 *
 * @author Maruma
 */
public class NXCTransmitter {
     private String firmware = "firm0328.lgo";

	/** NXC Compiler binary (nqc.exe) */	
	private String compiler;



	/** Compiler process */
	private Process process;

	/** NQC command to execute */
	private String [] command;

        private String Path;

	
	public NXCTransmitter(java.util.Properties props) {
		this.compiler = props.getProperty("nxc.compiler");
		this.process  = null;
                this.Path = props.getProperty("nxc.path");
		this.command  = new String[4];
   }
        
       
        //Mariam
        //04-09-2011
        public void Run(String filename)throws CompileException
        { 
            
                command[0] = compiler;
		command[1] = "-r";
               
		command[2] = filename;
		command[3] = "";
		
		exec(command);
        }


	/**
	 * Sends a program to the RCX.
	 */
        


	

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
		if (err.indexOf("Download failed") >= 0) {
			throw new CompileException(CompileException.ERR_NO_NXT);
		}
		
		else {
			System.out.println(err);
			throw new CompileException(CompileException.ERR_UNKNOWN);
		}
	}
    
}
