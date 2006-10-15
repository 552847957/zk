/* Execution.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jun  3 17:55:01     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui;

import java.util.Map;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.security.Principal;

import javax.servlet.jsp.el.VariableResolver;

import org.zkoss.idom.Document;
import org.zkoss.web.servlet.Servlets;
import org.zkoss.web.servlet.http.Encodes;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Evaluator;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.au.AuResponse;

/**
 * An execution of a client request (e.g., ServletRequest).
 * When a request sent from a client, the server constructs
 * a {@link Execution}
 * object to hold execution relevant info, and then serves the request
 * thru this execution.
 *
 * <p>A client request, e.g., HttpServletRequest, might consist of
 * multiple ZK request ({@link org.zkoss.zk.au.AuRequest}).
 * However, these ZK requests
 * must target the same desktop of pages ({@link Page}).
 *
 * <p>Because a request might come from HTTP or other protocol, Execution
 * also serves as an isolation layer.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 * @see Page
 */
public interface Execution extends Evaluator {
	/** Returns the desktop for this execution.
	 * Each execution is against exactly one desktop.
	 */
	public Desktop getDesktop();

	/** Returns whether this execution does asynchronous updates for the
	 * specified page (thru zkau).
	 *
	 * @return false if page is null, or page is not the page being
	 * asynchronous updated.
	 * Each execution remembers what page is being creating (or none being
	 * created). All other pages are consided as being asynchronous updated.
	 */
	public boolean isAsyncUpdate(Page page);

	/** Returns an array of String objects containing all of the values
	 * the given request parameter has, or null if the parameter does not exist.
	 */
	public String[] getParameterValues(String name);
	/** Returns the value of a request parameter as a String,
	 * or null if the parameter does not exist
	 */
	public String getParameter(String name);
	/** Returns a Map of the parameters of this request.
	 * Request parameters are extra information sent with the request.
	 */
	public Map getParameterMap();

	/** Evluates the specified expression with ${link #getVariableResolver}
	 * and {@link Page#getFunctionMapper} of the page of the specified
	 * component.
	 *
	 * <p>The function mapper is retrieved from component's page's function
	 * mapper ({@link Page#getFunctionMapper}).
	 * If null, the current page, if any, is used to retrieve
	 * the mapper.
	 *
	 * @param comp used as the self variable and to retrieve the function
	 * mapper. Ignored if null.
	 * @see #getVariableResolver
	 */
	public Object evaluate(Component comp, String expr, Class expectedType);
	/** Evluates the specified expression with ${link #getVariableResolver}
	 * and {@link Page#getFunctionMapper} of the specified
	 * page.
	 *
	 * <p>The function mapper is retrieved from component's page's function
	 * mapper ({@link Page#getFunctionMapper}).
	 * If null, the current page, if any, is used to retrieve
	 * the mapper.
	 *
	 * @param page used as the self variable and to retrieve the function
	 * mapper. Ignored if null.
	 * @see #getVariableResolver
	 */
	public Object evaluate(Page page, String expr, Class expectedType);

	/** Returns the variable resolver for this execution, or null if not
	 * available.
	 *
	 * <p>Note: the resolver is similar to PageContext's if this execution
	 * is caused by a HTTP request.
	 * @see #evaluate(Component,String,Class)
	 */
	public VariableResolver getVariableResolver();

	/** Queues an event to the current execution.
	 * The event will be processed (as if it is sent from the client).
	 */
	public void postEvent(Event evt);

