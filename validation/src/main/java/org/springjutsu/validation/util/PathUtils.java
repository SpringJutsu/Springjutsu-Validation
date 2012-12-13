package org.springjutsu.validation.util;

public class PathUtils {
	
	public static String appendPath(String... pathSegments) {
		String path = pathSegments[0];
		for (int i = 1; i < pathSegments.length; i++) {
			path += "." + pathSegments[i];
		}
		if (path.startsWith(".")) {
			path = path.substring(1);
		}
		return path;
	}
}
