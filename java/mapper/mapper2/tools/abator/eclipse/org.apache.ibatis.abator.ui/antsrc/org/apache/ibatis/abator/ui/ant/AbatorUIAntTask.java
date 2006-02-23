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
package org.apache.ibatis.abator.ui.ant;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.api.Abator;
import org.apache.ibatis.abator.config.AbatorConfiguration;
import org.apache.ibatis.abator.config.xml.AbatorConfigurationParser;
import org.apache.ibatis.abator.exception.InvalidConfigurationException;
import org.apache.ibatis.abator.exception.XMLParserException;
import org.apache.ibatis.abator.internal.util.StringUtility;
import org.apache.ibatis.abator.ui.plugin.EclipseProgressCallback;
import org.apache.ibatis.abator.ui.plugin.EclipseShellCallback;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Jeff Butler
 */
public class AbatorUIAntTask extends Task {

    private String configfile;

    /**
     *  
     */
    public AbatorUIAntTask() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        if (!StringUtility.stringHasValue(configfile)) {
            throw new BuildException("configfile is a required parameter");
        }

        List warnings = new ArrayList();

        File configurationFile = new File(configfile);
        if (!configurationFile.exists()) {
            throw new BuildException("configfile " + configfile
                    + " does not exist");
        }

        try {
            AbatorConfigurationParser cp = new AbatorConfigurationParser(
                    warnings);
            AbatorConfiguration config = cp
                    .parseAbatorConfiguration(configurationFile);

            Abator abator = new Abator(config, new EclipseShellCallback(),
                    warnings);

            EclipseProgressCallback progressCallback = null;
            IProgressMonitor monitor = (IProgressMonitor) getProject()
                    .getReferences()
                    .get(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
            if (monitor != null) {
                progressCallback = new EclipseProgressCallback(monitor);
            }

            abator.generate(progressCallback);

        } catch (XMLParserException e) {
            List errors = e.getErrors();
            Iterator iter = errors.iterator();
            while (iter.hasNext()) {
                log((String) iter.next());
            }

            throw new BuildException(e.getMessage());
        } catch (SQLException e) {
            throw new BuildException(e.getMessage());
        } catch (IOException e) {
            throw new BuildException(e.getMessage());
        } catch (InvalidConfigurationException e) {
            throw new BuildException(e.getMessage());
        } catch (InterruptedException e) {
            throw new BuildException("Cancelled by user");
        }

        Iterator iter = warnings.iterator();
        while (iter.hasNext()) {
            log((String) iter.next());
        }
    }

    /**
     * @return Returns the configfile.
     */
    public String getConfigfile() {
        return configfile;
    }

    /**
     * @param configfile
     *            The configfile to set.
     */
    public void setConfigfile(String configfile) {
        this.configfile = configfile;
    }
}