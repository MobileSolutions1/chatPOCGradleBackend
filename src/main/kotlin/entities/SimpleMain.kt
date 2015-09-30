package entities

/**
 * Created by marcelosenaga on 9/30/15.
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

import java.sql.SQLException
import java.util.HashMap
import java.util.concurrent.Callable

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.stmt.PreparedQuery
import com.j256.ormlite.stmt.QueryBuilder
import com.j256.ormlite.stmt.SelectArg
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import entities.CcsMessage

/**
 * Main sample routine to show how to do basic operations with the package.

 *
 *
 * **NOTE:** We use asserts in a couple of places to verify the results but if this were actual production code, we
 * would have proper error handling.
 *
 */
public class SimpleMain {

    private var messageDao: Dao<CcsMessage, Int>? = null

    @Throws(Exception::class)
    private fun doMain() {
        var connectionSource: ConnectionSource? = null
        try {
            // create our data-source for the database
            connectionSource = JdbcConnectionSource(DATABASE_URL)
            // setup our database and DAOs
            setupDatabase(connectionSource)
            // read and write some data
            readWriteData()
            // do a bunch of bulk operations
            readWriteBunch()
            // show how to use the SelectArg object
            useSelectArgFeature()
            // show how to use the SelectArg object
            useTransactions(connectionSource)
            println("\n\nIt seems to have worked\n\n")
        } finally {
            // destroy the data source which should close underlying connections
            if (connectionSource != null) {
                connectionSource.close()
            }
        }
    }

    /**
     * Setup our database and DAOs
     */
    @Throws(Exception::class)
    private fun setupDatabase(connectionSource: ConnectionSource) {

        messageDao = DaoManager.createDao<Dao<CcsMessage, Int>, CcsMessage>(connectionSource, CcsMessage::class.java)

        // if you need to create the table
        TableUtils.createTable(connectionSource, CcsMessage::class.java)
    }

    /**
     * Read and write some example data.
     */
    @Throws(Exception::class)
    private fun readWriteData() {
        // create an instance of CcsMessage
        val name = "Jim Coakley"
        val message = CcsMessage(name, "category", "messageId", HashMap<String, String>())

        // persist the message object to the database
        messageDao!!.create(message)
        val id = message.id
        verifyDb(id, message)

        // assign a password
        message.mCategory = "_secret"
        // update the database after changing the object
        messageDao!!.update(message)
        verifyDb(id, message)

        // query for all items in the database
        var messages = messageDao!!.queryForAll()
        assertEquals("Should have found 1 message matching our query", 1, messages.size().toLong())
        verifyCcsMessage(message, messages.get(0))

        // loop through items in the database
        var messageC = 0
        for (message2 in messageDao!!) {
            verifyCcsMessage(message, message2)
            messageC++
        }
        assertEquals("Should have found 1 message in for loop", 1, messageC.toLong())

        // construct a query using the QueryBuilder
        val statementBuilder = messageDao!!.queryBuilder()
        // shouldn't find anything: name LIKE 'hello" does not match our message
        statementBuilder.where().like("from", "hello")
        messages = messageDao!!.query(statementBuilder.prepare())
        assertEquals("Should not have found any messages matching our query", 0, messages.size().toLong())

        // should find our message: name LIKE 'Jim%' should match our message
        statementBuilder.where().like("from", name.substring(0, 3) + "%")
        messages = messageDao!!.query(statementBuilder.prepare())
        assertEquals("Should have found 1 message matching our query", 1, messages.size().toLong())
        verifyCcsMessage(message, messages.get(0))

        // delete the message since we are done with it
        messageDao!!.delete(message)
        // we shouldn't find it now
        assertNull("message was deleted, shouldn't find any", messageDao!!.queryForId(id))
    }

