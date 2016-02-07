package de.melvil.horizon.core;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class HorizonSettings {

	private DB db;
	private HTreeMap<String, String> settings;

	public HorizonSettings() {
		db = DBMaker.newFileDB(new File("data/settings.db")).make();
		settings = db.getHashMap("settings");
	}

	public void setSetting(String key, String value) {
		settings.put(key, value);
		db.commit();
	}

	public String getSetting(String key) {
		return settings.get(key);
	}

}
