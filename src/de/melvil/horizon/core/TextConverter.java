package de.melvil.horizon.core;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class TextConverter {

	public String createAnnotatedMarkup(String text, WordManager wordManager) {
		Document document = Jsoup.parse(text);
		document.head().append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		document.head().append("<link href=\"reader.css\" type=\"text/css\" rel=\"stylesheet\">");
		document.head().append("<script src=\"jquery.min.js\" type=\"text/javascript\"><script>");
		document.head().append("<script src=\"reader.js\" type=\"text/javascript\"><script>");
		List<Node> nodes = document.getElementById("text").childNodes();
		for (Node node : nodes) {
			if (node instanceof Element) {
				Element elem = (Element) node;
				if (elem.tag().getName().equals("span")) {
					String word = elem.text().toLowerCase();
					int strength = wordManager.getStrength(word);
					elem.addClass(getClassNameFromStrength(strength));
					String[] meanings = wordManager.getMeanings(word);
					if (meanings.length > 0)
						elem.attr("data-meaning", String.join(", ", meanings));
				}
			}
		}
		document.outputSettings().indentAmount(0).prettyPrint(false);
		return document.html();
	}

	private String getClassNameFromStrength(int strength) {
		switch (strength) {
		case -1:
			return "blue";
		case 0:
			return "red";
		case 1:
			return "yellow";
		case 2:
			return "green";
		default:
			return "white";
		}
	}

	public static void main(String[] args) {
		try {
			WordManager wm = new WordManager("default");
			TextConverter tc = new TextConverter();
			String original = FileUtils.readFileToString(
					new File("/home/melvil/git/horizon/data/default/default/001/polish test.txt"), "UTF-8");
			String generated = tc.createAnnotatedMarkup(original, wm);
			FileUtils.writeStringToFile(new File("/home/melvil/Downloads/test.html"), generated, "UTF-8", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
