/* DataBinder.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Feb  1 18:27:18     2007, Created by Henri
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zkplus.databind;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.metainfo.Annotation; 
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.scripting.Namespace;
import org.zkoss.zk.scripting.Interpreter;
import org.zkoss.zk.scripting.HierachicalAware;

import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListModel;

import org.zkoss.util.ModificationException;
import org.zkoss.lang.Objects;
import org.zkoss.lang.Primitives;
import org.zkoss.lang.reflect.Fields;

import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Map.Entry;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * The DataBinder used for binding ZK UI component and the backend data bean.
 *
 * @author Henri Chen
 */
public class DataBinder {
	public static final String NULLIFY = "none"; //used to nullify default configuration
	public static final String VARNAME = "zkplus.databind.VARNAME"; //_var name
	public static final String TEMPLATEMAP = "zkplus.databind.TEMPLATEMAP"; // template -> clone
	public static final String TEMPLATE = "zkplus.databind.TEMPLATE"; //clone -> template
	private static final String OWNER = "zkplus.databind.OWNER"; //the collection owner of the template component
	private static final String HASTEMPLATEOWNER = "zkplus.databind.HASTEMPLATEOWNER"; //whether has template owner (collection in collection)
	private static final Object NA = new Object();

	private Map _compBindingMap = new LinkedHashMap(29); //(comp, Map(attr, Binding))
	private Map _beans = new HashMap(29); //bean local to this DataBinder
	private Map _beanSameNodes = new HashMap(29); //(bean, Set(BindingNode)) bean same nodes, diff expression but actually hold the same bean
	private BindingNode _pathTree = new BindingNode("/", false, "/", false); //path dependency tree.
	private boolean _defaultConfig = true; //whether load default configuration from lang-addon.xml
	private boolean _init; //whether this databinder is initialized. 
	private EventListener _listener = new LoadOnSaveEventListener();
		//Databinder is init automatically when saveXXX or loadXxx is called
	
	protected Map _collectionItemMap = new HashMap(3);
	
	/** Binding bean to UI component. This is the same as 
	 * addBinding(Component comp, String attr, String expr, (List)null, (List)null, (String)null, (String)null). 
	 * @param comp The component to be associated.
	 * @param attr The attribute of the component to be associated.
	 * @param expr The expression to associate the data bean.
	 */
	public void addBinding(Component comp, String attr, String expr) {
		addBinding(comp, attr, expr, (List)null, (List)null, null, null);
	}

	/** Binding bean to UI component. 
	 * @param comp The component to be associated.
	 * @param attr The attribute of the component to be associated.
	 * @param expr The expression to associate the data bean.
	 * @param loadWhenEvents The event list when to load data.
	 * @param saveWhenEvent The event when to save data.
	 * @param access In the view of UI component: "load" load only, 
	 * "both" load/save, "save" save only when doing
	 * data binding. null means using the default access natural of the component. 
	 * e.g. Label.value is "load", but Textbox.value is "both".
	 * @param converter The converter class used to convert classes between component 
	 *  and the associated bean. null means using the default class conversion method.
	 */
	public void addBinding(Component comp, String attr, String expr,
		String[] loadWhenEvents, String saveWhenEvent, String access, String converter) {
		List loadEvents = null;
		if (loadWhenEvents != null && loadWhenEvents.length > 0) {
			loadEvents = new ArrayList(loadWhenEvents.length);
			for (int j = 0; j < loadWhenEvents.length; ++j) {
				loadEvents.add(loadWhenEvents[j]);
			}
		}
		addBinding(comp, attr, expr, loadEvents, saveWhenEvent, access, converter);
	}
	
	/** Binding bean to UI component. 
	 * @param comp The component to be associated.
	 * @param attr The attribute of the component to be associated.
	 * @param expr The expression to associate the data bean.
	 * @param loadWhenEvents The event list when to load data.
	 * @param saveWhenEvents The event when to save data.
	 * @param access In the view of UI component: "load" load only, 
	 * "both" load/save, "save" save only when doing
	 * data binding. null means using the default access natural of the component. 
	 * e.g. Label.value is "load", but Textbox.value is "both".
	 * @param converter The converter class used to convert classes between component 
	 *  and the associated bean. null means using the default class conversion method.
	 * @since 3.0.0
	 */
	public void addBinding(Component comp, String attr, String expr,
		String[] loadWhenEvents, String[] saveWhenEvents, String access, String converter) {
		List loadEvents = null;
		if (loadWhenEvents != null && loadWhenEvents.length > 0) {
			loadEvents = new ArrayList(loadWhenEvents.length);
			for (int j = 0; j < loadWhenEvents.length; ++j) {
				loadEvents.add(loadWhenEvents[j]);
			}
		}
		List saveEvents = null;
		if (saveWhenEvents != null && saveWhenEvents.length > 0) {
			saveEvents = new ArrayList(saveWhenEvents.length);
			for (int j = 0; j < saveWhenEvents.length; ++j) {
				saveEvents.add(saveWhenEvents[j]);
			}
		}
		addBinding(comp, attr, expr, loadEvents, saveEvents, access, converter);
	}
	
