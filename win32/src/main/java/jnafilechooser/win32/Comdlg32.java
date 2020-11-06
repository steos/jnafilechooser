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
import com.sun.jna.Structure;
import com.sun.jna.WString;
import java.util.Arrays;
import java.util.List;

public class Comdlg32
{
	static {
		Native.register("comdlg32");
	}

	public static native boolean GetOpenFileNameW(OpenFileName params);
	public static native boolean GetSaveFileNameW(OpenFileName params);
	public static native int CommDlgExtendedError();

	public static class OpenFileName extends Structure {
		public OpenFileName() {
			super();
			lStructSize = size();
		}
		public int lStructSize;
		public Pointer hwndOwner;
		public Pointer hInstance;
		public WString lpstrFilter;
		public WString lpstrCustomFilter;
		public int nMaxCustFilter;
		public int nFilterIndex;
		public Pointer lpstrFile;
		public int nMaxFile;
		public String lpstrDialogTitle;
		public int nMaxDialogTitle;
		public WString lpstrInitialDir;
		public WString lpstrTitle;
		public int Flags;
		public short nFileOffset;
		public short nFileExtension;
		public String lpstrDefExt;
		public Pointer lCustData;
		public Pointer lpfnHook;
		public Pointer lpTemplateName;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "lStructSize",
				"hwndOwner","hInstance","lpstrFilter","lpstrCustomFilter"
			,"nMaxCustFilter","nFilterIndex","lpstrFile","nMaxFile"
			,"lpstrDialogTitle","nMaxDialogTitle","lpstrInitialDir","lpstrTitle"
			,"Flags","nFileOffset","nFileExtension","lpstrDefExt"
			,"lCustData","lpfnHook","lpTemplateName"
			});
		}
	}

	// flags for the OpenFileName structure
	public final static int OFN_READONLY = 0x00000001;
	public final static int OFN_OVERWRITEPROMPT = 0x00000002;
	public static final int OFN_HIDEREADONLY = 0x00000004;
	public static final int OFN_NOCHANGEDIR = 0x00000008;
	public static final int OFN_SHOWHELP = 0x00000010;
	public static final int OFN_ENABLEHOOK = 0x00000020;
	public static final int OFN_ENABLETEMPLATE = 0x00000040;
	public static final int OFN_ENABLETEMPLATEHANDLE = 0x00000080;
	public static final int OFN_NOVALIDATE = 0x00000100;
	public static final int OFN_ALLOWMULTISELECT = 0x00000200;
	public static final int OFN_EXTENSIONDIFFERENT = 0x00000400;
	public static final int OFN_PATHMUSTEXIST = 0x00000800;
	public static final int OFN_FILEMUSTEXIST = 0x00001000;
	public static final int OFN_CREATEPROMPT = 0x00002000;
	public static final int OFN_SHAREAWARE = 0x00004000;
	public static final int OFN_NOREADONLYRETURN = 0x00008000;
	public static final int OFN_NOTESTFILECREATE = 0x00010000;
	public static final int OFN_NONETWORKBUTTON = 0x00020000;
	public static final int OFN_NOLONGNAMES = 0x00040000;
	public static final int OFN_EXPLORER = 0x00080000;
	public static final int OFN_NODEREFERENCELINKS = 0x00100000;
	public static final int OFN_LONGNAMES = 0x00200000;
	public static final int OFN_ENABLEINCLUDENOTIFY = 0x00400000;
	public static final int OFN_ENABLESIZING = 0x00800000;
	public static final int OFN_DONTADDTORECENT = 0x02000000;
	public static final int OFN_FORCESHOWHIDDEN = 0x10000000;

	// error codes from cderr.h which may be returned by
	// CommDlgExtendedError for the GetOpenFileName and
	// GetSaveFileName functions.
	public static final int CDERR_DIALOGFAILURE = 0xFFFF;
	public static final int CDERR_FINDRESFAILURE = 0x0006;
	public static final int CDERR_INITIALIZATION = 0x0002;
	public static final int CDERR_LOADRESFAILURE = 0x0007;
	public static final int CDERR_LOADSTRFAILURE = 0x0005;
	public static final int CDERR_LOCKRESFAILURE = 0x0008;
	public static final int CDERR_MEMALLOCFAILURE = 0x0009;
	public static final int CDERR_MEMLOCKFAILURE = 0x000A;
	public static final int CDERR_NOHINSTANCE = 0x0004;
	public static final int CDERR_NOHOOK = 0x000B;
	public static final int CDERR_NOTEMPLATE = 0x0003;
	public static final int CDERR_STRUCTSIZE = 0x0001;
	public static final int FNERR_SUBCLASSFAILURE = 0x3001;
	public static final int FNERR_INVALIDFILENAME = 0x3002;
	public static final int FNERR_BUFFERTOOSMALL = 0x3003;

}