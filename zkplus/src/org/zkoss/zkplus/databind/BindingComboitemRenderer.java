/* BindingComboitemRenderer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 3, 2008 10:54:54 AM , Created by jumperchen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkplus.databind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.sys.ComponentsCtrl;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
/**
 * @author jumperchen
 * @since 3.0.2
 */
/*package*/ class BindingComboitemRenderer implements org.zkoss.zul.ComboitemRenderer, org.zkoss.zul.ComboitemRendererExt, Serializable {
	private static final String KIDS = "zkplus.databind.KIDS";
	private Comboitem _template;
	private DataBinder _binder;
	private int x = 0;
	
	public BindingComboitemRenderer(Comboitem template, DataBinder binder) {
		_template = template;
		_binder = binder;
	}

	//link cloned components with bindings of templates
	private void linkTemplates(Component clone, Component template, Map templatemap) {
		if (_binder.existsBindings(template)) {
			templatemap.put(template, clone);
			clone.setAttribute(_binder.TEMPLATEMAP, templatemap);
			clone.setAttribute(_binder.TEMPLATE, template);
		}
		
		//Combobox in Listbox, Combobox in Grid no need to process
		if (template instanceof Grid || template instanceof Listbox) {
			return;
		}
		
		final Iterator itt = template.getChildren().iterator();
		final Iterator itc = clone.getChildren().iterator();
		while (itt.hasNext()) {
			final Component t = (Component) itt.next();
			final Component c = (Component) itc.next();
			linkTemplates(c, t, templatemap);	//recursive
		}
	}
	
	//setup id of cloned components (cannot called until the component is attached to Listbox)
	private void setupCloneIds(Component clone) {
		//bug #1813271: Data binding generates duplicate ids in grids/listboxes
		clone.setId("@" + clone.getUuid() + x++); //init id to @uuid to avoid duplicate id issue

		//Combobox in Listbox, Combobox in Grid no need to process
		if (clone instanceof Grid || clone instanceof Listbox) {
			return;
		}
		
		for(final Iterator it = clone.getChildren().iterator(); it.hasNext(); ) {
			setupCloneIds((Component) it.next()); //recursive
		}
	}

	public void render(Comboitem item, Object bean) throws Exception {
		final List kids = (List) item.getAttribute(KIDS);
		item.getChildren().addAll(kids);
		//item.removeAttribute(KIDS);
			
		//remove template mark of cloned component and its decendant
		_binder.setupTemplateComponent(item, null); 
			
		//setup clone id
		setupCloneIds(item);

		//bind bean to the associated listitem and its decendant
		final String varname = (String) _template.getAttribute(_binder.VARNAME);
		final Map templatemap = (Map) item.getAttribute(_binder.TEMPLATEMAP);
		templatemap.put(varname, bean);

		//apply the data binding
		_binder.loadComponent(item);
	}

	public Comboitem newComboitem(Combobox combobox) {
		//clone from template
		final Comboitem clone = (Comboitem)_template.clone();
		//TODO: see if databinder has this kind of Comboitem, if not, add new CollectionListItem 
		//avoid duplicate id error, will set to new id when render()
		if (!ComponentsCtrl.isAutoId(clone.getId())) {
			clone.setId("@"+ clone.getUuid() + x++);
		}
					
		//link cloned component with template
		//each Comboitem and and it decendants share the same templatemap
		Map templatemap = new HashMap(7);
		linkTemplates(clone, _template, templatemap);
		
		//link this template map to parent templatemap (Combobox in Combobox)
		Map parenttemplatemap = (Map) combobox.getAttribute(_binder.TEMPLATEMAP);
		if (parenttemplatemap != null) {
				templatemap.put(_binder.TEMPLATEMAP, parenttemplatemap);
		}
		//kept clone kids somewhere to avoid create too many components in browser
		final List kids = new ArrayList(clone.getChildren());
		clone.setAttribute(KIDS, kids);
		clone.getChildren().clear();
		return clone;
	}
}
