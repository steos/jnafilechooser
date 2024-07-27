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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.jna.Platform;

/**
 * JnaFileChooser is a wrapper around the native Windows file chooser
 * and folder browser that falls back to the Swing JFileChooser on platforms
 * other than Windows or if the user chooses a combination of features
 * that are not supported by the native dialogs (for example multiple
 * selection of directories).
 *
 * Example:
 * JnaFileChooser fc = new JnaFileChooser();
 * fc.setFilter("All Files", "*");
 * fc.setFilter("Pictures", "jpg", "jpeg", "gif", "png", "bmp");
 * fc.setMultiSelectionEnabled(true);
 * fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
 * if (fc.showOpenDialog(parent)) {
 *     Files[] selected = fc.getSelectedFiles();
 *     // do something with selected
 * }
 *
 * @see JFileChooser, WindowsFileChooser, WindowsFileBrowser
 */
public class JnaFileChooser
{
	private enum Action { Open, Save }

	/**
	 * the available selection modes of the dialog
	 */
	public enum Mode {
		Files(JFileChooser.FILES_ONLY),
		Directories(JFileChooser.DIRECTORIES_ONLY),
		FilesAndDirectories(JFileChooser.FILES_AND_DIRECTORIES);
		private final int jFileChooserValue;
		Mode(int jfcv) {
			this.jFileChooserValue = jfcv;
		}
		public int getJFileChooserValue() {
			return jFileChooserValue;
		}
	}

	protected File[] selectedFiles;
	protected File currentDirectory;
	protected ArrayList<String[]> filters;
	protected boolean multiSelectionEnabled;
	protected Mode mode;

	protected String defaultFile;
    protected String dialogTitle;
    protected String openButtonText;
    protected String saveButtonText;

