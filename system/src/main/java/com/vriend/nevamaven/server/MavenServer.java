package com.vriend.nevamaven.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
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
	private Map<String, HttpServletEvent> beforeRequest = new HashMap<>();
	private Map<String, HttpServletEvent> beforeProxy = new HashMap<>();
	private Map<String, HttpServletEvent> afterProxy = new HashMap<>();
	private Map<String, HttpServletEvent> afterRequest = new HashMap<>();

	public MavenServer(int port, String localRepositoryPath, String remoteRepositoryURL) throws Exception {

		this.port = port;
		this.localRepositoryPath = localRepositoryPath;
		this.remoteRepositoryURL = remoteRepositoryURL;

		validate();

	}

	public void bindBeforeProxy(String key, HttpServletEvent event) {
		beforeProxy.put(key, event);
	}

	public void bindAfterProxy(String key, HttpServletEvent event) {
		afterProxy.put(key, event);
	}

	public void bindBeforeRequest(String key, HttpServletEvent event) {
		beforeRequest.put(key, event);
	}

	public HttpServletEvent unbindBeforeRequest(String key) {
		return beforeRequest.remove(key);
	}

	public void bindAfterRequest(String key, HttpServletEvent event) {
		afterRequest.put(key, event);
	}

	public HttpServletEvent unbindAfterRequest(String key) {
		return afterRequest.remove(key);
	}

	public HttpServletEvent unbindBeforeProxy(String key) {
		return beforeProxy.remove(key);
	}

	public HttpServletEvent unbindAfterProxy(String key) {
		return afterProxy.remove(key);
	}

	private void validate() throws Exception {
		String pathURI = this.localRepositoryPath;
		pathURI = pathURI.replace('\\', '/');

		pathURI = NevamavenUtils.removeStartSeparator(pathURI);

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
 
	public void start() throws Exception {
		server = new ByHandlerServer();

		server.start(new FunctionalHandler(this::handle), this.port);
	}

	private void handle(final String target, final Request baseRequest, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {

		this.beforeRequest.values().forEach(e -> e.exec(request, response));

		handleByURI(request, response);

		// antes estava apenas quando o arquivo vinha local, ver se isso vai dar
		// algum problema se por sempre
		baseRequest.setHandled(true);

		this.afterRequest.values().forEach(e -> e.exec(request, response));

	}

	private void handleByURI(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String requestURI = request.getRequestURI().toString();
		
		requestURI = NevamavenUtils.addStartSeparator(requestURI,"/");

		StringBuffer resultPrint = new StringBuffer();
		resultPrint.append("URI:" + requestURI);

		boolean isSha1 = requestURI.endsWith(".sha1");
		boolean ismd5 = requestURI.endsWith(".md5");

		String pathURI = this.localRepositoryPath + requestURI;

		if (!NevamavenUtils.exist(this.localRepositoryPath, requestURI)) {

			if (!isSha1 && !ismd5) {
				notFoundRequest(request, response, resultPrint);
				return;

			}

			String requestURIWithoutExtension = requestURI;

			requestURIWithoutExtension = StringUtils.removeEnd(requestURIWithoutExtension, ".sha1");
			requestURIWithoutExtension = StringUtils.removeEnd(requestURIWithoutExtension, ".md5");

			boolean fileSourceNotFound = !NevamavenUtils.exist(this.localRepositoryPath, requestURIWithoutExtension);
			if (fileSourceNotFound) {
				notFoundRequest(request, response, resultPrint);
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

	private void notFoundRequest(HttpServletRequest request, HttpServletResponse response, StringBuffer resultPrint)
			throws IOException {

		this.beforeProxy.values().forEach(action -> action.exec(request, response));

		String requestURI = request.getRequestURI().toString();

		String redirectURL = this.remoteRepositoryURL + requestURI;
		resultPrint.append("=>redirect to ");
		resultPrint.append(redirectURL);

		response.sendRedirect(redirectURL);

		this.afterProxy.values().forEach(action -> action.exec(request, response));

		// response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return;
	}

	public void stop() throws Exception {
		server.stop();
	}

}
