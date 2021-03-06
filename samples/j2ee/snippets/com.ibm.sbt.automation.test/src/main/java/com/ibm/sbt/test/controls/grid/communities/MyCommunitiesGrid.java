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
package com.ibm.sbt.test.controls.grid.communities;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.sbt.automation.core.test.BaseGridTest;

/**
 * @author mwallace
 * 
 * @since 5 Mar 2013
 */
public class MyCommunitiesGrid extends BaseGridTest {

    @Test @Ignore
    public void testGrid() {
        assertTrue("Expected the test to generate a grid", checkGrid("Social_Communities_Controls_My_Communities_Grid",true,true));
    }
    
}
