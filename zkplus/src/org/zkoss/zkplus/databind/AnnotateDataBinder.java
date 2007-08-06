/* AnnotateDataBinder.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Nov 16 13:22:37     2006, Created by Henri Chen
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zkplus.databind;

import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.metainfo.Annotation; 

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Iterator;

/**
 * <p>The DataBinder that reads ZUML annotations to create binding info. The ZUML page must declare the 
 * XML namespace, xmlns:a="http://www.zkoss.org/2005/zk/annotation", to use the ZUML annotations. 
 * The annotation is declared before each Component or specified directly on the Component attributes. 
 * For example, the following annotation associates the 
 * attibute "value" of the component "textbox" to the bean's value "person.address.city".</p>
 * <pre>
 * &lt;a:bind value="person.address.city"/>
 * &lt;textbox/>
 * </pre>
 * <p>Or since ZK 2.4 you can annotate directly on the attribute "value" of the component "textbox" like this.</p>
 * <pre>
 * &lt;textbox value="@{person.address.city}"/>
 * </pre>
 * <p>The @{...} pattern tells the ZUML parser that this is for annotation.</p>
 *
 * <p>The AnnotateDataBinder knows "a:bind" annotation only. The complete format is like this if you declared it 
 * before the Component.
 * <pre>
 * &lt;a:bind attrY="bean's value;[tag:expression]..."/>
 * &lt;componentX/>
 * </pre>
 *
 * <p>You can also specify directly on the Component attribute, the complete format is like this:
 * <pre>
 * &lt;componentX attrY="@{bean's value,[tag='expression']...}"/>
 * </pre>
 *
 * <p>This associates the componentX's attribute attrY to the bean's value. The bean's value is something
 * in the form of beanid.field1.field2... You can either call {@link DataBinder#bindBean} to bind the beanid to a
 * real bean object or you can neglect it and this DataBinder would try to find it from the variables map via
 * ({@link org.zkoss.zk.ui.Component#getVariable} method. That is, all those variables defined in zscript are 
 * accessible by this DataBinder. Note that you can choose either two formats of annotaion as your will and you 
 * can even hybrid them together though it is generaly not a good practice.</p>
 *
 * <p>The tag:expression or tag='expression' is a generic form to bind more metainfo to the attrY of the componentX. 
 * The currently supported tag includes "load-when", "save-when", "access", and "converter"</p>
 *
 * <ul>
 * <li>load-when. You can specify the events concerned when to load the attribute of the component from the bean.
 * Multiple definition is allowed and would be called one by one.
 * For example, the following code snip tells DataBinder that the attribute "value" of Label "fullName" will load 
 * from "person.fullName" when the Textbox "firstName" or "lastName" fire "onChange" event.
 *
 * <p>Declare in front of the Component:</p>
 * <pre>
 * &lt;a:bind value="person.firstName"/>
 * &lt;textbox id="firstname"/>
 *
 * &lt;a:bind value="person.lastName"/>
 * &lt;textbox id="lastname"/>
 *
 * &lt;a:bind value="person.fullName; load-when:firstname.onChange; load-when:lastname.onChange"/>
 * &lt;label id="fullname"/>
 * </pre>
 * <p>Or specify directly on the Component's attribute:</p>
 * <pre>
 * &lt;textbox id="firstname" value="@{person.firstName}"/>
 * &lt;textbox id="lastname" value="@{person.lastName}"/>
 * &lt;label id="fullname" value="@{person.fullName, load-when='firstname.onChange,lastname.onChange'}"/>
 * </pre>
 * </li>
 *
 * <li>save-when. You can specify the events concerned when to save the attribute of the component into the bean.
 * Since ZK version 2.5.0, you can specify multiple events in save-when tag (i.e. before ZK 2.5.0, you can specify only
 * one event). The events specified, if fired, will trigger
 * this DataBinder to save the attribute of the component into bean. For example, the following code snip tells 
 * DataBinder that the attribute "value" of Textbox "firstName" will 
 * save into "person.firstName" when the Textbox itself fire "onChange" event.
 *
 * <p>Declare in front of the Component:</p>
 * <pre>
 * &lt;a:bind value="person.firstName; save-when:self.onChange"/>
 * &lt;textbox id="firstName"/>
 * </pre>
 *
 * <p>Or specify directly on the Component's attribute:</p>
 * <pre>
 * &lt;textbox id="firstName" value="@{person.firstName, save-when='self.onChange'}"/>
 * </pre>
 *
 * <p>However, you don't generally specify the save-when tag. If you don't specify it, the default events are used
 * depends on the natural charactieric of the component's attribute as defined in lang-addon.xml. For example, 
 * the save-when of Label.value is default to none while that of Textbox.value is default to self.onChange. 
 * That is, the following example is the same as the above one.</p>
 * <p>Declare in front of the Component:</p>
 * <pre>
 * &lt;a:bind value="person.firstName"/>
 * &lt;textbox id="firstName"/>
 * </pre>
 * <p>Or specifies directly on the Component's attribute:</p>
 * <pre>
 * &lt;textbox id="firstName" value="@{person.firstName}"/>
 * </pre>
 *
 * <p>On the other hand, you might not specify the save-when tag nor you want the default events to be used. Then you
 * can specify a "none" keyword or simply leave empty to indicate such cases.</p>
 * <pre>
 * &lt;a:bind value="person.firstName; save-when:none;"/>
 * &lt;textbox id="firstName"/>
 * </pre>
 * or
 * <pre>
 * &lt;a:bind value="person.firstName; save-when: ;"/>
 * &lt;textbox id="firstName"/>
 * </pre>
 * or
 * <pre>
 * &lt;textbox id="firstName" value="@{person.firstName, save-when='none'}"/>
 * </pre>
 * or
 * <pre>
 * &lt;textbox id="firstName" value="@{person.firstName, save-when=''}"/>
 * </pre>
 * </li>
 *
 * <p>Since 2.5.0, DataBinder supports validation phase before saving attribute content into bean property when 
 * triggered by the specified event in save-when tag. It will fire onBindingSave event to the data-binding component and
 * then fire onBindingValidate to the triggering component before really saving component attribute contents into bean's 
 * property. So application developers get the chance to handle the value validatiion before saving. In the following example
 * when end user click the "savebtn" button, an "onBindingSave" is first fired to "firtName" and "lastName" textboxes and 
 * then an "onBindingValidate" is fired to "savebtn" button. Application developers can register proper event handlers to do 
 * what they want to do.</p>
 * <pre>
 * &lt;textbox id="firstName" value="@{person.firstName, save-when="savebtn.onClick"}" onBindingSave="..."/>
 * &lt;textbox id="lastName" value="@{person.lastName, save-when="savebtn.onClick"}" onBindingSave="..."/>
 * &lt;button id="savebtn" label="save" onBindingValidate="..."/>
 * </pre>
 * 
 * <p>Note that the original textbox constraint mechanism is still there. This DataBinder validation phase is an 
 * add-on feature that can be applied to all components and attributes that use data binding mechanism.</p>
 *
 * <li>access. You can set the access mode of the attrY of the componentX to be "both"(load/save),  
 * "load"(load Only), "save"(save Only), or "none"(neither).  Multiple definition is NOT allowed 
 * and the later defined would 
 * override the previous defined one. The access mode would affects the behavior of the DataBinder's loadXxx
 * and saveXxx methods.
 * The {@link DataBinder#loadAll} and {@link DataBinder#loadComponent} would load only those attributes
 * with "both" or "load" access mode. The {@link DataBinder#saveAll} and 
 * {@link DataBinder#saveComponent} would save only those attributes with "both" or "save" access mode. If you
 * don't specify it, the default access mode depends on the natural characteristic of the component's attribute
 * as defined in lang-addon.xml. For example, Label.value is default to "load" access mode while Textbox.value 
 * is default to "both" access mode. For example, the following code snips tells DataBinder that Textbox "firstName" 
 * would allowing doing save into bean only not the other way.
 *
 * <p>Declare in front of the Component:</p>
 * <pre>
 * &lt;a:bind value="person.firstName;access:save;"/>
 * &lt;textbox id="firstName"/>
 * </pre>
 *
 * <p>Or specify directly on the Component's attribute:</p>
 * <pre>
 * &lt;textbox id="firstName" value="@{person.firstName, access='save'}"/>
 * </pre>
 * </li>
 *
 * <li>converter. You can specify the class name of the converter that implments the {@link TypeConverter} interface.
 * It is used to convert the value between component attribute and bean field.  Multiple definition is NOT allowed 
 * and the later defined would override the previous defined one.
 * Most of the time you don't have to specify this since this DataBinder supports converting most commonly 
 * used types. However, if you specify the TypeConverter class name, this DataBinder will new an instance and use 
 * it to cast the class.
 * </li>
 * </ul>
 * 
 * @since 2.4.0 Supporting @{...} annotations.
 * @since 2.5.0 Supporting multiple events of save-when tag and validation phase.
 * @author Henri Chen
 * @see AnnotateDataBinderInit
 * @see DataBinder
 */
