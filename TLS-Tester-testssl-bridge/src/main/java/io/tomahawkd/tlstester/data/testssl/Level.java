package io.tomahawkd.tlstester.data.testssl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tomahawkd
 */
public class Level {

	private static final Map<String, Integer> map = new HashMap<>();

	static {
		map.put("INFO", 0);
		map.put("OK", 1);
		map.put("LOW", 2);
		map.put("MEDIUM", 3);
		map.put("WARN", 4);
		map.put("HIGH", 5);
		map.put("CRITICAL", 6);
		map.put("FATAL", 7);
		map.put("DEBUG", -1);
	}

	private String levelName;
	private int level;

	private Level(int level, String levelName) {
		this.level = level;
		this.levelName = levelName;
	}

	public static Level getByName(String levelName) {
		Integer res = map.get(levelName);
		if (res == null) throw new IllegalArgumentException("Level " + levelName + " not implement");
		return new Level(res, levelName);
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return levelName;
	}
}
