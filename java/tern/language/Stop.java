/*
 * @(#) Stop.java
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


public class Stop extends PStatement {


   public static final int STOP_A = 103;
   public static final int STOP_B = 143;
   public static final int STOP_C = 109;

   protected char motor;

   
   public Stop(TopCode top) {
      super(top);
      switch (top.getCode()) {
      case STOP_A:
         this.motor = 'A';
         break;
      case STOP_B:
         this.motor = 'B';
         break;
      default:
         this.motor = 'C';
         break;
      }
   }


   public static void register() {
      StatementFactory.registerStatementType(
         new Stop(new TopCode(STOP_A)));
      StatementFactory.registerStatementType(
         new Stop(new TopCode(STOP_B)));
      StatementFactory.registerStatementType(
         new Stop(new TopCode(STOP_C)));
   }


   public int getCode() {
      return top.getCode();
   }


   public String getName() {
      return ("STOP " + motor);
   }

   
   public Statement newInstance(TopCode top) {
      return new Stop(top);
   }

	
   public void compile(Program program) throws CompileException {
      setDebugInfo(program);
      program.addInstruction("   Off(OUT_" + motor + ");");
      if (this.next != null) next.compile(program);
   }


	public void toXML(java.io.PrintWriter out) {
		switch (top.getCode()) {
		case STOP_A:  out.println("   <stop param='a' />");
		case STOP_B:  out.println("   <light param='off' />");
		case STOP_C:  out.println("   <stop param='c' />");
      }
	}
}
