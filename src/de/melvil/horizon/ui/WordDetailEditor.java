package de.melvil.horizon.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class WordDetailEditor extends Box {

	private MainWindow parent;

	private String currentWord;
	private int strength;

	private Box strengthBox = new Box(BoxLayout.Y_AXIS);
	private JLabel strengthLabel = new JLabel("Word Strength: ");
	private ButtonGroup strengthRadioGroup = new ButtonGroup();
	private JRadioButton strengthButton0 = new JRadioButton("New");
	private JRadioButton strengthButton1 = new JRadioButton("Learning");
	private JRadioButton strengthButton2 = new JRadioButton("Good");
	private JRadioButton strengthButton3 = new JRadioButton("Well-known");
	private JRadioButton strengthButton4 = new JRadioButton("Ignore");

	private Box meaningsBox = new Box(BoxLayout.Y_AXIS);
	private JTextField meaningsInput = new JTextField(30);
	private JButton meaningsAddButton = new JButton("Add");
	private JButton meaningsDeleteButton = new JButton("Delete Selected");
	private DefaultListModel<String> meaningsModel = new DefaultListModel<String>();
	private JList<String> meaningsList = new JList<String>(meaningsModel);

	public WordDetailEditor(MainWindow mainWindow) {
		super(BoxLayout.X_AXIS);
		parent = mainWindow;

		strengthBox.setMinimumSize(new Dimension(300, 0));
		strengthBox.add(strengthLabel);
		strengthBox.add(strengthButton0);
		strengthBox.add(strengthButton1);
		strengthBox.add(strengthButton2);
		strengthBox.add(strengthButton3);
		strengthBox.add(strengthButton4);
		strengthRadioGroup.add(strengthButton0);
		strengthRadioGroup.add(strengthButton1);
		strengthRadioGroup.add(strengthButton2);
		strengthRadioGroup.add(strengthButton3);
		strengthRadioGroup.add(strengthButton4);

		strengthButton0.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentWord != null) {
					parent.notifyStrengthChange(currentWord, 0);
				}
			}
		});

		strengthButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentWord != null) {
					parent.notifyStrengthChange(currentWord, 1);
				}
			}
		});

		strengthButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentWord != null) {
					parent.notifyStrengthChange(currentWord, 2);
				}
			}
		});

		strengthButton3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentWord != null) {
					parent.notifyStrengthChange(currentWord, 3);
				}
			}
		});

		strengthButton4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentWord != null) {
					parent.notifyStrengthChange(currentWord, 4);
				}
			}
		});

		Box inputBox = new Box(BoxLayout.X_AXIS);
		inputBox.setMaximumSize(new Dimension(300, 30));
		inputBox.add(meaningsInput);
		inputBox.add(meaningsAddButton);
		meaningsAddButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newMeaning = meaningsInput.getText().trim();
				if (!newMeaning.equals("")) {
					parent.getWordManager().addMeaning(currentWord, newMeaning);
					if (strength == -1)
						parent.notifyStrengthChange(currentWord, 0);
					parent.notifyMeaningChange(currentWord);
					meaningsModel.addElement(newMeaning);
					meaningsInput.setText("");
				}
			}
		});
		inputBox.add(meaningsDeleteButton);
		meaningsDeleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = meaningsList.getSelectedIndex();
				if (selected == -1)
					return;
				String meaning = meaningsModel.getElementAt(selected);
				meaningsModel.remove(selected);
				parent.getWordManager().deleteMeaning(currentWord, meaning);
				parent.notifyMeaningChange(currentWord);
			}
		});
		meaningsBox.add(inputBox);
		meaningsList.setMaximumSize(new Dimension(300, 600));
		meaningsBox.add(meaningsList);

		add(strengthBox);
		add(Box.createHorizontalGlue());
		add(meaningsBox);
	}

	public void display(String word) {
		currentWord = word;

		int strength = parent.getWordManager().getStrength(word);
		applyStrength(strength);

		meaningsInput.setText("");
		meaningsModel.clear();
		for (String meaning : parent.getWordManager().getMeanings(word)) {
			meaningsModel.addElement(meaning);
		}
	}

	public void applyStrength(int strength) {
		this.strength = strength;
		if (strength == 0)
			strengthButton0.setSelected(true);
		else if (strength == 1)
			strengthButton1.setSelected(true);
		else if (strength == 2)
			strengthButton2.setSelected(true);
		else if (strength == 3)
			strengthButton3.setSelected(true);
		else if (strength == 4)
			strengthButton4.setSelected(true);
		else
			strengthRadioGroup.clearSelection();
	}

}
