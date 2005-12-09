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
package org.apache.ibatis.abator.ui.actions;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.core.api.Abator;
import org.apache.ibatis.abator.core.api.GeneratedJavaFile;
import org.apache.ibatis.abator.core.api.GeneratedXmlFile;
import org.apache.ibatis.abator.core.api.ProgressCallback;
import org.apache.ibatis.abator.core.config.AbatorConfiguration;
import org.apache.ibatis.abator.core.config.xml.AbatorConfigurationParser;
import org.apache.ibatis.abator.core.exception.InvalidConfigurationException;
import org.apache.ibatis.abator.core.exception.XMLParserException;
import org.apache.ibatis.abator.ui.plugin.AbatorUIPlugin;
import org.apache.ibatis.abator.ui.plugin.EclipseInterface;
import org.apache.ibatis.abator.ui.plugin.UnableToMergeException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Jeff Butler
 */
public class RunAbatorThread implements IWorkspaceRunnable {
    private File inputFile;
    private List warnings;

    /**
     *  
     */
    public RunAbatorThread(File inputFile, List warnings) {
        super();
        this.inputFile = inputFile;
        this.warnings = warnings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Generating iBATIS Artifacts:", 1000);

        try {
            monitor.subTask("Parsing Configuration");

            AbatorConfigurationParser cp = new AbatorConfigurationParser(
                    warnings);
            AbatorConfiguration config = cp.parseAbatorConfiguration(inputFile);

            monitor.worked(50);

            Abator abator = new Abator(config, warnings);
            
            monitor.subTask("Generating Files from Database Tables");
            SubProgressMonitor spm = new SubProgressMonitor(monitor, 475);
            abator.generateFiles(new MyProgressCallback(spm));

            int workStepUnits = 475 / (abator.getGeneratedJavaFiles().size() + abator
                    .getGeneratedXmlFiles().size());

            EclipseInterface ei = new EclipseInterface();

            Iterator iter = abator.getGeneratedJavaFiles().iterator();
            while (iter.hasNext()) {
                GeneratedJavaFile gf = (GeneratedJavaFile) iter.next();

                checkForCancel(monitor);
                monitor.subTask("Saving file " + gf.getFileName());
                ei.saveJavaFile(gf);
                monitor.worked(workStepUnits);
            }

            iter = abator.getGeneratedXmlFiles().iterator();
            while (iter.hasNext()) {
                GeneratedXmlFile gf = (GeneratedXmlFile) iter.next();

                checkForCancel(monitor);
                monitor.subTask("Saving file " + gf.getFileName());
                try {
                    ei.saveXmlFile(gf);
                } catch (UnableToMergeException e) {
                    warnings.add(e.getMessage());

                    String message = "File merge error while running Abator.";

                    Status status = new Status(IStatus.ERROR, AbatorUIPlugin
                            .getPluginId(), IStatus.ERROR, message, e);

                    AbatorUIPlugin.getDefault().getLog().log(status);
                }
                monitor.worked(workStepUnits);
            }

            ei.refreshAllFolders();
        } catch (InterruptedException e) {
            throw new OperationCanceledException();
        } catch (SQLException e) {
            Status status = new Status(IStatus.ERROR, AbatorUIPlugin
                    .getPluginId(), IStatus.ERROR, e.getMessage(), e);
            throw new CoreException(status);
        } catch (XMLParserException e) {
            List errors = e.getErrors();
            MultiStatus multiStatus = new MultiStatus(AbatorUIPlugin
                    .getPluginId(), IStatus.ERROR,
                    "XML Parser Errors\n  See Details for more Information",
                    null);

            Iterator iter = errors.iterator();
            while (iter.hasNext()) {
                Status message = new Status(IStatus.ERROR, AbatorUIPlugin
                        .getPluginId(), IStatus.ERROR, (String) iter.next(),
                        null);

                multiStatus.add(message);
            }
            throw new CoreException(multiStatus);
        } catch (InvalidConfigurationException e) {
            List errors = e.getErrors();

            MultiStatus multiStatus = new MultiStatus(
                    AbatorUIPlugin.getPluginId(),
                    IStatus.ERROR,
                    "Invalid Configuration\n  See Details for more Information",
                    null);

            Iterator iter = errors.iterator();
            while (iter.hasNext()) {
                Status message = new Status(IStatus.ERROR, AbatorUIPlugin
                        .getPluginId(), IStatus.ERROR, (String) iter.next(),
                        null);

                multiStatus.add(message);
            }
            throw new CoreException(multiStatus);
        } finally {
            monitor.done();
        }
    }

    private void checkForCancel(IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
    
    private class MyProgressCallback implements ProgressCallback {
        
        private SubProgressMonitor subMonitor;
        
        public MyProgressCallback(SubProgressMonitor subMonitor) {
            this.subMonitor = subMonitor;
        }
        
        public void checkCancel() throws InterruptedException {
            if (subMonitor.isCanceled()) {
                throw new InterruptedException();
            }
        }
        
        public void finished() {
            subMonitor.done();
        }
        
        public void setTaskName(String taskName) {
            subMonitor.subTask(taskName);
            subMonitor.worked(1);
        }
        
        public void setTotalSteps(int totalSteps) {
            subMonitor.beginTask("Generating Files from Database Tables", totalSteps);
        }
    }
}
