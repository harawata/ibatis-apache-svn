
using Apache.Ibatis.DataMapper.Configuration.Module;
using Apache.Ibatis.DataMapper.OleDb.Test.Domain;
using Apache.Ibatis.DataMapper.TypeHandlers;

namespace Apache.Ibatis.DataMapper.OleDb.Test.Fixtures.Modules
{
    public class AliasModule: Module
    {

        public override void Load()
        {
            RegisterTypeHandler<bool, OuiNonBoolTypeHandlerCallback>("Varchar");
            RegisterTypeHandler<string, AnsiStringTypeHandler>();

            RegisterAlias<Order>("Order");
            RegisterAlias<Category>("Category");
            RegisterAlias<LineItem>("LineItem");
            RegisterAlias<LineItemCollection>("LineItemCollection");
            RegisterAlias<LineItemCollection2>("LineItemCollection2");
            RegisterAlias<HundredsTypeHandlerCallback>("HundredsBool");
            RegisterAlias<OuiNonBoolTypeHandlerCallback>("OuiNonBool");
            RegisterAlias<Account>("Account");

        }
    }
}
