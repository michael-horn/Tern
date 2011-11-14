/*
 * @(#) Wait.java
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
package tern.language;

import tern.compiler.*;
import topcodes.TopCode;


public class Wait extends PStatement {

	public static final int CODE = 611;
   
	public Wait(TopCode top) {
		super(top);
	}


	public static void register() {
      StatementFactory.registerStatementType(new Wait(new TopCode(CODE)));
	}


	public int getCode() {
		return Wait.CODE;
	}


	public String getName() {
		return ("WAIT");
	}

   
	public Statement newInstance(TopCode top) {
		return new Wait(top);
	}


	public void compile(Program program) throws CompileException {
	   setDebugInfo(program);
	   if (next != null && next instanceof Sensor) {
		   switch (next.getCode()) {
		   case Sensor.PRESS:
			   program.addInstruction("   WaitForPress();");
			   break;
		   case Sensor.RELEASE:
			   program.addInstruction("   WaitForRelease();");
			   break;
		   case Sensor.LIGHT:
			   program.addInstruction("   WaitForLight();");
			   break;
		   case Sensor.DARK:
			   program.addInstruction("   WaitForDark();");
			   break;
		   }
	   }
	   else if (next != null && next instanceof Num) {
		   int d = ((Num)next).getValue() * 100;
		   program.addInstruction("   Wait(" + d + ");");
	   }
	   if (this.next != null) next.compile(program);
	}


	public void toXML(java.io.PrintWriter out) {
		out.println("   <wait />");
	}
}
