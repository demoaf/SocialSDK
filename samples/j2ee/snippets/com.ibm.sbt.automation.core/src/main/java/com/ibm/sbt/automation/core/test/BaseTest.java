/*
 * © Copyright IBM Corp. 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package com.ibm.sbt.automation.core.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.commons.runtime.Application;
import com.ibm.commons.runtime.Context;
import com.ibm.commons.runtime.RuntimeFactory;
import com.ibm.commons.runtime.impl.app.RuntimeFactoryStandalone;
import com.ibm.commons.util.StringUtil;
import com.ibm.sbt.automation.core.environment.TestEnvironment;
import com.ibm.sbt.automation.core.environment.TestEnvironmentFactory;
import com.ibm.sbt.automation.core.test.pageobjects.ResultPage;
import com.ibm.sbt.automation.core.utils.Trace;

/**
 * @author mkataria
 * @author mwallace
 * 
 * @since Jan 23, 2013
 */
public abstract class BaseTest {

	protected TestEnvironment environment;
	private Application application;
	private Context context;
	protected AuthType authType = AuthType.NONE;
	protected SnippetType snippetType = SnippetType.JAVASCRIPT;

	private String snippetId;
	private Map<String, String> snippetParams = new HashMap<String, String>();

	private boolean resultsReady = false;

	public static enum AuthType {
		NONE, AUTO_DETECT, BASIC, OAUTH10, OAUTH20
	};

	public static enum SnippetType {
		JAVASCRIPT, JAVA, JAVASCRIPTFRAMEWORK, JAVAFRAMEWORK
	};

