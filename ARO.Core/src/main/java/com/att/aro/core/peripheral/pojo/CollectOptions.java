/*
 *  Copyright 2014 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.att.aro.core.peripheral.pojo;

import com.att.aro.core.video.pojo.Orientation;

public class CollectOptions {
	private int totalLines = 0;
	private String orientation = Orientation.PORTRAIT.toString();

	
	public String getOrientation() {
		return orientation;
	}
	
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public int getTotalLines() {
		return totalLines;
	}
	
	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}

}
