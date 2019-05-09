package com.vriend.nevamaven.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;

import com.vriend.nevamaven.NevamavenUtils;

public class MavenServer {

	private ByHandlerServer server = null;
	private String localRepositoryPath = "\\src\\main\\resources";
	private String remoteRepositoryURL;
	private int port = 7777;

	public MavenServer(int port, String localRepositoryPath, String remoteRepositoryURL) throws Exception {

		this.port = port;
		this.localRepositoryPath = localRepositoryPath;
		this.remoteRepositoryURL = remoteRepositoryURL;

		validate();

	}

	private void validate() throws Exception {
		String pathURI = this.localRepositoryPath;
		pathURI = pathURI.replace('\\', '/');

		pathURI = removeStartSeparator(pathURI);

		pathURI = "file:///" + pathURI;

		URI fileUri = null;
		try {
			fileUri = new URI(pathURI);
		} catch (URISyntaxException e) {
			throw new Exception(e);
		}

		File fileFromURI = new File(fileUri);

		if (!fileFromURI.exists()) {
			throw new Exception("Path [" + pathURI + "] isn't a valid path name.");
		}

		if (!fileFromURI.canRead()) {
			throw new Exception("Path [" + pathURI + "] cannot be read.");
		}

		if (!fileFromURI.isDirectory()) {
			throw new Exception("Path [" + pathURI + "] isn't a folder.");
		}
	}

	private String removeStartSeparator(String pathURI) {
		while (pathURI.startsWith("\\")) {
			pathURI = pathURI.substring(1);
		}
		while (pathURI.startsWith("/")) {
			pathURI = pathURI.substring(1);
		}
		return pathURI;
	}

	public void start() throws Exception {
		server = new ByHandlerServer();

		server.start(new FunctionalHandler(this::handle), this.port);
	}

	private void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String requestURI = baseRequest.getRequestURI().toString();

		handleByURI(requestURI, response);

		// antes estava apenas quando o arquivo vinha local, ver se isso vai dar
		// algum problema se por sempre
		baseRequest.setHandled(true);

	}

	private void handleByURI(String requestURI, HttpServletResponse response) throws IOException {
		StringBuffer resultPrint = new StringBuffer();
		resultPrint.append("URI" + requestURI);

		boolean isSha1 = requestURI.endsWith(".sha1");
		boolean ismd5 = requestURI.endsWith(".md5");

		String pathURI = this.localRepositoryPath + requestURI;

		if (!NevamavenUtils.exist(this.localRepositoryPath, requestURI)) {

			if (!isSha1 && !ismd5) {
				notFoundRequest(requestURI, response, resultPrint);
				return;

			}

			String requestURIWithoutExtension = requestURI;

			requestURIWithoutExtension = StringUtils.removeEnd(requestURIWithoutExtension, ".sha1");
			requestURIWithoutExtension = StringUtils.removeEnd(requestURIWithoutExtension, ".md5");

			boolean fileSourceNotFound = !NevamavenUtils.exist(this.localRepositoryPath, requestURIWithoutExtension);
			if (fileSourceNotFound) {
				notFoundRequest(requestURI, response, resultPrint);
				return;
			}

			buildChecksumFiles(this.localRepositoryPath, requestURI);
		}

		foundRequest(pathURI, response, resultPrint);

		System.out.println(resultPrint.toString());
	}

	private void buildChecksumFiles(String repositoryPath, String requestURI) throws IOException {
		buildChecksumFile(repositoryPath, requestURI, "sha1", t -> {
			try {
				return NevamavenUtils.sha1(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		buildChecksumFile(repositoryPath, requestURI, "md5", t -> {
			try {
				return NevamavenUtils.md5(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void buildChecksumFile(String repositoryPath, String requestURI, String algoritm,
			Function<byte[], byte[]> converter) throws IOException {

		String path = repositoryPath + requestURI;

		path = StringUtils.removeEnd(path, ".sha1");
		path = StringUtils.removeEnd(path, ".md5");

		File fileFromURI = new File(path);

		String output = null;

		try {
			InputStream input = FileUtils.openInputStream(fileFromURI);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copy(input, bos);

			byte content[] = bos.toByteArray();

			byte crypt[] = converter.apply(content);

			output = NevamavenUtils.toHexFormat(crypt);

			input = new ByteArrayInputStream(output.getBytes());

			File fileOutput = new File(path + "." + algoritm);

			FileOutputStream fout = new FileOutputStream(fileOutput);

			IOUtils.copy(input, fout);

			return;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void foundRequest(String filePath, HttpServletResponse response, StringBuffer resultPrint)
			throws IOException {

		resultPrint.append("=>200");

		response.setContentType("application/jar");
		response.setStatus(HttpServletResponse.SC_OK);

		try {
			File file = new File(filePath);
			InputStream input = FileUtils.openInputStream(file);

			IOUtils.copy(input, response.getOutputStream());

			return;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void notFoundRequest(String requestUri, HttpServletResponse response, StringBuffer resultPrint)
			throws IOException {

		String redirectURL = this.remoteRepositoryURL + requestUri;
		resultPrint.append("=>redirect to ");
		resultPrint.append(redirectURL);

		response.sendRedirect(redirectURL);

		// response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return;
	}

	public void stop() throws Exception {
		server.stop();
	}

}
