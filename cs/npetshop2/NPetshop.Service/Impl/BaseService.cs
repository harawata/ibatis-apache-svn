using System;
using IBatisNet.DataAccess;

namespace NPetshop.Service.Impl
{
	/// <summary>
	/// Summary description for BaseService.
	/// </summary>
	public abstract class BaseService
	{
		protected DaoManager _daoManager = null;

		public BaseService()
		{
			_daoManager = ServiceConfig.GetInstance(false).DaoManager;
		}
	}
}
