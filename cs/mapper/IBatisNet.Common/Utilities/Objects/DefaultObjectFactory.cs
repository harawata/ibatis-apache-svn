using System;

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// A factory that can create objects 
	/// via Activator.CreateInstance
	/// </summary>
	public class ActivatorObjectFactory : IObjectFactory
	{

		#region IObjectFactory members

		/// <summary>
		/// Create a new factory instance for a given type
		/// </summary>
		/// <param name="typeToCreate"></param>
		/// <returns></returns>
		public IFactory CreateFactory(Type typeToCreate)
		{
			return new ActivatorFactory( typeToCreate );
		}

		#endregion
	}
}
