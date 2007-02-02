/* UiEngineImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun  9 13:05:28     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.io.Writer;
import java.io.IOException;

import javax.servlet.jsp.el.FunctionMapper;

import org.zkoss.lang.D;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Objects;
import org.zkoss.lang.Threads;
import org.zkoss.lang.Exceptions;
import org.zkoss.lang.Expectable;
import org.zkoss.mesg.Messages;
import org.zkoss.util.logging.Log;

import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.sys.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.metainfo.*;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.scripting.*;
import org.zkoss.zk.au.*;

/**
 * An implementation of {@link UiEngine}.
 *
 * @author tomyeh
 */
public class UiEngineImpl implements UiEngine {
	private static final Log log = Log.lookup(UiEngineImpl.class);

	/** A pool of idle EventProcessingThread. */
	private final List _evtthds = new LinkedList();
	/** A map of suspended processing:
	 * (Desktop desktop, IdentityHashMap(Object mutex, List(EventProcessingThread)).
	 */
	private final Map _suspended = new HashMap();
	/** A map of resumed processing
	 * (Desktop desktop, List(EventProcessingThread)).
	 */
	private final Map _resumed = new HashMap();
	/** The maximal allowed # of event handling threads.*/
	private int _maxEvtThds;

	public UiEngineImpl() {
	}

	//-- UiEngine --//
	public void start(WebApp wapp) {
		final Integer v = wapp.getConfiguration().getMaxEventThreads();
		final int i = v != null ? v.intValue(): 100;
		_maxEvtThds = i > 0 ? i: 100;
	}
	public void stop(WebApp wapp) {
		synchronized (_evtthds) {
			for (Iterator it = _evtthds.iterator(); it.hasNext();)
				((EventProcessingThread)it.next()).cease();
			_evtthds.clear();
		}

		synchronized (_suspended) {
			for (Iterator it = _suspended.values().iterator(); it.hasNext();) {
				final Map map = (Map)it.next();
				synchronized (map) {
					for (Iterator i2 = map.values().iterator(); i2.hasNext();) {
						final List list = (List)i2.next();
						for (Iterator i3 = list.iterator(); i3.hasNext();)
							((EventProcessingThread)i3.next()).cease();
					}
				}
			}
			_suspended.clear();
		}
		synchronized (_resumed) {
			for (Iterator it = _resumed.values().iterator(); it.hasNext();) {
				final List list = (List)it.next();
				synchronized (list) {
					for (Iterator i2 = list.iterator(); i2.hasNext();)
						((EventProcessingThread)i2.next()).cease();
				}
			}
			_resumed.clear();
		}
	}

	public void cleanup(Desktop desktop) {
		if (log.debugable()) log.debug("Cleanup "+desktop);

		final Configuration conf = desktop.getWebApp().getConfiguration();
		final Map map;
		synchronized (_suspended) {
			map = (Map)_suspended.remove(desktop);
		}
		if (map != null) {
			synchronized (map) {
				for (Iterator it = map.values().iterator(); it.hasNext();) {
					final List list = (List)it.next();
					for (Iterator i2 = list.iterator(); i2.hasNext();) {
						final EventProcessingThread evtthd =
							(EventProcessingThread)i2.next();
						evtthd.ceaseSilently();
						conf.invokeEventThreadResumeAborts(
							evtthd.getComponent(), evtthd.getEvent());
					}
				}
			}
		}

		final List list;
		synchronized (_resumed) {
			list = (List)_resumed.remove(desktop);
		}
		if (list != null) {
			synchronized (list) {
				for (Iterator it = list.iterator(); it.hasNext();) {
					final EventProcessingThread evtthd =
						(EventProcessingThread)it.next();
					evtthd.ceaseSilently();
					conf.invokeEventThreadResumeAborts(
						evtthd.getComponent(), evtthd.getEvent());
				}
			}
		}
	}

