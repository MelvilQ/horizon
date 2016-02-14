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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.melvil.horizon.core.EpubImporter;

@SuppressWarnings("serial")
public class ImportEpubDialog extends JDialog {

	class ChapterTableModel extends AbstractTableModel {
		private List<String> chapterTitles;
		private List<String> chapterTexts;
		private List<Boolean> chapterSelected;

		public ChapterTableModel(List<String> chapterTexts) {
			this.chapterTexts = chapterTexts;
			chapterTitles = new ArrayList<String>();
			chapterSelected = new ArrayList<Boolean>();
			for (int i = 0; i < chapterTexts.size(); ++i) {
				chapterTitles.add(String.format("%02d", i));
				chapterSelected.add(true);
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Selected";
			else
				return "Title";
		}

		@Override
		public int getRowCount() {
			return chapterTitles.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0)
				return chapterSelected.get(row);
			else
				return chapterTitles.get(row);
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Class getColumnClass(int column) {
			if (column == 0)
				return Boolean.class;
			else
				return String.class;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 0) {
				chapterSelected.set(row, (Boolean) value);
			} else {
				String v = (String) value;
				chapterTitles.set(row, v);
				// auto-renaming of numbered chapters
				if (v.equals("01")) {
					for (int r = row + 1; r < chapterTitles.size(); ++r) {
						String vn = String.format("%02d", r - row + 1);
						setValueAt(vn, r, 1);
					}
					revalidate();
					repaint();
				}
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return true;
		}

		public void setCapterText(int i, String text) {
			chapterTexts.set(i, text);
		}

		public String getChapterText(int i) {
			return chapterTexts.get(i);
		}

		public List<String> getSelectedChapters() {
			List<String> selectedChapters = new ArrayList<String>();
			for (int i = 0; i < chapterTitles.size(); ++i) {
				if (chapterSelected.get(i))
					selectedChapters.add(chapterTitles.get(i));
			}
			return selectedChapters;
		}

		public List<String> getSelectedTexts() {
			List<String> selectedTexts = new ArrayList<String>();
			for (int i = 0; i < chapterTexts.size(); ++i) {
				if (chapterSelected.get(i))
					selectedTexts.add(chapterTexts.get(i));
			}
			return selectedTexts;
		}
	}

	private boolean canceled = true;

	private JButton fileChoosingButton = new JButton("Choose File...");
	private JLabel fileChoosingLabel = new JLabel("No file selected.");
	private JTextField genreInput = new JTextField(30);
	private JTextField folderInput = new JTextField(30);
	private ChapterTableModel chapterModel;
	private JScrollPane chapterTableScroll;
	private JScrollPane chapterTextScroll;
	private JTable chapterTable = new JTable();
	private JTextArea chapterTextEdit = new JTextArea(20, 20);
	private JButton cancelButton = new JButton("Cancel");
	private JButton okButton = new JButton("Import");

	public ImportEpubDialog(MainWindow parent, String genreDefault) {
		super(parent, "Import EPUB", ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setSize(1000, 600);
		setLocation(200, 100);

		Box fileChoosingBox = new Box(BoxLayout.X_AXIS);
		fileChoosingBox.add(fileChoosingButton);
		fileChoosingBox.add(Box.createHorizontalStrut(20));
		fileChoosingBox.add(fileChoosingLabel);
		fileChoosingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				String epubDir = parent.getWordManager().getSetting("epub_dir");
				File epubDirFile = new File(epubDir);
				if (!epubDir.equals("") && epubDirFile.exists())
					chooser.setCurrentDirectory(epubDirFile);
				chooser.setFileFilter(new FileNameExtensionFilter("EPUB files",
						"epub"));
				int success = chooser.showOpenDialog(parent);
				if (success != JFileChooser.APPROVE_OPTION)
					return;

				try {
					List<String> chapterTexts = EpubImporter.importEpub(chooser
							.getSelectedFile());
					if (chapterTexts == null || chapterTexts.size() == 0)
						throw new IOException();
					chapterModel = new ChapterTableModel(chapterTexts);
					chapterTable.setModel(chapterModel);
					fileChoosingLabel.setText(chooser.getSelectedFile()
							.getName());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(parent,
							"Could not read this EPUB file.",
							"Error reading EPUB file", JOptionPane.ERROR);
				}
			}
		});
		fileChoosingBox.setMaximumSize(new Dimension(10000, 20));
		getContentPane().add(fileChoosingBox);

		Box genreFolderBox = new Box(BoxLayout.X_AXIS);
		genreFolderBox.add(new JLabel("Genre: "));
		genreFolderBox.add(genreInput);
		genreInput.setText(genreDefault);
		genreFolderBox.add(Box.createHorizontalStrut(20));
		genreFolderBox.add(new JLabel("Folder: "));
		genreFolderBox.add(folderInput);
		genreFolderBox.setMaximumSize(new Dimension(1000, 20));
		getContentPane().add(genreFolderBox);

		chapterTable.setSize(100, 400);
		chapterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chapterTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting())
							return;
						int i = chapterTable.getSelectionModel()
								.getMinSelectionIndex();
						chapterTextEdit.setText(chapterModel.getChapterText(i));
						chapterTextEdit.setCaretPosition(0);
						chapterTextScroll.getVerticalScrollBar().setValue(0);
					}
				});

		chapterTextEdit.setMinimumSize(new Dimension(300, 0));
		chapterTextEdit.getDocument().addDocumentListener(
				new DocumentListener() {
					private void updateText(Document d) {
						try {
							int i = chapterTable.getSelectionModel()
									.getMinSelectionIndex();
							String text = d.getText(0, d.getLength());
							chapterModel.setCapterText(i, text);
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						updateText(e.getDocument());
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						updateText(e.getDocument());
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						updateText(e.getDocument());
					}
				});
		Box editBox = new Box(BoxLayout.X_AXIS);
		chapterTableScroll = new JScrollPane(chapterTable);
		chapterTextScroll = new JScrollPane(chapterTextEdit);
		chapterTableScroll
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chapterTableScroll
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		chapterTableScroll.setMinimumSize(new Dimension(100, 0));
		chapterTableScroll.setMaximumSize(new Dimension(100, 10000));
		chapterTextScroll
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chapterTextScroll
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editBox.add(chapterTableScroll);
		editBox.add(chapterTextScroll);
		getContentPane().add(editBox);

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
				canceled = false;
				// TODO check if input is valid
				setVisible(false);
				dispose();
			}
		});
		Box buttonsBox = new Box(BoxLayout.X_AXIS);
		buttonsBox.add(cancelButton);
		buttonsBox.add(okButton);
		getContentPane().add(buttonsBox);

		setVisible(true);
	}

	public boolean wasCanceled() {
		return canceled;
	}

	public String getGenre() {
		return genreInput.getText();
	}

	public String getFolder() {
		return folderInput.getText();
	}

	public List<String> getChapterTitles() {
		return chapterModel.getSelectedChapters();
	}

	public List<String> getChapterTexts() {
		return chapterModel.getSelectedTexts();
	}
}