	/** Binding bean to UI component. 
	 * @param comp The component to be associated.
	 * @param attr The attribute of the component to be associated.
	 * @param expr The expression to associate the data bean.
	 * @param loadWhenEvents The event list when to load data.
	 * @param saveWhenEvent The event when to save data.
	 * @param access In the view of UI component: "load" load only, 
	 * "both" load/save, "save" save only when doing
	 * data binding. null means using the default access natural of the component. 
	 * e.g. Label.value is "load", but Textbox.value is "both".
	 * @param converter The converter class used to convert classes between component 
	 *  and the associated bean. null means using the default class conversion method.
	 */
	public void addBinding(Component comp, String attr, String expr,
		List loadWhenEvents, String saveWhenEvent, String access, String converter) {
		List saveEvents = new ArrayList(1);
		saveEvents.add(saveWhenEvent);
		addBinding(comp, attr, expr, loadWhenEvents, saveEvents, access, converter);
	}
	
	/** Binding bean to UI component. 
	 * @param comp The component to be associated.
	 * @param attr The attribute of the component to be associated.
	 * @param expr The expression to associate the data bean.
	 * @param loadWhenEvents The event list when to load data.
	 * @param saveWhenEvents The event list when to save data.
	 * @param access In the view of UI component: "load" load only, 
	 * "both" load/save, "save" save only when doing
	 * data binding. null means using the default access natural of the component. 
	 * e.g. Label.value is "load", but Textbox.value is "both".
	 * @param converter The converter class used to convert classes between component 
	 *  and the associated bean. null means using the default class conversion method.
	 * @since 3.0.0
	 */
	public void addBinding(Component comp, String attr, String expr,
		List loadWhenEvents, List saveWhenEvents, String access, String converter) {
			
		//Since 2.5, 20070726, Henri Chen: we accept "each" to replace "_var" in collection data binding
		//Before 2.4.1
		//<a:bind _var="person">
		//<listitem...>
		//After 2.5
		//<listitem self="@{bind(each='person')}"...>
		//or <listitem self="@{each='person'}"...>
		if ("each".equals(attr)) {
			attr = "_var";
		}
			
		if (isDefaultConfig()) { //use default binding configuration
			//handle default-bind defined in lang-addon.xml
			Object[] objs = loadPropertyAnnotation(comp, attr, "default-bind");
			
			/* logically impossible to hold "expr" in default binding 
			if (expr == null && objs[0] != null) {
				expr = (String) objs[0];
			}
			*/

			if (loadWhenEvents == null && objs[1] != null) {
				loadWhenEvents = (List) objs[1];
			}
			if (saveWhenEvents == null && objs[2] != null) {
				saveWhenEvents = (List) objs[2];
			}
			if (access == null && objs[3] != null) {
				access = (String) objs[3];
			}
			if (converter == null && objs[4] != null) {
				converter = (String) objs[4];
			}
		}
	
		//nullify check
		boolean nullify = false;
		LinkedHashSet loadEvents = null;
		if (loadWhenEvents != null && loadWhenEvents.size() > 0) {
			loadEvents = new LinkedHashSet(loadWhenEvents.size());
			for(final Iterator it = loadWhenEvents.iterator(); it.hasNext();) {
				final String event = (String) it.next();
				if (NULLIFY.equals(event)) {
					loadEvents.clear();
					nullify = true;
				} else {
					nullify = false;
					loadEvents.add(event);
				}
			}
			if (loadEvents.isEmpty()) { 
				loadEvents = null;
			}
		}

		nullify = false;
		Set saveEvents = null;
		if (saveWhenEvents != null && saveWhenEvents.size() > 0) {
			saveEvents = new HashSet(saveWhenEvents.size());
			for(final Iterator it = saveWhenEvents.iterator(); it.hasNext();) {
				final String event = (String) it.next();
				if (NULLIFY.equals(event)) {
					saveEvents.clear();
					nullify = true;
				} else {
					nullify = false;
					saveEvents.add(event);
				}
			}
			if (saveEvents.isEmpty()) { 
				saveEvents = null;
			}
		}

		if (NULLIFY.equals(access)) {
			access = null;
		}
		
		if (NULLIFY.equals(converter)) {
			converter = null;
		}
		
		Map attrMap = (Map) _compBindingMap.get(comp);
		if (attrMap == null) {
			attrMap = new LinkedHashMap(3);
			_compBindingMap.put(comp, attrMap);
		}
			
		if (attrMap.containsKey(attr)) { //override
			final Binding binding = (Binding) attrMap.get(attr);
			binding.setExpression(expr);
			binding.setLoadWhenEvents(loadEvents);
			binding.setSaveWhenEvents(saveEvents);
			binding.setAccess(access);
			binding.setConverter(converter);
		} else {
			attrMap.put(attr, new Binding(this, comp, attr, expr, loadEvents, saveEvents, access, converter));
		}
	}
	
	/** Remove the binding associated with the attribute of the component.
	 * @param comp The component to be removed the data binding association.
	 * @param attr The attribute of the component to be removed the data binding association.
	 */
	public void removeBinding(Component comp, String attr) {
		Map attrMap = (Map) _compBindingMap.get(comp);
		if (attrMap != null) {
			attrMap.remove(attr);
		}
	}

	/** Given component and attr, return the associated {@link Binding}.
	 * @param comp the concerned component
	 * @param attr the concerned attribute
	 */
	public Binding getBinding(Component comp, String attr) {
		if (isClone(comp)) {
			comp = (Component) comp.getAttribute(TEMPLATE);
		}
		Map attrMap = (Map) _compBindingMap.get(comp);
		return attrMap != null ?  (Binding) attrMap.get(attr) : null;
	}

	/** Given component, return the associated list of {@link Binding}s.
	 * @param comp the concerned component
	 */
	public Collection getBindings(Component comp) {
		if (isClone(comp)) {
			comp = (Component) comp.getAttribute(TEMPLATE);
		}
		Map attrMap = (Map)_compBindingMap.get(comp);
		return attrMap != null ?  (Collection) attrMap.values() : null;
	}
	