	/** Whether to overwrite uri if both uri and params contain the same
	 * parameter.
	 */
	public static final int OVERWRITE_URI = Servlets.OVERWRITE_URI;
	/** Whether to ignore params if both uri and params contain the same
	 * parameter.
	 */
	public static final int IGNORE_PARAM = Servlets.IGNORE_PARAM;
	/** Whether to append params if both uri and params contain the same
	 * parameter. In other words, they both appear as the final query string.
	 */
	public static final int APPEND_PARAM = Servlets.APPEND_PARAM;
	/** Whether the specified parameters shall be passed thru the request's
	 * attribute called arg.
	 */
	public static final int PASS_THRU_ATTR = Servlets.PASS_THRU_ATTR;
	/** Includes a page.
	 *
	 * @param writer the output to write. If null, the response's default
	 * writer is used.
	 * @param page the page's uri; null to denote the same request
	 * @param mode one of {@link #OVERWRITE_URI}, {@link #IGNORE_PARAM},
	 * {@link #APPEND_PARAM} and {@link #PASS_THRU_ATTR}.
	 * It defines how to handle if both uri and params contains
	 * the same parameter.
	 * mode is used only if both uri contains query string and params is
	 * not empty.
	 */
	public void include(Writer writer, String page, Map params, int mode)
	throws IOException;
	/** A shortcut of include(null, page, null, 0).
	 */
	public void include(String page)
	throws IOException;
	/** Forwards to another page.
	 *
	 * <p>Note: this method can be called only when loading a page.
	 * Use {@link #sendRedirect(String)} instead if you want to change
	 * to another desktop when processing a request from the client.
	 *
	 * @param writer the output to write. If null, the response's default
	 * writer is used.
	 * @param page the page's uri; null to denote the same request
	 * @param mode one of {@link #OVERWRITE_URI}, {@link #IGNORE_PARAM},
	 * {@link #APPEND_PARAM} and {@link #PASS_THRU_ATTR}.
	 * It defines how to handle if both uri and params contains
	 * the same parameter.
	 * mode is used only if both uri contains query string and params is
	 * not empty.
	 */
	public void forward(Writer writer, String page, Map params, int mode)
	throws IOException;
	/** A shortcut of forward(null, page, null, 0).
	 */
	public void forward(String page)
	throws IOException;
	/** Returns whether this execution is included some other pages.
	 */
	public boolean isIncluded();

	/** Converts the specified URI to an absolute URI, if uri is related
	 * and the current execution is not included ({@link #isIncluded}).
	 *
	 * <p>Note: an asynchrous update is processed by the update servlet.
	 * It is different from the servlet for rendering the ZUML page.
	 * In other words, a relative URI won't be interpreted correctly,
	 * so you have to invoke this method to convert them if necessary.
	 *
	 * <p>In addtions, RequestDispatcher.include doesn't handle related URI
	 * well.
	 */
	public String toAbsoluteURI(String uri);

	/** Encodes an URL.
	 *
	 * <p>It resolves "*" contained in URI, if any, to the proper Locale,
	 * and the browser code.
	 * Refer to {@link Servlets#locate(ServletContext, ServletRequest, String, Locator)}
	 * for details. 
	 */
	public String encodeURL(String uri);

	/** Returns a java.security.Principal object containing the name of the
	 * current authenticated user.
	 * If the user has not been authenticated, the method returns null.
	 */
	public Principal getUserPrincipal();
	/** Returns a boolean indicating whether the authenticated user is
	 * included in the specified logical "role". Roles and role membership
	 * can be defined using deployment descriptors.
	 * If the user has not been authenticated, the method returns false.
	 *
	 * @param role a String specifying the name of the role
	 */
	public boolean isUserInRole(String role);
	/** Returns the login of the user making this request, if the user has
	 * been authenticated, or null if the user has not been authenticated.
	 * Whether the user name is sent with each subsequent request depends on
	 * the browser and type of authentication.
	 */
	public String getRemoteUser();
	/** Returns the fully qualified name of the client or the last proxy
	 * that sent the request.
	 * If the engine cannot or chooses not to resolve the hostname
	 * (to improve performance), this method returns the dotted-string form of
	 * the IP address.
	 */
	public String getRemoteName();
	/**  Returns the Internet Protocol (IP) address of the client or last
	 * proxy that sent the request.
	 */
	public String getRemoteAddr();
	/** Returns the host name of the server to which the request was sent.
	 * It is the value of the part before ":" in the Host header value, if any,
	 * or the resolved server name, or the server IP address.
	 *
	 * @see #getLocalName
	 */
	public String getServerName();
	/** Returns the port number to which the request was sent.
	 * It is the value of the part after ":" in the Host header value, if any,
	 * or the server port where the client connection was accepted on.
	 *
	 * @see #getLocalPort
	 */
	public int getServerPort();
	/** Returns the host name of the Internet Protocol (IP) interface
	 * on which the request was received.
	 *
	 * <p>Note: it is the host name defined in the server. To retrieve the name
	 * in URL, use {@link #getServerName}.
	 *
	 * @see #getServerName
	 */
	public String getLocalName();
	/** Returns the Internet Protocol (IP) address of the interface on which
	 * the request was received.
	 */
	public String getLocalAddr();
	/** Returns the Internet Protocol (IP) port number of the interface on which
	 * the request was received.
	 *
	 * @see #getServerPort
	 */
	public int getLocalPort();

