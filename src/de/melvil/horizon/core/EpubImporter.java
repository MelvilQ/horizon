package de.melvil.horizon.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.unbescape.html.HtmlEscape;

public class EpubImporter {

	public static List<String> importEpub(File epub) throws IOException {
		List<String> chapterTexts = new ArrayList<String>();
		EpubReader epubReader = new EpubReader();
		Book book = epubReader.readEpub(new FileInputStream(epub));
		Spine spine = book.getSpine();
		for (SpineReference reference : spine.getSpineReferences()) {
			Resource res = reference.getResource();
			String mediaType = res.getMediaType().getName();
			if (!mediaType.contains("html"))
				continue;
			String html = new String(res.getData(), res.getInputEncoding());
			Document document = Jsoup.parse(html);
			document.select("title").remove();
			document.outputSettings(new Document.OutputSettings()
					.prettyPrint(false));
			document.select("br").append("\\n");
			document.select("p").prepend("\\n\\n");
			document.select("h1").append("\\n\\n");
			document.select("h2").append("\\n\\n");
			document.select("h3").append("\\n\\n");
			document.select("h4").append("\\n\\n");
			document.select("h5").append("\\n\\n");
			document.select("h6").append("\\n\\n");
			String s = document.html().replaceAll("\\\\n", "\n");
			String text = Jsoup.clean(s, "", Whitelist.none(),
					new Document.OutputSettings().prettyPrint(false));
			text = HtmlEscape.unescapeHtml(text);
			chapterTexts.add(text);
		}
		return chapterTexts;
	}

	public static void main(String[] args) {
		String path = "C:/Users/Melvil/Documents/Ebooks/Englische Bücher/Sonstiges/";
		String filename = "Robert Skidelsky - Keynes The Return of the Master.epub";
		try {
			List<String> tt = EpubImporter
					.importEpub(new File(path + filename));
			for (String s : tt) {
				System.out.println(s);
				System.out.println("----------------------------------------");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
