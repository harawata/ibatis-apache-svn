
#region Apache Notice
/*****************************************************************************
 * $Revision: 408099 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2008/2005 - The Apache Software Foundation
 *  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ********************************************************************************/
#endregion

#region Using

using System;
using System.Collections.Specialized;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Security.Permissions;
using System.Xml;
using Apache.Ibatis.Common.Exceptions;
using Apache.Ibatis.Common.Logging;
using Apache.Ibatis.Common.Utilities.TypesResolver;
using Apache.Ibatis.Common.Xml;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Contracts;

#endregion

namespace Apache.Ibatis.Common.Resources
{
	/// <summary>
	/// A class to simplify access to resources.
	/// 
	/// The file can be loaded from the application root directory 
	/// (use the resource attribute) 
	/// or from any valid URL (use the url attribute). 
	/// For example,to load a fixed path file, use:
	/// &lt;properties url=”file:///c:/config/my.properties” /&gt;
	/// </summary>
	public class Resources
	{

		#region Fields

        public static readonly string ApplicationBase = AppDomain.CurrentDomain.SetupInformation.ApplicationBase;

        public static readonly string DefaultBasePath = AppDomain.CurrentDomain.BaseDirectory;

		private static readonly ILog _logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );

		#endregion

		#region Methods

        ///// <summary>
        ///// Protocole separator
        ///// </summary>
        // public const string PROTOCOL_SEPARATOR = "://";

        ///// <summary>
        ///// Strips protocol name from the resource name
        ///// </summary>
        ///// <param name="filePath">Name of the resource</param>
        ///// <returns>Name of the resource without protocol name</returns>
        //public static string GetFileSystemResourceWithoutProtocol(string filePath)
        //{
        //    int pos = filePath.IndexOf(PROTOCOL_SEPARATOR);
        //    if (pos == -1)
        //    {
        //        return filePath;
        //    }
        //    else
        //    {
        //        // skip forward slashes after protocol name, if any
        //        if (filePath.Length > pos + PROTOCOL_SEPARATOR.Length)
        //        {
        //            while (filePath[++pos] == '/')
        //            {
        //                ;
        //            }
        //        }
        //        return filePath.Substring(pos);
        //    }
        //}

        ///// <summary>
        ///// Get config file
        ///// </summary>
        ///// <param name="resourcePath">
        ///// A config resource path.
        ///// </param>
        ///// <returns>An XmlDocument representation of the config file</returns>
        //public static XmlDocument GetConfigAsXmlDocument(string resourcePath)
        //{
        //    XmlDocument config = new XmlDocument(); 
        //    XmlTextReader reader = null; 
        //    resourcePath = GetFileSystemResourceWithoutProtocol(resourcePath);
			
        //    if (!Resources.FileExists(resourcePath))
        //    {
        //        resourcePath = Path.Combine(_baseDirectory, resourcePath); 
        //    }

        //    try 
        //    { 
        //        reader = new XmlTextReader( resourcePath ); 				
        //        config.Load(reader); 
        //    } 
        //    catch(Exception e) 
        //    { 
        //        throw new ConfigurationException( 
        //            string.Format("Unable to load config file \"{0}\". Cause : {1}", 
        //            resourcePath, 
        //            e.Message ) ,e); 
        //    } 
        //    finally 
        //    { 
        //        if (reader != null) 
        //        { 
        //            reader.Close(); 
        //        } 
        //    } 
        //    return config; 

        //}

        /// <summary>
        /// Determines whether the specified file exists.
        /// </summary>
        /// <param name="filePath">The file to check.</param>
        /// <returns>
        /// true if the caller has the required permissions and path contains the name of an existing file
        /// false if the caller has the required permissions and path doesn't contain the name of an existing file
        /// else exception
        /// </returns>
        public static bool FileExists(string filePath)
        {
            if (File.Exists(filePath))
            {
                // true if the caller has the required permissions and path contains the name of an existing file; 
                return true;
            }
            else
            {
                // This method also returns false if the caller does not have sufficient permissions 
                // to read the specified file, 
                // no exception is thrown and the method returns false regardless of the existence of path.
                // So we check permissiion and throw an exception if no permission
                FileIOPermission filePermission = null;
                try
                {
                    // filePath must be the absolute path of the file. 
                    filePermission = new FileIOPermission(FileIOPermissionAccess.Read, filePath);
                }
                catch
                {
                    return false;
                }
                try
                {
                    filePermission.Demand();
                }
                catch (Exception e)
                {
                    throw new ResourceException(
                        string.Format("iBATIS doesn't have the right to read the config file \"{0}\". Cause : {1}",
                        filePath,
                        e.Message), e);
                }

                return false;
            }
        }


        ///// <summary>
        ///// Load an XML resource from a location specify by the node.
        ///// </summary>
        ///// <param name="node">An location node</param>
        ///// <param name="configStore">The config store.</param>
        ///// <returns>Return the Xml document load.</returns>
        //public static XmlDocument GetAsXmlDocument(XmlNode node, IConfigurationStore configStore)
        //{
        //    // precondiftion
        //    // node.Attributes["uri"] <> de null et empty

