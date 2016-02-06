package de.melvil.horizon.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class NewTextDialog extends JDialog {

	private JLabel genreLabel = new JLabel("Genre: ");
	private JTextField genreInput = new JTextField(30);
	private JLabel folderLabel = new JLabel("Folder: ");
	private JTextField folderInput = new JTextField(30);

	private boolean canceled = true;
	private String genre;
	private String folder;
	private String filename;
	private String text;

	private JLabel filenameLabel = new JLabel("File Name: ");
	private JTextField filenameInput = new JTextField(30);
	private JTextArea textInput = new JTextArea(40, 50);
	private JButton cancelButton = new JButton("Cancel");
	private JButton okButton = new JButton("Load");

	public NewTextDialog(JFrame parent, String genreDefault,
			String folderDefault) {
		super(parent, "New Text", ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setSize(600, 500);
		setLocation(200, 100);

		genreInput.setText(genreDefault);
		folderInput.setText(folderDefault);
		Box genreFolderBox = new Box(BoxLayout.X_AXIS);
		genreFolderBox.add(genreLabel);
		genreFolderBox.add(genreInput);
		genreFolderBox.add(Box.createRigidArea(new Dimension(40, 0)));
		genreFolderBox.add(folderLabel);
		genreFolderBox.add(folderInput);
		getContentPane().add(genreFolderBox);

		Box filenameBox = new Box(BoxLayout.X_AXIS);
		filenameBox.add(filenameLabel);
		filenameBox.add(filenameInput);
		getContentPane().add(filenameBox);

		textInput.setAutoscrolls(true);
		textInput.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		textInput.setFont(MainWindow.editFont);
		textInput.setLineWrap(true);
		textInput.setWrapStyleWord(true);
		textInput.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					textInput.paste();
				}
			}
		});
		JScrollPane textScroll = new JScrollPane(textInput,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textScroll.setViewportView(textInput);
		textScroll.setAutoscrolls(true);
		textScroll.setMaximumSize(new Dimension(560, 300));
		getContentPane().add(textScroll);

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
				genre = genreInput.getText();

				folder = folderInput.getText();
				filename = filenameInput.getText();
				text = textInput.getText();
				if (genre == null || genre.equals("") || folder == null
						|| folder.equals("") || filename == null
						|| filename.equals("") || text == null
						|| text.equals("")) {
					JOptionPane.showMessageDialog(textInput,
							"Please fill out all input fields.",
							"Missing input", JOptionPane.ERROR_MESSAGE);
					return;
				}
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
		return genre;
	}

	public String getFolder() {
		return folder;
	}

	public String getFileName() {
		return filename;
	}

	public String getText() {
		return text;
	}
}
