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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import jnafilechooser.api.WindowsFileChooser;
import jnafilechooser.api.WindowsFolderBrowser;

import com.sun.jna.Platform;

public class NativePorcelainDemo {
	public static void main(String[] args) throws Exception {
		if (!Platform.isWindows()) {
			System.err.println("Sorry, this only works on Windows");
			JOptionPane.showMessageDialog(null, "Sorry this only works on Windows",
					"OS not supported", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final JFrame frame = new JFrame(NativePorcelainDemo.class.getName());
		final JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
		final JButton chooseFile = new JButton("Choose File");
		final JButton chooseDir = new JButton("Choose Folder");
		content.add(chooseFile);
		content.add(chooseDir);

		chooseFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final WindowsFileChooser fc = new WindowsFileChooser();
				fc.setDefaultFilename("Filechooser Demo.txt");
				fc.setTitle("Filechooser Demo");
				fc.addFilter("All Files", "*");
				fc.addFilter("Pictures", "jpg", "jpeg", "gif", "bmp", "png");
				fc.addFilter("Text Files", "txt", "log", "nfo", "xml");
				if (fc.showOpenDialog(frame)) {
					final File f = fc.getSelectedFile();
					JOptionPane.showMessageDialog(frame, f.getAbsolutePath(),
						"Selection", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		chooseDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final WindowsFolderBrowser fb = new WindowsFolderBrowser();
				final File dir = fb.showDialog(frame);
				if (dir != null) {
					JOptionPane.showMessageDialog(frame, dir.getAbsolutePath(),
						"Selection", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		frame.setContentPane(content);
		frame.pack();
		frame.setSize(frame.getWidth() * 2, frame.getHeight());
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
