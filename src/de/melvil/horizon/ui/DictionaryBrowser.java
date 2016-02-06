package de.melvil.horizon.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

@SuppressWarnings("serial")
public class DictionaryBrowser extends JFXPanel {

	private WebEngine webEngine;
	private WebView webView;

	private MainWindow parent;

	public DictionaryBrowser(MainWindow mw) {
		parent = mw;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Group group = new Group();
				Scene scene = new Scene(group);
				setScene(scene);
				webView = new WebView();
				webView.setZoom(0.85);
				webEngine = webView.getEngine();
				group.getChildren().add(webView);
				webEngine.load("https://en.m.wiktionary.org/");
			}
		});
	}

	public void lookup(String word) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String dictURL = parent.getWordManager().getSetting("dict_url");
				if (dictURL == null) {
					dictURL = "https://en.m.wiktionary.org/wiki/$$$";
				}
				String url = dictURL.replace("$$$", word);
				webEngine.load(url);
			}
		});
	}

}