	/** Returns the portion of the request URI that indicates the context of
	 * the desktop. The path starts with a "/" character but does not end with
	 * a "/" character. For servlets in the default (root) context,
	 * this method returns "".
	 * <p>If the client is not using HTTP to access, this method return "";
	 */
	public String getContextPath();

	//-- page utilities --//
	/** Returns the page definition from the page file specified by an URI.
	 *
	 * <p>Implemetation Notes: this method must invoke
	 * {@link org.zkoss.zk.ui.sys.UiFactory#getPageDefinition(org.zkoss.zk.ui.sys.RequestInfo, String)}
	 *
	 * @param uri the URI of the page file.
	 *
	 * @see #getPageDefinitionDirectly(String, String)
	 * @see #getPageDefinitionDirectly(Document, String)
	 * @see #getPageDefinitionDirectly(Reader, String)
	 */
	public PageDefinition getPageDefinition(String uri);
	/** Converts the specified page content to a page definition.
	 *
	 * <p>Implemetation Notes: this method must invoke
	 * {@link org.zkoss.zk.ui.sys.UiFactory#getPageDefinitionDirectly(org.zkoss.zk.ui.sys.RequestInfo, String, String)}
	 *
	 * @param content the raw content of the page. It must be in ZUML.
	 * @param extension the default extension if the content doesn't specify
	 * an language. Ignored if null.
	 * If the content doesn't specify an language, {@link LanguageDefinition#lookupByExtension}
	 * is called.
	 * @see #getPageDefinitionDirectly(Document, String)
	 * @see #getPageDefinitionDirectly(Reader, String)
	 * @see #getPageDefinition
	 */
	public PageDefinition getPageDefinitionDirectly(String content, String extension);
	/** Converts the specified page content, in DOM, to a page definition.
	 *
	 * @param content the raw content of the page in DOM.
	 * @param extension the default extension if the content doesn't specify
	 * an language. Ignored if null.
	 * If the content doesn't specify an language, {@link LanguageDefinition#lookupByExtension}
	 * is called.
	 * @see #getPageDefinitionDirectly(String, String)
	 * @see #getPageDefinitionDirectly(Reader, String)
	 * @see #getPageDefinition
	 */
	public PageDefinition getPageDefinitionDirectly(Document content, String extension);
	/** Reads the raw content from a reader and converts it into
	 * a page definition.
	 *
	 * @param reader used to input the raw content of the page. It must be in ZUML.
	 * @param extension the default extension if the content of reader doesn't specify
	 * an language. Ignored if null.
	 * If the content doesn't specify an language, {@link LanguageDefinition#lookupByExtension}
	 * is called.
	 * @see #getPageDefinitionDirectly(String, String)
	 * @see #getPageDefinitionDirectly(Document, String)
	 * @see #getPageDefinition
	 */
	public PageDefinition getPageDefinitionDirectly(Reader reader, String extension)
	throws IOException;