	private static UiVisualizer getCurrentVisualizer() {
		final ExecutionCtrl execCtrl = ExecutionsCtrl.getCurrentCtrl();
		if (execCtrl == null)
			throw new IllegalStateException("Components can be accessed only in event listeners");
		return (UiVisualizer)execCtrl.getVisualizer();
	}
	public void pushOwner(Component comp) {
		getCurrentVisualizer().pushOwner(comp);
	}
	public void popOwner() {
		getCurrentVisualizer().popOwner();
	}
	public void addInvalidate(Page page) {
		if (page == null)
			throw new IllegalArgumentException();
		getCurrentVisualizer().addInvalidate(page);
	}
	public void addInvalidate(Component comp) {
		if (comp == null)
			throw new IllegalArgumentException();
		getCurrentVisualizer().addInvalidate(comp);
	}
	public void addSmartUpdate(Component comp, String attr, String value) {
		if (comp == null)
			throw new IllegalArgumentException();
		getCurrentVisualizer().addSmartUpdate(comp, attr, value);
	}
	public void addResponse(String key, AuResponse response) {
		getCurrentVisualizer().addResponse(key, response);
	}
	public void addMoved(Component comp, Component oldparent, Page oldpg, Page newpg) {
		if (comp == null)
			throw new IllegalArgumentException();
		getCurrentVisualizer().addMoved(comp, oldparent, oldpg, newpg);
	}
	/** Called before changing the component's UUID.
	 *
	 * @param addOnlyMoved if true, it is added only if it was moved
	 * before (see {@link #addMoved}).
	 */
	public void addUuidChanged(Component comp, boolean addOnlyMoved) {
		if (comp == null)
			throw new IllegalArgumentException();
		getCurrentVisualizer().addUuidChanged(comp, addOnlyMoved);
	}

	//-- Creating a new page --//
	public void execNewPage(Execution exec, Richlet richlet, Page page,
	Writer out) throws IOException {
		execNewPage0(exec, null, richlet, page, out);
	}
	public void execNewPage(Execution exec, PageDefinition pagedef,
	Page page, Writer out) throws IOException {
		execNewPage0(exec, pagedef, null, page, out);
	}
	/** It assumes exactly one of pagedef and richlet is not null.
	 */
	public void execNewPage0(final Execution exec, final PageDefinition pagedef,
	final Richlet richlet, final Page page, final Writer out) throws IOException {
		//Update the client type first. If this is the second page and not
		//belonging to the same client type, an exception is thrown
		final Desktop desktop = exec.getDesktop();
		final LanguageDefinition langdef = //default page
			pagedef != null ? pagedef.getLanguageDefinition():
			richlet != null ? richlet.getLanguageDefinition(): null;
		if (langdef != null)
			desktop.setClientType(langdef.getClientType()); //set and check!

		//It is possible this method is invoked when processing other exec
		final Execution oldexec = Executions.getCurrent();
		final ExecutionCtrl oldexecCtrl = (ExecutionCtrl)oldexec;
		final UiVisualizer olduv =
			oldexecCtrl != null ? (UiVisualizer)oldexecCtrl.getVisualizer(): null;

		final UiVisualizer uv;
		if (olduv != null) uv = doReactivate(exec, olduv);
		else uv = doActivate(exec, null);

		final ExecutionCtrl execCtrl = (ExecutionCtrl)exec;
		final Page old = execCtrl.getCurrentPage();
		final PageDefinition olddef = execCtrl.getCurrentPageDefinition();
		execCtrl.setCurrentPage(page);
		execCtrl.setCurrentPageDefinition(pagedef);

		final Configuration config = desktop.getWebApp().getConfiguration();
		boolean cleaned = false;
		try {
			config.invokeExecutionInits(exec, oldexec);

			if (olduv != null) olduv.setOwner(page);

			//Cycle 1: Creates all components

			//Note:
			//1) stylesheet, tablib are inited in Page's contructor
			//2) we add variable resolvers before init because
			//init's zscirpt might depend on it.
			if (pagedef != null) {
				page.addFunctionMapper(pagedef.getFunctionMapper());
				initVariableResolvers(pagedef, page);

				final Initiators inits = Initiators.doInit(pagedef, page);
				try {
					//Request 1472813: sendRedirect in init; test: sendRedirectNow.zul
					pagedef.init(page,
						!uv.isEverAsyncUpdate() && !uv.isAborting(), !uv.isAborting());
					if (!uv.isAborting())
						execCreate(exec, page, pagedef, null);
					inits.doAfterCompose(page);
				} catch(Throwable ex) {
					inits.doCatch(ex);
					throw UiException.Aide.wrap(ex);
				} finally {
					inits.doFinally();
				}
			} else {
				//FUTURE: a way to allow richlet to set page ID
				((PageCtrl)page).init(null, null, null, null);
				richlet.service(page);
			}

			//Cycle 2: process pending events
			Event event = nextEvent(uv);
			do {
				for (; event != null; event = nextEvent(uv)) {
					process(desktop, event);
					//Unlike execUpdate, we don't cache exception here
				}

				//Cycle 2a: processing resumed event processing
				resumeAll(desktop, uv, null);
			} while ((event = nextEvent(uv)) != null);

			//Cycle 3: Redraw the page (and responses)
			List responses = uv.getResponses();

			if (olduv != null && olduv.addToFirstAsyncUpdate(responses))
				responses = null;
				//A new ZK page might be included by an async update
				//(example: ZUL's include).
				//If so, we cannot generate the responses in the page.
				//Rather, we shall add them to the async update.

			((PageCtrl)page).redraw(responses, out);
		} catch (Throwable ex) {
			cleaned = true;

			final List errs = new LinkedList();
			errs.add(ex);
			config.invokeExecutionCleanups(exec, oldexec, errs);
				//CONSIDER: whether to pass cleanup's error to users

			if (!errs.isEmpty()) {
				ex = (Throwable)errs.get(0);
				if (ex instanceof IOException) throw (IOException)ex;
				throw UiException.Aide.wrap(ex);
			}
		} finally {
			if (!cleaned) config.invokeExecutionCleanups(exec, oldexec, null);
				//CONSIDER: whether to pass cleanup's error to users

			execCtrl.setCurrentPage(old); //restore it
			execCtrl.setCurrentPageDefinition(olddef); //restore it

			if (olduv != null) doDereactivate(exec, olduv);
			else doDeactivate(exec);
		}
	}

