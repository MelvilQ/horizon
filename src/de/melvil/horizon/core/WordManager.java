package de.melvil.horizon.core;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class WordManager {

	private DB db;
	private HTreeMap<String, String> settings;
	private HTreeMap<String, Integer> strengths;
	private HTreeMap<String, String> meanings;

	public WordManager(String lang) {
		db = DBMaker.newFileDB(new File("data/" + lang + "/" + lang + ".db"))
				.make();
		settings = db.getHashMap("settings");
		strengths = db.getHashMap("strengths");
		meanings = db.getHashMap("meanings");
	}

	public String getSetting(String key) {
		return settings.get(key);
	}

	public void setSetting(String key, String value) {
		settings.put(key, value);
		db.commit();
	}

	public int getStrength(String word) {
		if (strengths.containsKey(word))
			return strengths.get(word);
		else
			return -1;
	}

	public void setStrength(String word, int strength) {
		strengths.put(word, strength);
		db.commit();
	}

	public String[] getMeanings(String word) {
		String meaningStr = meanings.get(word);
		if (meaningStr == null)
			return new String[] {};
		else
			return meaningStr.split("\\|");
	}

	public void addMeaning(String word, String meaning) {
		String currentMeanings = meanings.get(word);
		if (currentMeanings == null)
			meanings.put(word, meaning);
		else
			meanings.put(word, currentMeanings + "|" + meaning);
		db.commit();
	}

	public int getNumberOfWords() {
		int number = 0;
		for (String w : strengths.keySet()) {
			int strength = strengths.get(w);
			if (strength != 4) {
				number++;
			}
		}
		return number;
	}

	public int getNumberOfWordsWithStrength(int strength) {
		int number = 0;
		for (String w : strengths.keySet()) {
			int s = strengths.get(w);
			if (strength == s) {
				number++;
			}
		}
		return number;
	}

	public double getWordsScore() {
		double score = 0.0;
		for (String w : strengths.keySet()) {
			int s = strengths.get(w);
			if (s == 0)
				score += 0.1;
			else if (s == 1)
				score += 0.3;
			else if (s == 2)
				score += 0.6;
			else if (s == 3)
				score += 1.0;
		}
		return score;
	}
}
