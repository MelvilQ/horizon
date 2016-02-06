package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;

import de.melvil.horizon.core.WordManager;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JMenuBar menuBar = new JMenuBar();
	private JLabel remainingCounter = new JLabel();

	private TextSelector selector = new TextSelector(this);
	private HorizonReader reader = new HorizonReader(this);
	private Box rightBox = new Box(BoxLayout.Y_AXIS);
	private WordDetailEditor editor = new WordDetailEditor(this);
	private DictionaryBrowser dictionary = new DictionaryBrowser(this);

	private WordManager wordManager;

	public static Font textFont;
	public static Font editFont;

	public MainWindow() {
		setTitle("Horizon");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		JMenu menu = new JMenu("Menu");
		JMenuItem wordManagerItem = new JMenuItem("Manage Words");
		menu.add(wordManagerItem);
		JMenuItem statisticsItem = new JMenuItem("View Statistics");
		statisticsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new StatisticsWindow();
			}
		});
		menu.add(statisticsItem);
		JMenuItem languagePreferencesItem = new JMenuItem(
				"Language Preferences");
		languagePreferencesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new LanguagePreferencesWindow(wordManager);
			}
		});
		menu.add(languagePreferencesItem);
		menu.addSeparator();
		JMenuItem aboutItem = new JMenuItem("About Horizon");
		menu.add(aboutItem);
		menu.addSeparator();
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		menu.add(exitItem);
		menuBar.add(menu);
		menuBar.add(Box.createHorizontalStrut(175));
		menuBar.add(remainingCounter);
		setJMenuBar(menuBar);

		selector.setMinimumSize(new Dimension(200, 600));
		selector.setMaximumSize(new Dimension(200, 1000));
		reader.setMinimumSize(new Dimension(620, 600));
		reader.setMaximumSize(new Dimension(620, 1000));
		getContentPane().add(selector);
		getContentPane().add(reader);
		rightBox.setMinimumSize(new Dimension(550, 600));
		rightBox.setMaximumSize(new Dimension(550, 1000));
		editor.setMaximumSize(new Dimension(500, 400));
		rightBox.add(editor);
		rightBox.add(dictionary);
		getContentPane().add(rightBox);

		textFont = new Font("Georgia", Font.PLAIN, 17);
		editFont = new Font("Georgia", Font.PLAIN, 12);

		notifyLanguageChange("fr");

		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void notifyLanguageChange(String lang) {
		selector.setLanguage(lang);
		wordManager = new WordManager(lang);
		reader.loadText("");
		editor.display("");
	}

	public void notifyLoadText(File textFile) {
		try {
			reader.loadText(FileUtils.readFileToString(textFile,
					Charset.forName("UTF-8")).replace("\uFEFF", ""));
			adjustRemainingWordsCounter();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Sorry! There was an error loading the text.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void notifyWordSelection(String word) {
		editor.display(word);
		dictionary.lookup(word);
	}

	public void notifyStrengthChange(String word, int strength) {
		wordManager.setStrength(word, strength);
		reader.changeStrengthOfWord(word, strength);
		editor.applyStrength(strength);
		adjustRemainingWordsCounter();
	}

	public void adjustRemainingWordsCounter() {
		remainingCounter.setText("Remaining Words: "
				+ reader.getNumberOfRemainingWords());
	}

	public WordManager getWordManager() {
		return wordManager;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

				ToolTipManager.sharedInstance().setInitialDelay(300);
				ToolTipManager.sharedInstance().setDismissDelay(10000);

				MainWindow mw = new MainWindow();
				mw.setVisible(true);
			}
		});
	}

}
