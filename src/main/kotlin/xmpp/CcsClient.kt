package xmpp

import org.jivesoftware.smack.*
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode
import org.jivesoftware.smack.filter.PacketTypeFilter
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.packet.PacketExtension
import org.jivesoftware.smack.provider.PacketExtensionProvider
import org.jivesoftware.smack.provider.ProviderManager
import org.json.simple.JSONValue
import org.json.simple.parser.ParseException
import org.xmlpull.v1.XmlPullParser
import java.lang
import java.text.SimpleDateFormat
import java.util.*
import java.util.List
import java.util.Map
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.ssl.SSLSocketFactory

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server.
 * Most of it has been taken more or less verbatim from Googles
 * documentation: http://developer.android.com/google/gcm/ccs.html
 * <br>
 * But some additions have been made. Bigger changes are annotated like that:
 * "/// new".
 * <br>
 * Those changes have to do with parsing certain type of messages
 * as well as with sending messages to a list of recipients. The original code
 * only covers sending one message to exactly one recipient.
 */
public class CcsClient {

    constructor(projectId: String, apiKey: String, debuggable: Boolean) {

        this.projectId = projectId
        this.apiKey = apiKey
        this.debuggable = debuggable

        // Add GcmPacketExtension
        ProviderManager.getInstance().addExtensionProvider(GCM_ELEMENT_NAME,
                GCM_NAMESPACE, object: PacketExtensionProvider {
            override fun parseExtension(parser: XmlPullParser): PacketExtension {
                val json = parser.nextText()
                return GcmPacketExtension(json)
            }
        })
    }

    val projectId: String
    val apiKey: String
    val debuggable: Boolean

    val logger = Logger.getLogger(this.javaClass.name)

    val GCM_SERVER = "gcm.googleapis.com"
    val GCM_PORT = 5235
    val GCM_ELEMENT_NAME = "gcm"
    val GCM_NAMESPACE = "google:mobile:data"

    val random = Random()
    var connection: XMPPConnection? = null
    var config: ConnectionConfiguration? = null

    companion object {

        private var singleton: CcsClient? = null

        fun create(projectId: String, apiKey: String, debuggable: Boolean): CcsClient {
            if(singleton == null) {
                return CcsClient(projectId, apiKey, debuggable)
            } else {
                return singleton!!
            }
        }

        fun create(): CcsClient {
            return singleton!!
        }

        private val CHAVE_API = "AIzaSyC3mluaIT8sbdXpJCgf-s_SUkRKTEmpCgg"
        private val REGISTRO_ID_DEVICE = "APA91bEO6VXRVVckexwLZLOMdb_XSX19UDJ45DCkMpTm2Ilh_CnwAOXOOIjlqj0VTm9FtRSXW6d1swBB0D9XTfxKNCL0jyNyLIdLkZKmOG3QeByblJ9bDLsEQ07mXCugRGTi9web1z8J"

        @JvmStatic fun main(args: Array<String>) {
            val projectId = "779755635636";
            val password = CHAVE_API;
            val toRegId = REGISTRO_ID_DEVICE;

            val ccsClient = CcsClient.Companion.create(projectId, password, true)

            try {
                ccsClient.connect()
            } catch (e: XMPPException) {
                e.printStackTrace()
            }

            // Send a sample hello downstream message to a device.
            val messageId = ccsClient.getRandomMessageId()
            val payload = HashMap<String, String>()
            val newString = SimpleDateFormat("H:mm:ss").format(Date())
            payload.put("SERVER_MESSAGE", "TESTE=" + newString)
            val collapseKey = "sample"
            val timeToLive = 10000L
            val delayWhileIdle = true
            ccsClient.send(ccsClient.createJsonMessage(toRegId, messageId, payload, collapseKey,
                    timeToLive, delayWhileIdle))
        }
    }
    /**
     * Returns a random message id to uniquely identify a message.
     *
     * <p>
     * Note: This is generated by a pseudo random number generator for
     * illustration purpose, and is not guaranteed to be unique.
     *
     */
    fun getRandomMessageId(): String {
        return "m-" + lang.Long.toString(random.nextLong())
    }

    /**
     * Sends a downstream GCM message.
     */
    fun send(jsonRequest: String) {
        val request = GcmPacketExtension(jsonRequest).toPacket()
        connection?.sendPacket(request)
    }