        //    return GetAsXmlDocument(node.Attributes["uri"].Value, configStore);
        //}

        ///// <summary>
        ///// Gets as XML document from the specified uri.
        ///// </summary>
        ///// <param name="uri">The URI.</param>
        ///// <returns>Return the Xml document load.</returns>
        //public static XmlDocument GetAsXmlDocument(string uri)
        //{
        //    return GetAsXmlDocument(uri, new DefaultConfigurationStore());
        //}

        /// <summary>
        /// Gets as XML document from the specified uri.
        /// </summary>
        /// <param name="uri">The URI.</param>
        ///// <param name="configStore">The config store.</param>, IConfigurationStore configStore
        /// <returns>Return the Xml document load.</returns>
        public static XmlDocument GetUriAsXmlDocument(string uri)
        {
            Contract.Require.That(uri, Is.Not.Null & Is.Not.Empty).When("retrieving uri in GetUriAsXmlDocument method");

            IResource resource = ResourceLoaderRegistry.GetResource(uri);

            XmlDocument xmlDocument = Resources.GetStreamAsXmlDocument(resource.Stream);

            return xmlDocument;
        }

		/// <summary>
		/// Get XmlDocument from a stream resource
		/// </summary>
		/// <param name="resource"></param>
		/// <returns></returns>
		public static XmlDocument GetStreamAsXmlDocument(Stream resource)
		{
			XmlDocument config = new XmlDocument();

			try 
			{
				config.Load(resource);
			}
			catch(Exception e)
			{
				throw new ConfigurationException(
					string.Format("Unable to load XmlDocument via stream. Cause : {0}", 
					e.Message ) ,e); 
			}

			return config;
		}

        /// <summary>
        /// Gets the URI as XML reader.
        /// </summary>
        /// <param name="uri">The URI.</param>
        /// <returns></returns>
        public static XmlTextReader GetUriAsXmlReader(string uri)
        {
            Contract.Require.That(uri, Is.Not.Null & Is.Not.Empty).When("retrieving uri in GetUriAsXmlReader method");

            IResource resource = ResourceLoaderRegistry.GetResource(uri);

            return Resources.GetStreamAsXmlReader(resource.Stream);
        }

        /// <summary>
        /// Gets the stream as XML reader.
        /// </summary>
        /// <param name="resource">The resource.</param>
        /// <returns></returns>
        public static XmlTextReader GetStreamAsXmlReader(Stream resource)
        {
            Contract.Require.That(resource, Is.Not.Null).When("Getting stream As XmlReader");

            XmlTextReader reader = null;

            try
            {
                reader = new XmlTextReader(resource);
            }
            catch (Exception e)
            {
                throw new ConfigurationException(
                    string.Format("Unable to load XmlReader via stream. Cause : {0}",
                    e.Message), e);
            }

            return reader;
        }

        ///// <summary>
        ///// Get the path resource of an url or resource location.
        ///// </summary>
        ///// <param name="node">The specification from where to load.</param>
        ///// <param name="properties">the global properties</param>
        ///// <returns></returns>
        //public static string GetValueOfNodeResourceUrl(XmlNode node, NameValueCollection properties)
        //{
        //    string path = null;

        //    if (node.Attributes["uri"] != null)
        //    {
        //        string ressource = NodeUtils.ParsePropertyTokens(node.Attributes["uri"].Value, properties);
        //        path = Path.Combine(_applicationBase, ressource);
        //    }
        //    else 
        //    {
        //        throw new ConfigurationException("");
        //    }

        //    return path;
        //}


        ///// <summary>
        ///// Get XmlDocument from a FileInfo resource
        ///// </summary>
        ///// <param name="resource"></param>
        ///// <returns></returns>
        //public static XmlDocument GetFileInfoAsXmlDocument(FileInfo resource)
        //{
        //    XmlDocument config = new XmlDocument();

        //    try 
        //    {
        //        config.Load( resource.FullName );
        //    }
        //    catch(Exception e)
        //    {
        //        throw new ConfigurationException(
        //            string.Format("Unable to load XmlDocument via FileInfo. Cause : {0}", 
        //            e.Message ) ,e); 
        //    }

        //    return config;
        //}

        ///// <summary>
        ///// Get XmlDocument from a Uri resource
        ///// </summary>
        ///// <param name="resource"></param>
        ///// <returns></returns>
        //public static XmlDocument GetUriAsXmlDocument(Uri resource)
        //{
        //    XmlDocument config = new XmlDocument();

        //    try 
        //    {
        //        config.Load( resource.AbsoluteUri );
        //    }
        //    catch(Exception e)
        //    {
        //        throw new ConfigurationException(
        //            string.Format("Unable to load XmlDocument via Uri. Cause : {0}", 
        //            e.Message ) ,e); 
        //    }

        //    return config;
        //}

        ///// <summary>
        ///// Get XmlDocument from relative (from root directory of the application) path resource
        ///// </summary>
        ///// <param name="resource"></param>
        ///// <returns></returns>
        //public static XmlDocument GetResourceAsXmlDocument(string resource)
        //{
        //    XmlDocument config = new XmlDocument();

