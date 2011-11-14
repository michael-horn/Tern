/*
 * @(#) Growl.java
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


public class Growl extends PStatement {

   public static final int CODE = 307;

   public Growl(TopCode top) {
      super(top);
   }


   public static void register() {
      StatementFactory.registerStatementType(
         new Growl(new TopCode(CODE)));
   }


   public int getCode() {
      return CODE;
   }


   public String getName() {
      return "GROWL";
   }


   public Statement newInstance(TopCode top) {
      return new Growl(top);
   }

	
   public void compile(Program program) throws CompileException {
      setDebugInfo(program);
      program.addInstruction("   Growl();");
      if (this.next != null) next.compile(program);
   }


	public void toXML(java.io.PrintWriter out) {
		out.println("   <growl />");
	}
}