    /**
     * Sends a message to multiple recipients. Kind of like the old
     * HTTP message with the list of regIds in the "registration_ids" field.
     */
    fun sendBroadcast(payload: HashMap<String, String>, collapseKey: String,
            timeToLive: Long, delayWhileIdle: Boolean, recipients: List<String>) {
        val map = createAttributeMap(null, null, payload, collapseKey, timeToLive, delayWhileIdle)
        recipients.forEach { toRegId ->
            val messageId = getRandomMessageId()
            map.put("message_id", messageId as Object)
            map.put("to", toRegId as Object)
            val jsonRequest = createJsonMessage(map)
            send(jsonRequest)
        }
    }

    /**
     * Handles an upstream data message from a device application.
     */
    fun handleIncomingDataMessage(msg: CcsMessage) {
        if (msg.mPayload.get("action") != null) {
            val processor = ProcessorFactory.getProcessor(msg.mPayload.get("action"))
            processor.handleMessage(msg)
        }
    }

    /**
     *
     */
    fun getMessage(jsonObject: Map<String, Object>): CcsMessage {
        val from = jsonObject.get("from").toString()

        // PackageName of the application that sent this message.
        val category = jsonObject.get("category").toString()

        // unique id of this message
        val messageId = jsonObject.get("message_id").toString()

        val payload = jsonObject.get("data") as HashMap<String, String>

        val msg = CcsMessage(from, category, messageId, payload)

        return msg
    }

    /**
     * Handles an ACK.
     *
     * <p>
     * By default, it only logs a INFO message, but subclasses could override it
     * to properly handle ACKS.
     */
    fun handleAckReceipt(jsonObject: Map<String, Object>) {
        val messageId = jsonObject.get("message_id").toString()
        val from = jsonObject.get("from").toString()
        logger.log(Level.INFO, "handleAckReceipt() from: " + from + ", messageId: " + messageId)
    }

    /**
     * Handles a NACK.
     *
     * <p>
     * By default, it only logs a INFO message, but subclasses could override it
     * to properly handle NACKS.
     */
    fun handleNackReceipt(jsonObject: Map<String, Object>) {
        val messageId = jsonObject.get("message_id").toString()
        val from = jsonObject.get("from").toString()
        logger.log(Level.INFO, "handleNackReceipt() from: " + from + ", messageId: " + messageId)
    }

    /**
     * Creates a JSON encoded GCM message.
     *
     * @param to RegistrationId of the target device (Required).
     * @param messageId Unique messageId for which CCS will send an "ack/nack"
     * (Required).
     * @param payload Message content intended for the application. (Optional).
     * @param collapseKey GCM collapse_key parameter (Optional).
     * @param timeToLive GCM time_to_live parameter (Optional).
     * @param delayWhileIdle GCM delay_while_idle parameter (Optional).
     * @return JSON encoded GCM message.
     */
    fun createJsonMessage(to: String?, messageId: String?, payload: HashMap<String, String>,
            collapseKey: String?, timeToLive: Long?, delayWhileIdle: Boolean): String {
        return createJsonMessage(createAttributeMap(to, messageId, payload,
                collapseKey, timeToLive, delayWhileIdle))
    }

    fun createJsonMessage(map: HashMap<String, Object>): String {
        return JSONValue.toJSONString(map)
    }

    fun createAttributeMap(to: String?, messageId: String?, payload: HashMap<String, String>,
            collapseKey: String?, timeToLive: Long?, delayWhileIdle: Boolean): HashMap<String, Object> {
        val message = HashMap<String, Object>()
        if (to != null) {
            message.put("to", to!! as java.lang.Object)
        }
        if (collapseKey != null) {
            message.put("collapse_key", collapseKey as java.lang.Object)
        }
        if (timeToLive != null) {
            message.put("time_to_live", timeToLive as java.lang.Object)
        }
        if (delayWhileIdle != null && delayWhileIdle) {
            message.put("delay_while_idle", true as java.lang.Object)
        }
        if (messageId != null) {
            message.put("message_id", messageId!! as java.lang.Object)
        }
        message.put("data", payload as java.lang.Object)
        return message
    }

