/*
 *  Copyright 2008 The Apache Software Foundation
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
package org.apache.ibatis.ibator.eclipse.core.callback;

import org.apache.ibatis.ibator.api.ProgressCallback;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * @author Jeff Butler
 */
public class EclipseProgressCallback implements ProgressCallback {
    private static final int INTROSPECTION_FACTOR = 2000;
    private static final int GENERATION_FACTOR = 4000;
    private static final int SAVE_FACTOR = 4000;

    private SubMonitor parentProgress;
    private SubMonitor currentChildProgress;
    private int currentTick;
    
    /**
     * 
     */
    public EclipseProgressCallback(IProgressMonitor progressMonitor) {
        super();
        parentProgress = SubMonitor.convert(progressMonitor,
                INTROSPECTION_FACTOR + GENERATION_FACTOR + SAVE_FACTOR);
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.core.api.ProgressCallback#checkCancel()
     */
    public void checkCancel() throws InterruptedException {
        if (currentChildProgress.isCanceled()) {
            throw new InterruptedException();
        }
    }

    public void generationStarted(int totalTasks) {
        currentChildProgress = parentProgress.newChild(GENERATION_FACTOR);
        currentTick = GENERATION_FACTOR / totalTasks;
        if (currentTick == 0) {
            currentTick = 1;
        }

        currentChildProgress.beginTask("Generating Files", GENERATION_FACTOR);
    }

    public void introspectionStarted(int totalTasks) {
        currentChildProgress = parentProgress.newChild(INTROSPECTION_FACTOR);
        currentTick = INTROSPECTION_FACTOR / totalTasks;
        if (currentTick == 0) {
            currentTick = 1;
        }

        currentChildProgress.beginTask("Introspecting Tables", INTROSPECTION_FACTOR);
    }

    public void saveStarted(int totalTasks) {
        currentChildProgress = parentProgress.newChild(SAVE_FACTOR);
        currentTick = SAVE_FACTOR / totalTasks;
        if (currentTick == 0) {
            currentTick = 1;
        }
        
        currentChildProgress.beginTask("Saving Generated Files", SAVE_FACTOR);
    }

    public void startTask(String taskName) {
        currentChildProgress.subTask(taskName);
        currentChildProgress.worked(currentTick);
    }

    public void done() {
        // ignore - don't call done on SubMonitors - leave that to the calling method
        ;
    }
}
