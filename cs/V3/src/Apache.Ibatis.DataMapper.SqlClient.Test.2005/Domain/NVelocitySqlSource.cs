using System.Collections.Generic;
using System.IO;
using Apache.Ibatis.Common.Contracts;
using Apache.Ibatis.DataMapper.MappedStatements;
using Apache.Ibatis.DataMapper.Model.Sql.External;
using NVelocity;
using NVelocity.App;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Domain
{
    /// <summary>
    /// NVelocity implemantation of <see cref="ISqlSource"/>
    /// which parse sql string
    /// </summary>
    /// <remarks>
    /// See http://www.castleproject.org/others/nvelocity/index.html
    /// </remarks>
    public class NVelocitySqlSource : ISqlSource
    {
        private readonly VelocityEngine velocityEngine = null;

        /// <summary>
        /// Initializes a new instance of the <see cref="NVelocitySqlSource"/> class.
        /// </summary>
        public NVelocitySqlSource()
        {
            velocityEngine = new VelocityEngine();
            velocityEngine.Init();

        }

        #region ISqlSource Members

        /// <summary>
        /// Gets the SQL.
        /// </summary>
        /// <param name="mappedStatement">The mapped statement.</param>
        /// <param name="parameterObject">The parameter object.</param>
        /// <returns></returns>
        /// <remarks>
        /// Paremeters should be typeof IDictionary<string, object>
        /// </remarks>
        public string GetSql(IMappedStatement mappedStatement, object parameterObject)
        {
            Contract.Assert.That(parameterObject, Is.TypeOf<IDictionary<string, object>>()).When("Processing NVelocity source for statement :" + mappedStatement.Id);

            StringWriter sw = new StringWriter();
            ExternalSql externalSql = (ExternalSql)mappedStatement.Statement.Sql;

            VelocityContext velocityContext = new VelocityContext();

            IDictionary<string, object> dico = (IDictionary<string, object>)parameterObject;

            foreach(string key in dico.Keys)
            {
                velocityContext.Put(key, dico[key]);
            }

            bool success = velocityEngine.Evaluate(velocityContext, sw, "error", externalSql.CommandText);

            return sw.GetStringBuilder().ToString();
        }

        #endregion
    }
}
