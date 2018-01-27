package io.github.syst3ms.skriptparser.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	public static List<String> readAllLines(File file) throws IOException {
		List<String> lines = new ArrayList<>();
		FileReader in = new FileReader(file);
		BufferedReader reader = new BufferedReader(in);
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}
}
