/*
 * �� Copyright IBM Corp. 2013
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

package com.ibm.sbt.services.client.connections.blogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Swati Singh
 *
 */
public class BlogPostUpdateDeleteTest extends BaseBlogServiceTest {

	private boolean ignoreAfter = false;

	@Before
	public void initBlogPostTestData() {
		blogPost = createBlogPost();
	}

	@Test
	public void createBlogPostTest() {
		try {
			ignoreAfter = false;
			BlogPost blogPost = new BlogPost(blogService, "");
			blogPost.setTitle("Test Blog Post" + System.currentTimeMillis());
			blogPost.setContent("Test Blog Post Content"+ System.currentTimeMillis());
			BlogPost blogPostReturned = blogService.createBlogPost(blogPost, blog.getHandle());
			assertNotNull(blogPostReturned.getTitle());
			assertEquals(unRandomize(blogPost.getTitle()), unRandomize(blogPostReturned.getTitle()));
			assertEquals(unRandomize(blogPost.getContent()), unRandomize(blogPostReturned.getContent()));
			deleteBlogPost(blogPostReturned);
		} catch (Exception e) {
			fail("Error calling blogService.createBlogPost() caused by: "+e.getMessage());
		}

	}

	@Test
	public void getBlogPost() {
		try {
			BlogPost blogPostGot = blogService.getBlogPost(blog.getHandle(), blogPost.getPostUuid());

			assertEquals(unRandomize(blogPost.getTitle()), unRandomize(blogPostGot.getTitle()));
			assertEquals(unRandomize(blogPost.getContent()), unRandomize(blogPostGot.getContent()));
		} catch (Exception e) {
			fail("Error calling blogService.getBlog() caused by: "+e.getMessage());
		}
	}

	@Test
	public void updateBlogPost() {
		try {
			blogPost.setTitle("New title " + System.currentTimeMillis());
			blogPost.setContent("New content " + System.currentTimeMillis());
			blogService.updateBlogPost(blogPost, blogPost.getBlogHandle());
			BlogPost postUpdated = blogService.getBlogPost(blogPost.getBlogHandle(), blogPost.getPostUuid());
			assertEquals(unRandomize(blogPost.getTitle()), unRandomize(postUpdated.getTitle()));
			assertEquals(unRandomize(blogPost.getContent()), unRandomize(postUpdated.getContent()));
		} catch (Exception e) {
			fail("Error calling blogService.updateBlog() caused by: "+e.getMessage());
		}
	}

	@Test
	public void deleteBlogPost() throws Exception {
		try {
			ignoreAfter = true;
			BlogPost blogPostGot = blogService.getBlogPost(blogPost.getBlogHandle(), blogPost.getPostUuid());
			assertEquals(blogPost.getTitle(), blogPostGot.getTitle());
			blogService.removeBlogPost(blogPost.getPostUuid(), blogPost.getBlogHandle());
			blogService.getBlogPost(blogPost.getBlogHandle(), blogPost.getPostUuid());
		}catch(Exception e) {
			assertNotNull(e.getMessage());
		}
	}

	@After
	public void deleteBlogPostTestDataOnExit() {
		if (ignoreAfter) return;
		try {
			blogService.removeBlogPost(blogPost.getPostUuid(), blogPost.getBlogHandle());
		} catch (BlogServiceException e) {
			fail("Error calling blogService.removeBlogPost() caused by: "+e.getMessage());
		}
	}
}
