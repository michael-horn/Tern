/*
 * @(#) Go.java
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

public class Go extends PStatement {


   public static final int FWD_A = 211;
   public static final int REV_A = 229;
   public static final int FWD_B = 117;
   public static final int REV_B = 87;
   public static final int FWD_C = 217;
   public static final int REV_C = 241;

   
   public Go(TopCode top) {
      super(top);
   }


   public static void register() {
      StatementFactory.registerStatementType(
         new Go(new TopCode(FWD_A)));
      StatementFactory.registerStatementType(
         new Go(new TopCode(REV_A)));
      StatementFactory.registerStatementType(
         new Go(new TopCode(FWD_B)));
      StatementFactory.registerStatementType(
         new Go(new TopCode(REV_B)));
      StatementFactory.registerStatementType(
         new Go(new TopCode(FWD_C)));
      StatementFactory.registerStatementType(
         new Go(new TopCode(REV_C)));
   }


   public int getCode() {
      return top.getCode();
   }


   public String getName() {
      return ("GO");
   }

   
   public Statement newInstance(TopCode top) {
      return new Go(top);
   }


   public void compile(Program program) throws CompileException {
      setDebugInfo(program);
      switch (top.getCode()) {
      case FWD_A:
         program.addInstruction("   StartMotor(OUT_A, 3);");
         break;
      case REV_A:
         program.addInstruction("   StartMotor(OUT_A, -3);");
         break;
      case FWD_B:
         program.addInstruction("   StartMotor(OUT_B, 3);");
         break;
      case REV_B:
         program.addInstruction("   StartMotor(OUT_B, -3);");
         break;
      case FWD_C:
         program.addInstruction("   StartMotor(OUT_C, 3);");
         break;
      case REV_C:
         program.addInstruction("   StartMotor(OUT_C, -3);");
         break;
      }
      if (this.next != null) next.compile(program);
   }


	public void toXML(java.io.PrintWriter out) {
      switch (top.getCode()) {
      case FWD_A: out.println("   <go param='fwd_a' />"); break;
      case REV_A: out.println("   <go param='rev_a' />"); break;
      case FWD_B: out.println("   <light param='on' />"); break;
      case REV_B: out.println("   <go param='rev_b' />"); break;
      case FWD_C: out.println("   <go param='fwd_c' />"); break;
      case REV_C: out.println("   <go param='rev_c' />"); break;
	  }
	}
}
