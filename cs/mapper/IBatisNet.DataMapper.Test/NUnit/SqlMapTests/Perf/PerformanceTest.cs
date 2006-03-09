using System;
using System.Data;
using System.Text;
using IBatisNet.DataMapper.Test.Domain;

using NUnit.Framework;
using IBatisNet.DataMapper;
using IBatisNet.Common;

namespace IBatisNet.DataMapper.Test.NUnit.SqlMapTests.Perf
{
    [TestFixture]
    [Category("MSSQL")]
    [Category("Performance")]
    public class PerformanceTest : BaseTest
    {

        #region DataMapper
        [Test]
        public void IbatisOnly()
        {
            for (int n = 2; n < 4000; n *= 2)
            {

                Simple[] simples = new Simple[n];
                object[] ids = new object[n];
                for (int i = 0; i < n; i++)
                {
                    simples[i] = new Simple();
                    simples[i].Init();
                    simples[i].Count = i;
                    simples[i].Id = i;
                }

                //Now do timings

                sqlMap.OpenConnection();
                long time = DateTime.Now.Ticks;
                Ibatis(simples, n, "h1");
                long ibatis = DateTime.Now.Ticks - time;
                sqlMap.CloseConnection();

                sqlMap.OpenConnection();
                time = DateTime.Now.Ticks;
                Ibatis(simples, n, "h2");
                ibatis += DateTime.Now.Ticks - time;
                sqlMap.CloseConnection();

                sqlMap.OpenConnection();
                time = DateTime.Now.Ticks;
                Ibatis(simples, n, "h2");
                ibatis += DateTime.Now.Ticks - time;
                sqlMap.CloseConnection();

                System.Console.WriteLine("Objects: " + n + " - iBATIS DataMapper: " + ibatis);
            }
            System.GC.Collect();
        }

        private void Ibatis(Simple[] simples, int N, string runname)
        {
            sqlMap.BeginTransaction(false);

            for (int i = 0; i < N; i++)
            {
                sqlMap.Insert("InsertSimple", simples[i]);
            }

            for (int i = 0; i < N; i++)
            {
                simples[i].Name = "NH - " + i + N + runname + " - " + System.DateTime.Now.Ticks;
                sqlMap.Update("UpdateSimple", simples[i]);
            }

            for (int i = 0; i < N; i++)
            {
                sqlMap.Delete("DeleteSimple", simples[i].Id);
            }

            sqlMap.CommitTransaction(false);
        } 
        #endregion

        #region ADO.NET
        [Test]
        public void AdoNetOnly()
        {
            for (int n = 2; n < 4000; n *= 2)
            {
                Simple[] simples = new Simple[n];
                for (int i = 0; i < n; i++)
                {
                    simples[i] = new Simple();
                    simples[i].Init();
                    simples[i].Count = i;
                    simples[i].Id = i;
                }

                //Now do timings

                IDbConnection _connection = sqlMap.DataSource.DbProvider.CreateConnection();
                _connection.ConnectionString = sqlMap.DataSource.ConnectionString;

                _connection.Open();

                long time = DateTime.Now.Ticks;
                DirectAdoNet(_connection, simples, n, "j1");
                long adonet = DateTime.Now.Ticks - time;
                _connection.Close();

                _connection.Open();
                time = DateTime.Now.Ticks;
                DirectAdoNet(_connection, simples, n, "j2");
                adonet += DateTime.Now.Ticks - time;
                _connection.Close();

                _connection.Open();
                time = DateTime.Now.Ticks;
                DirectAdoNet(_connection, simples, n, "j2");
                adonet += DateTime.Now.Ticks - time;
                _connection.Close();

                System.Console.Out.WriteLine("Objects: " + n + " Direct ADO.NET: " + adonet);
            }
            System.GC.Collect();
        }

