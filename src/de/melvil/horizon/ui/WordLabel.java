package de.melvil.horizon.ui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class WordLabel extends JLabel {

	private String word;
	private int strength;

	public WordLabel(String text) {
		super(text);
		setOpaque(true);
		setFont(MainWindow.textFont);
		setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
		this.word = text.toLowerCase();
	}

	public String getWord() {
		return word;
	}

	public void setStrength(int strength) {
		this.strength = strength;
		if (strength == -1) {
			setBackground(new Color(190, 220, 255));
		} else if (strength == 0) {
			setBackground(new Color(255, 220, 200));
		} else if (strength == 1) {
			setBackground(new Color(255, 255, 190));
		} else if (strength == 2) {
			setBackground(new Color(200, 255, 200));
		} else {
			setBackground(Color.WHITE);
		}
	}

	public void setSelected(boolean selected) {
		if (selected)
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(Color.GRAY, 2),
					BorderFactory.createEmptyBorder(0, 1, 0, 1)));
		else
			setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
	}

	public int getStrength() {
		return strength;
	}
	
	@Override
	public void setText(String text){
		super.setText(text);
		word = text.toLowerCase();
	}

}
