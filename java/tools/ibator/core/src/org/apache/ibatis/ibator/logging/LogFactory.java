/*
 *  Copyright 2009 The Apache Software Foundation
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
package org.apache.ibatis.ibator.logging;

import java.lang.reflect.Constructor;

import org.apache.ibatis.ibator.internal.IbatorObjectFactory;
import org.apache.ibatis.ibator.internal.util.messages.Messages;

/**
 * 
 * @author Clinton Begin
 * @author Jeff Butler
 *
 */
public class LogFactory {

    private static Constructor<?> logConstructor;

    static {
        tryImplementation("org.apache.log4j.Logger", //$NON-NLS-1$
                "org.apache.ibatis.ibator.logging.Log4jImpl"); //$NON-NLS-1$
        tryImplementation("java.util.logging.Logger", //$NON-NLS-1$
                "org.apache.ibatis.ibator.logging.JdkLoggingImpl"); //$NON-NLS-1$
    }

    private static void tryImplementation(String testClassName,
            String implClassName) {
        if (logConstructor == null) {
            try {
                IbatorObjectFactory.internalClassForName(testClassName);
                Class<?> implClass = IbatorObjectFactory.internalClassForName(implClassName);
                logConstructor = implClass
                        .getConstructor(new Class[] { Class.class });
            } catch (Throwable t) {
            }
        }
    }

    public static Log getLog(Class<?> aClass) {
        try {
            return (Log) logConstructor.newInstance(new Object[] { aClass });
        } catch (Throwable t) {
            throw new RuntimeException(Messages.getString("RuntimeError.21", //$NON-NLS-1$
                    aClass.getName(), t.getMessage()), t);
        }
    }

    /**
     * This method will switch the logging implementation to Java native logging.
     * This is useful in situations
     * where you want to use Java native logging to log Ibator activity but
     * Log4J is on the classpath. Note that this method is
     * only effective for log classes obtained after calling this method. If you
     * intend to use this method you should call it before calling any other
     * Ibator method.
     */
    public static synchronized void selectJavaLogging() {
        try {
            IbatorObjectFactory.internalClassForName("java.util.logging.Logger"); //$NON-NLS-1$
            Class<?> implClass = IbatorObjectFactory.internalClassForName(
                    "org.apache.ibatis.ibator.logging.JdkLoggingImpl"); //$NON-NLS-1$
            logConstructor = implClass
                    .getConstructor(new Class[] { Class.class });
        } catch (Throwable t) {
        }
    }
}
