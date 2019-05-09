package integration.com.vriend.commons.nevamaven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vriend.nevamaven.NevamavenUtils;
import com.vriend.nevamaven.client.WSClientException;

public class NevamavenUtilsTest {

	@BeforeClass
	public static void init() throws Exception {

	}

	@AfterClass
	public static void end() throws Exception {

	}

	@Test
	public void relativeToStaticPathTest() throws WSClientException, InterruptedException {

		String staticPath = NevamavenUtils.relativeToStaticPath("\\src\\main\\resources\\test.txt");
		String expected = "\\vriend\\nevamaven\\system\\src\\main\\resources\\test.txt";

		Assert.assertTrue("Expected ends with [" + expected + "] but was [" + staticPath + "]",
				staticPath.endsWith(expected));
	}

	// depois o mavenserver funcionar usando a nova funcao de md5 e sha1
	// criar testes apropriados

	@Test
	public void md5() throws Exception {

		InputStream input = NevamavenUtilsTest.class.getClassLoader().getResourceAsStream("test.txt");

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IOUtils.copy(input, output);

		byte outArray[] = output.toByteArray();

		byte md5[] = NevamavenUtils.md5(outArray);

		String hex = NevamavenUtils.toHexFormat(md5);

		Assert.assertEquals("027f735dd056dde559639227ae7c91c1", hex);
	}

	@Test
	public void sha1() throws Exception {

		InputStream input = NevamavenUtilsTest.class.getClassLoader().getResourceAsStream("test.txt");

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IOUtils.copy(input, output);

		byte outArray[] = output.toByteArray();

		byte sha1[] = NevamavenUtils.sha1(outArray);

		String hex = NevamavenUtils.toHexFormat(sha1);

		Assert.assertEquals("68ff54b25b1ec7ea70ca1f5a213e86623945a4a8", hex);
	}

	@Test
	public void toHexFormat() throws Exception {

		byte sha1[] = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
				101, 103, 107, 109, 113, 127 };

		Assert.assertEquals("020305070b0d1113171d1f25292b2f353b3d4347494f53596165676b6d717f",
				NevamavenUtils.toHexFormat(sha1));
	}

	@Test
	public void exist() throws Exception {

		Assert.assertTrue(NevamavenUtils.exist(new File("src/main/resources").getAbsolutePath(), "/test.txt"));
		Assert.assertFalse(NevamavenUtils.exist(new File("").getAbsolutePath(), "/test2.txt"));
	}

}
