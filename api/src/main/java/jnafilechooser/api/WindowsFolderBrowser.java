/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 */
package jnafilechooser.api;

import java.awt.Window;
import java.io.File;

import jnafilechooser.win32.Ole32;
import jnafilechooser.win32.Shell32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * The native Windows folder browser.
 *
 * Example:
 * WindowsFolderBrowser fb = new WindowsFolderBrowser();
 * File dir = fb.showDialog(parentWindow);
 * if (dir != null) {
 *     // do something with dir
 * }
 */
public class WindowsFolderBrowser
{
	private String title;

	/**
	 * creates a new folder browser
	 */
	public WindowsFolderBrowser() {
		title = null;
	}

	/**
	 * creates a new folder browser with text that can be used as title
	 * or to give instructions to the user
	 *
	 * @param title text that will be displayed at the top of the dialog
	 */
	public WindowsFolderBrowser(String title) {
		this.title = title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * displays the dialog to the user
	 *
	 * @param parent the parent window
	 *
	 * @return the selected directory or null if the user canceled the dialog
	 */
	public File showDialog(Window parent) {
		Ole32.OleInitialize(null);
		final Shell32.BrowseInfo params = new Shell32.BrowseInfo();
		params.hwndOwner = Native.getWindowPointer(parent);
		params.ulFlags =
			// disable the OK button if the user selects a virtual PIDL
			Shell32.BIF_RETURNONLYFSDIRS |
			// BIF_USENEWUI is only available as of Windows 2000/Me (Shell32.dll 5.0)
			// but I guess no one is using older versions anymore anyway right?!
			// I don't know what happens if this is executed where it's
			// not supported.
			Shell32.BIF_USENEWUI;
		if (title != null) {
			params.lpszTitle = title;
		}
		final Pointer pidl = Shell32.SHBrowseForFolder(params);
		if (pidl != null) {
			// MAX_PATH is 260 on Windows XP x32 so 4kB should
			// be more than big enough
			final Pointer path = new Memory(1024 * 4);
			Shell32.SHGetPathFromIDListW(pidl, path);
			final String filePath = path.getWideString(0);
			final File file = new File(filePath);
			Ole32.CoTaskMemFree(pidl);
			return file;
		}
		return null;
	}
}