	/** Creates components from the specified page definition.
	 *
	 * @param pagedef the page definition to use. It cannot be null.
	 * @param parent the parent component, or null if you want it to be
	 * a root component. If parent is null, the page is assumed to be
	 * the current page, which is determined by the execution context.
	 * @param arg a map of parameters that is accessible by the arg variable
	 * in EL, or by {@link Execution#getArg}.
	 * Ignored if null.
	 * @return the first component being created.
	 * @see #createComponents(String, Component, Map)
	 */
	public Component createComponents(PageDefinition pagedef,
	Component parent, Map arg);
	/** Creates components from a page file specified by an URI.
	 *
	 * <p>It loads the page definition from the specified URI (by
	 * use {@link #getPageDefinition} ), and then
	 * invokes {@link #createComponents(PageDefinition,Component,Map)}
	 * to create components.
	 *
	 * @param parent the parent component, or null if you want it to be
	 * a root component. If parent is null, the page is assumed to be
	 * the current page, which is determined by the execution context.
	 * @param arg a map of parameters that is accessible by the arg variable
	 * in EL, or by {@link Execution#getArg}.
	 * Ignored if null.
	 * @see #createComponents(PageDefinition, Component, Map)
	 * @see #createComponentsDirectly(String, String, Component, Map)
	 * @see #createComponentsDirectly(Document, String, Component, Map)
	 * @see #createComponentsDirectly(Reader, String, Component, Map)
	 */
	public Component createComponents(String uri, Component parent,
	Map arg);
	/** Creates components from the raw content specified by a string.
	 *
	 * <p>The raw content is parsed to a page defintion by use of
	 * {@link #getPageDefinitionDirectly(String, String)}, and then
	 * invokes {@link #createComponents(PageDefinition,Component,Map)}
	 * to create components.
	 *
	 * @param content the raw content of the page. It must be in ZUML.
	 * @param extension the default extension if the content doesn't specify
	 * an language. Ignored if null.
	 * If the content doesn't specify an language, {@link LanguageDefinition#lookupByExtension}
	 * is called.
	 * @param parent the parent component, or null if you want it to be
	 * a root component. If parent is null, the page is assumed to be
	 * the current page, which is determined by the execution context.
	 * @param arg a map of parameters that is accessible by the arg variable
	 * in EL, or by {@link Execution#getArg}.
	 * Ignored if null.
	 * @see #createComponents(PageDefinition, Component, Map)
	 * @see #createComponents(String, Component, Map)
	 * @see #createComponentsDirectly(Document, String, Component, Map)
	 * @see #createComponentsDirectly(Reader, String, Component, Map)
	 */
	public Component createComponentsDirectly(String content, String extension,
	Component parent, Map arg);
	/** Creates components from the raw content specified by a DOM tree.
	 *
	 * <p>The raw content is parsed to a page defintion by use of
	 * {@link #getPageDefinitionDirectly(Document, String)}, and then
	 * invokes {@link #createComponents(PageDefinition,Component,Map)}
	 * to create components.
	 *
	 * @param content the raw content in DOM.
	 * @param extension the default extension if the content doesn't specify
	 * an language. Ignored if null.
	 * If the content doesn't specify an language, {@link LanguageDefinition#lookupByExtension}
	 * is called.
	 * @param parent the parent component, or null if you want it to be
	 * a root component. If parent is null, the page is assumed to be
	 * the current page, which is determined by the execution context.
	 * @param arg a map of parameters that is accessible by the arg variable
	 * in EL, or by {@link Execution#getArg}.
	 * Ignored if null.
	 * @see #createComponents(PageDefinition, Component, Map)
	 * @see #createComponents(String, Component, Map)
	 * @see #createComponentsDirectly(Document, String, Component, Map)
	 * @see #createComponentsDirectly(Reader, String, Component, Map)
	 */
	public Component createComponentsDirectly(Document content, String extension,
	Component parent, Map arg);
	/** Creates components from the raw content read from the specified reader.
	 *
	 * <p>The raw content is loaded and parsed to a page defintion by use of
	 * {@link #getPageDefinitionDirectly(Reader,String)}, and then
	 * invokes {@link #createComponents(PageDefinition,Component,Map)}
	 * to create components.
	 *
	 * @param reader the reader to retrieve the raw content in ZUML.
	 * @param extension the default extension if the content of reader doesn't specify
	 * an language. Ignored if null.
	 * If the content doesn't specify an language, {@link LanguageDefinition#lookupByExtension}
	 * is called.
	 * @param parent the parent component, or null if you want it to be
	 * a root component. If parent is null, the page is assumed to be
	 * the current page, which is determined by the execution context.
	 * @param arg a map of parameters that is accessible by the arg variable
	 * in EL, or by {@link Execution#getArg}.
	 * Ignored if null.
	 * @see #createComponents(PageDefinition, Component, Map)
	 * @see #createComponents(String, Component, Map)
	 * @see #createComponentsDirectly(Document, String, Component, Map)
	 * @see #createComponentsDirectly(String, String, Component, Map)
	 */
	public Component createComponentsDirectly(Reader reader, String extension,
	Component parent, Map arg) throws IOException;