	private static final Event nextEvent(UiVisualizer uv) {
		final Event evt = ((ExecutionCtrl)uv.getExecution()).getNextEvent();
		return evt != null && !uv.isAborting() ? evt: null;
	}
	/** Cycle 1:
	 * Creates all child components defined in the specified definition.
	 * @return the first component being created.
	 */
	private static final Component execCreate(Execution exec, Page page,
	InstanceDefinition parentdef, Component parent)
	throws IOException {
		Component firstCreated = null;
		final PageDefinition pagedef = parentdef.getPageDefinition();
			//note: don't use page.getDefinition because createComponents
			//might be called from a page other than instance's
		for (Iterator it = parentdef.getChildren().iterator(); it.hasNext();) {
			final Object obj = it.next();
			if (obj instanceof InstanceDefinition) {
				final InstanceDefinition childdef = (InstanceDefinition)obj;
				final ForEach forEach = childdef.getForEach(page, parent);
				if (forEach == null) {
					if (isEffective(childdef, page, parent)) {
						final Component child =
							execCreateChild(exec, page, parent, childdef);
						if (firstCreated == null) firstCreated = child;
					}
				} else {
					while (forEach.next()) {
						if (isEffective(childdef, page, parent)) {
							final Component child =
								execCreateChild(exec, page, parent, childdef);
							if (firstCreated == null) firstCreated = child;
						}
					}
				}
			} else if (obj instanceof ZScript) {
				final ZScript script = (ZScript)obj;
				if (isEffective(script, page, parent)) {
					final Namespace ns = parent != null ?
						Namespaces.beforeInterpret(exec, parent):
						Namespaces.beforeInterpret(exec, page);
					try {
						page.interpret(script.getContent(page, parent), ns);
					} finally {
						Namespaces.afterInterpret(ns);
					}
				}
			} else {
				throw new IllegalStateException("Unknown object: "+obj);
			}
		}
		return firstCreated;
	}
	private static
	Component execCreateChild(Execution exec, Page page, Component parent,
	InstanceDefinition childdef) throws IOException {
		if (ComponentDefinition.ZK == childdef.getComponentDefinition()) {
			return execCreate(exec, page, childdef, parent);
		} else {
			final Component child = ((WebAppCtrl)exec.getDesktop().getWebApp())
				.getUiFactory().newComponent(page, parent, childdef);

			execCreate(exec, page, childdef, child); //recursive

			if (child instanceof AfterCompose)
				((AfterCompose)child).afterCompose();

			if (Events.isListenerAvailable(child, Events.ON_CREATE, false))
				Events.postEvent(
					new CreateEvent(Events.ON_CREATE, child, exec.getArg()));

			return child;
		}
	}
	private static final boolean isEffective(Condition cond,
	Page page, Component comp) {
		return comp != null ? cond.isEffective(comp): cond.isEffective(page);
	}

