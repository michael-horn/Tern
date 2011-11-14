/*
 * @(#) Repeat.java
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
 * 
 * Modified by Mariam Hussien - Oct,2011
 */
package tern.language;

import tern.compiler.*;
import topcodes.TopCode;


public class Repeat extends PStatement {

	public static final int BEGIN = 171;
	public static final int END   = 179;

	public static int NEST = 0;
	public static int VAR = 0;

	
	public Repeat(TopCode top) {
		super(top);
	}
	
	
	public static void register() {
		StatementFactory.registerStatementType(
			new Repeat(new TopCode(BEGIN)));
		StatementFactory.registerStatementType(
			new Repeat(new TopCode(END)));
	}
	
	
	public int getCode() {
		return top.getCode();
	}

	
	public String getName() {
		return ("REPEAT");
	}
	
	
	public Statement newInstance(TopCode top) {
		return new Repeat(top);
	}

	
	public void compile(Program program) throws CompileException {
		setDebugInfo(program);
		
		if (getCode() == BEGIN) {
			compileBeginRepeat(program);
		} else if (getCode() == END) {
			compileEndRepeat(program);
		}
	}
	
	
	protected void compileEndRepeat(Program program) throws CompileException {
		if (Repeat.NEST > 0) {
			program.addInstruction("   }");
			Repeat.NEST--;
		}
		if (this.next != null) next.compile(program);
	}
	
	
	protected void compileBeginRepeat(Program program) throws CompileException {
		
		if (next != null && next instanceof Num) {
			String var = "count" + VAR++;
			int param = ((Num)next).getValue();
			program.addInstruction("   int " + var + " = 0;");
			program.addInstruction("   while (" + var + " < " + param + ") {");
			program.addInstruction("      " + var + "++;");
		}
		else if (next != null && next instanceof Sensor) {
			Sensor sen = (Sensor)next;
                        String senType= sen.getType();
                        program.addInstruction("   SetSensor"+senType+"(" +
								   sen.getSensorID() + ");");
                        program.addInstruction("   while (" + sen.getTest() + ") {");
      		}
		else {
			program.addInstruction("   while (1) {");
		}
		int nest = NEST;
		NEST++;
		if (this.next != null) next.compile(program);
		if (NEST > nest) { 
			program.addInstruction("   }");
			NEST--;
		}
	}
	
	
	public void toXML(java.io.PrintWriter out) {
		if (getCode() == BEGIN) {
			out.println("   <repeat />");
		} else if (getCode() == END) {
			out.println("   <end-repeat />");
		}
	}
}
