package ibatortest.java2.execute.conditional.java2;

import java.sql.SQLException;

import ibatortest.java2.BaseTest;
import ibatortest.java2.generated.conditional.java2.dao.AwfulTableDAO;
import ibatortest.java2.generated.conditional.java2.dao.AwfulTableDAOImpl;
import ibatortest.java2.generated.conditional.java2.dao.FieldsblobsDAO;
import ibatortest.java2.generated.conditional.java2.dao.FieldsblobsDAOImpl;
import ibatortest.java2.generated.conditional.java2.dao.FieldsonlyDAO;
import ibatortest.java2.generated.conditional.java2.dao.FieldsonlyDAOImpl;
import ibatortest.java2.generated.conditional.java2.dao.PkblobsDAO;
import ibatortest.java2.generated.conditional.java2.dao.PkblobsDAOImpl;
import ibatortest.java2.generated.conditional.java2.dao.PkfieldsDAO;
import ibatortest.java2.generated.conditional.java2.dao.PkfieldsDAOImpl;
import ibatortest.java2.generated.conditional.java2.dao.PkfieldsblobsDAO;
import ibatortest.java2.generated.conditional.java2.dao.PkfieldsblobsDAOImpl;
import ibatortest.java2.generated.conditional.java2.dao.PkonlyDAO;
import ibatortest.java2.generated.conditional.java2.dao.PkonlyDAOImpl;
import ibatortest.java2.generated.conditional.java2.model.AwfulTable;
import ibatortest.java2.generated.conditional.java2.model.AwfulTableExample;
import ibatortest.java2.generated.conditional.java2.model.FieldsblobsExample;
import ibatortest.java2.generated.conditional.java2.model.FieldsblobsWithBLOBs;
import ibatortest.java2.generated.conditional.java2.model.Fieldsonly;
import ibatortest.java2.generated.conditional.java2.model.FieldsonlyExample;
import ibatortest.java2.generated.conditional.java2.model.Pkblobs;
import ibatortest.java2.generated.conditional.java2.model.PkblobsExample;
import ibatortest.java2.generated.conditional.java2.model.Pkfields;
import ibatortest.java2.generated.conditional.java2.model.PkfieldsExample;
import ibatortest.java2.generated.conditional.java2.model.Pkfieldsblobs;
import ibatortest.java2.generated.conditional.java2.model.PkfieldsblobsExample;
import ibatortest.java2.generated.conditional.java2.model.PkonlyExample;
import ibatortest.java2.generated.conditional.java2.model.PkonlyKey;

public class CountByExampleTests extends BaseTest {

    protected void setUp() throws Exception {
        super.setUp();
        initSqlMapClient(
                "ibatortest/java2/execute/conditional/java2/SqlMapConfig.xml",
                null);
    }

