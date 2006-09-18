/* XYModel.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 14 10:27:21     2006, Created by henrichen@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.zul.html;

import java.util.Collection;

/**
 * A XY chart data model.
 *
 * @author <a href="mailto:henrichen@potix.com">henrichen@potix.com</a>
 * @see Chart
 * @see SimpleXYModel
 */	
public interface XYModel extends ChartModel {

	/**
	 * Get a series of the specified index;
	 */
	public Comparable getSeries(int index);
	
	/**
	 * Get all series as a collection.
	 */
	public Collection getSeries();
	
	/**
	 * Get data count of a specified series.
	 * @param series the specified series.
	 */
	public int getDataCount(Comparable series);

	/**
	 * Get X value of a specified series and data index.
	 * @param series the series.
	 * @param index the data index.
	 */
	public Number getX(Comparable series, int index);

	/**
	 * Get Y value of a specified series and data index.
	 * @param series the series.
	 * @param index the data index.
	 */
	public Number getY(Comparable series, int index);

	/**
	 * Add an (x,y) into a series.
	 * @param series the series.
	 * @param x the x value.
	 * @param y the y value.
	 */	
	public void addValue(Comparable series, Number x, Number y);

	/**
	 * Set model to autosort on x value for each series.
	 */
	public void setAutoSort(boolean auto);

	/**
	 * check whether to autosort on x value for each series; default is true.
	 */
	public boolean isAutoSort();
	
	/**
	/**
	 * Remove data of a specified series.
	 * @param series the series
	 */
	public void removeSeries(Comparable series);

	/**
	 * Remove (x,Y) value of a specified series and data index.
	 * @param series the series.
	 * @param index the data index.
	 */	
	public void removeValue(Comparable series, int index);

	/**
	 * clear this model.
	 */	
	public void clear();
}	
