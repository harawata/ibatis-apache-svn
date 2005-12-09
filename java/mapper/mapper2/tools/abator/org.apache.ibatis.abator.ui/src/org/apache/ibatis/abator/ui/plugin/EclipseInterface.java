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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ibatis.abator.core.api.GeneratedFile;
import org.apache.ibatis.abator.core.api.GeneratedJavaFile;
import org.apache.ibatis.abator.core.api.GeneratedXmlFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class EclipseInterface {
	private Map projects;

	private Map folders;

	/**
	 *  
	 */
	public EclipseInterface() {
		super();
		projects = new HashMap();
		folders = new HashMap();
	}

	public void saveXmlFile(GeneratedXmlFile gf) throws CoreException, UnableToMergeException {
		IFolder folder = getFolder(gf);

		IFile file = folder.getFile(gf.getFileName());
		if (file.exists()) {
			XmlFileMerger merger = new XmlFileMerger(gf, file);
			String source = merger.getMergedSource();
			file.setContents(new ByteArrayInputStream(source.getBytes()), true,
					false, null);
		} else {
			String source = gf.getContent();

			file.create(new ByteArrayInputStream(source.getBytes()), true,
					null);
		}
	}

	public void saveJavaFile(GeneratedJavaFile gf) throws CoreException {
		IFolder folder = getFolder(gf);

		IFile file = folder.getFile(gf.getFileName());
		if (file.exists()) {
			JavaFileMerger merger = new JavaFileMerger(gf, file);
			String newSource = merger.getMergedSource();
			
			file.setContents(new ByteArrayInputStream(newSource.getBytes()), true,
					false, null);
		} else {
			String source = formatJavaSource(gf.getContent());
			file.create(new ByteArrayInputStream(source.getBytes()), true,
				null);
		}
	}

	public void refreshAllFolders() throws CoreException {
		Iterator iter = folders.values().iterator();
		while (iter.hasNext()) {
			IFolder folder = (IFolder) iter.next();

			folder.refreshLocal(IResource.DEPTH_ONE, null);
		}
	}

	private IFolder getFolder(GeneratedFile gf) throws CoreException {
		String key = gf.getTargetPackage() + gf.getTargetPackage();
		IFolder folder = (IFolder) folders.get(key);
		if (folder == null) {
			IJavaProject project = (IJavaProject) projects.get(gf
					.getTargetPackage());
			if (project == null) {
				project = getJavaProject(gf.getTargetProject());
				projects.put(gf.getTargetProject(), project);
			}

			IPackageFragmentRoot root = getPackageRoot(project);
			IPackageFragment targetPackage = getPackage(root, gf
					.getTargetPackage());

			folder = (IFolder) targetPackage.getCorrespondingResource();

			folders.put(key, folder);
		}

		return folder;
	}

	private IJavaProject getJavaProject(String javaProjectName)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(javaProjectName);
		IJavaProject javaProject;

		if (project.exists()) {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				javaProject = JavaCore.create(project);
			} else {
				Status status = new Status(IStatus.ERROR, AbatorUIPlugin
						.getPluginId(), IStatus.ERROR, "Project "
						+ javaProjectName + " is not a Java Project", null);

				throw new CoreException(status);
			}
		} else {
			Status status = new Status(IStatus.ERROR, AbatorUIPlugin
					.getPluginId(), IStatus.ERROR, "Project " + javaProjectName
					+ " does not exist", null);

			throw new CoreException(status);
		}

		return javaProject;
	}

	/**
	 * This method returns the first modifiable package fragment root in the
	 * java project
	 * 
	 * @param javaProject
	 * @return
	 */
	private IPackageFragmentRoot getPackageRoot(IJavaProject javaProject)
			throws CoreException {

		// find the first non-JAR package fragment root
		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
		IPackageFragmentRoot srcFolder = null;
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].isArchive() || roots[i].isReadOnly()
					|| roots[i].isExternal()) {
				continue;
			} else {
				srcFolder = roots[i];
				break;
			}
		}

		if (srcFolder == null) {
			Status status = new Status(IStatus.ERROR, AbatorUIPlugin
					.getPluginId(), IStatus.ERROR,
					"Cannot find source folder for project " + javaProject.getElementName(), null);
			throw new CoreException(status);
		}

		return srcFolder;
	}

	private IPackageFragment getPackage(IPackageFragmentRoot srcFolder,
			String packageName) throws CoreException {

		IPackageFragment fragment = srcFolder.getPackageFragment(packageName);
		if (!fragment.exists()) {
			fragment = srcFolder.createPackageFragment(packageName, true, null);
		}

		fragment.getCorrespondingResource().refreshLocal(IResource.DEPTH_ONE,
				null);

		return fragment;
	}

	private String formatJavaSource(String source) {
		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
		TextEdit te = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
				source, 0, source.length(), 0, "\n"); //$NON-NLS-1$

		if (te == null) {
			// no edits to make
			return source;
		}

		IDocument doc = new Document(source);
		String formattedSource;
		try {
			te.apply(doc);
			formattedSource = doc.get();
		} catch (BadLocationException e) {
			formattedSource = source;
		}

		return formattedSource;
	}
}