	/** Whether this component associated with any bindings.
	 */
	public boolean existsBindings(Component comp) {
		if (isClone(comp)) {
			comp = (Component) comp.getAttribute(TEMPLATE);
		}
		return _compBindingMap.containsKey(comp);
	}
	
	/** Whether this component and attribute associated with a binding.
	 */
	public boolean existBinding(Component comp, String attr) {
		if (isClone(comp)) {
			comp = (Component) comp.getAttribute(TEMPLATE);
		}
		if (_compBindingMap.containsKey(comp)) {
			Map attrMap = (Map) _compBindingMap.get(comp);
			return attrMap.containsKey(attr);
		}
		return false;
	}		
	
	/** Whether use the default binding configuration.
	 */
	public boolean isDefaultConfig(){
		return _defaultConfig;
	}
	
	/** Whether use the default binding configuration.
	 */
	public void setDefaultConfig(boolean b) {
		_defaultConfig = b;
	}
	
	/** Bind a real bean object to the specified beanid. You might not need to call this method because this
	 * DataBinder would look up the variable via the {@link org.zkoss.zk.ui.Component#getVariable} method
	 * if it cannot find the specified bean via the given beanid.
	 *
	 * @param beanid The bean id used in data binding.
	 * @param bean The real bean object to be associated with the bean id.
	 */
	public void bindBean(String beanid, Object bean) {
		_beans.put(beanid, bean);
	}
	
	
	/** Load value from the data bean property to a specified attribute of the UI component.
	 * @param comp the UI component to be loaded value.
	 * @param attr the UI component attribute to be loaded value.
	 */
	public void loadAttribute(Component comp, String attr) {
		if (isTemplate(comp) || comp.getPage() == null) {
			return; //skip detached component
		}
		init();
		Binding binding = getBinding(comp, attr);
		if (binding != null) {
			binding.loadAttribute(comp);
		}
	}			

	/** Save value from a specified attribute of the UI component to a data bean property.
	 * @param comp the UI component used to save value into backend data bean.
	 * @param attr the UI component attribute used to save value into backend data bean.
	 */
	public void saveAttribute(Component comp, String attr) {
		if (isTemplate(comp) || comp.getPage() == null) {
			return; //skip detached component
		}
		init();
		Binding binding = getBinding(comp, attr);
		if (binding != null) {
			binding.saveAttribute(comp);
		}
	}
	
	/** Load values from the data bean properties to all attributes of a specified UI component. 
	 * @param comp the UI component to be loaded value.
	 */
	public void loadComponent(Component comp) {
		if (isTemplate(comp) || comp.getPage() == null) {
			return; //skip detached component
		}
		init();
		Collection bindings = getBindings(comp);
		if (bindings != null) {
			loadAttrs(comp, bindings);
		}
			
		//load kids of this component
		for(final Iterator it = comp.getChildren().iterator(); it.hasNext();) {
			loadComponent((Component) it.next()); //recursive
		}
	}
	
	/** Save values from all attributes of a specified UI component to data bean properties.
	 * @param comp the UI component used to save value into backend data bean.
	 */
	public void saveComponent(Component comp) {
		if (isTemplate(comp) || comp.getPage() == null) {
			return; //skip detached component
		}
		init();
		Collection bindings = getBindings(comp);
		if (bindings != null) {
			saveAttrs(comp, bindings);
		}

		//save kids of this component
		for(final Iterator it = comp.getChildren().iterator(); it.hasNext();) {
			saveComponent((Component) it.next()); //recursive
		}
	}
	
	/** Load all value from data beans to UI components. */
	public void loadAll() {
		init();
		for (final Iterator it = _compBindingMap.keySet().iterator(); it.hasNext(); ) {
			final Component comp = (Component) it.next();
			loadComponent(comp);
		}
	}
	
	/** Save all values from UI components to beans. */
	public void saveAll() {
		init();
		for (final Iterator it = _compBindingMap.keySet().iterator(); it.hasNext(); ) {
			final Component comp = (Component) it.next();
			saveComponent(comp);
		}
	}

	private void loadAttrs(String expr, Collection attrs) {
		for(final Iterator it = attrs.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			Component comp = binding.getComponent();
			binding.loadAttribute(comp, expr);
		}
	}	

	private void loadAttrs(Component comp, Collection attrs) {
		for(final Iterator it = attrs.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.loadAttribute(comp);
		}
	}	

	private void saveAttrs(Component comp, Collection attrs) {
		for(final Iterator it = attrs.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.saveAttribute(comp);
		}
	}

	//[0] expr, [1] loadWhenEvents, [2] saveWhenEvents, [3] access, [4] converter
	protected Object[] loadPropertyAnnotation(Component comp, String propName, String bindName) {
		ComponentCtrl compCtrl = (ComponentCtrl) comp;
		Annotation ann = compCtrl.getAnnotation(propName, bindName);
		if (ann != null) {
			final Map attrs = ann.getAttributes(); //(tag, tagExpr)
			List loadWhenEvents = null;
			List saveWhenEvents = null;
			String access = null;
			String converter = null;
			String expr = null;
			for (final Iterator it = attrs.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String tag = (String) entry.getKey();
				String tagExpr = (String) entry.getValue();
				if ("save-when".equals(tag)) {
					saveWhenEvents = parseExpression(tagExpr, ",");
				} else if ("access".equals(tag)) {
					access = tagExpr;
				} else if ("converter".equals(tag)) {
					converter = tagExpr;
				} else if ("load-when".equals(tag)) {
					loadWhenEvents = parseExpression(tagExpr, ",");
				} else if ("value".equals(tag)) {
					expr = tagExpr;
				}
			}
			return new Object[] {expr, loadWhenEvents, saveWhenEvents, access, converter};
		}
		return new Object[5];
	}
	
