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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import jnafilechooser.api.JnaFileChooser;
import net.miginfocom.swing.MigLayout;

import com.sun.jna.Platform;

public class PortablePorcelainDemo {
	public static void main(String[] args) throws Exception {
		if (Platform.isWindows()) {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}

		final JCheckBox enableMultiSelect = new JCheckBox();
		final JComboBox<JnaFileChooser.Mode> selectionMode = new JComboBox<JnaFileChooser.Mode>(JnaFileChooser.Mode.values());
		final JComboBox<String> dialogType = new JComboBox<String>(new String[] { "Open", "Save" });
		final JCheckBox useFilter = new JCheckBox();
		final JButton choose = new JButton("Choose");
		final JFrame frame = new JFrame(PortablePorcelainDemo.class.getName());

		choose.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				final JnaFileChooser fc = new JnaFileChooser();
				fc.setTitle("Portable");
				fc.setMultiSelectionEnabled(enableMultiSelect.isSelected());
				fc.setMode((JnaFileChooser.Mode)selectionMode.getSelectedItem());
				if (useFilter.isSelected()) {
					fc.addFilter("All Files", "*");
					fc.addFilter("Pictures", "jpg", "jpeg", "gif", "png", "bmp");
					fc.addFilter("Text files", "txt", "log", "xml");
				}
				boolean result = false;
				if (dialogType.getSelectedItem().equals("Open")) {
					result = fc.showOpenDialog(frame);
				}
				else {
					result = fc.showSaveDialog(frame);
				}
				if (result) {
					JOptionPane.showMessageDialog(frame, fc.getSelectedFiles(),
						"Selection", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		final JPanel content = new JPanel(new MigLayout("", "[right][left]"));

		content.add(new JLabel("Enable multiselect:"));
		content.add(enableMultiSelect, "wrap");
		content.add(new JLabel("Use filter:"));
		content.add(useFilter, "wrap");
		content.add(new JLabel("Selection mode:"));
		content.add(selectionMode, "wrap");
		content.add(new JLabel("Dialog type:"));
		content.add(dialogType, "wrap");
		content.add(choose, "span 2, gaptop 8, align center");

		frame.setContentPane(content);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
