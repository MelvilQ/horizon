package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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

	private String dataPath;

	private String lang;
	private String genre;
	private String folder;
	private String chapter;

	private JComboBox<String> languageSelector;

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
	private JButton importEpubButton = new JButton("Import EPUB");

	private MainWindow parent;

	public TextSelector(MainWindow mainWindow) {
		super(BoxLayout.Y_AXIS);
		parent = mainWindow;

		dataPath = mainWindow.getSettings().getSetting("data_path");
		if (dataPath == null)
			dataPath = "data";

		ArrayList<String> languages = new ArrayList<String>();
		for (File langFolder : new File(dataPath).listFiles()) {
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
		genrePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		folderPane.setPreferredSize(new Dimension(200, 250));
		folderPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chapterPane.setPreferredSize(new Dimension(200, 500));
		chapterPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(genrePane);
		add(folderPane);
		add(chapterPane);

		newTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// calculate default for filename
				String defaultFilename = "";
				File defaultFolder = new File(dataPath + "/" + lang + "/"
						+ genre + "/" + folder + "/01.txt");
				if (!genre.equals("") && !folder.equals("")
						&& defaultFolder.exists()) {
					int i = 1;
					while (true) {
						defaultFilename = String.format("%02d", i);
						if (!new File(dataPath + "/" + lang + "/" + genre + "/"
								+ folder + "/" + defaultFilename + ".txt")
								.exists()) {
							break;
						}
						i += 1;
					}
				}
				// show dialog
				NewTextDialog dialog = new NewTextDialog(parent, genre, folder,
						defaultFilename);
				if (dialog.wasCanceled())
					return;
				// create folders if they don't exist
				File dir = new File(dataPath + "/" + lang + "/"
						+ dialog.getGenre() + "/" + dialog.getFolder());
				if (!dir.exists()) {
					dir.mkdirs();
				}
				// parse text
				String parsedText = TextParser.parseText(dialog.getText());
				// save text file
				File textFile = new File(dataPath + "/" + lang + "/"
						+ dialog.getGenre() + "/" + dialog.getFolder() + "/"
						+ dialog.getFileName() + ".txt");
				try {
					FileUtils.writeStringToFile(textFile, parsedText,
							Charset.forName("UTF-8"));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				// update listviews
				applyListViewSelection(dialog.getGenre(), dialog.getFolder(),
						dialog.getFileName());
				// show new text
				parent.notifyLoadText(textFile, genre + "/" + folder + "/"
						+ chapter);
			}
		});
		importEpubButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show dialog
				ImportEpubDialog dialog = new ImportEpubDialog(mainWindow,
						genre);
				if (dialog.wasCanceled())
					return;
				// create folders if they don't exist
				File dir = new File(dataPath + "/" + lang + "/"
						+ dialog.getGenre() + "/" + dialog.getFolder());
				if (!dir.exists()) {
					dir.mkdirs();
				}
				// for each chapter
				List<String> chapterTitles = dialog.getChapterTitles();
				List<String> chapterTexts = dialog.getChapterTexts();
				for (int i = 0; i < chapterTitles.size(); ++i) {
					// parse text
					String parsedText = TextParser.parseText(chapterTexts
							.get(i));
					// save text file
					File textFile = new File(dataPath + "/" + lang + "/"
							+ dialog.getGenre() + "/" + dialog.getFolder() + "/"
							+ chapterTitles.get(i) + ".txt");
					try {
						FileUtils.writeStringToFile(textFile, parsedText,
								Charset.forName("UTF-8"));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				// update listviews
				String firstChapter = chapterTitles.get(0);
				applyListViewSelection(dialog.getGenre(), dialog.getFolder(),
						firstChapter);
				// show new text
				parent.notifyLoadText(new File(dataPath + "/" + lang + "/"
						+ genre + "/" + folder + "/" + chapter + ".txt"), genre
						+ "/" + folder + "/" + chapter);
			}
		});
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		buttonBox.add(newTextButton);
		buttonBox.add(importEpubButton);
		add(buttonBox);

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
				populateModelWithDirContent(folderModel, dataPath + "/" + lang
						+ "/" + genre, true);
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
				populateModelWithDirContent(chapterModel, dataPath + "/" + lang
						+ "/" + genre + "/" + folder, false);
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
				parent.notifyLoadText(new File(dataPath + "/" + lang + "/"
						+ genre + "/" + folder + "/" + chapter + ".txt"), genre
						+ "/" + folder + "/" + chapter);
			}
		});
	}

	public void setLanguage(String lang) {
		this.lang = lang;
		// to add "default" to the selector
		if(languageSelector.getItemCount() == 0)
			languageSelector.addItem(lang);
		// select the language in the dropdown
		languageSelector.setSelectedItem(lang);
		// display folders etc.
		populateModelWithDirContent(genreModel, dataPath + "/" + lang, true);
	}

	public void selectLastText() {
		String lastText = parent.getSettings().getSetting("current_text");
		if (lastText == null)
			return;
		File lastTextFile = new File(dataPath + "/" + lang + "/" + lastText
				+ ".txt");
		if (!lastTextFile.exists())
			return;
		String[] genreFolderChapter = lastText.split("/");
		applyListViewSelection(genreFolderChapter[0], genreFolderChapter[1],
				genreFolderChapter[2]);
		parent.notifyLoadText(lastTextFile, lastText);
	}

	private void applyListViewSelection(String genre, String folder,
			String chapter) {
		this.genre = genre;
		this.folder = folder;
		this.chapter = chapter;
		disableListeners();
		populateModelWithDirContent(genreModel, dataPath + "/" + lang, true);
		genreList.setSelectedValue(genre, true);
		populateModelWithDirContent(folderModel, dataPath + "/" + lang + "/"
				+ genre, true);
		folderList.setSelectedValue(folder, true);
		populateModelWithDirContent(chapterModel, dataPath + "/" + lang + "/"
				+ genre + "/" + folder, false);
		chapterList.setSelectedValue(chapter, true);
		enableListeners();
	}
}
