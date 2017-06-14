package com.jiabo.letty.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IOUtil {

	public static List<String> readLines(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		return lines;
	}

	public static String readToString(InputStream stream) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int i = 0;
		byte[] bytes = new byte[1024];
		while ((i = stream.read(bytes)) > -1) {
			bos.write(bytes, 0, i);
		}
		bos.close();
		stream.close();
		return new String(bos.toByteArray());
	}

	public static String readToString(String file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int i = 0;
		byte[] bytes = new byte[1024];
		while ((i = stream.read(bytes)) > -1) {
			bos.write(bytes, 0, i);
		}
		bos.close();
		stream.close();
		return new String(bos.toByteArray());
	}

	public static byte[] readToByte(String file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int i = 0;
		byte[] bytes = new byte[1024];
		while ((i = stream.read(bytes)) > -1) {
			bos.write(bytes, 0, i);
		}
		bos.close();
		stream.close();
		return bos.toByteArray();
	}
}