    public void testFieldsOnlyCountByExample() {
        FieldsonlyDAO dao = new FieldsonlyDAOImpl(sqlMapClient);
    
        try {
            Fieldsonly record = new Fieldsonly();
            record.setDoublefield(new Double(11.22));
            record.setFloatfield(new Double(33.44));
            record.setIntegerfield(new Integer(5));
            dao.insert(record);
    
            record = new Fieldsonly();
            record.setDoublefield(new Double(44.55));
            record.setFloatfield(new Double(66.77));
            record.setIntegerfield(new Integer(8));
            dao.insert(record);
    
            record = new Fieldsonly();
            record.setDoublefield(new Double(88.99));
            record.setFloatfield(new Double(100.111));
            record.setIntegerfield(new Integer(9));
            dao.insert(record);
    
            FieldsonlyExample example = new FieldsonlyExample();
            example.createCriteria().andIntegerfieldGreaterThan(new Integer(5));
    
            int rows = dao.countByExample(example);
            assertEquals(2, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(3, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testPKOnlyCountByExample() {
        PkonlyDAO dao = new PkonlyDAOImpl(sqlMapClient);
    
        try {
            PkonlyKey key = new PkonlyKey();
            key.setId(new Integer(1));
            key.setSeqNum(new Integer(3));
            dao.insert(key);
    
            key = new PkonlyKey();
            key.setId(new Integer(5));
            key.setSeqNum(new Integer(6));
            dao.insert(key);
    
            key = new PkonlyKey();
            key.setId(new Integer(7));
            key.setSeqNum(new Integer(8));
            dao.insert(key);
    
            PkonlyExample example = new PkonlyExample();
            example.createCriteria().andIdGreaterThan(new Integer(4));
            int rows = dao.countByExample(example);
            assertEquals(2, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(3, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testPKFieldsBlobsCountByExample() {
        PkfieldsblobsDAO dao = new PkfieldsblobsDAOImpl(sqlMapClient);
    
        try {
            Pkfieldsblobs record = new Pkfieldsblobs();
            record.setId1(new Integer(3));
            record.setId2(new Integer(4));
            record.setFirstname("Jeff");
            record.setLastname("Smith");
            record.setBlob1(generateRandomBlob());
            dao.insert(record);
    
            record = new Pkfieldsblobs();
            record.setId1(new Integer(5));
            record.setId2(new Integer(6));
            record.setFirstname("Scott");
            record.setLastname("Jones");
            record.setBlob1(generateRandomBlob());
            dao.insert(record);
    
            PkfieldsblobsExample example = new PkfieldsblobsExample();
            example.createCriteria().andId1NotEqualTo(new Integer(3));
            int rows = dao.countByExample(example);
            assertEquals(1, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(2, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testPKFieldsCountByExample() {
        PkfieldsDAO dao = new PkfieldsDAOImpl(sqlMapClient);
    
        try {
            Pkfields record = new Pkfields();
            record.setFirstname("Jeff");
            record.setLastname("Smith");
            record.setId1(new Integer(1));
            record.setId2(new Integer(2));
            dao.insert(record);
    
            record = new Pkfields();
            record.setFirstname("Bob");
            record.setLastname("Jones");
            record.setId1(new Integer(3));
            record.setId2(new Integer(4));
    
            dao.insert(record);
    
            PkfieldsExample example = new PkfieldsExample();
            example.createCriteria().andLastnameLike("J%");
            int rows = dao.countByExample(example);
            assertEquals(1, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(2, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testPKBlobsCountByExample() {
        PkblobsDAO dao = new PkblobsDAOImpl(sqlMapClient);
    
        try {
            Pkblobs record = new Pkblobs();
            record.setId(new Integer(3));
            record.setBlob1(generateRandomBlob());
            record.setBlob2(generateRandomBlob());
            dao.insert(record);
    
            record = new Pkblobs();
            record.setId(new Integer(6));
            record.setBlob1(generateRandomBlob());
            record.setBlob2(generateRandomBlob());
            dao.insert(record);
    
            PkblobsExample example = new PkblobsExample();
            example.createCriteria().andIdLessThan(new Integer(4));
            int rows = dao.countByExample(example);
            assertEquals(1, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(2, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testFieldsBlobsCountByExample() {
        FieldsblobsDAO dao = new FieldsblobsDAOImpl(sqlMapClient);
    
        try {
            FieldsblobsWithBLOBs record = new FieldsblobsWithBLOBs();
            record.setFirstname("Jeff");
            record.setLastname("Smith");
            record.setBlob1(generateRandomBlob());
            record.setBlob2(generateRandomBlob());
            dao.insert(record);
    
            record = new FieldsblobsWithBLOBs();
            record.setFirstname("Scott");
            record.setLastname("Jones");
            record.setBlob1(generateRandomBlob());
            record.setBlob2(generateRandomBlob());
            dao.insert(record);
    
            FieldsblobsExample example = new FieldsblobsExample();
            example.createCriteria().andFirstnameLike("S%");
            int rows = dao.countByExample(example);
            assertEquals(1, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(2, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testAwfulTableCountByExample() {
        AwfulTableDAO dao = new AwfulTableDAOImpl(sqlMapClient);
    
        try {
            AwfulTable record = new AwfulTable();
            record.seteMail("fred@fred.com");
            record.setEmailaddress("alsofred@fred.com");
            record.setFirstFirstName("fred1");
            record.setFourthFirstName("fred4");
            record.setFrom("from field");
            record.setId1(new Integer(1));
            record.setId2(new Integer(2));
            record.setId5(new Integer(5));
            record.setId6(new Integer(6));
            record.setId7(new Integer(7));
            record.setSecondCustomerId(new Integer(567));
            record.setSecondFirstName("fred2");
            record.setThirdFirstName("fred3");
    
            dao.insert(record);
    
            record = new AwfulTable();
            record.seteMail("fred2@fred.com");
            record.setEmailaddress("alsofred2@fred.com");
            record.setFirstFirstName("fred11");
            record.setFourthFirstName("fred44");
            record.setFrom("from from field");
            record.setId1(new Integer(11));
            record.setId2(new Integer(22));
            record.setId5(new Integer(55));
            record.setId6(new Integer(66));
            record.setId7(new Integer(77));
            record.setSecondCustomerId(new Integer(567567));
            record.setSecondFirstName("fred22");
            record.setThirdFirstName("fred33");
    
            dao.insert(record);
    
            AwfulTableExample example = new AwfulTableExample();
            example.createCriteria().andEMailLike("fred@%");
            int rows = dao.countByExample(example);
            assertEquals(1, rows);
    
            example.clear();
            rows = dao.countByExample(example);
            assertEquals(2, rows);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }
}