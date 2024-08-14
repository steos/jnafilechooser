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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jnafilechooser.win32.Comdlg32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.WString;

/**
 * The native Windows file chooser dialog.
 *
 * Example:
 * WindowsFileChooser fc = new WindowsFileChooser("C:\\");
 * fc.addFilter("All Files", "*");
 * fc.addFilter("Text files", "txt", "log", "xml", "css", "html");
 * fc.addFilter("Source code", "java", "c", "cpp", "cc", "h", "hpp");
 * fc.addFilter("Binary files", "exe", "class", "jar", "dll", "so");
 * if (fc.showOpenDialog(parent)) {
 *     File f = fc.getSelectedFile();
 *     // do something with f
 * }
 *
 * Note that although you can set the initial directory Windows will
 * determine the initial directory according to the following rules
 * (the initial directory is referred to as "lpstrInitialDir"):
 *
 * Windows 7:
 * 1. If lpstrInitialDir has the same value as was passed the first time the
 *    application used an Open or Save As dialog box, the path most recently
 *    selected by the user is used as the initial directory.
 * 2. Otherwise, if lpstrFile contains a path, that path is the initial
 *    directory.
 * 3. Otherwise, if lpstrInitialDir is not NULL, it specifies the initial
 *    directory.
 * 4. If lpstrInitialDir is NULL and the current directory contains any files of
 *    the specified filter types, the initial directory is the current
 *    directory.
 * 5. Otherwise, the initial directory is the personal files directory of the
 *    current user.
 * 6. Otherwise, the initial directory is the Desktop folder.
 *
 * Windows 2000/XP/Vista:
 * 1. If lpstrFile contains a path, that path is the initial directory.
 * 2. Otherwise, lpstrInitialDir specifies the initial directory.
 * 3. Otherwise, if the application has used an Open or Save As dialog box in
 *    the past, the path most recently used is selected as the initial
 *    directory. However, if an application is not run for a long time, its
 *    saved selected path is discarded.
 * 4. If lpstrInitialDir is NULL and the current directory contains any files
 *    of the specified filter types, the initial directory is the current
 *    directory.
 * 5. Otherwise, the initial directory is the personal files directory of the
 *    current user.
 * 6. Otherwise, the initial directory is the Desktop folder.
 *
 * Therefore you probably want to use an exe wrapper like WinRun4J in order
 * for this to work properly on Windows 7. Otherwise multiple programs may
 * interfere with each other. Unfortunately there doesn't seem to be a way
 * to override this behaviour.
 *
 * {@link http://msdn.microsoft.com/en-us/library/ms646839.aspx}
 * {@link http://winrun4j.sourceforge.net/}
 */
public class WindowsFileChooser
{
    public static final int MAX_PATH = 260;
	protected File selectedFile;
	protected File currentDirectory;
	protected ArrayList<String[]> filters;

	protected String defaultFilename = "";
	protected String dialogTitle = "";

    private int filterIndex = 1;
    private boolean addToRecent = false;
    private boolean multipleSelection = false;
    private int maxNumberOfFiles = 10000;
    private File[] selectedFiles = null;