	//late init
	protected void init() {
		if (!_init) {
			_init = true;

			// init CollectionItem
			initCollectionItem();
			
			//setup all added bindings
			final Set varnameSet = new HashSet();
			final LinkedHashSet toBeDetached = new LinkedHashSet();
			for(final Iterator it = _compBindingMap.entrySet().iterator(); it.hasNext(); ) {
				final Entry me = (Entry) it.next();
				final Component comp = (Component) me.getKey();
				final Map attrMap = (Map) me.getValue();
				final Collection bindings = attrMap.values();
				
				//_var special case; meaning a template component
				if (attrMap.containsKey("_var")) {
					setupTemplateComponent(comp, getComponentCollectionOwner(comp)); //setup as template components
					String varname = ((Binding)attrMap.get("_var")).getExpression();
					varnameSet.add(varname);
					comp.setAttribute(VARNAME, varname);
					setupBindingRenderer(comp); //setup binding renderer
					toBeDetached.add(comp);
				}

				if (bindings != null) {
					//construct the path dependant tree
					setupPathTree(bindings, varnameSet);
				
					//register save-when event
					registerSaveEvents(comp, bindings);
					
					//register load-when events
					registerLoadEvents(comp, bindings);
				}
			}
            
			//detach template components so they will not interfer the visual part
			for(final Iterator it = toBeDetached.iterator(); it.hasNext(); ) {
				final Component comp = (Component) it.next();
				comp.detach();
			}
		}
	}
	
	private void initCollectionItem(){
		addCollectionItem(Listitem.class.getName(), new ListitemCollectionItem());
		addCollectionItem(Row.class.getName(), new RowCollectionItem());
		addCollectionItem(Comboitem.class.getName(), new ComboitemCollectionItem());
	}
	
	/**
	 * Adds a CollectionItem for this comp.
	 * @see CollectionItem
	 * @since 3.0.0
	 */
	public void addCollectionItem(String comp, CollectionItem decor){
		_collectionItemMap.put(comp, decor);
	}
	
	//get Collection owner of a given collection item.
	private Component getComponentCollectionOwner(Component comp) {		
		CollectionItem decor = getBindingCollectionItem(comp);
		return decor.getComponentCollectionOwner(comp);		
	}
	/**
	 * Returns a CollectionItem by the comp accordingly.
	 * @see CollectionItem
	 * @since 3.0.0
	 */
	protected CollectionItem getBindingCollectionItem(Component comp){
		String name = comp.getClass().getName();
		if (comp instanceof Listitem) {
			name = Listitem.class.getName();
		} else if (comp instanceof Row) {
			name = Row.class.getName();
		}
		CollectionItem decorName = (CollectionItem)_collectionItemMap.get(name);
		if(decorName != null){
			return decorName;
		}else{
			throw new UiException("Cannot find associated CollectionItem:"+comp);
		}		
	}
	//get Collection owner of a given collection item.
	private Component getCollectionOwner(Component comp) {
		if (isTemplate(comp)) {
			return (Component) comp.getAttribute(OWNER);
		}
		return getComponentCollectionOwner(comp);
	}
	
	//get associated clone of a given bean and template component
	private Component getCollectionItem(Component comp, Object bean) {
		Component owner = getCollectionOwner(comp);	
		CollectionItem decor = getBindingCollectionItem(comp);
		final ListModel xmodel = decor.getModelByOwner(owner);
		if (xmodel instanceof BindingListModel) {
  			final BindingListModel model = (BindingListModel) xmodel;
  			int index = model.indexOf(bean);
  			if (index >= 0) {
    			return lookupClone(decor.getComponentAtIndexByOwner(owner, index), comp);
    		}
    	}
		return null;
	}
  		
	//set the binding renderer for the template listitem component
	private void setupBindingRenderer(Component comp) {
		getBindingCollectionItem(comp).setupBindingRenderer(comp, this);		
	}
	
	private void setupPathTree(Collection bindings, Set varnameSet) {
		for(final Iterator it = bindings.iterator(); it.hasNext(); ) {
			final Binding binding = (Binding) it.next();
			String[] paths = binding.getPaths();
			for(int j = 0; j < paths.length; ++j) {
				final String path = (String) paths[j];
				_pathTree.addBinding(path, binding, varnameSet);
			}
		}
	}
	
	private void registerSaveEvents(Component comp, Collection bindings) {
		for(final Iterator it = bindings.iterator(); it.hasNext(); ) {
			final Binding binding = (Binding) it.next();
			binding.registerSaveEvents(comp);
		}
	}

	private void registerLoadEvents(Component comp, Collection bindings) {
		for(final Iterator it = bindings.iterator(); it.hasNext(); ) {
			final Binding binding = (Binding) it.next();
			binding.registerLoadEvents(comp);
		}
	}

	
	//whether exists the specified bean in this DataBinder.
	/* package */ boolean existsBean(String beanid) {
		return _beans.containsKey(beanid);
	}

	//get a bean by the beanid from this Data binder
	/* package */ Object getBean(String beanid) {
		return _beans.get(beanid);
	}

	//set a bean into this Data binder
	/* package */ void setBean(String beanid, Object bean) {
		_beans.put(beanid, bean);
	}
	
