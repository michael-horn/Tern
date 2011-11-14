/*
 * @(#) StatementFactory.java
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
package tern.compiler;

import java.util.List;
import java.util.Iterator;
import topcodes.*;


public class StatementFactory {


	protected static List<Statement> stypes =
		new java.util.ArrayList<Statement>();
	
	
	/**
	 * Registers a new statement type.  This must be called for each
	 * statement type when an application is loaded.
	 */
	public static void registerStatementType(Statement s) {
		stypes.add(s);
	}
	
	
	/**
	 * Called by the tangible compiler to generate new statements
	 * from topcodes found in an image.
	 */
	public static Statement createStatement(TopCode top) {
		int code = (int)top.getCode();
		for (Statement s : stypes) {
			if (s.getCode() == code) {
				return s.newInstance(top);
			}
		}
		return null;
	}

	
	public static int getStatementCount() {
		return stypes.size();
	}
	
	
	public static Iterator<Statement> getStatements() {
		return stypes.iterator();
	}
}