	/**
	 * creates a new file chooser with multiselection disabled and mode set
	 * to allow file selection only.
	 */
	public JnaFileChooser() {
		filters = new ArrayList<>();
		multiSelectionEnabled = false;
		mode = Mode.Files;
		selectedFiles = new File[] { null };

		defaultFile = "";
        dialogTitle = "";
        openButtonText = "";
        saveButtonText = "";
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * @param currentDirectory the initial directory
	 */
	public JnaFileChooser(File currentDirectory) {
		this();
        if (currentDirectory != null) {
			this.currentDirectory = currentDirectory.isDirectory() ?
				currentDirectory : currentDirectory.getParentFile();
		}
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * @param currentDirectoryPath the initial directory
	 */
	public JnaFileChooser(String currentDirectoryPath) {
		this(currentDirectoryPath != null ?
			new File(currentDirectoryPath) : null);
	}

	/**
	 * shows a dialog for opening files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	public boolean showOpenDialog(Window parent) {
		return showDialog(parent, Action.Open);
	}

	/**
	 * shows a dialog for saving files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	public boolean showSaveDialog(Window parent) {
		return showDialog(parent, Action.Save);
	}

	private boolean showDialog(Window parent, Action action) {
		if (Platform.isWindows()) {
			if (mode == Mode.Files) {
				return showWindowsFileChooser(parent, action);
			}
			else if (mode == Mode.Directories) {
				return showWindowsFolderBrowser(parent);
			}
		}

		// fallback to Swing
		return showSwingFileChooser(parent, action);
	}

	private boolean showSwingFileChooser(Window parent, Action action) {
		final JFileChooser fc = new JFileChooser(currentDirectory);
		fc.setMultiSelectionEnabled(multiSelectionEnabled);
		fc.setFileSelectionMode(mode.getJFileChooserValue());

		// set select file
		if (!defaultFile.isEmpty() & action == Action.Save) {
			File fsel = new File(defaultFile);
			fc.setSelectedFile(fsel);
		}
		if (!dialogTitle.isEmpty()) {
			fc.setDialogTitle(dialogTitle);
		}
		if (action == Action.Open & !openButtonText.isEmpty()) {
			fc.setApproveButtonText(openButtonText);
		} else if (action == Action.Save & !saveButtonText.isEmpty()) {
			fc.setApproveButtonText(saveButtonText);
		}

		// build filters
		if (!filters.isEmpty()) {
			boolean useAcceptAllFilter = false;
			for (final String[] spec : filters) {
				// the "All Files" filter is handled specially by JFileChooser
				if (spec[1].equals("*")) {
					useAcceptAllFilter = true;
					continue;
				}
				fc.addChoosableFileFilter(new FileNameExtensionFilter(
					spec[0], Arrays.copyOfRange(spec, 1, spec.length)));
			}
			fc.setAcceptAllFileFilterUsed(useAcceptAllFilter);
		}

		int result;
		if (action == Action.Open) {
			result = fc.showOpenDialog(parent);
		}
		else {
			if (saveButtonText.isEmpty()) {
				result = fc.showSaveDialog(parent);
            }
			else {
				result = fc.showDialog(parent, null);
            }
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFiles = multiSelectionEnabled ?
				fc.getSelectedFiles() : new File[] { fc.getSelectedFile() };
			currentDirectory = fc.getCurrentDirectory();
			return true;
		}

		return false;
	}

	private boolean showWindowsFileChooser(Window parent, Action action) {
		final WindowsFileChooser fc = new WindowsFileChooser(currentDirectory);
		fc.setFilters(filters);
		fc.setMultiSelectionEnabled(multiSelectionEnabled);
		if (!defaultFile.isEmpty())
			fc.setDefaultFilename(defaultFile);

		if (!dialogTitle.isEmpty()) {
			fc.setTitle(dialogTitle);
		}

		final boolean result = fc.showDialog(parent, action == Action.Open);
		if (result) {
            selectedFiles = multiSelectionEnabled ? fc.getSelectedFiles() : new File[]{fc.getSelectedFile()};
			currentDirectory = fc.getCurrentDirectory();
		}
		return result;
	}

	private boolean showWindowsFolderBrowser(Window parent) {
		final WindowsFolderBrowser fb = new WindowsFolderBrowser();
		if (!dialogTitle.isEmpty()) {
			fb.setTitle(dialogTitle);
		}
		final File file = fb.showDialog(parent);
		if (file != null) {
			selectedFiles = new File[] { file };
			currentDirectory = file.getParentFile() != null ?
				file.getParentFile() : file;
			return true;
		}

		return false;
	}

	/**
	 * add a filter to the user-selectable list of file filters
	 *
     * @param name   name of the filter
     * @param filter you must pass at least 1 argument, the arguments are the file
     *               extensions.
	 */
	public void addFilter(String name, String... filter) {
		if (filter.length < 1) {
			throw new IllegalArgumentException();
		}
		ArrayList<String> parts = new ArrayList<>();
		parts.add(name);
		Collections.addAll(parts, filter);
		filters.add(parts.toArray(new String[0]));
	}

	/**
	 * sets the selection mode
	 *
	 * @param mode the selection mode
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public void setCurrentDirectory(String currentDirectoryPath) {
		this.currentDirectory = (currentDirectoryPath != null ? new File(currentDirectoryPath) : null);
	}

	/**
	 * sets whether to enable multiselection
	 *
	 * @param enabled true to enable multiselection, false to disable it
	 */
	public void setMultiSelectionEnabled(boolean enabled) {
		this.multiSelectionEnabled = enabled;
	}

	public boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

	public void setDefaultFileName(String dfile) {
		this.defaultFile = dfile;
	}

	/**
	 * set a title name
	 *
	 * @param title of dialog
	 *
	 */
	public void setTitle(String title) {
		this.dialogTitle = title;
	}

	/**
	 * set an open button name
	 *
	 * @param buttonText button text
	 *
	 */
	public void setOpenButtonText(String buttonText) {
		this.openButtonText = buttonText;
	}

	/**
	 * set a save button name
	 *
	 * @param buttonText button text
	 *
	 */
	public void setSaveButtonText(String buttonText) {
		this.saveButtonText = buttonText;
	}

	public File[] getSelectedFiles() {
		return selectedFiles;
	}

	public File getSelectedFile() {
		return selectedFiles[0];
	}

	public File getCurrentDirectory() {
		return currentDirectory;
	}
}
