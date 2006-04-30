using System;

namespace IBatisNet.Common.Test.Domain
{
    public enum Days : int
    {
        Sat = 1,
        Sun = 2,
        Mon = 3,
        Tue = 4,
        Wed = 5,
        Thu = 6,
        Fri = 7
    };

	/// <summary>
	/// Summary description for Property.
	/// </summary>
	public class Property
	{
        public int publicInt = int.MinValue;

		private string _string = string.Empty;
        private int _int = int.MinValue;
		private DateTime _dateTime = DateTime.MinValue;
		private decimal _decimal = decimal.MinValue;
		private sbyte _sbyte = sbyte.MinValue;
		private byte _byte = byte.MinValue;
		private char _char = char.MinValue;
		private short _short = short.MinValue;
		private ushort _ushort = ushort.MinValue;
		private uint _uint = uint.MinValue;
		private long _long = long.MinValue;
		private ulong _ulong = ulong.MinValue;
		private bool _bool = false;
		private double _double = double.MinValue;
		private float _float = float.MinValue;
		private Guid _guid = Guid.Empty;
		private TimeSpan _timeSpan = TimeSpan.MinValue;
		private Account _account = null;
        private Days _day;
		private Type _type = null;

#if dotnet2
        private Int32? _intNullable = null;

        public Int32? IntNullable
        {
            get { return _intNullable; }
            set { _intNullable = value; }
        }
#endif

		public Property()
        {
			_type = typeof(int);
        }

        public Days Day
        {
            get { return _day; }
            set { _day = value; }
        }

		public string String
		{
			get { return _string; }
			set { _string = value; }
		}

		public int Int
		{
			get { return _int; }
			set { _int = value; }
		}

		public DateTime DateTime
		{
			get { return _dateTime; }
			set { _dateTime = value; }
		}

		public decimal Decimal
		{
			get { return _decimal; }
			set { _decimal = value; }
		}

		public sbyte SByte
		{
			get { return _sbyte; }
			set { _sbyte = value; }
		}

		public byte Byte
		{
			get { return _byte; }
			set { _byte = value; }
		}

		public char Char
		{
			get { return _char; }
			set { _char = value; }
		}

		public short Short
		{
			get { return _short; }
			set { _short = value; }
		}

		public ushort UShort
		{
			get { return _ushort; }
			set { _ushort = value; }
		}

		public uint UInt
		{
			get { return _uint; }
			set { _uint = value; }
		}

		public long Long
		{
			get { return _long; }
			set { _long = value; }
		}

		public ulong ULong
		{
			get { return _ulong; }
			set { _ulong = value; }
		}

		public bool Bool
		{
			get { return _bool; }
			set { _bool = value; }
		}

		public double Double
		{
			get { return _double; }
			set { _double = value; }
		}

		public float Float
		{
			get { return _float; }
			set { _float = value; }
		}

		public Guid Guid
		{
			get { return _guid; }
			set { _guid = value; }
		}

		public TimeSpan TimeSpan
		{
			get { return _timeSpan; }
			set { _timeSpan = value; }
		}

		public Account Account
		{
			get { return _account; }
			set { _account = value; }
		}
	}
}
