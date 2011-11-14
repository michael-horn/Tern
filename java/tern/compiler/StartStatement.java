/*
 * @(#) StartStatement.java
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

/**
 * This interface is used to identify the one statement in a program
 * that begins the flow-of-control chain.  Every valid program must
 * have at least one StartStatement.  Tangible language
 * implementations should have at least one statement type that
 * implements this interface.
 *
 * @author Michael Horn
 * @version $Revision: 1.2 $, $Date: 2008/11/08 16:56:57 $
 */
public interface StartStatement {
	
	/**
	 * If true, only one of these statements is allowed in a program
	 */
	public boolean isUnique();
}