        private void DirectAdoNet(IDbConnection c, Simple[] simples, int N, string runname)
        {
            IDbCommand insert = InsertCommand();
            IDbCommand delete = DeleteCommand();
            IDbCommand select = SelectCommand();
            IDbCommand update = UpdateCommand();

            IDbTransaction t = c.BeginTransaction();

            insert.Connection = c;
            delete.Connection = c;
            select.Connection = c;
            update.Connection = c;

            insert.Transaction = t;
            delete.Transaction = t;
            select.Transaction = t;
            update.Transaction = t;

            insert.Prepare();
            delete.Prepare();
            select.Prepare();
            update.Prepare();

            for (int i = 0; i < N; i++)
            {
                ((IDbDataParameter)insert.Parameters[0]).Value = simples[i].Name;
                ((IDbDataParameter)insert.Parameters[1]).Value = simples[i].Address;
                ((IDbDataParameter)insert.Parameters[2]).Value = simples[i].Count;
                ((IDbDataParameter)insert.Parameters[3]).Value = simples[i].Date;
                ((IDbDataParameter)insert.Parameters[4]).Value = simples[i].Pay;
                ((IDbDataParameter)insert.Parameters[5]).Value = simples[i].Id;

                insert.ExecuteNonQuery();
            }

            for (int i = 0; i < N; i++)
            {
                ((IDbDataParameter)update.Parameters[0]).Value = "DR - " + i + N + runname + " - " + System.DateTime.Now.Ticks;
                ((IDbDataParameter)update.Parameters[1]).Value = simples[i].Address;
                ((IDbDataParameter)update.Parameters[2]).Value = simples[i].Count;
                ((IDbDataParameter)update.Parameters[3]).Value = simples[i].Date;
                ((IDbDataParameter)update.Parameters[4]).Value = simples[i].Pay;
                ((IDbDataParameter)update.Parameters[5]).Value = simples[i].Id;

                update.ExecuteNonQuery();
            }

            for (int i = 0; i < N; i++)
            {
                ((IDbDataParameter)delete.Parameters[0]).Value = simples[i].Id;
                delete.ExecuteNonQuery();
            }

            t.Commit();
        }

        private IDbCommand DeleteCommand()
        {
            string sql = "delete from Simples where id = ";
            sql += "@id";

            IDbCommand cmd = sqlMap.DataSource.DbProvider.CreateCommand();
            cmd.CommandText = sql;

            IDbDataParameter prm = cmd.CreateParameter();
            prm.ParameterName = "@id";
            prm.DbType = DbType.Int32;
            cmd.Parameters.Add(prm);

            return cmd;
        }

        private IDbCommand InsertCommand()
        {
            string sql = "insert into Simples ( name, address, count, date, pay, id ) values (";
            for (int i = 0; i < 6; i++)
            {
                if (i > 0) sql += ", ";
                sql += "@param" + i.ToString();
            }

            sql += ")";

            IDbCommand cmd = sqlMap.DataSource.DbProvider.CreateCommand();
            cmd.CommandText = sql;
            AppendInsertUpdateParams(cmd);

            return cmd;
        }

        private IDbCommand SelectCommand()
        {
            string sql = "SELECT s.id, s.name, s.address, s.count, s.date, s.pay FROM Simples s";

            IDbCommand cmd = sqlMap.DataSource.DbProvider.CreateCommand();
            cmd.CommandText = sql;

            return cmd;
        }

        private IDbCommand UpdateCommand()
        {
            string sql = "update Simples set";
            sql += (" name = " + "@param0");
            sql += (", address = " + "@param1");
            sql += (", count = " + "@param2");
            sql += (", date = " + "@param3");
            sql += (", pay = " + "@param4");
            sql += " where id = " + "@param5";

            IDbCommand cmd = sqlMap.DataSource.DbProvider.CreateCommand();
            cmd.CommandText = sql;

            AppendInsertUpdateParams(cmd);

            return cmd;
        }

        private void AppendInsertUpdateParams(IDbCommand cmd)
        {
            IDbDataParameter[] prm = new IDbDataParameter[6];
            for (int j = 0; j < 6; j++)
            {
                prm[j] = cmd.CreateParameter();
                prm[j].ParameterName = "@param" + j.ToString();
                cmd.Parameters.Add(prm[j]);
            }

            int i = 0;
            prm[i].DbType = DbType.String;
            prm[i].Size = 255;
            i++;

            prm[i].DbType = DbType.String;
            prm[i].Size = 200;
            i++;

            prm[i].DbType = DbType.Int32;
            i++;

            prm[i].DbType = DbType.DateTime;
            i++;

            prm[i].DbType = DbType.Decimal;
            prm[i].Scale = 2;
            prm[i].Precision = 5;
            i++;

            prm[i].DbType = DbType.Int32;
            i++;

        } 
        #endregion

