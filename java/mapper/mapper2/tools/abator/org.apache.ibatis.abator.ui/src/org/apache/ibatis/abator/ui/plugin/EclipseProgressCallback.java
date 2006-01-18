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

import org.apache.ibatis.abator.api.ProgressCallback;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Jeff Butler
 */
public class EclipseProgressCallback implements ProgressCallback {

    IProgressMonitor progressMonitor;
    
    /**
     * 
     */
    public EclipseProgressCallback(IProgressMonitor progressMonitor) {
        super();
        this.progressMonitor = progressMonitor;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.core.api.ProgressCallback#setTotalSteps(int)
     */
    public void setTotalSteps(int totalSteps) {
        progressMonitor.beginTask("Generating Files from Database Tables",
                totalSteps);
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.core.api.ProgressCallback#setTaskName(java.lang.String)
     */
    public void setTaskName(String taskName) {
        progressMonitor.subTask(taskName);
        progressMonitor.worked(1);
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.core.api.ProgressCallback#finished()
     */
    public void finished() {
        progressMonitor.done();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.core.api.ProgressCallback#checkCancel()
     */
    public void checkCancel() throws InterruptedException {
        if (progressMonitor.isCanceled()) {
            throw new InterruptedException();
        }
    }
}
