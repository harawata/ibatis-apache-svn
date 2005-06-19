

using System.Collections;
using NPetshop.Domain.Accounts;
using NPetshop.Persistence.Interfaces.Accounts;
using NPetshop.Service.Interfaces;

namespace NPetshop.Service.Impl
{
	/// <summary>
	/// Summary description for AccountService.
	/// </summary>
	public class AccountService : BaseService, IAccountService
	{
		#region Private Fields 
		private IAccountDao _accountDao = null;
		#endregion

		#region Constructor
		public AccountService():base() 
		{
			_accountDao = _daoManager.GetDao( typeof(IAccountDao) ) as IAccountDao;
		}
		#endregion

		#region Public methods

		public Account GetAccount(string username) 
		{
			Account account = null;

			account = _accountDao.GetAccount(username);

			return account;
		}

		public Account GetAccount(string login, string password) 
		{
			Account account = null;

			account = _accountDao.GetAccount(login, password);

			return account;
		}

		public void InsertAccount(Account account) 
		{
			_daoManager.BeginTransaction();
			try
			{
				_accountDao.InsertAccount(account);
				_daoManager.CommitTransaction();
			}
			catch
			{
				_daoManager.RollBackTransaction();
				throw;
			}
		}

		public void UpdateAccount(Account account) 
		{
			_daoManager.BeginTransaction();
			try
			{
				_accountDao.UpdateAccount(account);
				_daoManager.CommitTransaction();
			}
			catch
			{
				_daoManager.RollBackTransaction();
				throw;
			}
		}

		public IList GetUsernameList() 
		{
			return _accountDao.GetUsernameList();
		}
		#endregion

	}
}
