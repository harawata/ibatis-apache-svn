using System;

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// Create objects via Activator.CreateInstance
	/// </summary>
	public class ActivatorFactory : IFactory
	{
		private Type _typeToCreate = null;

		/// <summary>
		/// 
		/// </summary>
		/// <param name="typeToCreate"></param>
		public ActivatorFactory(Type typeToCreate)
		{
			_typeToCreate = typeToCreate;
		}

		#region IFactory members

		/// <summary>
		/// Create a new object instance via via Activator.CreateInstance
		/// </summary>
		/// <returns></returns>
		public object CreateInstance()
		{
			return Activator.CreateInstance( _typeToCreate );
		}

		#endregion
	}
}
