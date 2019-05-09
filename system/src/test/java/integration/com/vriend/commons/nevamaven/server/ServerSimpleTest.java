package integration.com.vriend.commons.nevamaven.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Request;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vriend.nevamaven.NevamavenUtils;
import com.vriend.nevamaven.client.WSClientException;
import com.vriend.nevamaven.client.WSClientHttpURLConnection;
import com.vriend.nevamaven.client.WSReponse;
import com.vriend.nevamaven.server.ByHandlerServer;
import com.vriend.nevamaven.server.FunctionalHandler;

public class ServerSimpleTest {

	private static ByHandlerServer server = null;

	private static void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		String resourcesPath = "\\src\\main\\resources";
		String pathURI = NevamavenUtils.relativeToStaticPath(resourcesPath + baseRequest.getRequestURI().toString());
		pathURI = pathURI.replace('\\', '/');

		URI fileUri = null;
		try {
			fileUri = new URI("file:///" + pathURI);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}

		File fileFromURI = new File(fileUri);

		StringBuffer resultPrint = new StringBuffer();

		resultPrint.append("URI" + baseRequest.getRequestURI());

		if (!fileFromURI.exists()) {
			notFoundRequest(response, resultPrint);
		} else {
			foundRequest(baseRequest, response, fileFromURI, resultPrint);
		}

		System.out.println(resultPrint.toString());

	}
 

	private static void foundRequest(Request baseRequest, HttpServletResponse response, File file,
			StringBuffer resultPrint) throws IOException {
		resultPrint.append("=>200");

		response.setContentType("application/jar");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.getWriter().print(FileUtils.readFileToString(file));
	}

	private static void notFoundRequest(HttpServletResponse response, StringBuffer resultPrint) {
		resultPrint.append("=>404");
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return;
	}

	@BeforeClass
	public static void init() throws Exception {

		server = new ByHandlerServer();

		server.start(new FunctionalHandler(ServerSimpleTest::handle), 7777);

	}

	@AfterClass
	public static void end() throws Exception {
		server.stop();
	}

	@Test
	public void relativeToStaticPathTest() throws WSClientException, InterruptedException {

		String staticPath = NevamavenUtils.relativeToStaticPath("\\src\\main\\resources\\test.txt");
		String expected = "\\vriend\\nevamaven\\system\\src\\main\\resources\\test.txt";

		Assert.assertTrue("Expected ends with [" + expected + "] but was [" + staticPath + "]",
				staticPath.endsWith(expected));
	}

	@Test
	public void find() throws WSClientException, InterruptedException {

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		WSReponse response = client.exec("http://localhost:7777/test.txt");

		Assert.assertEquals("Xisto123", response.getOutput());

	}
 
}
