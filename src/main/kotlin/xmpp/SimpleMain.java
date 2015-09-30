package xmpp;

/**
 * Created by marcelosenaga on 9/30/15.
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Main sample routine to show how to do basic operations with the package.
 *
 * <p>
 * <b>NOTE:</b> We use asserts in a couple of places to verify the results but if this were actual production code, we
 * would have proper error handling.
 * </p>
 */
public class SimpleMain {

    // we are using the in-memory H2 database
    private final static String DATABASE_URL = "jdbc:mysql://localhost/test?user=root&password=";

    private Dao<CcsMessage, Integer> messageDao;

    public static void main(String[] args) throws Exception {
        // turn our static method into an instance of Main
        new SimpleMain().doMain(args);
    }

    private void doMain(String[] args) throws Exception {
        ConnectionSource connectionSource = null;
        try {
            // create our data-source for the database
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            // setup our database and DAOs
            setupDatabase(connectionSource);
            // read and write some data
            readWriteData();
            // do a bunch of bulk operations
            readWriteBunch();
            // show how to use the SelectArg object
            useSelectArgFeature();
            // show how to use the SelectArg object
            useTransactions(connectionSource);
            System.out.println("\n\nIt seems to have worked\n\n");
        } finally {
            // destroy the data source which should close underlying connections
            if (connectionSource != null) {
                connectionSource.close();
            }
        }
    }

    /**
     * Setup our database and DAOs
     */
    private void setupDatabase(ConnectionSource connectionSource) throws Exception {

        messageDao = DaoManager.createDao(connectionSource, CcsMessage.class);

        // if you need to create the table
        TableUtils.createTable(connectionSource, CcsMessage.class);
    }

    /**
     * Read and write some example data.
     */
    private void readWriteData() throws Exception {
        // create an instance of CcsMessage
        String name = "Jim Coakley";
        CcsMessage message = new CcsMessage(name, "category", "messageId", new HashMap<String, String>());

        // persist the message object to the database
        messageDao.create(message);
        int id = message.getId();
        verifyDb(id, message);

        // assign a password
        message.setmCategory("_secret");
        // update the database after changing the object
        messageDao.update(message);
        verifyDb(id, message);

        // query for all items in the database
        List<CcsMessage> messages = messageDao.queryForAll();
        assertEquals("Should have found 1 message matching our query", 1, messages.size());
        verifyCcsMessage(message, messages.get(0));

        // loop through items in the database
        int messageC = 0;
        for (CcsMessage message2 : messageDao) {
            verifyCcsMessage(message, message2);
            messageC++;
        }
        assertEquals("Should have found 1 message in for loop", 1, messageC);

        // construct a query using the QueryBuilder
        QueryBuilder<CcsMessage, Integer> statementBuilder = messageDao.queryBuilder();
        // shouldn't find anything: name LIKE 'hello" does not match our message
        statementBuilder.where().like("from", "hello");
        messages = messageDao.query(statementBuilder.prepare());
        assertEquals("Should not have found any messages matching our query", 0, messages.size());

        // should find our message: name LIKE 'Jim%' should match our message
        statementBuilder.where().like("from", name.substring(0, 3) + "%");
        messages = messageDao.query(statementBuilder.prepare());
        assertEquals("Should have found 1 message matching our query", 1, messages.size());
        verifyCcsMessage(message, messages.get(0));

        // delete the message since we are done with it
        messageDao.delete(message);
        // we shouldn't find it now
        assertNull("message was deleted, shouldn't find any", messageDao.queryForId(id));
    }

