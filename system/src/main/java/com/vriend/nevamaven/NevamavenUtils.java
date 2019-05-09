package com.vriend.nevamaven;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;

import org.apache.commons.lang3.StringUtils;

public class NevamavenUtils {
	public static String relativeToStaticPath(String relative) {
		return new File("").getAbsolutePath() + relative;
	}

	public static byte[] md5(final byte[] content) throws Exception {

		return hash(content, "MD5");
	}

	public static byte[] sha1(final byte[] content) throws Exception {

		return hash(content, "SHA-1");
	}

	public static byte[] hash(final byte[] content, final String algorytm) throws Exception {
		final MessageDigest md = MessageDigest.getInstance(algorytm);

		md.update(content);
		return md.digest();

	}

	public static String toHexFormat(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	public static boolean exist(String resourcePath, String requestURI) throws IOException {

		String pathURI = resourcePath + requestURI;
		pathURI = pathURI.replace('\\', '/');

		URI fileUri = null;
		try {
			fileUri = new URI("file:///" + pathURI);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}

		File fileFromURI = new File(fileUri);

		return fileFromURI.exists();
	}
	
 
}