	public Component createComponents(Execution exec,
	PageDefinition pagedef, Page page, Component parent, Map arg) {
		if (pagedef == null)
			throw new IllegalArgumentException("pagedef");
		if (parent != null)
			page = parent.getPage();
		else if (page != null)
			parent = ((PageCtrl)page).getDefaultParent();

		final ExecutionCtrl execCtrl = (ExecutionCtrl)exec;
		if (!execCtrl.isActivated())
			throw new IllegalStateException("Not activated yet");

		final Page old = execCtrl.getCurrentPage();
		final PageDefinition olddef = execCtrl.getCurrentPageDefinition();
		execCtrl.setCurrentPage(page);
		execCtrl.setCurrentPageDefinition(pagedef);
		exec.pushArg(arg != null ? arg: Collections.EMPTY_MAP);

		//Note: we add taglib, stylesheets and var-resolvers to the page
		//it might cause name pollution but we got no choice since they
		//are used as long as components created by this method are alive
		if (page != null) page.addFunctionMapper(pagedef.getFunctionMapper());
		initVariableResolvers(pagedef, page);

		final Initiators inits = Initiators.doInit(pagedef, page);
		try {
			final Component comp = execCreate(exec, page, pagedef, parent);
			inits.doAfterCompose(page);
			return comp;
		} catch (Throwable ex) {
			inits.doCatch(ex);
			throw UiException.Aide.wrap(ex);
		} finally {
			exec.popArg();
			execCtrl.setCurrentPage(old); //restore it
			execCtrl.setCurrentPageDefinition(olddef); //restore it

			inits.doFinally();
		}
	}
	private static final void initVariableResolvers(PageDefinition pagedef,
	Page page) {
		final List resolvs = pagedef.newVariableResolvers(page);
		if (!resolvs.isEmpty())
			for (Iterator it = resolvs.iterator(); it.hasNext();)
				page.addVariableResolver((VariableResolver)it.next());
	}

	public void sendRedirect(String uri, String target) {
		if (uri != null && uri.length() == 0)
			uri = null;
		final UiVisualizer uv = getCurrentVisualizer();
		uv.setAbortingReason(
			new AbortBySendRedirect(
				uri != null ? uv.getExecution().encodeURL(uri): "", target));
	}
	public void setAbortingReason(AbortingReason aborting) {
		final UiVisualizer uv = getCurrentVisualizer();
		uv.setAbortingReason(aborting);
	}