    /**
     * Example of reading and writing a large(r) number of objects.
     */
    private void readWriteBunch() throws Exception {

        Map<String, CcsMessage> messages = new HashMap<String, CcsMessage>();
        for (int i = 1; i <= 10; i++) {
            String name = Integer.toString(i);
            CcsMessage message = new CcsMessage(name, "category", "messageId", new HashMap<String, String>());
            // persist the message object to the database, it should return 1
            messageDao.create(message);
            messages.put(name, message);
        }

        // query for all items in the database
        List<CcsMessage> all = messageDao.queryForAll();
        System.out.println("messages.size() = " + messages.size());
        System.out.println("all.size() = " + all.size());
        assertEquals("Should have found same number of messages in map", messages.size(), all.size());
        for (CcsMessage message : all) {
            assertTrue("Should have found message in map", messages.containsValue(message));
            verifyCcsMessage(messages.get(message.getmFrom()), message);
        }

        // loop through items in the database
        int messageC = 0;
        for (CcsMessage message : messageDao) {
            assertTrue("Should have found message in map", messages.containsValue(message));
            verifyCcsMessage(messages.get(message.getmFrom()), message);
            messageC++;
        }
        assertEquals("Should have found the right number of messages in for loop", messages.size(), messageC);
    }

    /**
     * Example of created a query with a ? argument using the {@link SelectArg} object. You then can set the value of
     * this object at a later time.
     */
    private void useSelectArgFeature() throws Exception {

        String name1 = "foo";
        String name2 = "bar";
        String name3 = "baz";
        assertEquals(1, messageDao.create(new CcsMessage(name1, "category", "messageId", new HashMap<String, String>())));
        assertEquals(1, messageDao.create(new CcsMessage(name2, "category", "messageId", new HashMap<String, String>())));
        assertEquals(1, messageDao.create(new CcsMessage(name3, "category", "messageId", new HashMap<String, String>())));

        QueryBuilder<CcsMessage, Integer> statementBuilder = messageDao.queryBuilder();
        SelectArg selectArg = new SelectArg();
        // build a query with the WHERE clause set to 'name = ?'
        statementBuilder.where().like("from", selectArg);
        PreparedQuery<CcsMessage> preparedQuery = statementBuilder.prepare();

        // now we can set the select arg (?) and run the query
        selectArg.setValue(name1);
        List<CcsMessage> results = messageDao.query(preparedQuery);
        assertEquals("Should have found 1 message matching our query", 1, results.size());
        assertEquals(name1, results.get(0).getmFrom());

        selectArg.setValue(name2);
        results = messageDao.query(preparedQuery);
        assertEquals("Should have found 1 message matching our query", 1, results.size());
        assertEquals(name2, results.get(0).getmFrom());

        selectArg.setValue(name3);
        results = messageDao.query(preparedQuery);
        assertEquals("Should have found 1 message matching our query", 1, results.size());
        assertEquals(name3, results.get(0).getmFrom());
    }

    /**
     * Example of created a query with a ? argument using the {@link SelectArg} object. You then can set the value of
     * this object at a later time.
     */
    private void useTransactions(ConnectionSource connectionSource) throws Exception {
        String name = "trans1";
        final CcsMessage message = new CcsMessage(name, "category", "messageId", new HashMap<String, String>());
        assertEquals(1, messageDao.create(message));

        TransactionManager transactionManager = new TransactionManager(connectionSource);
        try {
            // try something in a transaction
            transactionManager.callInTransaction(new Callable<Void>() {
                public Void call() throws Exception {
                    // we do the delete
                    assertEquals(1, messageDao.delete(message));
                    assertNull(messageDao.queryForId(message.getId()));
                    // but then (as an example) we throw an exception which rolls back the delete
                    throw new Exception("We throw to roll back!!");
                }
            });
            fail("This should have thrown");
        } catch (SQLException e) {
            // expected
        }

        assertNotNull(messageDao.queryForId(message.getId()));
    }

    /**
     * Verify that the message stored in the database was the same as the expected object.
     */
    private void verifyDb(int id, CcsMessage expected) throws SQLException, Exception {
        // make sure we can read it back
        CcsMessage message2 = messageDao.queryForId(id);
        if (message2 == null) {
            throw new Exception("Should have found id '" + id + "' in the database");
        }
        verifyCcsMessage(expected, message2);
    }

    /**
     * Verify that the message is the same as expected.
     */
    private static void verifyCcsMessage(CcsMessage expected, CcsMessage message2) {
        assertEquals("expected name does not equal message name", expected.getmFrom(), message2.getmFrom());
        assertEquals("expected password does not equal message name", expected.getmCategory(), message2.getmCategory());
    }
}