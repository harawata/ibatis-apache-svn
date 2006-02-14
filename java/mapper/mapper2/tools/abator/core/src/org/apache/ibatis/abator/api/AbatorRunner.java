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
package org.apache.ibatis.abator.api;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.config.AbatorConfiguration;
import org.apache.ibatis.abator.config.xml.AbatorConfigurationParser;
import org.apache.ibatis.abator.exception.InvalidConfigurationException;
import org.apache.ibatis.abator.exception.XMLParserException;
import org.apache.ibatis.abator.internal.DefaultShellCallback;

/**
 * This class allows Abator to be run from the command line.
 * 
 * @author Jeff Butler
 */
public class AbatorRunner {

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			return;
		}
		
		String configfile = args[0];
		boolean overwrite = "true".equalsIgnoreCase(args[1]);
		
        List warnings = new ArrayList();
        
        File configurationFile = new File(configfile);
        if (!configurationFile.exists()) {
            writeLine("configFile " + configfile + " does not exist");
            return;
        }

        try {
            AbatorConfigurationParser cp = new AbatorConfigurationParser(
                warnings);
            AbatorConfiguration config = cp.parseAbatorConfiguration(configurationFile);
            
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            
            Abator abator = new Abator(config, callback, warnings);
            
            abator.generate(null);
            
        } catch (XMLParserException e) {
        	writeLine("XML Parser Errors occured:");
        	writeLine();
            List errors = e.getErrors();
            Iterator iter = errors.iterator();
            while (iter.hasNext()) {
                writeLine((String) iter.next());
            }
            
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            // ignore (will never happen with the DefaultShellCallback)
            ;
        }
        
        Iterator iter = warnings.iterator();
        while (iter.hasNext()) {
            writeLine((String) iter.next());
        }
        
        if (warnings.size() == 0) {
        	writeLine("Abator finshed successfully.");
        } else {
        	writeLine();
        	writeLine("Abator finshed successfully, there were warninigs.");
        }
	}
	
	private static void usage() {
		writeLine("Abator code generator for iBATIS.  Usage:");
		writeLine("   java -jar abatorxx.jar configFile overwrite");
		writeLine();
		writeLine("Where:");
		writeLine("   configFile: the name of the abator XML configuration file");
		writeLine("   overwrite: true if exsting Java files should be overwritten");
	}
	
	private static void writeLine(String message) {
		System.out.println(message);
	}

	private static void writeLine() {
		System.out.println();
	}
}
