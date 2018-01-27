package de.melvil.horizon.ui;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import de.melvil.horizon.core.WordManager;

@SuppressWarnings("serial")
public class StatisticsDialog extends JDialog {

	public StatisticsDialog(MainWindow parent) {
		super(parent, "Statistics", ModalityType.APPLICATION_MODAL);
		setSize(600, 400);
		setLocation(200, 100);

		File dataFolder = new File("data");
		File[] langFolders = dataFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		int numLangs = langFolders.length;
		WordManager wordManager;
		JTable table = new JTable();

		class StatsTableModel extends DefaultTableModel {
			public StatsTableModel() {
				super(7, numLangs + 1);
			}
		}
		table.setModel(new StatsTableModel());
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		getContentPane().add(table);

		model.setValueAt("Language", 0, 0);
		model.setValueAt("Total", 1, 0);
		model.setValueAt("Well-known", 2, 0);
		model.setValueAt("Good", 3, 0);
		model.setValueAt("Learning", 4, 0);
		model.setValueAt("New", 5, 0);
		model.setValueAt("Score", 6, 0);

		for (int i = 0; i < numLangs; ++i) {
			String lang = langFolders[i].getName();
			model.setValueAt(lang, 0, i + 1);
			wordManager = new WordManager(lang);
			model.setValueAt(wordManager.getNumberOfWords(), 1, i + 1);
			model.setValueAt(wordManager.getNumberOfWordsWithStrength(3), 2,
					i + 1);
			model.setValueAt(wordManager.getNumberOfWordsWithStrength(2), 3,
					i + 1);
			model.setValueAt(wordManager.getNumberOfWordsWithStrength(1), 4,
					i + 1);
			model.setValueAt(wordManager.getNumberOfWordsWithStrength(0), 5,
					i + 1);
			double score = Math.round(wordManager.getWordsScore() * 10.0) / 10.0;
			model.setValueAt(score, 6, i + 1);
		}

		setVisible(true);
	}

}
