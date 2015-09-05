package com.hyperiongray.court.parser;

import java.io.File;
import java.util.Map;

public interface IParser {

	public static final String BOTH = "both";

	Stats getStats();

	Map<DataKey, String> parseFile(File file) throws Exception;

}