	//-- Asynchronous updates --//
	public void execUpdate(Execution exec, List requests, Writer out)
	throws IOException {
		if (requests == null)
			throw new IllegalArgumentException("null requests");
		assert D.OFF || ExecutionsCtrl.getCurrentCtrl() == null:
			"Impossible to re-activate for update: old="+ExecutionsCtrl.getCurrentCtrl()+", new="+exec+", reqs="+requests;

		final Execution oldexec = Executions.getCurrent();
		final UiVisualizer uv = doActivate(exec, requests);
		if (uv == null)
			return; //done (request is added to the exec currently activated)

		final Desktop desktop = exec.getDesktop();
		final Configuration config = desktop.getWebApp().getConfiguration();
		final Monitor monitor = config.getMonitor();
		if (monitor != null) {
			try {
				monitor.beforeUpdate(desktop, requests);
			} catch (Throwable ex) {
				log.error(ex);
			}
		}

		boolean cleaned = false;
		try {
			config.invokeExecutionInits(exec, oldexec);
			final RequestQueue rque = ((DesktopCtrl)desktop).getRequestQueue();
			final List errs = new LinkedList();
			final long tmexpired = System.currentTimeMillis() + 3000;
				//Tom Yeh: 20060120
				//Don't process all requests if this thread has processed
				//a while. Thus, user could see the response sooner.
			for (AuRequest request; System.currentTimeMillis() < tmexpired
			&& (request = rque.nextRequest()) != null;) {
				//Cycle 1: Process one request
				//Don't process more such that requests will be queued
				//adn we have the chance to optimize them
				try {
					process(exec, request, !errs.isEmpty());
				} catch (ComponentNotFoundException ex) {
					//possible because the previous might remove some comp
					//so ignore it
					if (log.debugable()) log.debug("Component not found: "+request);
				} catch (Throwable ex) {
					handleError(ex, uv, errs);
					//we don't skip request to avoid mis-match between c/s
				}

				//Cycle 2: Process any pending events posted by components
				Event event = nextEvent(uv);
				do {
					for (; event != null; event = nextEvent(uv)) {
						try {
							process(desktop, event);
						} catch (Throwable ex) {
							handleError(ex, uv, errs);
							break; //skip the rest of events! 
						}
					}

					//Cycle 2a: processing resumed event processing
					resumeAll(desktop, uv, errs);
				} while ((event = nextEvent(uv)) != null);
			}

			//Cycle 3: Generate output
			cleaned = true;
			config.invokeExecutionCleanups(exec, oldexec, errs);

			List responses;
			try {
				//Note: we have to call visualizeErrors before uv.getResponses,
				//since it might create/update components
				if (!errs.isEmpty())
					visualizeErrors(exec, uv, errs);

				responses = uv.getResponses();
			} catch (Throwable ex) {
				responses = new LinkedList();
				responses.add(new AuAlert(Exceptions.getMessage(ex)));

				log.error(ex);
				errs.add(ex); //so invokeExecutionCleanups knows it
			}

			if (rque.hasRequest())
				responses.add(new AuEcho());

			responseSequenceId(desktop, out);
			response(responses, out);

			if (log.debugable())
				if (responses.size() < 5 || log.finerable()) log.debug("Responses: "+responses);
				else log.debug("Responses: "+responses.subList(0, 5)+"...");

			out.flush();
				//flush before deactivating to make sure it has been sent
		} catch (Throwable ex) {
			if (!cleaned) {
				cleaned = true;
				final List errs = new LinkedList();
				errs.add(ex);
				config.invokeExecutionCleanups(exec, oldexec, errs);
				ex = errs.isEmpty() ? null: (Throwable)errs.get(0);
			}

			if (ex != null) {
				if (ex instanceof IOException) throw (IOException)ex;
				throw UiException.Aide.wrap(ex);
			}
		} finally {
			if (!cleaned) config.invokeExecutionCleanups(exec, oldexec, null);

			doDeactivate(exec);

			if (monitor != null) {
				try {
					monitor.afterUpdate(desktop);
				} catch (Throwable ex) {
					log.error(ex);
				}
			}
		}
	}
	/** Handles each error. The erros will be queued to the errs list
	 * and processed later by {@link #visualizeErrors}.
	 */
	private static final
	void handleError(Throwable ex, UiVisualizer uv, List errs) {
		final Throwable err = ex;
		final Throwable t = Exceptions.findCause(ex, Expectable.class);
		if (t == null) {
			log.realCauseBriefly(ex);
		} else {
			ex = t;
			if (log.debugable()) log.debug(Exceptions.getRealCause(ex));
		}

		final String msg = Exceptions.getMessage(ex);
		if (ex instanceof WrongValueException) {
			final Component comp = ((WrongValueException)ex).getComponent();
			if (comp != null) {
				uv.addResponse("wrongValue", new AuAlert(comp, msg));
				return;
			}
		}

		errs.add(err);
	}
	/** Post-process the errors to represent them to the user.
	 * Note: errs must be non-empty
	 */
	private final
	void visualizeErrors(Execution exec, UiVisualizer uv, List errs) {
		final StringBuffer sb = new StringBuffer(128);
		for (Iterator it = errs.iterator(); it.hasNext();) {
			final Throwable t = (Throwable)it.next();
			if (sb.length() > 0) sb.append('\n');
			sb.append(Exceptions.getMessage(t));
		}
		final String msg = sb.toString();

		final Throwable err = (Throwable)errs.get(0);
		final String location =
			exec.getDesktop().getWebApp().getConfiguration().getErrorPage(err);
		if (location != null) {
			try {
				exec.setAttribute("javax.servlet.error.message", msg);
				exec.setAttribute("javax.servlet.error.exception", err);
				exec.setAttribute("javax.servlet.error.exception_type", err.getClass());
				exec.setAttribute("javax.servlet.error.status_code", new Integer(500));
				final Component c = exec.createComponents(location, null, null);
				if (c == null) {
					log.error("No component in "+location);
				} else {
					process(exec.getDesktop(), new Event(Events.ON_MODAL, c, null));
					return; //done
				}
			} catch (Throwable ex) {
				log.realCause("Unable to generate custom error page, "+location, ex);
			}
		}

		uv.addResponse(null, new AuAlert(msg)); //default handling
	}

	/** Processing the request and stores result into UiVisualizer.
	 * @param everError whether any error ever occured before processing this
	 * request.
	 */
	private void process(Execution exec, AuRequest request, boolean everError) {
		if (log.debugable()) log.debug("Processing request: "+request);

		final ExecutionCtrl execCtrl = (ExecutionCtrl)exec;
		execCtrl.setCurrentPage(request.getPage());
		request.getCommand().process(request, everError);
	}
	/** Processing the event and stores result into UiVisualizer. */
	private void process(Desktop desktop, Event event) {
		if (log.debugable()) log.debug("Processing event: "+event);
		final Component comp = event.getTarget();
		if (comp != null) {
			//Note: a component might be removed before event being processed
			if (comp.getPage() != null)
				processEvent(comp, event);
		} else {
			//since an event might change the page/desktop/component relation,
			//we copy roots first
			final List roots = new LinkedList();
			for (Iterator it = desktop.getPages().iterator(); it.hasNext();) {
				roots.addAll(((Page)it.next()).getRoots());
			}
			for (Iterator it = roots.iterator(); it.hasNext();) {
				final Component c = (Component)it.next();
				if (c.getPage() != null) //might be removed, so check first
					processEvent(c, event);
			}
		}
	}

