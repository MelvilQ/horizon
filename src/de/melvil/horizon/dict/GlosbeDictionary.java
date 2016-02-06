package de.melvil.horizon.dict;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GlosbeDictionary implements Dictionary {

	@Override
	public List<Pair<String, String>> lookup(String term) {
		List<Pair<String, String>> results = new ArrayList<Pair<String, String>>();
		try {
			String url = "https://glosbe.com/gapi/translate?from=fra&dest=deu&format=json&phrase="
					+ URLEncoder.encode(term, "UTF-8");
			InputStream in = new URL(url).openStream();
			String jsonStr = IOUtils.toString(in, "UTF-8");
			JSONObject json = (JSONObject) new JSONParser().parse(jsonStr);
			if (!json.get("result").equals("ok"))
				throw new Exception("war nix...");
			JSONArray tuc = (JSONArray) json.get("tuc");
			for (int i = 0; i < tuc.size(); ++i) {
				JSONObject translation = (JSONObject) tuc.get(i);
				translation = (JSONObject) translation.get("phrase");
				String transStr = (String) translation.get("text");
				results.add(new ImmutablePair<String, String>(term, transStr));
			}
		} catch (Exception e) {
			return results;
		}
		return results;
	}

	public static void main(String[] args) {
		GlosbeDictionary d = new GlosbeDictionary();
		List<Pair<String, String>> results = d.lookup("fait");
		for (Pair<String, String> p : results) {
			System.out.println(p.getLeft() + " -> " + p.getRight());
		}
	}
}
