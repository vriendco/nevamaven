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

	public static String buildAttributePrintable(String key, String value) {
		String att = StringUtils.rightPad(key, 30) + StringUtils.rightPad(":", 5) + value;
		return att;
	}
	
	public static String addStartSeparator(String path, String separator) {
		path = removeStartSeparator(path);
		return separator + path;
	}

	public static String removeStartSeparator(String path) {
		while (path.startsWith("\\") || path.startsWith("/")) {
			return removeStartSeparator(path.substring(1));
		}

		return path;
	}

	public static String removeEndSeparator(String path) {
		while (path.endsWith("\\") || path.endsWith("/")) {
			return removeEndSeparator(StringUtils.left(path, path.length() - 1));
		}

		return path;
	}

	public static String addEndSeparator(String path, String separator) {
		path = removeEndSeparator(path);
		return path + separator;
	}

	public static boolean exist(String resourcePath, String requestURI) throws IOException {

		requestURI = NevamavenUtils.removeStartSeparator(requestURI);

		String pathURI = resourcePath +  NevamavenUtils.addStartSeparator(requestURI,"/");
		pathURI = pathURI.replace('\\', '/');

		pathURI = NevamavenUtils.removeStartSeparator(pathURI);

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