	public void wait(Object mutex) throws InterruptedException {
		if (mutex == null)
			throw new IllegalArgumentException("null mutex");

		final Thread thd = Thread.currentThread();
		if (!(thd instanceof EventProcessingThread))
			throw new UiException("This method can be called only in an event listener, not in paging loading.");
		if (D.ON && log.finerable()) log.finer("Suspend "+thd+" on "+mutex);

		final EventProcessingThread evtthd = (EventProcessingThread)thd;
		evtthd.newEventThreadSuspends(mutex);
			//it may throw an exception, so process it before updating _suspended

		final Execution exec = Executions.getCurrent();
		final Desktop desktop = exec.getDesktop();

		Map map;
		synchronized (_suspended) {
			map = (Map)_suspended.get(desktop);
			if (map == null)
				_suspended.put(desktop, map = new IdentityHashMap(3));
					//note: we have to use IdentityHashMap because user might
					//use Integer or so as mutex
		}
		synchronized (map) {
			List list = (List)map.get(mutex);
			if (list == null)
				map.put(mutex, list = new LinkedList());
			list.add(evtthd);
		}
		try {
			EventProcessingThread.doSuspend(mutex);
		} catch (Throwable ex) {
			//error recover
			synchronized (map) {
				final List list = (List)map.get(mutex);
				if (list != null) {
					list.remove(evtthd);
					if (list.isEmpty()) map.remove(mutex);
				}
			}
			if (ex instanceof InterruptedException)
				throw (InterruptedException)ex;
			throw UiException.Aide.wrap(ex, "Unable to suspend "+evtthd);
		}
	}
	public void notify(Object mutex) {
		notify(Executions.getCurrent().getDesktop(), mutex);
	}
	public void notify(Desktop desktop, Object mutex) {
		if (desktop == null || mutex == null)
			throw new IllegalArgumentException("desktop and mutex cannot be null");

		final Map map;
		synchronized (_suspended) {
			map = (Map)_suspended.get(desktop);
			if (map == null) return; //nothing to notify
		}

		final EventProcessingThread evtthd;
		synchronized (map) {
			final List list = (List)map.get(mutex);
			if (list == null) return; //nothing to notify

			//Note: list is never empty
			evtthd = (EventProcessingThread)list.remove(0);
			if (list.isEmpty()) map.remove(mutex); //clean up
		}
		addResumed(desktop, evtthd);
	}
	public void notifyAll(Object mutex) {
		final Execution exec = Executions.getCurrent();
		if (exec == null)
			throw new UiException("resume can be called only in processing a request");
		notifyAll(exec.getDesktop(), mutex);
	}
	public void notifyAll(Desktop desktop, Object mutex) {
		if (desktop == null || mutex == null)
			throw new IllegalArgumentException("desktop and mutex cannot be null");

		final Map map;
		synchronized (_suspended) {
			map = (Map)_suspended.get(desktop);
			if (map == null) return; //nothing to notify
		}

		final List list;
		synchronized (map) {
			list = (List)map.remove(mutex);
			if (list == null) return; //nothing to notify
		}
		for (Iterator it = list.iterator(); it.hasNext();)
			addResumed(desktop, (EventProcessingThread)it.next());
	}
	/** Adds to _resumed */
	private void addResumed(Desktop desktop, EventProcessingThread evtthd) {
		if (D.ON && log.finerable()) log.finer("Ready to resume "+evtthd);
		List list;
		synchronized (_resumed) {
			list = (List)_resumed.get(desktop);
			if (list == null)
				_resumed.put(desktop, list = new LinkedList());
		}
		synchronized (list) {
			list.add(evtthd);
		}
	}

	/** Does the real resume.
	 * Note: {@link #resume} only puts a thread into a resume queue in execution.
	 */
	private void resumeAll(Desktop desktop, UiVisualizer uv, List errs) {
		//We have to loop because a resumed thread might resume others
		for (;;) {
			final List list;
			synchronized (_resumed) {
				list = (List)_resumed.remove(desktop);
				if (list == null) return; //nothing to resume; done
			}

			synchronized (list) {
				for (Iterator it = list.iterator(); it.hasNext();) {
					final EventProcessingThread evtthd =
						(EventProcessingThread)it.next();
					if (D.ON && log.finerable()) log.finer("Resume "+evtthd);
					if (uv.isAborting()) {
						evtthd.ceaseSilently();
					} else {
						try {
							if (evtthd.doResume()) //wait it complete or suspend again
								recycleEventThread(evtthd); //completed
						} catch (Throwable ex) {
							recycleEventThread(evtthd);
							if (errs == null) {
								log.error("Unable to resume "+evtthd, ex);
								throw UiException.Aide.wrap(ex);
							}
							handleError(ex, uv, errs);
						}
					}
				}
			}
		}
	}
	/** Process an event. */
	private void processEvent(Component comp, Event event) {
		if (comp.getPage() == null) {
			if (D.ON && log.debugable()) log.debug("Event is ignored due to dead");
			return; //nothing to do
		}

		EventProcessingThread evtthd = null;
		synchronized (_evtthds) {
			if (!_evtthds.isEmpty())
				evtthd = (EventProcessingThread)_evtthds.remove(0);
		}

		if (evtthd == null)
			evtthd = new EventProcessingThread();

		try {
			if (evtthd.processEvent(comp, event))
				recycleEventThread(evtthd);
		} catch (Throwable ex) {
			recycleEventThread(evtthd);
			throw UiException.Aide.wrap(ex);
		}
	}
	private void recycleEventThread(EventProcessingThread evtthd) {
		if (!evtthd.isCeased()) {
			if (evtthd.isIdle()) {
				synchronized (_evtthds) {
					if (_evtthds.size() < _maxEvtThds) {
						_evtthds.add(evtthd); //return to pool
						return; //done
					}
				}
			}
			evtthd.ceaseSilently();
		}
	}

