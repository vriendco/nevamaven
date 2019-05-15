package integration.com.vriend.commons.nevamaven.server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vriend.nevamaven.NevamavenUtils;
import com.vriend.nevamaven.client.WSClientException;
import com.vriend.nevamaven.client.WSClientHttpURLConnection;
import com.vriend.nevamaven.client.WSReponse;
import com.vriend.nevamaven.server.HttpServletEvent;
import com.vriend.nevamaven.server.MavenServer;

public class MavenServerTest {

	private static MavenServer server = null;

	@BeforeClass
	public static void init() throws Exception {

		boolean javaTest = new File(NevamavenUtils.relativeToStaticPath("\\src\\main\\resources\\")).exists();
		boolean mavenTest = new File(NevamavenUtils.relativeToStaticPath("\\src\\test\\resources\\")).exists();

		if (javaTest) {
			server = new MavenServer(7777, NevamavenUtils.relativeToStaticPath("\\src\\main\\resources\\"),
					"http://central.maven.org/maven2");
		} else if (mavenTest) {
			server = new MavenServer(7777, NevamavenUtils.relativeToStaticPath("\\src\\test\\resources\\"),
					"http://central.maven.org/maven2");
		} else {
			throw new Exception("Test enviroment not supported");
		}
		server.start();
	}

	@AfterClass
	public static void end() throws Exception {
		server.stop();
		System.out.println(NevamavenUtils.relativeToStaticPath("\\src\\main\\resources\\"));

		try {
			FileUtils.forceDelete(new File("src\\main\\resources\\test.txt.md5"));
			FileUtils.forceDelete(new File("src\\main\\resources\\test.txt.sha1"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test(expected = Exception.class)
	public void wrongPath() throws Exception {

		String staticPath = NevamavenUtils.relativeToStaticPath("\\errorPath\\main\\resources\\");

		new MavenServer(5555, NevamavenUtils.relativeToStaticPath(staticPath), "http://mvn.com");
	}

	@Test(expected = Exception.class)
	public void fileName() throws Exception {

		String staticPath = NevamavenUtils.relativeToStaticPath("\\src\\main\\resources\\test.txt");

		new MavenServer(5555, NevamavenUtils.relativeToStaticPath(staticPath), "http://mvn.com");
	}

	@Test
	public void find() throws WSClientException, InterruptedException {

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		WSReponse response = client.exec("http://localhost:7777/test.txt");

		Assert.assertEquals("Xisto123", response.getOutput());

	}
	@Test
	public void beforeProxy() throws WSClientException, InterruptedException {

		final StringBuffer out = new StringBuffer();

		HttpServletEvent event =  (req, res) -> out.append(req.getRequestURI());
		 
		server.bindBeforeProxy("beforeProxy",event);

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		client.exec("http://localhost:7777/test.txt");

		Assert.assertEquals("", out.toString());
		
		client.exec("http://localhost:7777/testNotFound.txt");
		
		Assert.assertEquals("/testNotFound.txt", out.toString());
		
		HttpServletEvent eventDeleted = server.unbindBeforeProxy("beforeProxy");
		
		Assert.assertEquals(event.hashCode(),eventDeleted.hashCode());

	}
	
	@Test
	public void afterProxy() throws WSClientException, InterruptedException {

		final StringBuffer out = new StringBuffer();

		HttpServletEvent event =  (req, res) -> out.append(req.getRequestURI());
		 
		server.bindAfterProxy("afterProxy",event);

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		client.exec("http://localhost:7777/test.txt");

		Assert.assertEquals("", out.toString());
		
		client.exec("http://localhost:7777/testNotFound.txt");
		
		Assert.assertEquals("/testNotFound.txt", out.toString());
		
		HttpServletEvent eventDeleted = server.unbindAfterProxy("afterProxy");
		
		Assert.assertEquals(event.hashCode(),eventDeleted.hashCode());

	}

	@Test
	public void beforeRequest() throws WSClientException, InterruptedException {

		final StringBuffer out = new StringBuffer();

		HttpServletEvent event =  (req, res) -> out.append(req.getRequestURI());
		 
		server.bindBeforeRequest("beforeRequest",event);

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		client.exec("http://localhost:7777/test.txt");

		Assert.assertEquals("/test.txt", out.toString());
		
		HttpServletEvent eventDeleted = server.unbindBeforeRequest("beforeRequest");
		
		Assert.assertEquals(event.hashCode(),eventDeleted.hashCode());

	}

	@Test
	public void afterRequest() throws WSClientException, InterruptedException {

		final StringBuffer out = new StringBuffer();

		HttpServletEvent event =  (req, res) -> out.append(req.getRequestURI());
		 
		server.bindBeforeRequest("afterRequest",event);

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		client.exec("http://localhost:7777/test.txt");

		Assert.assertEquals("/test.txt", out.toString());
		
		HttpServletEvent eventDeleted = server.unbindBeforeRequest("afterRequest");
		
		Assert.assertEquals(event.hashCode(),eventDeleted.hashCode());

	}
	 

	@Test
	public void sha1() throws WSClientException, InterruptedException {

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		WSReponse response = client.exec("http://localhost:7777/test.txt.sha1");

		Assert.assertEquals("68ff54b25b1ec7ea70ca1f5a213e86623945a4a8", response.getOutput());

	}

	@Test
	public void md5() throws WSClientException, InterruptedException {

		WSClientHttpURLConnection client = new WSClientHttpURLConnection();

		WSReponse response = client.exec("http://localhost:7777/test.txt.md5");

		Assert.assertEquals("027f735dd056dde559639227ae7c91c1", response.getOutput());

	}

}
