package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;

import de.melvil.horizon.core.TextParser;

@SuppressWarnings("serial")
public class TextSelector extends Box {

	private String lang;
	private String genre;
	private String folder;
	private String chapter;

	private JComboBox<String> languageSelector;;

	private DefaultListModel<String> genreModel = new DefaultListModel<String>();
	private JList<String> genreList = new JList<String>(genreModel);
	private JScrollPane genrePane = new JScrollPane(genreList);

	private DefaultListModel<String> folderModel = new DefaultListModel<String>();
	private JList<String> folderList = new JList<String>(folderModel);
	private JScrollPane folderPane = new JScrollPane(folderList);

	private DefaultListModel<String> chapterModel = new DefaultListModel<String>();
	private JList<String> chapterList = new JList<String>(chapterModel);
	private JScrollPane chapterPane = new JScrollPane(chapterList);

	private JButton newTextButton = new JButton("New Text");

	private MainWindow parent;

	public TextSelector(MainWindow mainWindow) {
		super(BoxLayout.Y_AXIS);
		parent = mainWindow;

		ArrayList<String> languages = new ArrayList<String>();
		for (File langFolder : new File("data").listFiles()) {
			if (langFolder.isDirectory())
				languages.add(langFolder.getName());
		}
		languageSelector = new JComboBox<String>(
				languages.toArray(new String[0]));
		languageSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.notifyLanguageChange((String) languageSelector
						.getSelectedItem());
			}
		});
		add(languageSelector);

		genrePane.setMinimumSize(new Dimension(200, 100));
		genrePane.setMaximumSize(new Dimension(200, 100));
		folderPane.setPreferredSize(new Dimension(200, 250));
		chapterPane.setPreferredSize(new Dimension(200, 500));
		add(genrePane);
		add(folderPane);
		add(chapterPane);
		add(newTextButton);

		genreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chapterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		enableListeners();
	}

	private void populateModelWithDirContent(DefaultListModel<String> model,
			String dirName, boolean dirMode) {
		model.clear();
		File dir = new File(dirName);
		for (File f : dir.listFiles()) {
			if (dirMode && f.isDirectory())
				model.addElement(f.getName());
			else if (!dirMode && f.getName().endsWith(".txt"))
				model.addElement(f.getName().replace(".txt", ""));
		}
	}

	private void disableListeners() {
		for (ListSelectionListener l : genreList.getListSelectionListeners()) {
			genreList.removeListSelectionListener(l);
		}
		for (ListSelectionListener l : folderList.getListSelectionListeners()) {
			folderList.removeListSelectionListener(l);
		}
		for (ListSelectionListener l : chapterList.getListSelectionListeners()) {
			chapterList.removeListSelectionListener(l);
		}
	}

	private void enableListeners() {
		genreList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				genre = genreList.getSelectedValue();
				folderModel.clear();
				chapterModel.clear();
				folder = null;
				chapter = null;
				if (genre == null)
					return;
				populateModelWithDirContent(folderModel, "data/" + lang + "/"
						+ genre, true);
			}
		});

		folderList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				folder = folderList.getSelectedValue();
				chapterModel.clear();
				chapter = null;
				if (folder == null)
					return;
				populateModelWithDirContent(chapterModel, "data/" + lang + "/"
						+ genre + "/" + folder, false);
			}
		});

		chapterList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				chapter = chapterList.getSelectedValue();
				if (chapter == null)
					return;
				parent.notifyLoadText(new File("data/" + lang + "/" + genre
						+ "/" + folder + "/" + chapter + ".txt"));
			}
		});

		newTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewTextDialog dialog = new NewTextDialog(parent, genre, folder);
				if (dialog.wasCanceled())
					return;
				// create folders if they don't exist
				File dir = new File("data/" + lang + "/" + dialog.getGenre()
						+ "/" + dialog.getFolder());
				if (!dir.exists()) {
					dir.mkdirs();
				}
				// parse text
				String parsedText = TextParser.parseText(dialog.getText());
				// save text file
				File textFile = new File("data/" + lang + "/"
						+ dialog.getGenre() + "/" + dialog.getFolder() + "/"
						+ dialog.getFileName() + ".txt");
				try {
					FileUtils.writeStringToFile(textFile, parsedText,
							Charset.forName("UTF-8"));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				// update listviews
				genre = dialog.getGenre();
				folder = dialog.getFolder();
				chapter = dialog.getFileName();
				disableListeners();
				populateModelWithDirContent(genreModel, "data/" + lang, true);
				populateModelWithDirContent(folderModel, "data/" + lang + "/"
						+ genre, true);
				populateModelWithDirContent(chapterModel, "data/" + lang + "/"
						+ genre + "/" + folder, false);
				enableListeners();
				// show new text
				parent.notifyLoadText(textFile);
			}
		});
	}

	public void setLanguage(String lang) {
		this.lang = lang;
		languageSelector.setSelectedItem(lang);
		populateModelWithDirContent(genreModel, "data/" + lang, true);
	}
}
