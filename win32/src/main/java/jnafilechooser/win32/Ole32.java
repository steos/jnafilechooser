/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 */
package jnafilechooser.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class Ole32 {
	static {
		Native.register("ole32");
	}
	public static native Pointer OleInitialize(Pointer pvReserved);
	public static native void CoTaskMemFree(Pointer pv);
}
