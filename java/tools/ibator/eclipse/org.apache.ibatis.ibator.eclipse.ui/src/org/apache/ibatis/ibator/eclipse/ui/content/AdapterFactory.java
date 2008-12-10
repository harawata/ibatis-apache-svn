/*
 *  Copyright 2006 The Apache Software Foundation
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
package org.apache.ibatis.ibator.eclipse.ui.content;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * This class provides a factory for different adapters used in the plugin.
 * Namely, this class decides if any particular file is an ibator configuration
 * file or not.
 * 
 * @author Jeff Butler
 */
public class AdapterFactory implements IAdapterFactory {

    public AdapterFactory() {
        super();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IFile
                && adapterType == IbatorConfigurationFileAdapter.class) {
            if (isIbatorConfigurationFile((IFile) adaptableObject)) {
                return new IbatorConfigurationFileAdapter(
                        (IFile) adaptableObject);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[] { IbatorConfigurationFileAdapter.class };
    }

    private boolean isIbatorConfigurationFile(IFile file) {
        String fileName = file.getName();
        if (fileName.length() > 4) {
            String extension = fileName.substring(fileName.length() - 4);
            if (!extension.equalsIgnoreCase(".xml")) { //$NON-NLS-1$
                return false;
            }
        } else {
            return false;
        }

        InputStream is;
        try {
            is = file.getContents();
        } catch (CoreException e) {
            return false;
        }
        
        IbatorConfigVerifyer verifyer = new IbatorConfigVerifyer(is);
        
        boolean rc = verifyer.isIbatorConfigFile();
        
        try {
            is.close();
        } catch (IOException e) {
            // ignore
            ;
        }
        
        return rc;
    }
}