	//-- Generate output from a response --//
	/** Output the next response sequence ID.
	 */
	private static void responseSequenceId(Desktop desktop, Writer out)
	throws IOException {
		final String attr = "org.zkoss.zk.ui.respId";
		final Integer val = (Integer)desktop.getAttribute(attr);
		final int i =
			val == null ? 0: (val.intValue() + 1) & 0x3ff; //range: 0 ~ 1023
		desktop.setAttribute(attr, new Integer(i));

		out.write("\n<sid>");
		out.write(Integer.toString(i));
		out.write("</sid>");
	}
	public void response(AuResponse response, Writer out)
	throws IOException {
		out.write("\n<r><c>");
		out.write(response.getCommand());
		out.write("</c>");
		final String[] data = response.getData();
		if (data != null) {
			for (int j = 0; j < data.length; ++j) {
				out.write("\n<d>");
				encodeXML(data[j], out);
				out.write("</d>");
			}
		}
		out.write("\n</r>");
	}
	public void response(List responses, Writer out)
	throws IOException {
		for (Iterator it = responses.iterator(); it.hasNext();)
			response((AuResponse)it.next(), out);
	}
	private static void encodeXML(String data, Writer out)
	throws IOException {
		if (data == null || data.length() == 0)
			return;

		//20051208: Tom Yeh
		//The following codes are tricky.
		//Reason:
		//1. nested CDATA is not allowed
		//2. Firefox (1.0.7)'s XML parser cannot handle over 4096 chars
		//	if CDATA is not used
		int j = 0;
		for (int k; (k = data.indexOf("]]>", j)) >= 0;) {
			encodeByCData(data.substring(j, k), out);
			out.write("]]&gt;");
			j = k + 3;
		}
		encodeByCData(data.substring(j), out);
	}
	private static void encodeByCData(String data, Writer out)
	throws IOException {
		for (int j = data.length(); --j >= 0;) {
			final char cc = data.charAt(j);
			if (cc == '<' || cc == '>' || cc == '&') {
				out.write("<![CDATA[");
				out.write(data);
				out.write("]]>");
				return;
			}
		}
		out.write(data);
	}

	public void activate(Execution exec) {
		assert D.OFF || ExecutionsCtrl.getCurrentCtrl() == null:
			"Impossible to re-activate for update: old="+ExecutionsCtrl.getCurrentCtrl()+", new="+exec;
		doActivate(exec, null);
	}
	public void deactivate(Execution exec) {
		doDeactivate(exec);
	}

