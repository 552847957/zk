/* Loader.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jun  3 09:13:02     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.util.resource;

/**
 * A loader that could load a resource to another format.
 * It is mainly designed to work with {@link ResourceCache}.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public interface Loader {
	/** Returns whether to call {@link #getLastModified}.
	 * If false, it assumes the current cached content is up-to-date.
	 *
	 * @param expiredMillis how many milli-seconds are expired after the last
	 * check. In most cases, just return true if expiredMillis > 0
	 */
	public boolean shallCheck(Object src, long expiredMillis);
	/** Returns the last modified time, or -1 if reload is required or not exists.
	 */
	public long getLastModified(Object src);
	/** Loads the resource.
	 * @return null if not found
	 * @exception Exception you might throw any exception which will be
	 * passed back to the caller of {@link ResourceCache#get}
	 */
	public Object load(Object src) throws Exception;
}
