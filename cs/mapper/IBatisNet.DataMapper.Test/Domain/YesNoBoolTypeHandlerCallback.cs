using System;

using IBatisNet.DataMapper.TypesHandler;

namespace IBatisNet.DataMapper.Test.Domain
{
	/// <summary>
	/// YesNoBoolTypeHandlerCallback.
	/// </summary>
	public class YesNoBoolTypeHandlerCallback : ITypeHandlerCallback
	{
		private const string YES = "Oui";
		private const string NO = "Non";

		#region ITypeHandlerCallback members

		public object GetNullValue(string nullValue)
		{
			if (YES.Equals(nullValue)) 
			{
				return true;
			} 
			else if (NO.Equals(nullValue)) 
			{
				return false;
			} 
			else 
			{
				throw new Exception("Unexpected value " + nullValue + " found where "+YES+" or "+NO+" was expected.");
			}		
		}

		public object GetResult(IResultGetter getter)
		{
			string s = getter.Value as string;
			if (YES.Equals(s)) 
			{
				return true;
			}
			else if (NO.Equals(s)) 
			{
				return false;
		} 
			else 
			{
 				 throw new Exception("Unexpected value " + s + " found where "+YES+" or "+NO+" was expected.");
			}
		}

		public void SetParameter(IParameterSetter setter, object parameter)
		{
			bool b = Convert.ToBoolean(parameter);
			if (b) 
			{
				setter.Value = YES;
			} 
			else 
			{
				setter.Value = NO;
			}			
		}

		#endregion
	}
}
