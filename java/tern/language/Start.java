/*
 * @(#) Start.java
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

import topcodes.*;
import tern.compiler.*;


public class Start extends PStatement implements StartStatement {

   
   public static final int CODE = 569;

   
   public Start(TopCode top) {
      super(top);
   }


   public static void register() {
      StatementFactory.registerStatementType(
         new Start(new TopCode(CODE)));
   }

   
   public int getCode() {
      return CODE;
   }


   public Statement newInstance(TopCode top) {
      return new Start(top);
   }
   
   
   public String getName() {
      return "START";
   }


   public boolean isUnique() {
      return true;
   }


   public void registerConnections(ConnectionMap map) {
      map.addConnector(this, "next", 2.9, 0);
   }
   

   public void compile(Program program) throws CompileException {
      setDebugInfo(program);
      //for nqc
    //  program.addInstruction("#include \"nqc/base.nqc\"");
      
      //for nxc
      program.addInstruction("#include \"base.h\"");
      program.addInstruction("");
      program.addInstruction("task main() {");
     // program.addInstruction("   Begin();");
      if (next != null) next.compile(program);
      program.addInstruction("}");
   }


	public void toXML(java.io.PrintWriter out) {
		out.println("   <begin />");
	}
}