    /**
     * Example of reading and writing a large(r) number of objects.
     */
    @Throws(Exception::class)
    private fun readWriteBunch() {

        val messages = HashMap<String, CcsMessage>()
        for (i in 1..10) {
            val name = Integer.toString(i)
            val message = CcsMessage(name, "category", "messageId", HashMap<String, String>())
            // persist the message object to the database, it should return 1
            messageDao!!.create(message)
            messages.put(name, message)
        }

        // query for all items in the database
        val all = messageDao!!.queryForAll()
        println("messages.size() = " + messages.size())
        println("all.size() = " + all.size())
        assertEquals("Should have found same number of messages in map", messages.size().toLong(), all.size().toLong())
        for (message in all) {
            assertTrue("Should have found message in map", messages.containsValue(message))
            verifyCcsMessage(messages.get(message.mFrom), message)
        }

        // loop through items in the database
        var messageC = 0
        for (message in messageDao!!) {
            assertTrue("Should have found message in map", messages.containsValue(message))
            verifyCcsMessage(messages.get(message.mFrom), message)
            messageC++
        }
        assertEquals("Should have found the right number of messages in for loop", messages.size().toLong(), messageC.toLong())
    }

    /**
     * Example of created a query with a ? argument using the [SelectArg] object. You then can set the value of
     * this object at a later time.
     */
    @Throws(Exception::class)
    private fun useSelectArgFeature() {

        val name1 = "foo"
        val name2 = "bar"
        val name3 = "baz"
        assertEquals(1, messageDao!!.create(CcsMessage(name1, "category", "messageId", HashMap<String, String>())).toLong())
        assertEquals(1, messageDao!!.create(CcsMessage(name2, "category", "messageId", HashMap<String, String>())).toLong())
        assertEquals(1, messageDao!!.create(CcsMessage(name3, "category", "messageId", HashMap<String, String>())).toLong())

        val statementBuilder = messageDao!!.queryBuilder()
        val selectArg = SelectArg()
        // build a query with the WHERE clause set to 'name = ?'
        statementBuilder.where().like("from", selectArg)
        val preparedQuery = statementBuilder.prepare()

        // now we can set the select arg (?) and run the query
        selectArg.setValue(name1)
        var results = messageDao!!.query(preparedQuery)
        assertEquals("Should have found 1 message matching our query", 1, results.size().toLong())
        assertEquals(name1, results.get(0).mFrom)

        selectArg.setValue(name2)
        results = messageDao!!.query(preparedQuery)
        assertEquals("Should have found 1 message matching our query", 1, results.size().toLong())
        assertEquals(name2, results.get(0).mFrom)

        selectArg.setValue(name3)
        results = messageDao!!.query(preparedQuery)
        assertEquals("Should have found 1 message matching our query", 1, results.size().toLong())
        assertEquals(name3, results.get(0).mFrom)
    }

    /**
     * Example of created a query with a ? argument using the [SelectArg] object. You then can set the value of
     * this object at a later time.
     */
    @Throws(Exception::class)
    private fun useTransactions(connectionSource: ConnectionSource) {
        val name = "trans1"
        val message = CcsMessage(name, "category", "messageId", HashMap<String, String>())
        assertEquals(1, messageDao!!.create(message).toLong())

        val transactionManager = TransactionManager(connectionSource)
        try {
            // try something in a transaction
            transactionManager.callInTransaction(object : Callable<Void> {
                @Throws(Exception::class)
                override fun call(): Void {
                    // we do the delete
                    assertEquals(1, messageDao!!.delete(message).toLong())
                    assertNull(messageDao!!.queryForId(message.id))
                    // but then (as an example) we throw an exception which rolls back the delete
                    throw Exception("We throw to roll back!!")
                }
            })
            fail("This should have thrown")
        } catch (e: SQLException) {
            // expected
        }

        assertNotNull(messageDao!!.queryForId(message.id))
    }

    /**
     * Verify that the message stored in the database was the same as the expected object.
     */
    @Throws(SQLException::class, Exception::class)
    private fun verifyDb(id: Int, expected: CcsMessage) {
        // make sure we can read it back
        val message2 = messageDao!!.queryForId(id) ?: throw Exception("Should have found id '$id' in the database")
        verifyCcsMessage(expected, message2)
    }

    companion object {

        // we are using the in-memory H2 database
        private val DATABASE_URL = "jdbc:mysql://localhost/test?user=root&password="

        @Throws(Exception::class)
        @JvmStatic public fun main(args: Array<String>) {
            // turn our static method into an instance of Main
            SimpleMain().doMain()
        }

        /**
         * Verify that the message is the same as expected.
         */
        private fun verifyCcsMessage(expected: CcsMessage?, message2: CcsMessage?) {
            assertEquals("expected name does not equal message name", expected?.mFrom, message2?.mFrom)
            assertEquals("expected password does not equal message name", expected?.mCategory, message2?.mCategory)
        }
    }
}