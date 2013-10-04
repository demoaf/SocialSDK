package com.ibm.sbt.test.js.connections.activities.api;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.sbt.automation.core.test.connections.BaseActivitiesTest;
import com.ibm.sbt.automation.core.test.pageobjects.JavaScriptPreviewPage;
import com.ibm.sbt.services.client.connections.activity.Activity;
import com.ibm.sbt.services.client.connections.activity.ActivityNode;

public class CreateActivityNode extends BaseActivitiesTest {

	static final String SNIPPET_ID = "Social_Activities_API_CreateActivityNode";

	String activityNodeId = null;
	Activity activity;

	@Before
	public void init() {
		activity = createActivity();
		addSnippetParam("sample.activityId", activity.getActivityId());
	}

	@After
	public void destroy() {
		deleteActivityNode(activityNodeId);
		deleteActivity(activity.getActivityId());
	}

	@Test
	public void testCreateActivity() {
		JavaScriptPreviewPage previewPage = executeSnippet(SNIPPET_ID);
		JsonJavaObject json = previewPage.getJson();
		activityNodeId = json.getAsString("getActivityNodeId");
		Assert.assertNotNull(activityNodeId);
	}
}