    /**
     * Creates a JSON encoded ACK message for an upstream message received from
     * an application.
     *
     * @param to RegistrationId of the device who sent the upstream message.
     * @param messageId messageId of the upstream message to be acknowledged to
     * CCS.
     * @return JSON encoded ack.
     */
    fun createJsonAck(to: String, messageId: String): String {
        val message = HashMap<String, String>()
        message.put("message_type", "ack")
        message.put("to", to)
        message.put("message_id", messageId)
        return JSONValue.toJSONString(message)
    }

    /**
     * Creates a JSON encoded NACK message for an upstream message received from
     * an application.
     *
     * @param to RegistrationId of the device who sent the upstream message.
     * @param messageId messageId of the upstream message to be acknowledged to
     * CCS.
     * @return JSON encoded nack.
     */
    fun createJsonNack(to: String, messageId: String): String {
        val message = HashMap<String, String>()
        message.put("message_type", "nack")
        message.put("to", to)
        message.put("message_id", messageId)
        return JSONValue.toJSONString(message)
    }

    /**
     * Connects to GCM Cloud Connection Server using the supplied credentials.
     * @throws XMPPException
     */
    fun connect() {
        config = ConnectionConfiguration(GCM_SERVER, GCM_PORT)
        config?.setSecurityMode(SecurityMode.enabled)
        config?.setReconnectionAllowed(true)
        config?.setRosterLoadedAtLogin(false)
        config?.setSendPresence(false)
        config?.setSocketFactory(SSLSocketFactory.getDefault())

        // NOTE: Set to true to launch a window with information about packets sent and received
        config?.setDebuggerEnabled(debuggable)

        connection = XMPPConnection(config)
        connection?.connect()

        connection?.addConnectionListener(object: ConnectionListener {

            override fun reconnectionSuccessful() {
                logger.info("Reconnecting..")
            }

            override fun reconnectionFailed(e: Exception) {
                logger.log(Level.INFO, "Reconnection failed.. ", e)
            }

            override fun reconnectingIn(seconds: Int) {
                logger.log(Level.INFO, "Reconnecting in %d secs", seconds)
            }

            override fun connectionClosedOnError(e: Exception) {
                logger.log(Level.INFO, "Connection closed on error.")
            }

            override fun connectionClosed() {
                logger.info("Connection closed.")
            }
        })

        // Handle incoming packets
        connection?.addPacketListener(object: PacketListener {

            override fun processPacket(packet: Packet) {
                logger.log(Level.INFO, "Received: " + packet.toXML())
                val incomingMessage = packet as Message
                val gcmPacket = incomingMessage.getExtension(GCM_NAMESPACE) as GcmPacketExtension
                val json = gcmPacket.json
                try {
                    val jsonMap = JSONValue.parseWithException(json) as Map<String, Object>
                    handleMessage(jsonMap)
                } catch (e: ParseException) {
                    logger.log(Level.SEVERE, "Error parsing JSON " + json, e)
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "Couldn't send echo.", e)
                }
            }
        }, PacketTypeFilter(Message::class.java))

        // Log all outgoing packets
        connection?.addPacketInterceptor(object: PacketInterceptor {
            override fun interceptPacket(packet: Packet) {
                logger.log(Level.INFO, "Sent: {0}", packet.toXML())
            }
        }, PacketTypeFilter(Message::class.java))

        connection?.login(projectId + "@gcm.googleapis.com", apiKey)
        logger.log(Level.INFO, "logged in: " + projectId)
    }

    fun handleMessage(jsonMap: Map<String, Object>) {
        // present for "ack"/"nack", null otherwise
        val messageType = jsonMap.get("message_type")

        if (messageType == null) {
            val msg = getMessage(jsonMap)
            // Normal upstream data message
            try {
                handleIncomingDataMessage(msg)
                // Send ACK to CCS
                val ack = createJsonAck(msg.mFrom, msg.mMessageId)
                send(ack)
            }
            catch (e: Exception) {
                // Send NACK to CCS
                val nack = createJsonNack(msg.mFrom, msg.mMessageId)
                send(nack)
            }
        } else if ("ack".equals(messageType.toString())) {
            // Process Ack
            handleAckReceipt(jsonMap)
        } else if ("nack".equals(messageType.toString())) {
            // Process Nack
            handleNackReceipt(jsonMap)
        } else {
            logger.log(Level.WARNING, "Unrecognized message type (%s)",
                    messageType.toString())
        }
    }
}