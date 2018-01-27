package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.melvil.horizon.core.WordManager;

@SuppressWarnings("serial")
public class LanguagePreferencesDialog extends JDialog {

	private WordManager wordManager;
	private JTextField dictUrlInput = new JTextField(100);
	private JTextField epubDirInput = new JTextField(100);
	private JButton chooseButton = new JButton("Choose");

	public LanguagePreferencesDialog(MainWindow parent, WordManager wm) {
		super(parent, "Language Preferences", ModalityType.APPLICATION_MODAL);
		this.wordManager = wm;

		setSize(600, 200);
		setLocation(200, 100);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		getContentPane().add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel dictUrlLabel = new JLabel("Dictionary URL (use $$$ as placeholder for the lookup term): ");
		dictUrlInput.setText(wordManager.getSetting("dict_url"));
		getContentPane().add(dictUrlLabel);
		dictUrlInput.setMaximumSize(new Dimension(2000, 30));
		getContentPane().add(dictUrlInput);

		getContentPane().add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel epubDirLabel = new JLabel("EPUB directory");
		epubDirInput.setText(wordManager.getSetting("epub_dir"));
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dirChooser = new JFileChooser();
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int success = dirChooser.showOpenDialog(parent);
				if (success != JFileChooser.APPROVE_OPTION)
					return;
				epubDirInput.setText(dirChooser.getSelectedFile().getAbsolutePath());
			}
		});
		Box epubDirInputBox = new Box(BoxLayout.X_AXIS);
		epubDirInput.setMaximumSize(new Dimension(2000, 30));
		epubDirInputBox.add(epubDirInput);
		epubDirInputBox.add(chooseButton);
		getContentPane().add(epubDirLabel);
		getContentPane().add(epubDirInputBox);

		getContentPane().add(Box.createRigidArea(new Dimension(0, 40)));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				wordManager.setSetting("dict_url", dictUrlInput.getText());
				wordManager.setSetting("epub_dir", epubDirInput.getText());
				setVisible(false);
			}
		});
		Box buttonsBox = new Box(BoxLayout.X_AXIS);
		buttonsBox.add(cancelButton);
		buttonsBox.add(okButton);
		getContentPane().add(buttonsBox);

		setVisible(true);
	}

}
