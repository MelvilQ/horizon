package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

import de.melvil.horizon.core.HorizonSettings;
import de.melvil.horizon.core.TextConverter;
import de.melvil.horizon.core.WordManager;

@SuppressWarnings("serial")
public class LibraryExportDialog extends JDialog {

	private HorizonSettings settings;
	private WordManager wordManager;
	private TextConverter textConverter;

	private JTextField exportDirInput = new JTextField(100);
	private JButton chooseButton = new JButton("Choose");
	private JButton cancelButton = new JButton("Cancel");
	private JButton okButton = new JButton("Export");

	public LibraryExportDialog(MainWindow parent, HorizonSettings settings, WordManager wordManager) {
		super(parent, "Library Export", ModalityType.APPLICATION_MODAL);
		this.settings = settings;
		this.wordManager = wordManager;
		this.textConverter = new TextConverter();

		setSize(600, 170);
		setLocation(200, 100);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
		getContentPane().add(new JLabel("Export Directory: "));
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dirChooser = new JFileChooser();
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int success = dirChooser.showOpenDialog(parent);
				if (success != JFileChooser.APPROVE_OPTION)
					return;
				exportDirInput.setText(dirChooser.getSelectedFile().getAbsolutePath());
			}
		});
		Box exportDirInputBox = new Box(BoxLayout.X_AXIS);
		exportDirInput.setMaximumSize(new Dimension(2000, 30));
		exportDirInputBox.add(exportDirInput);
		exportDirInputBox.add(chooseButton);
		getContentPane().add(exportDirInputBox);
		
		getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean success = doExport();
				if (!success)
					return;
				setVisible(false);
				dispose();
			}
		});
		Box buttonsBox = new Box(BoxLayout.X_AXIS);
		buttonsBox.add(cancelButton);
		buttonsBox.add(okButton);
		getContentPane().add(buttonsBox);

		String exportDir = settings.getSetting("default_export_dir");
		if (exportDir == null || !new File(exportDir).exists())
			exportDir = "";
		exportDirInput.setText(exportDir);

		setVisible(true);
	}

	private boolean doExport() {
		String lang = settings.getSetting("current_lang");
		String sourceDir = settings.getSetting("data_path");
		if (sourceDir == null) {
			sourceDir = "data";
		}
		sourceDir += "/" + lang;
		File sourceDirFile = new File(sourceDir);
		if (!sourceDirFile.exists()) {
			JOptionPane.showMessageDialog(this, "Could not find data directory.");
			return false;
		}

		String exportDir = exportDirInput.getText();
		File exportDirFile = new File(exportDir);
		if (!exportDirFile.exists() || !exportDirFile.isDirectory()) {
			JOptionPane.showMessageDialog(this, "Invalid export directory. Please choose another one.");
			return false;
		}
		try {
			FileUtils.cleanDirectory(exportDirFile);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Export was not successful. Could not empty the export directory.");
			return false;
		}

		List<String> errorLog = new ArrayList<String>();

		copyDirectory(sourceDirFile, exportDirFile, errorLog);

		if (!errorLog.isEmpty()) {
			StringBuilder sb = new StringBuilder(256);
			sb.append("The following errors have been logged during the export: ");
			sb.append(System.lineSeparator());
			for (String errorMsg : errorLog) {
				sb.append(errorMsg);
			}
			JOptionPane.showMessageDialog(this, sb.toString());
			return false;
		}

		settings.setSetting("default_export_dir", exportDir);
		return true;
	}

	private void copyDirectory(File srcDir, File targetDir, List<String> errorLog) {
		for (File file : srcDir.listFiles()) {
			if (!file.isDirectory() && file.getName().endsWith(".txt")) {
				// generate html file
				try {
					String text = FileUtils.readFileToString(file);
					String html = textConverter.createAnnotatedMarkup(text, wordManager);
					String htmlFilePath = targetDir.getAbsolutePath() + "/"
							+ file.getName().replaceAll(".txt", ".html");
					File htmlFile = new File(htmlFilePath);
					FileUtils.writeStringToFile(htmlFile, html);
				} catch (IOException e) {
					errorLog.add("The text file " + file.getAbsolutePath() + "could not be copied.");
				}
			} else if (!file.isDirectory() && file.getName().endsWith(".mp3")) {
				// copy audio file
				try {
					FileUtils.copyFileToDirectory(file, targetDir);
				} catch (IOException e) {
					errorLog.add("The mp3 file " + file.getAbsolutePath() + " could not be copied.");
				}
			} else if (file.isDirectory()) {
				// create a new directory in the target directory
				String newDirPath = targetDir.getAbsolutePath() + "/" + file.getName();
				File newDir = new File(newDirPath);
				boolean success = newDir.mkdirs();
				if (!success) {
					errorLog.add("The directory " + file.getAbsolutePath() + " could not be copied.");
					continue;
				}
				// copy the source folder contents into the target directory
				copyDirectory(file, newDir, errorLog);
			}
		}
	}

}