	public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			ISO_FORMAT);
	static {
		TimeZone utc = TimeZone.getTimeZone("UTC");
		dateFormat.setTimeZone(utc);
	}

	/**
	 * Create the environment to use with this test
	 */
	public BaseTest() {
		environment = initTestEnvironment();
	}

	/**
	 * Default behaviour before each test execution
	 */
	@Before
	public void assumeValidEnvironment() {
		Assume.assumeTrue(isEnvironmentValid());
	}

	/**
	 * Default behaviour after each test execution
	 */
	@After
	public void doQuit() {
		takeScreenshot();

		logTransport();

		if (environment.isQuitDriver()) {
			environment.quitDriver();
		}
	}

	/**
	 * <p>
	 * override this method if you want the test to be run only when a specific
	 * condition is met, i.e. not on juery or only if dojo version is greather
	 * than 1.6 remember to and it with super implementation!
	 * </p>
	 * <p>
	 * i.e.
	 * 
	 * <pre>
	 * return super.isEnvironmentValid() &amp;&amp; !environment.isLibrary(&quot;jquery&quot;);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return {boolean}
	 */
	protected boolean isEnvironmentValid() {
		return true;
	}

	/**
	 * Takes a screenshots from the current browser window, on a best effort
	 * basis (screenshots must be enabled, configured, the tests need file write
	 * permission to the configured path etc)
	 */
	public void takeScreenshot() {
		System.out.println("taking screenshot");
		File snap = null;
		try {
			if (environment.isTakeScreenshots()) {

				WebDriver wd = environment.getCurrentDriver();
				snap = ((TakesScreenshot) wd).getScreenshotAs(OutputType.FILE);
				File dest = new File(environment.getScreenshotsPath(), this
						.getClass().getName());
				String sc = environment.isSmartCloud() ? "smartcloud-"
						: "connections-";
				String br = environment
						.getProperty(TestEnvironment.PROP_BROWSER) == null ? "firefox"
						: environment.getProperty(TestEnvironment.PROP_BROWSER);
				String js = environment
						.getProperty(TestEnvironment.PROP_JAVASCRIPT_LIB) == null ? "dojo180"
						: environment
								.getProperty(TestEnvironment.PROP_JAVASCRIPT_LIB);
				dest = new File(dest, sc + br + "-" + js + "-" + snap.getName());
				FileUtils.copyFile(snap, dest);
			}
		} catch (Throwable t) {
			System.err.println("Unable to take snapshot");
			t.printStackTrace(System.err);
			System.err.println("continuing with tests");
			// driver might be crashed after failing test, cleaning up.
			environment.quitDriver();
		}
		if (snap != null) {
			try {
				snap.delete();
			} catch (Throwable t1) {
				try {
					snap.deleteOnExit();
				} catch (Throwable t2) {
				}
			}
		}
	}

	public void logTransport() {
		if (!environment.isDebugTransport()) {
			return;
		}

		WebDriver webDriver = environment.getCurrentDriver();
		WebElement mockData = webDriver.findElement(By.id("mockData"));
		if (mockData != null) {
			Trace.log(mockData.getText());
		}
	}

	/**
	 * Return the specified property from the test environment
	 * 
	 * @param name
	 * @return {String} 
	 */
	public String getProperty(String name) {
		return environment.getProperty(name);
	}

	/**
	 * Set the authentication type to use for this test
	 * 
	 * @param authType
	 */
	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	/**
	 * Set the authentication type to use for this test
	 * 
	 * @param authType
	 * @param forcedReauth
	 */
	public void setAuthType(AuthType authType, boolean forcedReauth) {
		this.authType = authType;
		if (forcedReauth)
			cleanBrowserState();
	}

	/**
	 * @return the authType
	 */
	public AuthType getAuthType() {
		return authType;
	}

	/**
	 * @param snippetType
	 *            the snippetType to set
	 */
	public void setSnippetType(SnippetType snippetType) {
		this.snippetType = snippetType;
	}

	/**
	 * @return the snippetType
	 */
	public SnippetType getSnippetType() {
		return snippetType;
	}

	/**
	 * Set the snippet id
	 * 
	 * @param snippetId
	 */
	public void setSnippetId(String snippetId) {
		this.snippetId = snippetId;
	}

	/**
	 * Return the snippet id
	 * 
	 * @return {String}
	 */
	public String getSnippetId() {
		return snippetId;
	}

	/**
	 * Add a nippet param which will be passed to the snippet when it is invoked
	 * 
	 * @param key
	 * @param value
	 */
	public void addSnippetParam(String key, String value) {
		snippetParams.put(key, value);
	}

	/**
	 * Add a snippet param which will be passed to the snippet when it is
	 * invoked
	 * 
	 * @param key
	 * @param values
	 */
	public void addSnippetParam(String key, String[] values) {
		snippetParams.put(key, StringUtil.concatStrings(values, ',', true));
	}

	/**
	 * Return a map of parameters which should be passed from the test to the
	 * snippet
	 * 
	 * @return {Map<String, String>}
	 */
	public Map<String, String> getSnippetParams() {
		return snippetParams;
	}

	/**
	 * Return the current test environment
	 * 
	 * @return {TestEnvironment}
	 */
	public TestEnvironment getTestEnvironment() {
		return environment;
	}

	/**
	 * Launch the specified snippet
	 */
	public ResultPage launchSnippet(String snippetId) {
		return launchSnippet(snippetId, authType);
	}

	/**
	 * Launch the specified snippet
	 */
	public ResultPage launchSnippet(String snippetId, AuthType authType) {
		return launchSnippet(snippetId, authType, 0);
	}

	/**
	 * Launch the specified snippet
	 */
	public ResultPage launchSnippet(String snippetId, AuthType authType,
			long delay) {
		assertNotNull("Expected a non null snippetId", snippetId);
		resultsReady = false;
		setSnippetId(snippetId);
		setAuthType(authType);

		ResultPage resultPage = environment.launchSnippet(this);
		assertNotNull("Null result from snippet: " + snippetId, resultPage);

		if (delay > 0) {
			// optional delay to allow a test to return results
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
			}
			// TODO: this should not be necessary, investigate why is there
			resultPage = environment.getPageObject(resultPage.getWebDriver());
		}

		return resultPage;
	}

	/**
	 * Wait for the test result to be present
	 */
	public WebElement waitForResult(int timeout) {
		WebElement elementMatch = environment.waitForElement(
				getAuthenticatedMatch(), timeout, getAuthenticatedCondition());
		if (elementMatch != null) {
			this.setResultsReady();
		}
		return elementMatch;
	}

	/**
	 * Wait for a specific element to be present
	 */
	public WebElement waitForElement(final String match, final int secs,
			final String condition) {
		return environment.waitForElement(match, secs, condition);
	}

	/**
	 * Wait the specified interval for the specified web element to be available
	 * with specified text value
	 */
	public WebElement waitForText(final String id, final String match,
			final int secs) {
		return environment.waitForText(id, match, secs);
	}

	public WebElement waitForText(final String xPath, final int seconds,
			final String expectedText) {
		return environment.waitForText(xPath, seconds, expectedText);
	}

	/**
	 * Wait the specified interval for the specified web element to be available
	 * with any text value
	 */
	public WebElement waitForText(final String id, final int secs) {
		return environment.waitForText(id, secs);
	}

	/**
	 * Wait the specified interval for the specified web element to be available
	 * with the specified children
	 */
	public WebElement waitForChildren(String tagName, String xpath, int timeout) {
		return environment.waitForChildren(tagName, xpath, timeout);
	}

	/**
	 * Return a WebDriver for the window with the specified title
	 */
	public WebDriver findPopup(String title) {
		return environment.findPopup(title);
	}

	/**
	 * Dump the specified result page to the trace log
	 */
	public void dumpResultPage(ResultPage resultPage) {
		WebElement webElement = resultPage.getWebElement();
		environment.dumpWebElement(webElement);
	}

	/**
	 * Return the condition used to confirm authentication
	 */
	public String getAuthenticatedCondition() {
		return "idWithText";
	}

	/**
	 * Return the match used to confirm authentication
	 */
	public String getAuthenticatedMatch() {
		return "content";
	}

	/*
	 * Initialize the TestEnvironment to use
	 */
	protected TestEnvironment initTestEnvironment() {
		return TestEnvironmentFactory.getEnvironment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ Snippet Id: ").append(snippetId);
		sb.append(", Snippet Type: ").append(snippetType);
		sb.append(", Authorization Type: ").append(authType);
		sb.append(", Authenticated Condition: ").append(
				getAuthenticatedCondition());
		sb.append(", Authenticated Match: ")
				.append(getAuthenticatedCondition());
		sb.append(" ]");
		return sb.toString();
	}

	private void cleanBrowserState() {
		environment.cleanBrowserState();
	}

	public boolean isResultsReady() {
		return resultsReady;
	}

	public void setResultsReady() {
		this.resultsReady = true;
	}

	public String join(List strList) {
		StringBuilder sb = new StringBuilder();
		for (Object str : strList) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * Create the context (if needed)
	 */
	protected Context createContext() {

		if (context == null) {
			RuntimeFactory runtimeFactory = new RuntimeFactoryStandalone() {
				@Override
				public Context initContext(Application application,
						Object request, Object response) {
					Context context = super.initContext(application, request,
							response);
					TestEnvironmentFactory.getEnvironment().decorateContext(
							context);
					return context;
				}
			};
			application = runtimeFactory.initApplication(null);
			context = Context.init(application, null, null);
		}
		return context;
	}

	/**
	 * Destroy the context
	 */
	@After
	public void destroyContext() {
		if (context != null) {
			try {
				Context.destroy(context);
			} catch (IllegalStateException ise) {
				// can ignore this
			}
			context = null;
		}
	}
}