        //    try 
        //    {
        //        config.Load( Path.Combine(_applicationBase, resource) );
        //    }
        //    catch(Exception e)
        //    {
        //        throw new ConfigurationException(
        //            string.Format("Unable to load file via resource \"{0}\" as resource. Cause : {1}", 
        //            resource, 
        //            e.Message ) ,e); 
        //    }

        //    return config;
        //}


        ///// <summary>
        ///// Get XmlDocument from absolute path resource
        ///// </summary>
        ///// <param name="url"></param>
        ///// <returns></returns>
        //public static XmlDocument GetUrlAsXmlDocument(string url)
        //{
        //    XmlDocument config = new XmlDocument();

        //    try 
        //    {
        //        config.Load(url);
        //    }
        //    catch(Exception e)
        //    {
        //        throw new ConfigurationException(
        //            string.Format("Unable to load file via url \"{0}\" as url. Cause : {1}",
        //            url, 
        //            e.Message  ) ,e);
        //    }

        //    return config;
        //}

		
        ///// <summary>
        ///// Doit aller ds AssemblyResource
        ///// </summary>
        ///// <param name="resource"></param>
        ///// <returns></returns>
        //public static XmlDocument GetEmbeddedResourceAsXmlDocument(string resource)
        //{
        //    XmlDocument config = new XmlDocument();
        //    bool isLoad = false;

        //    FileAssemblyInfo fileInfo = new FileAssemblyInfo (resource);
        //    if (fileInfo.IsAssemblyQualified)
        //    {
        //        Assembly assembly = Assembly.Load(fileInfo.AssemblyName);

        //        Stream stream = assembly.GetManifestResourceStream(fileInfo.ResourceFileName);
        //        // JIRA - IBATISNET-103 
        //        if (stream == null)
        //        {
        //            stream = assembly.GetManifestResourceStream(fileInfo.FileName);
        //        }
        //        if (stream != null)
        //        {
        //            try
        //            {
        //                config.Load(stream);
        //                isLoad = true;
        //            }
        //            catch(Exception e)
        //            {
        //                throw new ConfigurationException(
        //                    string.Format("Unable to load file \"{0}\" in embedded resource. Cause : {1}",
        //                    resource, 
        //                    e.Message  ) ,e);
        //            }
        //        }
        //    } 
        //    else
        //    {
        //        // bare type name... loop thru all loaded assemblies
        //        Assembly [] assemblies = AppDomain.CurrentDomain.GetAssemblies ();
        //        foreach (Assembly assembly in assemblies)
        //        {
        //            Stream stream = assembly.GetManifestResourceStream(fileInfo.FileName);
        //            if (stream != null)
        //            {
        //                try
        //                {
        //                    config.Load(stream);
        //                    isLoad = true;
        //                }
        //                catch(Exception e)
        //                {
        //                    throw new ConfigurationException(
        //                        string.Format("Unable to load file \"{0}\" in embedded resource. Cause : ",
        //                        resource, 
        //                        e.Message  ) ,e);
        //                }
        //                break;
        //            }
        //        }
        //    }

        //    if (isLoad == false) 
        //    {
        //        _logger.Error("Could not load embedded resource from assembly");
        //        throw new ConfigurationException(
        //            string.Format("Unable to load embedded resource from assembly \"{0}\".",
        //            fileInfo.OriginalFileName));
        //    }

        //    return config;
        //}


        ///// <summary>
        ///// Load a file from a given resource path
        ///// </summary>
        ///// <param name="resourcePath">
        ///// The resource path
        ///// </param>
        ///// <returns>return a FileInfo</returns>
        //public static FileInfo GetFileInfo(string resourcePath)
        //{
        //    FileInfo fileInfo = null;
        //    resourcePath = GetFileSystemResourceWithoutProtocol(resourcePath);

        //    if ( !Resources.FileExists(resourcePath)) 
        //    {
        //        resourcePath = Path.Combine(_applicationBase, resourcePath);
        //    }

        //    try
        //    {
        //        //argument : The fully qualified name of the new file, or the relative file name. 
        //        fileInfo = new FileInfo(resourcePath);
        //    }
        //    catch(Exception e)
        //    {
        //        throw new ConfigurationException(
        //            string.Format("Unable to load file \"{0}\". Cause : \"{1}\"", resourcePath, e.Message),e);
        //    }
        //    return fileInfo;

        //}


        ///// <summary>
        ///// Resolves the supplied type name into a <see cref="System.Type"/> instance.
        ///// </summary>
        ///// <param name="typeName">
        ///// The (possibly partially assembly qualified) name of a <see cref="System.Type"/>.
        ///// </param>
        ///// <returns>
        ///// A resolved <see cref="System.Type"/> instance.
        ///// </returns>
        ///// <exception cref="System.TypeLoadException">
        ///// If the type cannot be resolved.
        ///// </exception>
        //[Obsolete("Use Apache.Ibatis.Common.Utilities.TypeUtils")]
        //public static Type TypeForName(string typeName)
        //{
        //    return TypeUtils.ResolveType(typeName);
        //        //_cachedTypeResolver.Resolve(className);
        //}

		#endregion

	}
}
