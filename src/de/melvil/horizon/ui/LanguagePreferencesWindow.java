package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.melvil.horizon.core.WordManager;

@SuppressWarnings("serial")
public class LanguagePreferencesWindow extends JFrame {

	private WordManager wordManager;
	private JTextField dictUrlInput = new JTextField(100);
	private JTextField epubDirInput = new JTextField(100);

	public LanguagePreferencesWindow(WordManager wm) {
		super();
		this.wordManager = wm;

		setSize(600, 200);
		setLocation(200, 100);
		setTitle("Language Preferences");
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		getContentPane().add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel dictUrlLabel = new JLabel(
				"Dictionary URL (use $$$ as placeholder for the lookup term): ");
		dictUrlInput.setText(wordManager.getSetting("dict_url"));
		getContentPane().add(dictUrlLabel);
		dictUrlInput.setMaximumSize(new Dimension(2000, 20));
		getContentPane().add(dictUrlInput);

		getContentPane().add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel epubDirLabel = new JLabel("EPUB directory");
		epubDirInput.setText(wordManager.getSetting("epub_dir"));
		epubDirInput.setMaximumSize(new Dimension(2000, 20));
		getContentPane().add(epubDirLabel);
		getContentPane().add(epubDirInput);

		getContentPane().add(Box.createRigidArea(new Dimension(0, 40)));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				wordManager.setSetting("dict_url", dictUrlInput.getText());
				wordManager.setSetting("epub_dir", epubDirInput.getText());
				setVisible(false);
			}
		});
		getContentPane().add(okButton);

		setVisible(true);
	}

}