public class AnnotateDataBinder extends DataBinder {
	/**
	 * Constructor that read all binding annotations of the components inside the specified desktop.
	 * @param desktop the ZUML desktop.
	 */
	public AnnotateDataBinder(Desktop desktop) {
		this(desktop, true);
	}
	
	/**
	 * Constructor that read all binding annotations of the components inside the specified page.
	 * @param page the ZUML page.
	 */
	public AnnotateDataBinder(Page page) {
		this(page, true);
	}
	
	/**
	 * Constructor that read all binding annotations in the components inside the specified component (inclusive).
	 * @param comp the ZUML component.
	 */
	public AnnotateDataBinder(Component comp) {
		this(comp, true);
	}

	/**
	 * Constructor that read all binding annotations of the components inside the specified desktop.
	 * @param desktop the ZUML desktop.
	 * @param defaultConfig whether load default binding configuration defined in lang-addon.xml
	 */
	public AnnotateDataBinder(Desktop desktop, boolean defaultConfig) {
		setDefaultConfig(defaultConfig);
		for (final Iterator	it = desktop.getComponents().iterator(); it.hasNext(); ) {
			loadAnnotations((Component) it.next());
		}			
	}
	
	/**
	 * Constructor that read all binding annotations of the components inside the specified page.
	 * @param page the ZUML page.
	 * @param defaultConfig whether load default binding configuration defined in lang-addon.xml
	 */
	public AnnotateDataBinder(Page page, boolean defaultConfig) {
		setDefaultConfig(defaultConfig);
		for (final Iterator it = page.getRoots().iterator(); it.hasNext(); ) {
			loadAnnotations((Component) it.next());
		}
	}
	