	//-- Common private utilities --//
	/** Activates the specified execution for processing.
	 *
	 * @param requests a list of requests to process.
	 * Activation assumes it is asynchronous update if it is not null.
	 * @return the exec info if the execution is granted;
	 * null if request has been added to the exec currently activated
	 */
	private static UiVisualizer doActivate(Execution exec, List requests) {
		final Desktop desktop = exec.getDesktop();
		final DesktopCtrl desktopCtrl = (DesktopCtrl)desktop;
		final Session sess = desktop.getSession();
		final boolean asyncupd = requests != null;
		if (log.finerable()) log.finer("Activating "+desktop);

		assert D.OFF || Executions.getCurrent() == null: "Use doReactivate instead";

		final boolean inProcess = asyncupd
			&& desktopCtrl.getRequestQueue().addRequests(requests);

		//lock desktop
		final UiVisualizer uv;
		final Map eis = getVisualizers(sess);
		synchronized (eis) {
			for (;;) {
				final UiVisualizer old = (UiVisualizer)eis.get(desktop);
				if (old == null) break; //grantable

				if (inProcess) return null;

				try {
					eis.wait(120*1000);
				} catch (InterruptedException ex) {
					throw UiException.Aide.wrap(ex);
				}
			}

			//grant
			if (asyncupd) desktopCtrl.getRequestQueue().setInProcess();
				//set the flag asap to free more following executions
			eis.put(desktop, uv = new UiVisualizer(exec, asyncupd));
			desktopCtrl.setExecution(exec);
		}

		final ExecutionCtrl execCtrl = (ExecutionCtrl)exec;
		execCtrl.setVisualizer(uv);
		ExecutionsCtrl.setCurrent(exec);

		try {
			execCtrl.onActivate();
		} catch (Throwable ex) {
			doDeactivate(exec);
			throw UiException.Aide.wrap(ex);
		}
		return uv;
	}
	/** Deactivates the execution. */
	private static final void doDeactivate(Execution exec) {
		final Desktop desktop = exec.getDesktop();
		final Session sess = desktop.getSession();
		if (log.finerable()) log.finer("Deactivating "+desktop);

		final ExecutionCtrl execCtrl = (ExecutionCtrl)exec;
		try {
			try {
				execCtrl.onDeactivate();
			} catch (Throwable ex) {
				log.warning("Ignored: failed to deactivate "+desktop, ex);
			}

			//Unlock desktop
			final Map eis = getVisualizers(sess);
			synchronized (eis) {
				final Object o = eis.remove(desktop);
				assert D.OFF || o != null;
				((DesktopCtrl)desktop).setExecution(null);
				eis.notify(); //wakeup doActivate's wait
			}
		} finally {
			execCtrl.setCurrentPage(null);
			execCtrl.setVisualizer(null);
			ExecutionsCtrl.setCurrent(null);
		}

		final SessionCtrl sessCtrl = (SessionCtrl)sess;
		if (sessCtrl.isInvalidated()) sessCtrl.invalidateNow();
	}
	/** Re-activates for another execution. It is callable only for
	 * creating new page (execNewPage). It is not allowed for async-update.
	 * <p>Note: doActivate cannot handle reactivation. In other words,
	 * the caller has to detect which method to use.
	 */
	private static UiVisualizer doReactivate(Execution curExec, UiVisualizer olduv) {
		final Desktop desktop = curExec.getDesktop();
		final Session sess = desktop.getSession();
		if (log.finerable()) log.finer("Re-activating "+desktop);

		assert D.OFF || olduv.getExecution().getDesktop() == desktop:
			"old dt: "+olduv.getExecution().getDesktop()+", new:"+desktop;

		final UiVisualizer uv = new UiVisualizer(olduv, curExec);
		final Map eis = getVisualizers(sess);
		synchronized (eis) {
			final Object o = eis.put(desktop, uv);
			if (o != olduv)
				throw new InternalError(); //wrong olduv
			((DesktopCtrl)desktop).setExecution(curExec);
		}

		final ExecutionCtrl curCtrl = (ExecutionCtrl)curExec;
		curCtrl.setVisualizer(uv);
		ExecutionsCtrl.setCurrent(curExec);

		try {
			curCtrl.onActivate();
		} catch (Throwable ex) {
			doDereactivate(curExec, olduv);
			throw UiException.Aide.wrap(ex);
		}
		return uv;
	}
	/** De-reactivated exec. Work with {@link #doReactivate}.
	 */
	private static void doDereactivate(Execution curExec, UiVisualizer olduv) {
		if (olduv == null) throw new IllegalArgumentException("null");

		final Desktop desktop = curExec.getDesktop();
		final Session sess = desktop.getSession();
		if (log.finerable()) log.finer("Deactivating "+desktop);

		final ExecutionCtrl curCtrl = (ExecutionCtrl)curExec;
		try {
			curCtrl.onDeactivate();
		} catch (Throwable ex) {
			log.warning("Ignored: failed to deactivate "+desktop, ex);
		}
		curCtrl.setCurrentPage(null);
		curCtrl.setVisualizer(null); //free memory

		final Execution oldexec = olduv.getExecution();
		final Map eis = getVisualizers(sess);
		synchronized (eis) {
			eis.put(desktop, olduv);
			((DesktopCtrl)desktop).setExecution(oldexec);
		}
		ExecutionsCtrl.setCurrent(oldexec);
	}
	/** Returns a map of (Page, UiVisualizer). */
	private static Map getVisualizers(Session sess) {
		synchronized (sess) {
			final String attr = "org.zkoss.zk.ui.Visualizers";
			Map eis = (Map)sess.getAttribute(attr);
			if (eis == null)
				sess.setAttribute(attr, eis = new HashMap());
			return eis;
		}
	}
}
