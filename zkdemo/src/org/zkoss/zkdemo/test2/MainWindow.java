/* MainWindow.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 30, 2007 11:06:22 AM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkdemo.test2;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Dennis.Chen
 *
 */
public class MainWindow extends Window{

	ListModelList fileModel = new ListModelList();
	
	static String[] skipList=new String[]{"index.zul"};
	static String path = "/test2";
	
	public MainWindow(){
		fileModel = new ListModelList();
	}
	
	Iframe iframe;
	Listbox lb;
	public void onCreate(){
		iframe = (Iframe)getFellow("w2").getFellow("ifr");
		lb = (Listbox)getFellow("w1").getFellow("lb");
		lb.setItemRenderer(new FileitemRenderer());
		lb.setModel(fileModel);
		updateModel();
		
		lb.addEventListener("onSelect",new EventListener(){

			public void onEvent(Event event) throws Exception {
				int index = lb.getSelectedIndex();
				if(((Checkbox)getFellow("w1").getFellow("newb")).isChecked()){
					Clients.evalJavaScript("newWindow(\""+((File)fileModel.get(index)).getName()+"\")");
				}else{
					iframe.setSrc(path+"/"+((File)fileModel.get(index)).getName());
				}
				
				
			}});
		
		getFellow("w1").getFellow("fnf").addEventListener("onChange",new EventListener(){

			public void onEvent(Event arg0) throws Exception {
				updateModel();
			}});
		getFellow("w1").getFellow("reg").addEventListener("onCheck",new EventListener(){
			public void onEvent(Event arg0) throws Exception {
				updateModel();
			}});
	}
	
	private void updateModel(){
		fileModel.clear();
		String r = getDesktop().getWebApp().getRealPath("/");
		File test2 = new File(r,path);
		String pattern = ((Textbox)getFellow("w1").getFellow("fnf")).getValue();
		boolean reg = ((Checkbox)getFellow("w1").getFellow("reg")).isChecked();
		File[] files = test2.listFiles(new MyFilenameFilter(pattern,reg));
		Collections.addAll(fileModel,files);
	}
	
	static class MyFilenameFilter implements FilenameFilter{

		String str;
		boolean reg;
		Pattern pattern;
		public MyFilenameFilter(String pattern,boolean reg){
			if(pattern==null) pattern="";
			pattern = pattern.trim();
			this.str = pattern;
			this.reg = reg;
			if(reg){
				this.pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE); 
			}
		}
		
		public boolean accept(File dir, String name) {
			if(str.equals("")) return false;
			for(int i=0;i<skipList.length;i++){
				if(name.equals(skipList[i])) return false;
			}
			if(name.endsWith(".zul")){
				name = name.substring(0,name.length()-4);
			}else if(name.endsWith(".zhtml")){
				name = name.substring(0,name.length()-5);
			}
			if(name.matches("[a-zA-z0-9]*-[a-zA-z0-9]*-[0-9]*")){
				return false;
			}
			
			if(reg){
				Matcher matcher = pattern.matcher(name);
				return matcher.matches();
			}else{
				return name.toUpperCase().indexOf(str.toUpperCase())>=0;
			}
		}
		
	}
	
	static class FileitemRenderer implements ListitemRenderer {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd mm:ss");
		
		public void render(Listitem item, Object data) throws Exception {
			File file = (File)data;
			new Listcell(file.getName()).setParent(item);
			new Listcell(format.format(new Date(file.lastModified()))).setParent(item);
		}

	}
}
