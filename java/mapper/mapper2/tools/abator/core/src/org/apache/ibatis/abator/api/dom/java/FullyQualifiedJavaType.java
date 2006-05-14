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
package org.apache.ibatis.abator.api.dom.java;

/**
 * @author Jeff Butler
 */
public class FullyQualifiedJavaType implements Comparable {
    private static FullyQualifiedJavaType intInstance = null;
    private static FullyQualifiedJavaType listInstance = null;
    private static FullyQualifiedJavaType mapInstance = null;
    private static FullyQualifiedJavaType hashMapInstance = null;
    private static FullyQualifiedJavaType arrayListInstance = null;
    private static FullyQualifiedJavaType stringInstance = null;
    private static FullyQualifiedJavaType booleanInstance = null;
    
    private String shortName;
    private String fullyQualifiedName;
    private boolean explicitlyImported;
    private String packageName;
    private boolean primitive;
    private String wrapperClass;

    /**
     * 
     */
    public FullyQualifiedJavaType(String fullyQualifiedName) {
        super();
        this.fullyQualifiedName = fullyQualifiedName;
        
		int lastIndex = fullyQualifiedName.lastIndexOf('.');
		if (lastIndex == -1) {
		    shortName = fullyQualifiedName;
		    explicitlyImported = false;
		    packageName = ""; //$NON-NLS-1$
		    
		    if ("byte".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Byte"; //$NON-NLS-1$
		    } else if ("short".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Short"; //$NON-NLS-1$
		    } else if ("int".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Integer"; //$NON-NLS-1$
		    } else if ("long".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Long"; //$NON-NLS-1$
		    } else if ("char".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Character"; //$NON-NLS-1$
		    } else if ("float".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Float"; //$NON-NLS-1$
		    } else if ("double".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Double"; //$NON-NLS-1$
		    } else if ("boolean".equals(fullyQualifiedName)) { //$NON-NLS-1$
		        primitive = true;
		        wrapperClass = "Boolean"; //$NON-NLS-1$
		    } else {
		        primitive = false;
		        wrapperClass = null;
		    }
		} else {
		    shortName = fullyQualifiedName.substring(lastIndex + 1);
			packageName = fullyQualifiedName.substring(0, lastIndex);
			if ("java.lang".equals(packageName)) { //$NON-NLS-1$
			    explicitlyImported = false;
			} else {
			    explicitlyImported = true;
			}
		}
    }
    
    /**
     * @return Returns the explicitlyImported.
     */
    public boolean isExplicitlyImported() {
        return explicitlyImported;
    }
    /**
     * @return Returns the fullyQualifiedName.
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    /**
     * @return Returns the packageName.
     */
    public String getPackageName() {
        return packageName;
    }
    /**
     * @return Returns the shortName.
     */
    public String getShortName() {
        return shortName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof FullyQualifiedJavaType)) {
			return false;
		}

		FullyQualifiedJavaType other = (FullyQualifiedJavaType) obj;
		
        return fullyQualifiedName.equals(other.fullyQualifiedName);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fullyQualifiedName.hashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return fullyQualifiedName;
    }
    
    /**
     * @return Returns the primitive.
     */
    public boolean isPrimitive() {
        return primitive;
    }
    
    /**
     * @return Returns the wrapperClass.
     */
    public String getWrapperClass() {
        return wrapperClass;
    }
    
    /**
     * Utility method - returns the single instance of the int type
     * 
     * @return
     */
    public static FullyQualifiedJavaType getIntInstance() {
        if (intInstance == null) {
            intInstance = new FullyQualifiedJavaType("int"); //$NON-NLS-1$
        }
        
        return intInstance;
    }

    public static FullyQualifiedJavaType getMapInstance() {
        if (mapInstance == null) {
            mapInstance = new FullyQualifiedJavaType("java.util.Map"); //$NON-NLS-1$
        }
        
        return mapInstance;
    }

    public static FullyQualifiedJavaType getListInstance() {
        if (listInstance == null) {
            listInstance = new FullyQualifiedJavaType("java.util.List"); //$NON-NLS-1$
        }
        
        return listInstance;
    }

    public static FullyQualifiedJavaType getHashMapInstance() {
        if (hashMapInstance == null) {
            hashMapInstance = new FullyQualifiedJavaType("java.util.HashMap"); //$NON-NLS-1$
        }
        
        return hashMapInstance;
    }

    public static FullyQualifiedJavaType getArrayListInstance() {
        if (arrayListInstance == null) {
            arrayListInstance = new FullyQualifiedJavaType("java.util.ArrayList"); //$NON-NLS-1$
        }
        
        return arrayListInstance;
    }

    public static FullyQualifiedJavaType getStringInstance() {
        if (stringInstance == null) {
            stringInstance = new FullyQualifiedJavaType("java.lang.String"); //$NON-NLS-1$
        }
        
        return stringInstance;
    }
    
    public static FullyQualifiedJavaType getBooleanInstance() {
        if (booleanInstance == null) {
            booleanInstance = new FullyQualifiedJavaType("boolean"); //$NON-NLS-1$
        }
        
        return booleanInstance;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return fullyQualifiedName.compareTo(((FullyQualifiedJavaType)o).fullyQualifiedName);
    }
}