	/**
	 * creates a new file chooser
	 */
	public WindowsFileChooser() {
		filters = new ArrayList<>();
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * If the given file is not a directory the parent file will be used instead.
	 *
	 * @param currentDirectory the initial directory
	 */
	public WindowsFileChooser(File currentDirectory) {
		filters = new ArrayList<>();
		if (currentDirectory != null) {
			this.currentDirectory = currentDirectory.isDirectory() ?
				currentDirectory : currentDirectory.getParentFile();
		}
	}

	/**
	 * creates a new file chooser with the specified initial directory path
	 *
	 * @param currentDirectoryPath the initial directory path; may be null
	 */
	public WindowsFileChooser(String currentDirectoryPath) {
		this(currentDirectoryPath != null ?
			new File(currentDirectoryPath) : null);
	}

	// this is a package private method used by the JnaFileChooser
	// facade to directly set the filter list
	void setFilters(ArrayList<String[]> filters) {
		this.filters = filters;
	}

	/**
	 * add a filter to the user-selectable list of file filters
	 *
	 * @param name name of the filter
	 * @param filter you must pass at least 1 argument, the arguments
	 *               are the file extensions.
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
	 * set a title name
	 *
	 * @param title of dialog
	 * 
	 */
	public void setTitle(String title) {
		this.dialogTitle = title;
	}

	/**
	 * show the dialog for opening a file
	 *
	 * @param parent the parent window of the dialog
	 *
	 * @return true if the user clicked ok, false otherwise
	 */
	public boolean showOpenDialog(Window parent) {
		return showDialog(parent, true);
	}

	/**
	 * show the dialog for saving a file
	 *
	 * @param parent the parent window of the dialog
	 *
	 * @return true if the user clicked ok, false otherwise
	 */
	public boolean showSaveDialog(Window parent) {
		return showDialog(parent, false);
	}

	/*
	 * shows the dialog
	 *
	 * @param parent the parent window
	 * @param open whether to show the open dialog, if false save dialog is shown
	 *
	 * @return true if the user clicked ok, false otherwise
	 */
	boolean showDialog(Window parent, boolean open) {
		final Comdlg32.OpenFileName params = new Comdlg32.OpenFileName();
		params.Flags =
			// use explorer-style interface
			Comdlg32.OFN_EXPLORER
			// the dialog changes the current directory when browsing,
			// this flag causes the original value to be restored after the
			// dialog returns
			| Comdlg32.OFN_NOCHANGEDIR
			// disable "open as read-only" feature
			| Comdlg32.OFN_HIDEREADONLY
			// enable resizing of the dialog
			| Comdlg32.OFN_ENABLESIZING;

		params.hwndOwner = parent == null ? null : Native.getWindowPointer(parent);

        if (!addToRecent) {
            params.Flags = params.Flags | Comdlg32.OFN_DONTADDTORECENT;
        }

        if (multipleSelection) {
            params.Flags = params.Flags | Comdlg32.OFN_ALLOWMULTISELECT;
        }

		// lpstrFile contains the selection path after the dialog
		// returns. It must be big enough for the path to fit or
		// GetOpenFileName returns an error (FNERR_BUFFERTOOSMALL).
		// MAX_PATH is 260 so 4*260+1 bytes should be big enough (I hope...)
		// http://msdn.microsoft.com/en-us/library/aa365247.aspx#maxpath
		final int bufferLength = multipleSelection ? maxNumberOfFiles * MAX_PATH : MAX_PATH;
		// 4 bytes per char + 1 null byte
		final int bufferSize = 4 * bufferLength + 1;
		params.lpstrFile = new Memory(bufferSize);

		if (!defaultFilename.isEmpty()) {
			params.lpstrFile.setWideString(0, defaultFilename);
		} else {
		    params.lpstrFile.clear(bufferSize);
		}
		if (!dialogTitle.isEmpty()) {
			params.lpstrTitle = new WString(dialogTitle);
		}

		// nMaxFile
		// http://msdn.microsoft.com/en-us/library/ms646839.aspx:
		// "The size, in characters, of the buffer pointed to by
		// lpstrFile. The buffer must be large enough to store the
		// path and file name string or strings, including the
		// terminating NULL character."

		// Therefore because we're using the unicode version of the
		// API the nMaxFile value must be 1/4 of the lpstrFile
		// buffer size plus one for the terminating null byte.
		params.nMaxFile = bufferLength;

		if (currentDirectory != null) {
			params.lpstrInitialDir = new WString(currentDirectory.getAbsolutePath());
		}

		// build filter string if filters were specified
		if (!filters.isEmpty()) {
			params.lpstrFilter = new WString(buildFilterString());
			params.nFilterIndex = filterIndex;
		}

		final boolean approved = open ?
			Comdlg32.GetOpenFileNameW(params) :
			Comdlg32.GetSaveFileNameW(params);

        // clear selection
        selectedFiles = null;

        if (approved) {
			// nFilterIndex is updated if user changed the selected filter
			filterIndex = params.nFilterIndex;

            if (multipleSelection) {
                final byte[] bytes = params.lpstrFile.getByteArray(0, bufferSize);
                final List<String> filePaths = bytesToFilePaths(bytes);

                if (filePaths.size() == 1) {
                    selectedFile = new File(filePaths.get(0));
                    currentDirectory = selectedFile.getParentFile();
                    selectedFiles = new File[1];
                    selectedFiles[0] = selectedFile;
                } else if (filePaths.size() > 1) {
                    selectedFiles = new File[filePaths.size() - 1];
                    currentDirectory = new File(filePaths.get(0));
                    for (int i = 1; i < filePaths.size(); i++) {
                        selectedFiles[i - 1] = new File(currentDirectory, filePaths.get(i));
                    }
                    selectedFile = selectedFiles[0];
                }
            } else {
                final String filePath = params.lpstrFile.getWideString(0);

                selectedFile = new File(filePath);
                currentDirectory = selectedFile.getParentFile();

                selectedFiles = new File[1];
                selectedFiles[0] = selectedFile;
            }
        }
		else {
			final int errCode = Comdlg32.CommDlgExtendedError();
			// if the code is 0 the user clicked cancel
			if (errCode != 0) {
				throw new RuntimeException(
					"GetOpenFileName failed with error " + errCode);
			}
		}
		return approved;
	}

	/*
	 * builds a filter string
	 *
	 * from MSDN:
	 * A buffer containing pairs of null-terminated filter strings. The last
	 * string in the buffer must be terminated by two NULL characters.
	 *
	 * The first string in each pair is a display string that describes the
	 * filter (for example, "Text Files"), and the second string specifies the
	 * filter pattern (for example, "*.TXT"). To specify multiple filter
	 * patterns for a single display string, use a semicolon to separate the
	 * patterns (for example, "*.TXT;*.DOC;*.BAK").
	 *
	 * http://msdn.microsoft.com/en-us/library/ms646839.aspx
	 */
	private String buildFilterString() {
		final StringBuilder filterStr = new StringBuilder();
		for (final String[] spec : filters) {
			final String label = spec[0];
			// add label and terminate with null byte
			filterStr.append(label);
			filterStr.append('\0');
			// build file extension patterns separated by a
			// semicolon and terminated by a null byte
			for (int i = 1; i < spec.length; ++i) {
				filterStr.append("*.");
				filterStr.append(spec[i]);
				filterStr.append(';');
			}
			// remove last superfluous ";" and add terminator
			filterStr.deleteCharAt(filterStr.length() - 1);
			filterStr.append('\0');
		}
		// final terminator
		filterStr.append('\0');
		return filterStr.toString();
	}

	/**
	 * returns the file selected by the user
	 *
	 * @return the selected file; null if the dialog was canceled or never shown
	 */
	public File getSelectedFile() {
		return selectedFile;
	}

	/**
	 * returns the current directory
	 *
	 * This is always the parent directory of the chosen file, even if you
	 * enter an absolute path to a file that doesn't exist in the current
	 * directory.
	 *
	 * @return the current directory
	 */
	public File getCurrentDirectory() {
		return currentDirectory;
	}

    public void setDefaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }

