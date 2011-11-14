/*
 * @(#) CompileException.java
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

public class CompileException extends Exception {

	public static final int ERR_NONE      = 0;
	public static final int ERR_NO_BLOCKS = 1;
	public static final int ERR_NO_BEGIN  = 2;
	public static final int ERR_SAVE_FILE = 3;
	public static final int ERR_CAMERA    = 4;
	public static final int ERR_NO_NQC    = 5;
	public static final int ERR_NO_RCX    = 6;
	public static final int ERR_NO_TOWER  = 7;
	public static final int ERR_LOAD_FILE = 8;
	public static final int ERR_FIRMWARE  = 9;
	public static final int ERR_UNKNOWN   = 10;
        public static final int ERR_NO_NXT    = 11;
	
	protected int code = ERR_NONE;

	public CompileException(int code) {
		super();
		this.code = code;
	}
	
	public int getErrorCode() {
		return this.code;
	}
}

