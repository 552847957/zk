/* Uploads.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Aug 14 12:46:31     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.http;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import org.zkoss.lang.D;
import org.zkoss.lang.Strings;
import org.zkoss.lang.Exceptions;
import org.zkoss.mesg.Messages;
import org.zkoss.util.CollectionsX;
import org.zkoss.util.logging.Log;
import org.zkoss.util.media.Media;
import org.zkoss.util.media.AMedia;
import org.zkoss.image.AImage;
import org.zkoss.sound.AAudio;

import org.zkoss.web.servlet.Servlets;

import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.sys.DesktopCtrl;

/**
 * The utility used to process file upload.
 *
 * @author tomyeh
 */
/*package*/ class Uploads {
	private static final Log log = Log.lookup(Uploads.class);

	/** Processes a file uploaded from the client.
	 */
	public static void process(Session sess, ServletContext ctx,
	HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		final Map attrs = new HashMap();
		String alert = null, uuid = null,
			nextURI = "~./zul/html/fileupload-done.dsp";
		try {
			if (!isMultipartContent(request)) {
				alert = "enctype must be multipart/form-data";
			} else {
				uuid = request.getParameter("uuid");
				if (uuid == null || uuid.length() == 0) {
					alert = "uuid is required";
				} else {
					attrs.put("uuid", uuid);

					final String dtid = (String)request.getParameter("dtid");
					if (dtid == null || dtid.length() == 0) {
						alert = "dtid is required";
					} else {
						final Desktop desktop = ((WebAppCtrl)sess.getWebApp())
							.getDesktopCache(sess).getDesktop(dtid);
						final Map params = parseRequest(request, desktop);

						final String uri = (String)params.get("nextURI");
						if (uri != null && uri.length() != 0)
							nextURI = uri;

						processItems(desktop, params, attrs);
					}
				}
			}
		} catch (Throwable ex) {
			if (uuid == null) {
				uuid = request.getParameter("uuid");
				if (uuid != null)
					attrs.put("uuid", uuid);
			}
			if (ex instanceof ComponentNotFoundException) {
				alert = Messages.get(MZk.UPDATE_OBSOLETE_PAGE, uuid);
			} else {
				log.realCauseBriefly("Failed to upload", ex);
				alert = Exceptions.getMessage(ex);
			}
		}

		if (alert != null)
			attrs.put("alert", alert);
		if (D.ON && log.finerable()) log.finer(attrs);

		Servlets.forward(ctx, request, response,
			nextURI, attrs, Servlets.PASS_THRU_ATTR);
	}

	/** Process fileitems named file0, file1 and so on.
	 */
	private static final
	void processItems(Desktop desktop, Map params, Map attrs)
	throws IOException {
		final List meds = new LinkedList();
		final Object fis = params.get("file");
		if (fis instanceof FileItem) {
			meds.add(processItem((FileItem)fis));
		} else {
			for (Iterator it = ((List)fis).iterator(); it.hasNext();) {
				meds.add(processItem((FileItem)it.next()));
			}
		}

		final String contentId = Strings.encode(
			new StringBuffer(12).append("z__ul_"),
			((DesktopCtrl)desktop).getNextKey()).toString();
		attrs.put("contentId", contentId);
		desktop.setAttribute(contentId, meds);
	}
	/** Process the specified fileitem.
	 */
	private static final Media processItem(FileItem fi)
	throws IOException {
		String name = getBaseName(fi);
		if (name != null) {
		//Not sure whether a name might contain ;jsessionid or similar
		//But we handle this case: x.y;z
			final int j = name.lastIndexOf(';');
			if (j > 0) {
				final int k = name.lastIndexOf('.');
				if (k >= 0 && j > k && k > name.lastIndexOf('/'))
					name = name.substring(0, j);
			}
		}

		final String ctype = fi.getContentType(),
			ctypelc = ctype != null ? ctype.toLowerCase(): null;
		if (ctype != null)
			if (ctypelc.startsWith("image/")) {
				try {
					return fi.isInMemory() ? new AImage(name, fi.get()):
						new AImage(name, fi.getInputStream());
							//note: AImage converts stream to binary array
				} catch (Throwable ex) {
					if (log.debugable()) log.debug("Unknown file format: "+ctype);
				}
			} else if (ctypelc.startsWith("audio/")) {
				try {
					return fi.isInMemory() ? new AAudio(name, fi.get()):
						new StreamAudio(name, fi);
				} catch (Throwable ex) {
					if (log.debugable()) log.debug("Unknown file format: "+ctype);
				}
			}

		if (ctypelc != null && ctypelc.startsWith("text/")) {
			final String charset = getCharset(ctype);
			return fi.isInMemory() ?
				new AMedia(name, null, ctype, fi.getString(charset)):
				new ReaderMedia(name, null, ctype, fi, charset);
		} else {
			return fi.isInMemory() ?
				new AMedia(name, null, ctype, fi.get()):
				new StreamMedia(name, null, ctype, fi);
		}
	}
	private static String getCharset(String ctype) {
		final String ctypelc = ctype.toLowerCase();
		for (int j = 0; (j = ctypelc.indexOf("charset", j)) >= 0; j += 7) {
			int k = Strings.skipWhitespacesBackward(ctype, j - 1);
			if (k < 0 || ctype.charAt(k) == ';') {
				k = Strings.skipWhitespaces(ctype, j + 7);
				if (k <= ctype.length() && ctype.charAt(k) == '=') {
					j = ctype.indexOf(';', ++k);
					String charset =
						(j >= 0 ? ctype.substring(k, j): ctype.substring(k)).trim();
					if (charset.length() > 0)
						return charset;
					break; //use default
				}
			}
		}

		return "UTF-8";
	}

	/** Parses the multipart request into a map of
	 * (String nm, FileItem/String/List(FileItem/String)).
	 */
	private static Map parseRequest(HttpServletRequest request,
	Desktop desktop)
	throws FileUploadException {
		final Map params = new HashMap();
		final ServletFileUpload sfu =
			new ServletFileUpload(new ZkFileItemFactory(desktop, request));
		final Configuration cfg = desktop.getWebApp().getConfiguration();
		final int maxsz = cfg.getMaxUploadSize();
		sfu.setSizeMax(maxsz >= 0 ? 1024L*maxsz: -1);

		for (Iterator it = sfu.parseRequest(request).iterator(); it.hasNext();) {
			final FileItem fi = (FileItem)it.next();
			final String nm = fi.getFieldName();
			final Object val;
			if (fi.isFormField()) {
				val = fi.getString();
			} else {
				val = fi;
			}

			final Object old = params.put(nm, val);
			if (old != null) {
				final List vals;
				if (old instanceof List) {
					params.put(nm, vals = (List)old);
				} else {
					params.put(nm, vals = new LinkedList());
					vals.add(old);
				}
				vals.add(val);
			}
		}
		return params;
	}
	/** Returns the base name for FileItem (i.e., removing path).
	 */
	private static String getBaseName(FileItem fi) {
		String name = fi.getName();
		if (name == null)
			return null;

		final String[] seps = {"/", "\\", "%5c", "%5C", "%2f", "%2F"};
		for (int j = seps.length; --j >= 0;) {
			final int k = name.lastIndexOf(seps[j]);
			if (k >= 0)
				name = name.substring(k + seps[j].length());
		}
		return name;
	}

	/** Returns whether the request contains multipart content.
	 */
	public static final boolean isMultipartContent(HttpServletRequest request) {
		return "post".equals(request.getMethod().toLowerCase())
			&& FileUploadBase.isMultipartContent(new ServletRequestContext(request));
	}

	private static class StreamMedia extends AMedia {
		private final FileItem _fi;
		public StreamMedia(String name, String format, String ctype, FileItem fi) {
			super(name, format, ctype, DYNAMIC_STREAM);
			_fi = fi;
		}
		public java.io.InputStream getStreamData() {
			try {
				return _fi.getInputStream();
			} catch (IOException ex) {
				throw new UiException("Unable to read "+_fi, ex);
			}
		}
	}
	private static class ReaderMedia extends AMedia {
		private final FileItem _fi;
		private final String _charset;
		public ReaderMedia(String name, String format, String ctype,
		FileItem fi, String charset) {
			super(name, format, ctype, DYNAMIC_STREAM);
			_fi = fi;
			_charset = charset;
		}
		public java.io.Reader getReaderData() {
			try {
				return new java.io.InputStreamReader(
					_fi.getInputStream(), _charset);
			} catch (IOException ex) {
				throw new UiException("Unable to read "+_fi, ex);
			}
		}
	}
	private static class StreamAudio extends AAudio {
		private final FileItem _fi;
		public StreamAudio(String name, FileItem fi) throws IOException {
			super(name, DYNAMIC_STREAM);
			_fi = fi;
		}
		public java.io.InputStream getStreamData() {
			try {
				return _fi.getInputStream();
			} catch (IOException ex) {
				throw new UiException("Unable to read "+_fi, ex);
			}
		}
	}
}
