/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 */
package jnafilechooser.demo;

import static jnafilechooser.win32.Comdlg32.*;
import static jnafilechooser.win32.Ole32.*;
import static jnafilechooser.win32.Shell32.*;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import jnafilechooser.win32.Comdlg32.OpenFileName;
import jnafilechooser.win32.Shell32.BrowseInfo;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class PlumbingDemo {
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame frame = new JFrame(PlumbingDemo.class.getName());
		final JButton chooseFolder = new JButton("Choose Folder");
		final JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
		frame.setContentPane(content);
		content.add(chooseFolder);
		chooseFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OleInitialize(null);
				final BrowseInfo params = new BrowseInfo();
				params.hwndOwner = Native.getWindowPointer(frame);
				params.ulFlags = BIF_USENEWUI | BIF_RETURNONLYFSDIRS;
				final Pointer pidl = SHBrowseForFolder(params);
				if (pidl != null) {
					final Pointer path = new Memory(260);
					path.clear(260);
					SHGetPathFromIDListW(pidl, path);
					final String pathStr = path.getWideString(0);
					System.out.println(pathStr);
				}
				CoTaskMemFree(pidl);
			}
		});
		final JButton chooseFile = new JButton("Choose File");
		content.add(chooseFile);
		chooseFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OpenFileName params = new OpenFileName();
				params.hwndOwner = Native.getWindowPointer(frame);
				params.nMaxFile = 260;
				params.lpstrFile = new Memory(260);
				params.lpstrFile.clear(260);
				String filterStr = "All Files\0*.*\0Text Files\0*.txt;*.log\0Pictures\0*.png;*.jpg;*.bmp;*.gif\0\0";
				params.lpstrFilter = new WString(filterStr);
				params.lpstrCustomFilter = null;
				params.nFilterIndex = 1;
				params.Flags = OFN_EXPLORER | OFN_ENABLESIZING
					| OFN_NOCHANGEDIR | OFN_HIDEREADONLY;

				if (GetOpenFileNameW(params)) {
					System.out.println(params.lpstrFile.getWideString(0));
				}
				else {
					int err = CommDlgExtendedError();
					System.out.println(err);
				}
			}
		});
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
