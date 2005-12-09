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
package org.apache.ibatis.abator.core.api;

/**
 * This interface can be implemented to return progress information from the file generation
 * process.  This interface is loosely based on the standard Eclipse IProgressMonitor interface,
 * but does not implement all its methods.
 * 
 * @author Jeff Butler
 */
public interface ProgressCallback {
    /**
     * Called to designate the maximum number os setTaskName messages that will be sent
     * 
     * @param totalSteps
     */
    
    void setTotalSteps(int totalSteps);
    
    /**
     * Called to denote the beginning of another task
     * 
     * @param taskName
     */
    void setTaskName(String taskName);
    
    /**
     * Called when all tasks are finished
     *
     */
    void finished();

    /**
     * 
     * @throws InterruptedException if the task should finish
     */
    void checkCancel() throws InterruptedException;
}
