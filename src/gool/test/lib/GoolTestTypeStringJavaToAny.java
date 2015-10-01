package gool.test.lib;

import static org.junit.Assert.*;
import gool.generator.android.AndroidPlatform;
import gool.generator.common.Platform;
import gool.generator.cpp.CppPlatform;
import gool.generator.csharp.CSharpPlatform;
import gool.generator.java.JavaPlatform;
import gool.generator.objc.ObjcPlatform;
import gool.generator.python.PythonPlatform;
import gool.test.TestHelperJava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GoolTestTypeStringJavaToAny {
	/*
	 * At this day, the GOOL system supports 6 output languages that are
	 * symbolized by Platforms. You may comment/uncomment these platforms to
	 * enable/disable tests for the corresponding output language.
	 * 
	 * You may also add your own tests by creating a new method within this
	 * class preceded by a @Test annotation.
	 */
	private List<Platform> platforms = Arrays.asList(

			(Platform) JavaPlatform.getInstance(),
			(Platform) CSharpPlatform.getInstance(),
			(Platform) CppPlatform.getInstance(),
			(Platform) PythonPlatform.getInstance()// ,
			// (Platform) AndroidPlatform.getInstance() ,
			// (Platform) ObjcPlatform.getInstance()

			);

	private static class GoolTestExecutor {
		private static final String CLEAN_UP_REGEX = "Note:.*?[\r\n]|(\\w+>\\s)|[\\r\\n]+";
		private String input;
		private String expected;
		private List<Platform> testedPlatforms;
		private List<Platform> excludedPlatforms;

		public GoolTestExecutor(String input, String expected,
				List<Platform> testedPlatforms, List<Platform> excludedPlatforms) {
			this.input = input;
			this.expected = expected;
			this.testedPlatforms = testedPlatforms;
			this.excludedPlatforms = excludedPlatforms;
		}

		public void compare(Platform platform, int test) throws Exception {
//			if (excludedPlatforms.contains(platform)) {
//				String errorMsg = "The following target platform(s) have been excluded for this test: ";
//				for (Platform p : excludedPlatforms)
//					if (testedPlatforms.contains(p))
//						errorMsg += p + " ";
//				Assert.fail(errorMsg
//						+ "\nThis test may contain some patterns that are not supported by GOOL at the moment for these target platforms. You may see the GOOL wiki for further documentation.");
//			}
			if (excludedPlatforms.contains(platform)){
				System.err.println("The following target platform(s) have been "
						+ "excluded for this test:" + platform.getName());
				return;
			}

			// This inserts a package which is mandatory for android
			// TODO Not the ideal place to put it also com.test should be in the
			// properties file
			if (platform instanceof AndroidPlatform) {
				this.input = "package com.test; " + input;
			}
			String result = compileAndRun(platform);
			// The following instruction is used to remove some logging data
			// at the beginning of the result string
			if (platform == ObjcPlatform.getInstance()
					&& result.indexOf("] ") != -1)
				result = result.substring(result.indexOf("] ") + 2);

			// Assert.assertEquals(String.format("The platform %s", platform),
			// expected, result);
			TestHelperJava.assertTestAPIFile(
					String.format("The platform %s", platform), expected,
					result, test);

		}

		protected String compileAndRun(Platform platform) throws Exception {
			String cleanOutput = cleanOutput(TestHelperJava.generateCompileRun(
					platform, input, MAIN_CLASS_NAME));
			return cleanOutput;
		}

		private static String cleanOutput(String result) {
			return result.replaceAll(CLEAN_UP_REGEX, "").trim();
		}
	}

	private static final String MAIN_CLASS_NAME = "TestTypeStringJavaToAny";

	private List<Platform> testNotImplementedOnPlatforms = new ArrayList<Platform>();

	private void excludePlatformForThisTest(Platform platform) {
		testNotImplementedOnPlatforms.add(platform);
	}

	@BeforeClass
	public static void init() {
	}


	@Test
	public void goolTypeStringJavaToCppTest01() throws Exception { 
		String input = TestHelperJava
						.surroundWithClassMainFile(
								"/* Test creation of a String object */"
										+ "String s = new String();"
										+ "if (s != null){ System.out.println(\"true\"); }"
										+ "else{ System.out.println(\"false\"); }",
										MAIN_CLASS_NAME + "_Test01");
						String expected = "true";
						excludePlatformForThisTest((Platform) CSharpPlatform.getInstance());
						compareResultsDifferentPlatforms(input, expected, 1);
	}

	@Test
	public void goolTypeStringJavaToCppTest02() throws Exception { 
		String input = TestHelperJava
						.surroundWithClassMainFile(
								"/* Test length method translation */"
										+ "String s = new String();"
										+ "if (s.length() == 0){ System.out.println(\"true\"); }"
										+ "else{ System.out.println(\"false\"); }",
								MAIN_CLASS_NAME + "_Test02");
		String expected = "true";
		excludePlatformForThisTest((Platform) JavaPlatform.getInstance());
		excludePlatformForThisTest((Platform) CSharpPlatform.getInstance());
		compareResultsDifferentPlatforms(input, expected, 1);
	}
	
	@Test
	public void goolTypeStringJavaToCppTest03() throws Exception { 
		String input = TestHelperJava
						.surroundWithClassMainFile(
								"/* Test isEmpty method translation */"
										+ "String s = new String();"
										+ "if (s.isEmpty()){ System.out.println(\"true\"); }"
										+ "else{ System.out.println(\"false\"); }",
								MAIN_CLASS_NAME + "_Test03");
		String expected = "true";
		excludePlatformForThisTest((Platform) CSharpPlatform.getInstance());
		excludePlatformForThisTest((Platform) CppPlatform.getInstance());

		compareResultsDifferentPlatforms(input, expected, 1);
	}
	
	@Test
	public void goolTypeStringJavaToCppTest04() throws Exception { 
		String input = TestHelperJava
						.surroundWithClassMainFile(
								"/* Test creation of an initializing String object with character sequence */"
										+ "String s = new String(\"test\");"
										+ "if (s != null){ System.out.println(\"true\"); }"
										+ "else{ System.out.println(\"false\"); }",
								MAIN_CLASS_NAME + "_Test04");
		String expected = "true";
		excludePlatformForThisTest((Platform) CSharpPlatform.getInstance());
		excludePlatformForThisTest((Platform) CppPlatform.getInstance());
		compareResultsDifferentPlatforms(input, expected, 1);
	}
	
	private void compareResultsDifferentPlatforms(String input,
			String expected, int test) throws Exception {
		compareResultsDifferentPlatforms(new GoolTestExecutor(input, expected,
				platforms, testNotImplementedOnPlatforms), test);
		this.testNotImplementedOnPlatforms = new ArrayList<Platform>();
	}

	private void compareResultsDifferentPlatforms(GoolTestExecutor executor,
			int test) throws Exception {
		for (Platform platform : platforms) {
			executor.compare(platform, test);
		}
	}

}