	/** Sends a temporary redirect response to the client using the specified
	 * redirect location URL.
	 *
	 * <p>It is the same as sendRedirect(url, null).
	 *
	 * <p>After calling this method, the caller shall end the processing
	 * immediately (by returning). All pending requests and events will
	 * be dropped.
	 *
	 * @param uri the URI to redirect to, or null to reload the same page
	 */
	public void sendRedirect(String uri);
	/** Sends a temporary redirect response to the client using the specified
	 * redirect location URL and redirect to the specified browser window.
	 *
	 * <p>After calling this method, the caller shall end the processing
	 * immediately (by returning). All pending requests and events will
	 * be dropped.
	 *
	 * <p>Note: if you specify target other than null and "_self", it'll
	 * fail if the browser prevent the server to popup a window.
	 *
	 * @param uri the URI to redirect to, or null to reload the same page
	 * @param target the name of the browser window that send-redirect will
	 * load the sepcified URI, or null if the current browser window
	 * is used.
	 */
	public void sendRedirect(String uri, String target);

	/** Returns the parameters (aka., arg) if {@link #pushArg} is called recently,
	 * or an empty map if not available.
	 *
	 * <p>The use of parameters is application dependent.
	 * ZK only provides the way to store ({@link #pushArg} and to
	 * restore {@link #popArg}. And, let the parameters being accessible in
	 * EL by referring it as the arg variable.
	 *
	 * <p>Currently, {@link #createComponents(String,Component,Map)}
	 * and similar methods use this mechanism to let caller customize a page
	 * definition.
	 *
	 * <p>Notice that {@link #createComponents(String,Component,Map)}
	 * pops arg after creating components, and before processing any event.
	 * In other words, it is not aviable for event listener, including onCreate.
	 * However, {@link org.zkoss.zk.ui.event.CreateEvent#getArg} preserves
	 * the map for its event listeners.
	 */
	public Map getArg();
	/** Pushes the parameters (aka., arg) that EL could refer it by the arg variable.
	 * Remember to call {@link #popArg} in the finally clause.
	 * @see #getArg
	 */
	public void pushArg(Map arg);
	/** Pops the parameters (aka., arg) that is pushed by {@link #pushArg}.
	 * <p>It never throws an exception.
	 * @see #getArg
	 */
	public void popArg();

	/** Adds an asynchronous response ({@link AuResponse}) which will be
	 * sent to client at the end of the execution.
	 *
	 * <p>If {@link AuResponse#getDepends} is not null, the response
	 * depends on the returned componet. In other words, the response
	 * is removed if the component is removed.
	 * If it is null, the response is component-independent.
	 *
	 * @param key could be anything. The second invocation of this method
	 * in the same execution with the same key will override the previous one.
	 */
	public void addAuResponse(String key, AuResponse resposne);

	/** Returns whether the client is a browser.
	 */
	public boolean isBrowser();
	/** Returns whether the client is a robot (such as Web crawlers).
	 *
	 * <p>Because there are too many robots, it returns true if the user-agent
	 * is not recognized.
	 */
	public boolean isRobot();
	/** Returns whether the client is Internet Explorer.
	 * If true, it also implies {@link #isExplorer7} is true.
	 */
	public boolean isExplorer();
	/** Returns whether the client is Internet Explorer 7 or later.
	 */
	public boolean isExplorer7();
	/** Returns whether the client is Gecko based, such as Mozilla, Firefox and Camino.
	 */
	public boolean isGecko();
	/** Returns whether the client is Safari.
	 */
	public boolean isSafari();

	/** Returns the native request, or null if not available.
	 *
	 * <p>The returned object depends on the Web container.
	 * If it is based Java servlet container, an instance of
	 * javax.servlet.ServletRequest is returned.
	 */
	public Object getNativeRequest();
	/** Returns the native response, or null if not available.
	 *
	 * <p>The returned object depends on the Web container.
	 * If it is based Java servlet container, an instance of
	 * javax.servlet.ServletResponse is returned.
	 */
	public Object getNativeResponse();

	/** Returns the value of the specified request attribute.
	 */
	public Object getAttribute(String name);
	/** Sets the value of the specified request attribute.
	 */
	public void setAttribute(String name, Object value);
	/** Removes the specified request attribute.
	 */
	public void removeAttribute(String name);
	/** Returns a map of request attributes associated with this session.
	 */
	public Map getAttributes();
}
