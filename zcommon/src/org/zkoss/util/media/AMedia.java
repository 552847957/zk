/* AMedia.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu May 27 15:10:46     2004, Created by tomyeh
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.util.media;

import java.io.Reader;
import java.io.InputStream;
import java.io.StringReader;
import java.io.ByteArrayInputStream;

import org.zkoss.io.NullInputStream;
import org.zkoss.io.NullReader;

/**
 * A media object holding content such PDF, HTML, DOC or XLS content.
 *
 * @author tomyeh
 */
public class AMedia implements Media {
	/** Used if you want to implement a meida whose input stream is created
	 * dynamically each time {@link #getStreamData} is called.
	 * @see #AMedia(String,String,String,InputStream)
	 */
	protected static final InputStream DYNAMIC_STREAM = new NullInputStream();
	/** Used if you want to implement a meida whose reader is created
	 * dynamically each time {@link #getReaderData} is called.
	 * @see #AMedia(String,String,String,Reader)
	 */
	protected static final Reader DYNAMIC_READER = new NullReader();

	/** The binary data, {@link #getByteData}. */
	private byte[] _bindata;
	/** The text data, {@link #getStringData}. */
	private String _strdata;
	/** The input stream, {@link #getStreamData} */
	private InputStream _isdata;
	/** The input stream, {@link #getReaderData} */
	private Reader _rddata;
	/** The content type. */
	private String _ctype;
	/** The format (e.g., pdf). */
	private String _format;
	/** The name (usually filename). */
	private String _name;

	/** Construct with name, format, content type and binary data.
	 *
	 * <p>It tries to construct format and ctype from each other or name.
	 *
	 * @param name the name (usually filename); might be null.
	 * @param format the format; might be null.
	 * @param ctype the content type; might be null.
	 * @param data the binary data; never null
	 */
	public AMedia(String name, String format, String ctype, byte[] data) {
		if (data == null)
			throw new NullPointerException("data");
		_bindata = data;
		setup(name, format, ctype);
	}
	/** Construct with name, format, content type and text data.
	 *
	 * <p>It tries to construct format and ctype from each other or name.
	 *
	 * @param name the name (usually filename); might be null.
	 * @param format the format; might be null.
	 * @param ctype the content type; might be null.
	 * @param data the text data; never null
	 */
	public AMedia(String name, String format, String ctype, String data) {
		if (data == null)
			throw new NullPointerException("data");
		_strdata = data;
		setup(name, format, ctype);
	}
	/** Construct with name, format, content type and stream data (binary).
	 *
	 * <p>It tries to construct format and ctype from each other or name.
	 *
	 * @param name the name (usually filename); might be null.
	 * @param format the format; might be null.
	 * @param ctype the content type; might be null.
	 * @param data the binary data; never null.
	 * If the input stream is created dyanmically each tiime {@link #getStreamData}
	 * is called, you shall pass {@link #DYNAMIC_STREAM}
	 * as the data argument. Then, override {@link #getStreamData} to return
	 * the correct stream.
	 */
	public AMedia(String name, String format, String ctype, InputStream data) {
		if (data == null)
			throw new NullPointerException("data");
		_isdata = data;
		setup(name, format, ctype);
	}
	/** Construct with name, format, content type and reader data (textual).
	 *
	 * <p>It tries to construct format and ctype from each other or name.
	 *
	 * @param name the name (usually filename); might be null.
	 * @param format the format; might be null.
	 * @param ctype the content type; might be null.
	 * @param data the string data; never null
	 * If the reader is created dyanmically each tiime {@link #getReaderData}
	 * is called, you shall pass {@link #DYNAMIC_READER}
	 * as the data argument. Then, override {@link #getReaderData} to return
	 * the correct reader.
	 */
	public AMedia(String name, String format, String ctype, Reader data) {
		if (data == null)
			throw new NullPointerException("data");
		_rddata = data;
		setup(name, format, ctype);
	}
	/** Sets up the format and content type.
	 * It assumes one of them is not null.
	 */
	private void setup(String name, String format, String ctype) {
		if (ctype != null && format == null) {
			format = ContentTypes.getFormat(ctype);
		} else if (ctype == null && format != null) {
			ctype = ContentTypes.getContentType(format);
		}
		if (name != null) {
			if (format == null) {
				final int j = name.lastIndexOf('.');
				if (j >= 0) {
					format = name.substring(j + 1);
					if (ctype == null) {
						ctype = ContentTypes.getContentType(format);
					}
				}
			}
		}

		_name = name;
		_format = format;
		_ctype = ctype;
	}

	//-- Media --//
	public boolean isBinary() {
		return _bindata != null || _isdata != null;
	}
	public boolean inMemory() {
		return _bindata != null || _strdata != null;
	}
	public byte[] getByteData() {
		if (_bindata == null) throw newIllegalStateException();
		return _bindata;
	}
	public String getStringData() {
		if (_strdata == null) throw newIllegalStateException();
		return _strdata;
	}
	public InputStream getStreamData() {
		if (_isdata != null) return _isdata;
		if (_bindata != null) return new ByteArrayInputStream(_bindata);
		throw newIllegalStateException();
	}
	public Reader getReaderData() {
		if (_rddata != null) return _rddata;
		if (_strdata != null) return new StringReader(_strdata);
		throw newIllegalStateException();
	}
	private IllegalStateException newIllegalStateException() {
		return new IllegalStateException(
			"Use get"
			+(_bindata != null ? "Byte": _strdata != null ? "String":
				_isdata != null ? "Stream": "Reader")
			+ "Data() instead");
	}

	public String getName() {
		return _name;
	}
	public String getFormat() {
		return _format;
	}
	public String getContentType() {
		return _ctype;
	}

	//-- Object --//
	public String toString() {
		return _name != null ? _name: "Media "+_format;
	}
}
