package de.melvil.horizon.ui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class AboutHorizonDialog extends JDialog {

	public AboutHorizonDialog(MainWindow parent) {
		super(parent, "About Horizon", ModalityType.APPLICATION_MODAL);
		setSize(400, 120);
		setLocation(300, 200);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
		getContentPane().add(new JLabel("  Horizon, (c) 2016–2018 by Patrick Pauli (@MelvilQ)"));
		getContentPane().add(new JLabel("  Published under the MIT License"));
		getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
		getContentPane().add(new SwingLink("&nbsp;&nbsp;Visit Project Page on GitHub", "https://github.com/MelvilQ/horizon"));

		setVisible(true);
	}

	private class SwingLink extends JLabel {
		private static final long serialVersionUID = 8273875024682878518L;
		private String text;
		private URI uri;

		public SwingLink(String text, String uri) {
			super();
			setup(text, URI.create(uri));
		}

		public void setup(String t, URI u) {
			text = t;
			uri = u;
			setText(text);
			setToolTipText(uri.toString());
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					open(uri);
				}

				public void mouseEntered(MouseEvent e) {
					setText(text, false);
				}

				public void mouseExited(MouseEvent e) {
					setText(text, true);
				}
			});
		}

		@Override
		public void setText(String text) {
			setText(text, true);
		}

		public void setText(String text, boolean ul) {
			super.setText("<html><span style=\"color: #000099;\">" + text + "</span></html>");
			this.text = text;
		}

		private void open(URI uri) {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