	public int getFilterIndex() {
		return filterIndex;
	}

	public void setFilterIndex(int filterIndex) {
		this.filterIndex = filterIndex;
	}

	public boolean isAddToRecent() {
		return addToRecent;
	}

	public void setAddToRecent(boolean addToRecent) {
		this.addToRecent = addToRecent;
	}

	public boolean isMultipleSelection() {
		return multipleSelection;
	}

	public void setMultiSelectionEnabled(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
	}

	public int getMaxNumberOfFiles() {
		return maxNumberOfFiles;
	}

	public void setMaxNumberOfFiles(int maxNumberOfFiles) {
		this.maxNumberOfFiles = maxNumberOfFiles;
	}

	public File[] getSelectedFiles() {
		return selectedFiles;
	}

	public static List<String> bytesToFilePaths(byte[] bytes) {
        final List<String> filePaths = new ArrayList<>();
        int from = 0;
        for (int i = 0; i < bytes.length - 1; i += 2) {
            if (bytes[i] == 0 && bytes[i + 1] == 0) {
                if (i > from) {
                    filePaths.add(new String(Arrays.copyOfRange(bytes, from, i),
                            StandardCharsets.UTF_16LE)); //UTF 2-bytes little-endian CharSet seems to work...
                    from = i + 2;
                } else {
                    break;
                }
            }
        }
        return filePaths;
    }
}