	/**
	 * Sets up the specified comp and its decendents to be as template (or not)
	 */
	public void setupTemplateComponent(Component comp, Object owner) {
		if (existsBindings(comp)) {
			if (comp.getAttribute(OWNER) != null) {
				comp.setAttribute(HASTEMPLATEOWNER, Boolean.TRUE); //owner is a template
			}
			comp.setAttribute(OWNER, owner);
		}
		List kids = comp.getChildren();
		for(final Iterator it = kids.iterator(); it.hasNext(); ) {
			setupTemplateComponent((Component) it.next(), owner); //recursive
		}
	}
	
	//parse token and return as a List of String
	/* package */ static List parseExpression(String expr, String separator) {
		if (expr == null) {
			return null;
		}
		List results = new ArrayList(5);
		while(true) {
			int j = expr.indexOf(separator);
			if (j < 0) {
				results.add(expr.trim());
				return results;
			}
			results.add(expr.substring(0, j).trim());

			if (expr.length() <= (j+1)) {
				return results;
			}
			expr = expr.substring(j+1);
		}
	}

	//whether a component is a binding template rather than a real component
	/* package */ static boolean isTemplate(Component comp) {
		return comp.getAttribute(OWNER) != null;
	}
	
	//whether a cloned component from the template.
	/* package */ static boolean isClone(Component comp) {
		//bug #1813055  Multiple listboxes with same selectedItem causes NPE
		return comp != null && (comp.getAttribute(TEMPLATE) instanceof Component);
	}
	
	//whether has template owner (collection in collection)
	/* package */ static boolean hasTemplateOwner(Component comp) {
		//bug #1813055  Multiple listboxes with same selectedItem causes NPE
		return comp != null && (comp.getAttribute(HASTEMPLATEOWNER) != null);
	}
	
	//set a bean to SameNode Set 
	/* package */ void setBeanSameNodes(Object bean, Set set) {
		_beanSameNodes.put(bean, set);
	}
	
	//get SameNode Set of the given bean
	/* package */ Set getBeanSameNodes(Object bean) {
		return (Set) _beanSameNodes.get(bean);
	}
	
	//remove SameNode set of the given bean
	/* package */ Set removeBeanSameNodes(Object bean) {
		return (Set) _beanSameNodes.remove(bean);
	}

	/** traverse the path nodes and return the final bean.
	 */
	/* package */ Object getBeanAndRegisterBeanSameNodes(Component comp, String path) {
		return myGetBeanWithExpression(comp, path, true);
	}
	
	private Object getBeanWithExpression(Component comp, String path) {
		return myGetBeanWithExpression(comp, path, false);
	}
	
	private Object myGetBeanWithExpression(Component comp, String path, boolean registerNode) {
		Object bean = null;
		BindingNode currentNode = _pathTree;
		final List nodeids = parseExpression(path, ".");
		final Iterator it = nodeids.iterator();
		if (it != null && it.hasNext()) {
			String nodeid = (String) it.next();
			currentNode = (BindingNode) currentNode.getKidNode(nodeid);
			if (currentNode == null) {
				throw new UiException("Cannot find the specified databind bean expression:" + path);
			}
			bean = lookupBean(comp, nodeid);
			if (registerNode) {
				registerBeanNode(bean, currentNode);
			}
		} else {
			throw new UiException("Incorrect format of databind bean expression:" + path);
		}
		
		while(bean != null && it.hasNext()) {
			String nodeid = (String) it.next();
			currentNode = (BindingNode) currentNode.getKidNode(nodeid);
			if (currentNode == null) {
				throw new UiException("Cannot find the specified databind bean expression:" + path);
			}
			bean = fetchValue(bean, currentNode, nodeid, registerNode);
		}

		return bean;
	}
	
