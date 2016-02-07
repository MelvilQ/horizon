package de.melvil.horizon.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import de.melvil.horizon.core.WordManager;

@SuppressWarnings("serial")
public class HorizonReader extends JScrollPane {

	private MainWindow parent;
	private Box box = new Box(BoxLayout.Y_AXIS);
	private JPanel panel;

	private List<WordLabel> labelList;
	private WordLabel selectedLabel;
	private MultiValuedMap<String, WordLabel> labelsByWord;
	private Set<String> remainingUnknownWords;

	private JPopupMenu popupMenu = new JPopupMenu();

	public HorizonReader(MainWindow mainWindow) {
		parent = mainWindow;

		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getVerticalScrollBar().setUnitIncrement(20);

		box.setOpaque(true);
		box.setBackground(Color.WHITE);
		setViewportView(box);

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("D"), "right");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		getActionMap().put("right", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectNextMarkedWord();
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("A"), "left");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		getActionMap().put("left", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPreviousMarkedWord();
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("C"), "very right");
		getActionMap().put("very right", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectNextUnknownWord();
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("Y"), "very left");
		getActionMap().put("very left", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPreviousUnknownWord();
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("W"), "up");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		getActionMap().put("up", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedLabel == null)
					return;
				int strength = selectedLabel.getStrength();
				if (strength <= 3 && strength > 0) {
					strength -= 1;
					parent.notifyStrengthChange(selectedLabel.getWord(),
							strength);
				}
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("S"), "down");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		getActionMap().put("down", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedLabel == null)
					return;
				int strength = selectedLabel.getStrength();
				if (strength < 3 && strength >= 0) {
					strength += 1;
					parent.notifyStrengthChange(selectedLabel.getWord(),
							strength);
				}
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "enter");
		getActionMap().put("enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedLabel == null)
					return;
				parent.notifyStrengthChange(selectedLabel.getWord(), 3);
				selectNextUnknownWord();
			}
		});

		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke("I"), "ignore");
		getActionMap().put("ignore", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedLabel == null)
					return;
				if (selectedLabel.getStrength() == -1) {
					parent.notifyStrengthChange(selectedLabel.getWord(), 4);
					selectNextUnknownWord();
				}
			}
		});

		JMenuItem wellknownItem = new JMenuItem("Well-known");
		JMenuItem ignoreItem = new JMenuItem("Ignore");
		wellknownItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WordLabel clickedWord = (WordLabel) popupMenu.getInvoker();
				parent.notifyStrengthChange(clickedWord.getWord(), 3);
			}
		});
		ignoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WordLabel clickedWord = (WordLabel) popupMenu.getInvoker();
				parent.notifyStrengthChange(clickedWord.getWord(), 4);
			}
		});
		popupMenu.add(wellknownItem);
		popupMenu.add(ignoreItem);
	}

	public void loadText(String text) {
		
		getVerticalScrollBar().setValue(0);

		selectedLabel = null;
		labelList = new ArrayList<WordLabel>();
		labelsByWord = new ArrayListValuedHashMap<String, WordLabel>();
		remainingUnknownWords = new HashSet<String>();

		box.removeAll();

		if (text.equals("")) {
			revalidate();
			repaint();
			return;
		}

		makeNewLine();

		List<Node> nodes = Jsoup.parse(text).getElementById("text")
				.childNodes();
		for (Node node : nodes) {
			if (node instanceof Element) {
				Element elem = (Element) node;
				if (elem.tag().getName().equals("span")) {
					WordLabel label = new WordLabel(elem.text());
					labelList.add(label);
					labelsByWord.put(label.getWord(), label);
					panel.add(label);
				} else if (elem.tag().getName().equals("br")) {
					makeNewLine();
				}
			} else if (node instanceof TextNode) {
				TextNode textnode = (TextNode) node;
				JLabel textlabel = new JLabel(textnode.text());
				textlabel.setFont(MainWindow.textFont);
				panel.add(textlabel);
			}
		}

		for (WordLabel label : labelList) {
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					WordLabel clickedWord = (WordLabel) e.getComponent();
					selectWord(clickedWord);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					WordLabel clickedWord = (WordLabel) e.getComponent();
					if (e.isPopupTrigger() && clickedWord.getStrength() == -1) {
						popupMenu.show(clickedWord, e.getX(), e.getY());
					}
				}
			});
		}

		WordManager wordManager = parent.getWordManager();
		for (String word : labelsByWord.keySet()) {
			int strength = wordManager.getStrength(word);
			String meaning = String.join(", ", wordManager.getMeanings(word));
			Collection<WordLabel> labels = labelsByWord.get(word);
			for (WordLabel label : labels) {
				label.setStrength(strength);
				if (strength >= 0 && strength < 3 && !meaning.equals(""))
					label.setToolTipText(meaning);
				if (strength == -1)
					remainingUnknownWords.add(label.getWord());
			}
		}

		revalidate();
		repaint();
	}

	private void makeNewLine() {
		box.add(Box.createRigidArea(new Dimension(0, 10)));
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new WrapLayout(FlowLayout.LEADING, 0, 2));
		panel.setMaximumSize(new Dimension(580, 100000));
		panel.setAlignmentX(0.35f);
		box.add(panel);
	}

	private void selectWord(WordLabel label) {
		if (selectedLabel == label)
			return;
		if (selectedLabel != null)
			selectedLabel.setSelected(false);
		selectedLabel = label;
		selectedLabel.setSelected(true);
		selectedLabel.requestFocus();
		revalidate();
		repaint();
		parent.notifyWordSelection(label.getWord());
	}

	private void selectNextUnknownWord() {
		int i = -1;
		if (selectedLabel != null)
			i = labelList.indexOf(selectedLabel);
		for (i += 1; i < labelList.size(); ++i) {
			if (labelList.get(i).getStrength() == -1) {
				selectWord(labelList.get(i));
				break;
			}
		}
	}

	private void selectPreviousUnknownWord() {
		int i = labelList.size();
		if (selectedLabel != null)
			i = labelList.indexOf(selectedLabel);
		for (i -= 1; i >= 0; --i) {
			if (labelList.get(i).getStrength() == -1) {
				selectWord(labelList.get(i));
				break;
			}
		}
	}

	private void selectNextMarkedWord() {
		int i = -1;
		if (selectedLabel != null)
			i = labelList.indexOf(selectedLabel);
		for (i += 1; i < labelList.size(); ++i) {
			if (labelList.get(i).getStrength() < 3) {
				selectWord(labelList.get(i));
				break;
			}
		}
	}

	private void selectPreviousMarkedWord() {
		int i = labelList.size();
		if (selectedLabel != null)
			i = labelList.indexOf(selectedLabel);
		for (i -= 1; i >= 0; --i) {
			if (labelList.get(i).getStrength() < 3) {
				selectWord(labelList.get(i));
				break;
			}
		}
	}

	public void changeStrengthOfWord(String word, int strength) {
		remainingUnknownWords.remove(word);
		for (WordLabel l : labelsByWord.get(word)) {
			l.setStrength(strength);
		}
	}

	public int getNumberOfRemainingWords() {
		return remainingUnknownWords.size();
	}
	
	public void applyMeaning(String word){
		int strength = parent.getWordManager().getStrength(word);
		String meaning = String.join(", ", parent.getWordManager().getMeanings(word));
		Collection<WordLabel> labels = labelsByWord.get(word);
		for (WordLabel label : labels) {
			if (strength >= 0 && strength < 3 && !meaning.equals("")){
				label.setToolTipText(meaning);
			}
		}
		revalidate();
		repaint();
	}
}
