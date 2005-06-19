
using System;

using IBatisNet.Common.Utilities;
using IBatisNet.DataAccess;
using IBatisNet.DataAccess.Configuration;

namespace NPetshop.Service
{
	/// <summary>
	/// Summary description for ServiceConfig.
	/// </summary>
	public class ServiceConfig
	{
		static private object _synRoot = new Object();
		static private ServiceConfig _instance;

		private DaoManager _daoManager = null;

		/// <summary>
		/// Remove public constructor. prevent instantiation.
		/// </summary>
		private ServiceConfig(){}

		static public ServiceConfig GetInstance(bool test)
		{
			if (_instance==null)
			{
				lock(_synRoot)
				{
					if (_instance==null)
					{
						DomDaoManagerBuilder builder = new DomDaoManagerBuilder();
						ConfigureHandler handler = new ConfigureHandler( ServiceConfig.Reset );
						if (test)
						{
							builder.Configure(@"..\..\..\NPetshop.Persistence\dao.config");
						}
						else
						{
							builder.Configure(@"..\NPetshop.Persistence\dao.config");							
						}

						_instance = new ServiceConfig();
						_instance._daoManager = DaoManager.GetInstance("SqlMapDao");
					}
				}
			}
			return _instance;
		}


		/// <summary>
		/// Reset the singleton
		/// </summary>
		/// <remarks>
		/// Must verify ConfigureHandler signature.
		/// </remarks>
		/// <param name="obj">
		/// </param>
		static public void Reset(object obj)
		{
			_instance =null;
		}

		public DaoManager DaoManager
		{
			get
			{
				return _daoManager;
			}
		}

	}
}