        [Test]
        public void Many()
        {
            long ibatis = 0;
            long adonet = 0;

            //for(int n = 0; n < 20; n++) 
            for (int n = 0; n < 5; n++)
            {
                Simple[] simples = new Simple[n];
                for (int i = 0; i < n; i++)
                {
                    simples[i] = new Simple();
                    simples[i].Init();
                    simples[i].Count = i;
                    simples[i].Id = i;
                }

                sqlMap.OpenConnection();
                Ibatis(simples, n, "h0");
                sqlMap.CloseConnection();

                IDbConnection _connection = sqlMap.DataSource.DbProvider.CreateConnection();
                _connection.ConnectionString = sqlMap.DataSource.ConnectionString;

                _connection.Open();
                DirectAdoNet(_connection, simples, n, "j0");
                _connection.Close();

                sqlMap.OpenConnection();
                Ibatis(simples, n, "h0");
                sqlMap.CloseConnection();

                _connection.Open();
                DirectAdoNet(_connection, simples, n, "j0");
                _connection.Close();

                // now do timings

                int loops = 30;

                for (int runIndex = 1; runIndex < 4; runIndex++)
                {

                    long time = DateTime.Now.Ticks;
                    for (int i = 0; i < loops; i++)
                    {
                        //using (IDalSession session = sqlMap.OpenConnection() )
                        //{
                        sqlMap.OpenConnection();
                            Ibatis(simples, n, "h" + runIndex.ToString());
                         sqlMap.CloseConnection();
                        //}
                    }
                    ibatis += DateTime.Now.Ticks - time;

                    time = DateTime.Now.Ticks;
                    for (int i = 0; i < loops; i++)
                    {
                        _connection.Open();
                        DirectAdoNet(_connection, simples, n, "j" + runIndex.ToString());
                        _connection.Close();
                    }
                    adonet += DateTime.Now.Ticks - time;


                }
            }
            System.Console.Out.WriteLine("iBatis DataMapper : " + ibatis + "ms / Direct ADO.NET: " + adonet + "ms = Ratio: " + (((float)ibatis / adonet)).ToString());

            System.GC.Collect();
        }

        [Test]
        public void Simultaneous()
        {
            long ibatis = 0;
            long adonet = 0;

            for (int n = 2; n < 4000; n *= 2)
            {
                Simple[] simples = new Simple[n];
                for (int i = 0; i < n; i++)
                {
                    simples[i] = new Simple();
                    simples[i].Init();
                    simples[i].Count = i;
                    simples[i].Id = i;
                }

                sqlMap.OpenConnection();
                Ibatis(simples, n, "h0");
                sqlMap.CloseConnection();

                IDbConnection _connection = sqlMap.DataSource.DbProvider.CreateConnection();
                _connection.ConnectionString = sqlMap.DataSource.ConnectionString;

                _connection.Open();
                DirectAdoNet(_connection, simples, n, "j0");
                _connection.Close();

                sqlMap.OpenConnection();
                Ibatis(simples, n, "h0");
                sqlMap.CloseConnection();

                _connection.Open();
                DirectAdoNet(_connection, simples, n, "j0");
                _connection.Close();

                //Now do timings

                sqlMap.OpenConnection();
                long time = DateTime.Now.Ticks;
                Ibatis(simples, n, "h1");
                ibatis = DateTime.Now.Ticks - time;
                sqlMap.CloseConnection();

                _connection.Open();
                time = DateTime.Now.Ticks;
                DirectAdoNet(_connection, simples, n, "j1");
                adonet = DateTime.Now.Ticks - time;
                _connection.Close();

                sqlMap.OpenConnection();
                time = DateTime.Now.Ticks;
                Ibatis(simples, n, "h2");
                ibatis += DateTime.Now.Ticks - time;
                sqlMap.CloseConnection();

                _connection.Open();
                time = DateTime.Now.Ticks;
                DirectAdoNet(_connection, simples, n, "j2");
                adonet += DateTime.Now.Ticks - time;
                _connection.Close();

                sqlMap.OpenConnection();
                time = DateTime.Now.Ticks;
                Ibatis(simples, n, "h2");
                ibatis += DateTime.Now.Ticks - time;
                sqlMap.CloseConnection();

                _connection.Open();
                time = DateTime.Now.Ticks;
                DirectAdoNet(_connection, simples, n, "j2");
                adonet += DateTime.Now.Ticks - time;
                _connection.Close();
                System.Console.Out.WriteLine("Objects " + n + " iBATIS DataMapper : " + ibatis + "ms / Direct ADO.NET: " + adonet + "ms = Ratio: " + (((float)ibatis / adonet)).ToString());
            }

            System.GC.Collect();
        }
    }
}
