/* SimpleGroupsModel.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 2, 2008 9:50:12 AM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.Arrays;
import java.util.Comparator;

import org.zkoss.zul.event.GroupsDataEvent;

/**
 * A simple implementation of {@link GroupsModel}.
 * Note: It assumes the content is immutable.
 * @author Dennis.Chen
 * @since 3.5.0
 * @see GroupsModel
 */
public class SimpleGroupsModel extends AbstractGroupsModel implements GroupsModelExt{
	
	/**
	 * member field to store group data
	 */
	protected Object[][] _data;
	
	/**
	 * member field to store group head data
	 */
	protected Object[] _heads;
	
	/**
	 * member field to store group foot data
	 */
	protected Object[] _foots;
	
	/**
	 * Constructor.
	 * When using this constructor , {@link #getGroup(int)} will return the corresponding Object[] of data. 
	 * {@link #hasGroupfoot(int)} will always false 
	 * @param data a 2 dimension array to represent groups data
	 */
	public SimpleGroupsModel(Object[][] data){
		this(data,null,(Object[])null);
	}
	
	/**
	 * Constructor
	 * When using this constructor , 
	 * {@link #getGroup(int)} will return the corresponding Object depends on heads. 
	 * {@link #hasGroupfoot(int)} will always return false
	 * @param data a 2 dimension array to represent groups data
	 * @param heads an array to represent head data of group
	 */
	public SimpleGroupsModel(Object[][] data,Object[] heads){
		this(data,heads,(Object[])null);
	}
	
	/**
	 * Constructor
	 * When using this constructor , 
	 * {@link #getGroup(int)} will return the corresponding Object depends on heads.  
	 * The return value of {@link #hasGroupfoot(int)} and {@link #getGroupfoot(int)} 
	 * are depends on foots. 
	 *   
	 * @param data a 2 dimension array to represent groups data
	 * @param heads an array to represent head data of group
	 * @param foots an array to represent foot data of group, if an element in this array is null, then 
	 * {@link #hasGroupfoot(int)} will return false in corresponding index.
	 */
	public SimpleGroupsModel(Object[][] data,Object[] heads,Object[] foots){
		if (data == null)
			throw new NullPointerException();
		_data = data;
		_heads = heads;
		_foots = foots;
	}

	public Object getChild(int groupIndex, int index) {
		return _data[groupIndex][index];
	}


	public int getChildCount(int groupIndex) {
		return _data[groupIndex].length;
	}


	public Object getGroup(int groupIndex) {
		return  _heads==null?_data[groupIndex]:_heads[groupIndex];
	}


	public int getGroupCount() {
		return _data.length;
	}

	public Object getGroupfoot(int groupIndex) {
		return _foots == null ? null:_foots[groupIndex];
	}

	public boolean hasGroupfoot(int groupIndex) {
		return _foots == null ? false:_foots[groupIndex]!=null;
	}


	/**
	 * Do nothing in default implementation, however developer can override it to 
	 * re-group by manipulating {@link #_data},{@link #_heads},{@link #_foots}
	 */
	public void group(Comparator cmpr, boolean ascending, int colIndex) {
	}

	/**
	 * Sort each data in each group by Comparator, developer could override {@link #sortGroupData(Object, Object[], Comparator, boolean, int)
	 * to customize.
	 */
	public void sort(Comparator cmpr, boolean ascending, int colIndex) {
		for(int i=0;i<_data.length;i++){
			sortGroupData(_heads==null?_data[i]:_heads[i],_data[i],cmpr,ascending,colIndex);
		}
		fireEvent(GroupsDataEvent.GROUPS_CHANGED,-1,0,_data.length-1);
	}

	protected void sortGroupData(Object group,Object[] groupdata,Comparator cmpr,boolean ascending, int colIndex){
		Arrays.sort(groupdata,cmpr);
	}

}