	/**
	 * Constructor that read all binding annotations in the components inside the specified component (inclusive).
	 * @param comp the ZUML component.
	 * @param defaultConfig whether load default binding configuration defined in lang-addon.xml
	 */
	public AnnotateDataBinder(Component comp, boolean defaultConfig) {
		setDefaultConfig(defaultConfig);
		loadAnnotations(comp);
	}
	
	
	private void loadAnnotations(Component comp) {
		loadComponentAnnotation(comp);
		loadComponentPropertyAnnotation(comp);

		final List children = comp.getChildren();
		for (final Iterator it = children.iterator(); it.hasNext(); ) {
			loadAnnotations((Component) it.next()); //recursive back
		}
	}
	
	private void loadComponentPropertyAnnotation(Component comp) {
		loadComponentPropertyAnnotationByAnnotName(comp, "default");
		loadComponentPropertyAnnotationByAnnotName(comp, "bind");
	}

	private void loadComponentPropertyAnnotationByAnnotName(Component comp, String annotName) {
		ComponentCtrl compCtrl = (ComponentCtrl) comp;
		final List props = compCtrl.getAnnotatedPropertiesBy(annotName);
		for (final Iterator it = props.iterator(); it.hasNext(); ) {
			final String propName = (String) it.next();
			//[0] value, [1] loadWhenEvents, [2] saveWhenEvents, [3] access, [4] converter
			final Object[] objs = loadPropertyAnnotation(comp, propName, annotName);
			addBinding(comp, propName, (String) objs[0], 
					(List) objs[1], (List) objs[2], (String) objs[3], (String) objs[4]);
		}
	}
	
	private void loadComponentAnnotation(Component comp) {
		loadComponentAnnotation(comp, "default");
		loadComponentAnnotation(comp, "bind");
	}
	
	private void loadComponentAnnotation(Component comp, String annotName) {
		ComponentCtrl compCtrl = (ComponentCtrl) comp;
		Annotation ann = compCtrl.getAnnotation(annotName);
		if (ann != null) {
			Map attrs = ann.getAttributes();
			for(final Iterator it = attrs.entrySet().iterator(); it.hasNext();) {
				Entry me = (Entry) it.next();
				String attr = (String) me.getKey();
				//[0] bean value, [1 ~ *] tag:expression
				List expr = parseExpression((String) me.getValue(), ";");
				if (expr == null || expr.get(0) == null) {
					throw new UiException("Cannot find any bean value in the annotation <a:bind "+attr+"=\"\"/> for component "+comp+", id="+comp.getId());
				} else {
					List tags = parseExpression((String)expr.get(0), ":");
					if (tags.size() > 1) {
						throw new UiException("bean value must be defined as the first statement in the annotation <a:bind "+attr+"=\"\"/> for component "+comp+", id="+comp.getId());
					}
				}
				
				List loadWhenEvents = null;
				List saveWhenEvents = null;
				String access = null;
				String converter = null;
				
				//process tags
				for(int j = 1; j < expr.size(); ++j) {
					List tags = parseExpression((String)expr.get(j), ":");
					if (tags == null) {
						continue; //skip
					}
					if ("load-when".equals(tags.get(0))) {
						if (tags.size() > 1 && tags.get(1) != null) {
							loadWhenEvents = parseExpression((String)tags.get(1), ",");
						} else {
							loadWhenEvents.add(NULLIFY);
						}
					} else if ("save-when".equals(tags.get(0))) {
						if (tags.size() > 1 && tags.get(1) != null) {
							saveWhenEvents = parseExpression((String)tags.get(1), ",");
						} else {
							saveWhenEvents.add(NULLIFY);
						}
					} else if ("access".equals(tags.get(0))) {
						access = tags.size() > 1 ? (String) tags.get(1) : NULLIFY;
					} else if ("converter".equals(tags.get(0))) {
						converter = tags.size() > 1 ? (String) tags.get(1) : NULLIFY;
					}
				}
				
				if (loadWhenEvents != null && loadWhenEvents.isEmpty()) {
					loadWhenEvents = null;
				}
				if (saveWhenEvents != null && saveWhenEvents.isEmpty()) {
					saveWhenEvents = null;
				}
				
				addBinding(comp, attr, (String) expr.get(0), loadWhenEvents, saveWhenEvents, access, converter);
			}
		}
	}
}
