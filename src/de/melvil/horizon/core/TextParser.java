package de.melvil.horizon.core;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

public class TextParser {

	public static String parseText(String text) {
		StringBuilder parsed = new StringBuilder(65536);
		boolean inWord = false;
		parsed.append("<div id=\"text\">");
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if (inWord && isLetter(c)) {
				parsed.append(transformCharacter(c));
			} else if (inWord && !isLetter(c)) {
				parsed.append("</span>");
				parsed.append(transformCharacter(c));
				inWord = false;
			} else if (!inWord && isLetter(c)) {
				parsed.append("<span>");
				parsed.append(transformCharacter(c));
				inWord = true;
			} else if (!inWord && !isLetter(c)) {
				parsed.append(transformCharacter(c));
			}
		}
		parsed.append("</div>");
		return parsed.toString();
	}

	private static boolean isLetter(char c) {
		return Character.isLetter(c);
	}

	private static String transformCharacter(char c) {
		if (c == '\n')
			return "<br>";
		else if (c == '<')
			return "[";
		else if (c == '>')
			return "]";
		else
			return "" + c;
	}

	public static void main(String[] args) {
		System.out.println(parseText("I read a lot of books during a year!"));
		System.out
				.println(parseText("Je mange une pomme. J'ai mangé une pomme aujourd'hui."));
		System.out
				.println(parseText("В землях юго-западной Руси слово Россия встречается в латинских памятниках XVI-XVII веков, в частности, слово Rossia или Russia употребляется в курсах Киево-Могилянской академии. При Петре I, расширившем западные пределы Русского царства в ходе Северной войны, в 1721 году была провозглашена Российская империя."));

		try {
			String orig = FileUtils.readFileToString(new File(
					"data/en/newspapers/French News/this is a test.txt"), Charset.forName("UTF-8"));
			String parsed = parseText(orig);
			FileUtils.writeStringToFile(new File(
					"data/en/newspapers/French News/this is a test2.txt"),
					parsed, Charset.forName("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
