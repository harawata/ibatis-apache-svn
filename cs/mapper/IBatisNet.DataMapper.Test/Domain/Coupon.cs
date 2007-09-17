

using System.Collections.Generic;

namespace IBatisNet.DataMapper.Test.Domain
{
    public class Coupon
    {
        private int id;
        private string _code;
        private IList<int> _brandIds = new List<int>(); 

        public virtual int Id
        {
            get { return id; }
            set { id = value; }
        }

        public string Code
        {
            get { return _code; }
            set { _code = value; }
        }

        public IList<int> BrandIds
        {
            get { return _brandIds; }
            set { _brandIds = value; }
        }
    }
}