	private Object fetchValue(Object bean, BindingNode node, String nodeid, boolean registerNode) {
		if (bean != null) {
			try {
				bean = Fields.get(bean, nodeid);
			} catch (NoSuchMethodException ex) {
				//feature#1766905 Binding to Map
				if (bean instanceof Map) {
					bean = ((Map)bean).get(nodeid);
				} else {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		if (registerNode) {
			registerBeanNode(bean, node);
		}
		return bean;
	}
	

	/* package */ void setBeanAndRegisterBeanSameNodes(Component comp, Object val, Binding binding, 
	String path, boolean autoConvert, Object rawval, List loadOnSaveInfos) {
		Object orgVal = null;
		Object bean = null;
		BindingNode currentNode = _pathTree;
		boolean refChanged = false; //wether this setting change the reference
		String beanid = null;
		final List nodeids = parseExpression(path, ".");
		final List nodes = new ArrayList(nodeids.size());
		final Iterator it = nodeids.iterator();
		if (it != null && it.hasNext()) {
			beanid = (String) it.next();
			currentNode = (BindingNode) currentNode.getKidNode(beanid);
			if (currentNode == null) {
				throw new UiException("Cannot find the specified databind bean expression:" + path);
			}
			nodes.add(currentNode);
			bean = lookupBean(comp, beanid);
		} else {
			throw new UiException("Incorrect format of databind bean expression:" + path);
		}
		
		if (!it.hasNext()) { //assign back to where bean is stored
			orgVal = bean;
			if(Objects.equals(orgVal, val)) {
				return; //same value, no need to do anything
			}
			if (existsBean(beanid)) {
				setBean(beanid, val);
			} else if (!setZScriptVariable(comp, beanid, val)) {
				comp.setVariable(beanid, val, false);
			}
			refChanged = true;
		} else {
			if (bean == null) {
				return; //no bean to set value, skip
			}
			int sz = nodeids.size() - 2; //minus first and last beanid in path
			for(;bean != null && it.hasNext() && sz > 0; --sz) {
				beanid = (String) it.next();
				currentNode = (BindingNode) currentNode.getKidNode(beanid);
				if (currentNode == null) {
					throw new UiException("Cannot find the specified databind bean expression:" + path);
				}
				nodes.add(currentNode);
				try {
					bean = Fields.get(bean, beanid);
				} catch (NoSuchMethodException ex) {
					//feature#1766905 Binding to Map
					if (bean instanceof Map) {
						bean = ((Map)bean).get(beanid);
					} else {
						throw UiException.Aide.wrap(ex);
					}
				}
			}
			if (bean == null) {
				return; //no bean to set value, skip
			}
			beanid = (String) it.next();
			try {
				orgVal = Fields.get(bean, beanid);
				if(Objects.equals(orgVal, val)) {
					return; //same value, no need to do anything
				}
				Fields.set(bean, beanid, val, autoConvert);
			} catch (NoSuchMethodException ex) {
				//feature#1766905 Binding to Map
				if (bean instanceof Map) {
					((Map)bean).put(beanid, val);
				} else {
					throw UiException.Aide.wrap(ex);
				}
			} catch (ModificationException ex) {
				throw UiException.Aide.wrap(ex);
			}
			if (!isPrimitive(val) && !isPrimitive(orgVal)) { //val is a bean (null is not primitive)
				currentNode = (BindingNode) currentNode.getKidNode(beanid);
				if (currentNode == null) {
					throw new UiException("Cannot find the specified databind bean expression:" + path);
				}
				nodes.add(currentNode);
				bean = orgVal;
				refChanged = true;
			}
		}
		
		if (val != null) {
			if (refChanged && !binding.isLoadable() && binding.isSavable()) { //the sameNodes should change accordingly.
				registerBeanNode(val, currentNode);
			}
			//20070309, Henri Chen: Tricky. 
			//When loading page, listbox.selectedItem == null. The _var Listitem will not be able to 
			//associate with the selectedItem (no way to associate via null bean). When end user then 
			//select one Listitem, we have to force such association.
			if (rawval instanceof Component) {
				Binding varbinding = getBinding((Component) rawval, "_var");
				if (varbinding != null) {
					registerBeanNode(val, currentNode);
					getBeanAndRegisterBeanSameNodes((Component) rawval, varbinding.getExpression());
				}
			}
		}
			
		//TODO:Henri Chen: Is it possible to make the loadOnSave event to be called once only for a
		//setXxx. So avoid load a node several times?
			
		//register "onLoadSave" listener to this component if have not done so.
		if (!comp.isListenerAvailable("onLoadOnSave", true)) {
			comp.addEventListener("onLoadOnSave", _listener);
		}

		Object[] loadOnSaveInfo = 
			new Object[] {this, currentNode, binding, (refChanged ? val : bean), Boolean.valueOf(refChanged), nodes, comp};
		if (loadOnSaveInfos != null) {
			loadOnSaveInfos.add(loadOnSaveInfo);
		} else {
			//do loadOnSave immediately
			Events.postEvent(new Event("onLoadOnSave", comp, loadOnSaveInfo));
		}
	}
	
	private void registerBeanNode(Object bean, BindingNode node) {
		if (isPrimitive(bean)) {
			return;
		}
		final Set nodeSameNodes = node.getSameNodes();
		final Set binderSameNodes = getBeanSameNodes(bean);
		//variable node(with _var) is special. Assume selectedItem then _var. 
		//e.g. a Listitem but no selectedItem yet
		if (node.isVar() && binderSameNodes == null) {
			return;
		}
		
		if (!nodeSameNodes.contains(bean)) {
			//remove the old bean
			for(final Iterator it = nodeSameNodes.iterator(); it.hasNext();) {
				final Object elm = it.next();
				if (!(elm instanceof BindingNode)) {
					it.remove();
					removeBeanSameNodes(elm); //remove the binderSameNodes of the original bean
					break;
				}
			}
			//add the new bean if not null
			if (bean != null) {
				nodeSameNodes.add(bean);
			}
		}

		if (binderSameNodes == null) {
			if (bean != null) {
				setBeanSameNodes(bean, nodeSameNodes);
			}
		} else {
			node.mergeAndSetSameNodes(binderSameNodes);
		}
		
	}

	private boolean isPrimitive(Object bean) {
		//String is deemed as primitive and null is not primitive
		return (bean instanceof String) 
			|| (bean != null && Primitives.toPrimitive(bean.getClass()) != null)
			|| (bean instanceof Date)
			|| (bean instanceof Number);
	}
	
	/** Sets the variable to all loaded interpreters, if it was defined in
	 * the interpreter.
	 *
	 * @return whether it is set to the interpreter
	 */
	private boolean setZScriptVariable(Component comp, String beanid, Object val) {
		//for all loaded interperter, assign val to beanid
		boolean found = false;
		final Namespace ns = comp.getNamespace();
		for(final Iterator it = comp.getPage().getLoadedInterpreters().iterator();
		it.hasNext();) {
			final Interpreter ip = (Interpreter) it.next();
			if (ip instanceof HierachicalAware) {
				final HierachicalAware ha = (HierachicalAware)ip;
				if (ha.containsVariable(ns, beanid)) {
					ha.setVariable(ns, beanid, val);
					found = true;
				}
			} else if (ip.containsVariable(beanid)) {
				ip.setVariable(beanid, val);
				found = true;
			}
		}
		return found;
	}

	/*package*/ Object lookupBean(Component comp, String beanid) {
		//fetch the bean object
		Object bean = null;
		if (isClone(comp)) {
			bean = myLookupBean1(comp, beanid);
			if (bean != NA) {
				return bean;
			}
		}
		if (existsBean(beanid)) {
			bean = getBean(beanid);
		} else if (beanid.startsWith("/")) { //a absolute component Path: // or /
			bean = Path.getComponent(beanid);
		} else if (beanid.startsWith(".")) { //a relative component Path: ./ or ../
			bean = Path.getComponent(comp.getSpaceOwner(), beanid);
		} else {
			final Page page = comp.getPage();
			if (page != null)
				bean = page.getZScriptVariable(comp.getNamespace(), beanid);
			if (bean == null)
				bean = comp.getVariable(beanid, false);
		}
		return bean;
	}

	//given a beanid and a template, return the associated bean
	//return NA if cannot find it
	private Object myLookupBean1(Component comp, String beanid) {
		Map templatemap = (Map) comp.getAttribute(TEMPLATEMAP);
		return myLookupBean2(beanid, templatemap);
	}
	private Object myLookupBean2(String beanid, Map templatemap) {
		if (templatemap != null) {
			if (templatemap.containsKey(beanid)) { //got it
				return templatemap.get(beanid);
			} else { //search up the parent templatemap
				templatemap = (Map) templatemap.get(TEMPLATEMAP);
				return myLookupBean2(beanid, templatemap); //recursive
			}
		}
		return NA; //not available
	}

	//given a clone and a template, return the associated clone of that template.
	/*package*/ static Component lookupClone(Component srcClone, Component srcTemplate) {
		if (isTemplate(srcTemplate)) {
			Map templatemap = (Map) srcClone.getAttribute(TEMPLATEMAP);
			return myLookupClone(srcTemplate, templatemap);
		}
		return null;
	}
	private static Component myLookupClone(Component srcTemplate, Map templatemap) {
		if (templatemap != null) {
			if (templatemap.containsKey(srcTemplate)) { //got it
				return (Component) templatemap.get(srcTemplate);
			} else { //search up the parent templatemap
				templatemap = (Map) templatemap.get(TEMPLATEMAP);
				return myLookupClone(srcTemplate, templatemap); //recursive
			}
		}
		return null;
	}

	// Given parentNode, path, and level, return associate same kid nodes of parent
	// a1.b.c -> a2.b.c, a3.b.c, ...
	private Set getAssociateSameNodes(BindingNode parentNode, String path, int level) {
		final List nodeids = DataBinder.parseExpression(path, ".");
		final int sz = nodeids.size();
		final List subids = nodeids.subList(sz - level, sz);

		Object bean = null;
		for (final Iterator it = parentNode.getSameNodes().iterator(); it.hasNext();) {
			Object obj = it.next();
			if (!(obj instanceof BindingNode)) {
				bean = obj;
				break;
			}
		}
		
		//for each same node, find the associated kid node
		final Set assocateSameNodes = new HashSet();
		for (final Iterator it = parentNode.getSameNodes().iterator(); it.hasNext();) {
			//locate the associate kid node
			BindingNode currentNode = null;
			final Object obj = it.next();
			if (!(obj instanceof BindingNode) || currentNode == parentNode) {
				continue;
			}

			currentNode = (BindingNode) obj;
			for(final Iterator itx = subids.iterator(); itx.hasNext();) {
				final String nodeid = (String) itx.next();
				currentNode = (BindingNode) currentNode.getKidNode(nodeid);
				if (currentNode == null) {
					break;
				}
			}

			if (currentNode != null) {
				if (!currentNode.isVar()) {
					assocateSameNodes.add(currentNode);
				} else { //a var node, specialcase, find the var root
					Component varRootComp = getVarRootComponent(currentNode);
					assocateSameNodes.add(new Object[] {currentNode, varRootComp});
				}
			}
		}
		return assocateSameNodes;
	}
	
	private Component getVarRootComponent(BindingNode node) {
		final BindingNode varRootNode = node.getRootNode(_pathTree);
		
		Object bean = null;
		for (final Iterator it = varRootNode.getSameNodes().iterator(); it.hasNext();) {
			Object obj = it.next();
			if (!(obj instanceof BindingNode)) {
				bean = obj;
				break;
			}
		}
		
		Component comp = null;
		for(final Iterator itx = varRootNode.getBindings().iterator(); itx.hasNext();) {
			Binding binding = (Binding) itx.next();
			if ("_var".equals(binding.getAttr())) {
				comp = binding.getComponent();
				break;
			}
		}

		return getCollectionItem(comp, bean);		
	}
	
	private class LoadOnSaveEventListener implements EventListener {
		public LoadOnSaveEventListener() {
		}
		
		//-- EventListener --//
		public void onEvent(Event event) {
			final Set walkedNodes = new HashSet(32);
			final Set loadedBindings = new HashSet(32*2);

			Object obj = event.getData();
			if (obj instanceof List) {
				for(final Iterator it = ((List)obj).iterator(); it.hasNext();) {
					final Object[] data = (Object[]) it.next();
					doLoad(data, walkedNodes, loadedBindings);
				}
			} else {
				doLoad((Object[]) obj, walkedNodes, loadedBindings);
			}
		}
		
		private void doLoad(Object[] data, Set walkedNodes, Set loadedBindings) {
			if (!data[0].equals(DataBinder.this)) {
				return; //not for this DataBinder, skip
			}
			final BindingNode node = (BindingNode) data[1]; //to be loaded nodes
			final Binding savebinding = (Binding) data[2]; //to be excluded binding
			final Object bean = data[3]; //saved bean
			final boolean refChanged = ((Boolean) data[4]).booleanValue(); //whether bean itself changed
			final List nodes = (List) data[5]; //the complete nodes along the path to the node
			final Component savecomp = (Component) data[6]; //saved comp that trigger this load-on-save event
			if (savecomp != null) {
				loadAllNodes(bean, node, savecomp, savebinding, refChanged, nodes, walkedNodes, loadedBindings);
			}
		}
		
		/** Load all associated BindingNodes below the given nodes (depth first traverse).
		 */
		private void loadAllNodes(Object bean, BindingNode node, Component collectionComp, 
			Binding savebinding, boolean refChanged, List nodes, Set walkedNodes, Set loadedBindings) {
			myLoadAllNodes(bean, node, collectionComp, walkedNodes, savebinding, loadedBindings, refChanged);

			//for each ancestor, find associated same nodes			
			if (!nodes.isEmpty()) {
				final String path = node.getPath();
				int level = 1;
				for(final ListIterator it = nodes.listIterator(nodes.size()-1); it.hasPrevious(); ++level) {
					final BindingNode parentNode = (BindingNode) it.previous();
					final Set associateSameNodes = getAssociateSameNodes(parentNode, path, level);
					for(final Iterator itx = associateSameNodes.iterator(); itx.hasNext();) {
						Object obj = itx.next();
						if (obj instanceof BindingNode) {
							BindingNode samenode = (BindingNode) obj;
							myLoadAllNodes(bean, samenode, collectionComp, walkedNodes, savebinding, loadedBindings, refChanged);
						} else {
							BindingNode samenode = (BindingNode)((Object[])obj)[0];
							Component varRootComp = (Component) ((Object[])obj)[1];
							myLoadAllNodes(bean, samenode, varRootComp, walkedNodes, savebinding, loadedBindings, refChanged);

						}
					}
				}
			}
		}


		
		private void myLoadAllNodes(Object bean, BindingNode node, Component collectionComp,
		Set walkedNodes, Binding savebinding, Set loadedBindings, boolean refChanged) {
			if (walkedNodes.contains(node)) {
				return; //already walked, skip
			}
			//mark as walked already
			walkedNodes.add(node);

			//the component might have been removed
			if (collectionComp == null) {
				return;
			}
			//loading
			collectionComp = loadBindings(bean, node, collectionComp, savebinding, loadedBindings, refChanged);
			
			for(final Iterator it = node.getKidNodes().iterator(); it.hasNext();) {
				final BindingNode kidnode = (BindingNode) it.next();
				final Object kidbean = fetchValue(bean, kidnode, kidnode.getNodeId(), true);
				myLoadAllNodes(kidbean, kidnode, collectionComp, walkedNodes, savebinding, loadedBindings, true); //recursive
			}
			
			for(final Iterator it = new ArrayList(node.getSameNodes()).iterator(); it.hasNext();) {
				final Object obj = it.next();
				if (obj instanceof BindingNode) {
					final BindingNode samenode = (BindingNode) obj;
					if (node == samenode) {
						continue;
					}
					if (samenode.isVar()) { // -> var node
						//var node must traverse from the root 
						//even a root, must make sure the samebean (could be diff)
						//even the same bean, if a inner var root(collection in collection), not a real root
						if (!samenode.isRoot() || !isSameBean(samenode, bean) || samenode.isInnerCollectionNode()) { 
							continue;
						}
					} else if (node.isVar() && !isSameBean(samenode, bean)) { //var -> !var, must same bean
						continue;
					}
					myLoadAllNodes(bean, samenode, collectionComp, walkedNodes, savebinding, loadedBindings, refChanged); //recursive
				}
			}
		}
	
		//return nearest collection item Component (i.e. Listitem)
		private Component loadBindings(Object bean, BindingNode node, Component collectionComp, 
		Binding savebinding, Set loadedBindings, boolean refChanged) {
			final Collection bindings = node.getBindings();
			for(final Iterator it = bindings.iterator(); it.hasNext();) {
				final Binding binding = (Binding) it.next();
				if (loadedBindings.contains(binding)) {
					continue;
				}
				loadedBindings.add(binding);

				// bug 1775051: a multiple selection Listbox. When onSelect and loadOnSave cause 
				// setSelectedItem (loading) to be called and cause deselection of other multiple 
				// selected items. Must skip such case.
				
				// save binding that cause this loadOnSave, no need to load again.
				if (binding == savebinding) {
					continue;
				}

				Component comp = binding.getComponent();
				if (isTemplate(comp)) { //a template component, locate the listitem
					Component clonecomp = null;
					if (isClone(collectionComp)) { //A listbox in listbox
						clonecomp = lookupClone(collectionComp, comp);
					} else {
						clonecomp = getCollectionItem(comp, bean);
					}
					if ("_var".equals(binding.getAttr())) {
						if (clonecomp == null) { //the comp is in another Listbox
							clonecomp = getCollectionItem(comp, bean);
						}
						collectionComp = clonecomp;
					}
					comp = clonecomp;
				}
				
				if (refChanged) {
					binding.loadAttribute(comp);
				}
			}
			return collectionComp;
		}
		private boolean isSameBean(BindingNode node, Object bean) {	
			final Collection bindings = node.getBindings();
			if (bindings.isEmpty()) {
				return true;
			}
			final Component comp = ((Binding)bindings.iterator().next()).getComponent();
			if (isTemplate(comp)) {
				return true;
			}
			final Object nodebean = getBeanWithExpression(comp, node.getPath());
			return Objects.equals(nodebean, bean);
		}
	}
}
