package de.melvil.horizon.dict;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public interface Dictionary {
	
	List<Pair<String, String>> lookup(String term);

}
