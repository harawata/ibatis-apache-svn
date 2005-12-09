/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ibatis.abator.ui.plugin;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.core.api.GeneratedXmlFile;
import org.eclipse.core.resources.IFile;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format.TextMode;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class handles the task of merging changes into an existing XML
 * file.
 * 
 * @author Jeff Butler
 */
public class XmlFileMerger {
	private class NullEntityResolver implements EntityResolver {
		/**
		 * returns an empty reader.  This is done so that the SAX parser
		 * doesn't attempt to read a DTD.  We don't need that support
		 * for the merge and it can cause problems on systems that aren't
		 * Internet connected.
		 */
		public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
			
			StringReader sr = new StringReader("");
			
			return new InputSource(sr);
		}
	}

	private GeneratedXmlFile generatedXmlFile;

	private IFile existingFile;

	/**
	 *  
	 */
	public XmlFileMerger(GeneratedXmlFile generatedXmlFile, IFile existingFile) {
		super();
		this.generatedXmlFile = generatedXmlFile;
		this.existingFile = existingFile;
	}

	public String getMergedSource() throws UnableToMergeException {
		try {
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			builder.setReuseParser(true);
			builder.setEntityResolver(new NullEntityResolver());
			
			Document existingDocument = builder.build(existingFile.getLocation().toFile());
			StringReader sr = new StringReader(generatedXmlFile.getContent());
			Document newDocument = builder.build(sr);
			
			DocType newDocType = newDocument.getDocType();
			DocType existingDocType = existingDocument.getDocType();
			
			if (!newDocType.getElementName().equals(existingDocType.getElementName())
					|| !newDocType.getSystemID().equals(existingDocType.getSystemID())
					|| !newDocType.getPublicID().equals(existingDocType.getPublicID())) {
				throw new UnableToMergeException("The exisiting XML file "
						+ existingFile.getName() 
						+ " is not the same format as the generated file.  The existing file will not be changed.");
			}

			Element existingRootElement = existingDocument.getRootElement();
			Element newRootElement = newDocument.getRootElement();
			
			// reconcile the namespace
			Attribute namespace = newRootElement.getAttribute("namespace");
			existingRootElement.removeAttribute("namespace");
			existingRootElement.setAttribute((Attribute) namespace.clone());

			// remove the old Abator generated elements
			List children = existingRootElement.getChildren();
			Iterator iter = children.iterator();
			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				Attribute id = element.getAttribute("id");
				if (id != null) {
					String value = id.getValue();
					if (value.startsWith("abatorgenerated_")) {
						iter.remove();
					}
				}
			}

			// add the new Abator generated elements
			children = newRootElement.getChildren();
			iter = children.iterator();
			int i = 0;
			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				existingRootElement.addContent(i++, (Element) element.clone());
			}

			Format format = Format.getRawFormat();
	        format.setIndent("  ");
	        format.setTextMode(TextMode.TRIM_FULL_WHITE);
			format.setExpandEmptyElements(false);

			XMLOutputter outputter = new XMLOutputter(format);

			CharArrayWriter caw = new CharArrayWriter();
			outputter.output(existingDocument, caw);

			String newSource = caw.toString();
			return newSource;
		} catch (JDOMException e) {
			throw new UnableToMergeException("JDOMException while attempting to merge the XML file "
					+ existingFile.getName()
						+ ".  The existing file will not be changed.", e);
		} catch (IOException e) {
			throw new UnableToMergeException("IOException while attempting to merge the XML file "
					+ existingFile.getName()
						+ ".  The existing file will not be changed.", e);
		}
	}
}
