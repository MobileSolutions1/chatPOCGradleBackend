package xmpp

import org.jivesoftware.smack.packet.DefaultPacketExtension
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.util.StringUtils
import java.lang

/**
 * Created by marcelo on 23/09/15.
 *
 * XMPP Packet Extension for GCM Cloud Connection Server.
 */
public val GCM_ELEMENT_NAME = "gcm"
public val GCM_NAMESPACE = "google:mobile:data"

public class GcmPacketExtension(val json: String): DefaultPacketExtension(GCM_ELEMENT_NAME, GCM_NAMESPACE) {

    override fun toXML(): String {
        return lang.String.format("<%s xmlns=\"%s\">%s</%s>", GCM_ELEMENT_NAME,
                GCM_NAMESPACE, json, GCM_ELEMENT_NAME)
    }

    fun toPacket(): Packet {
        return object : Message() {
            // Must override toXML() because it includes a <body>
            override fun toXML(): String {
                val buf = StringBuilder()
                buf.append("<message")
                if (getXmlns() != null) {
                    buf.append(" xmlns=\"").append(getXmlns()).append("\"")
                }
                if (getLanguage() != null) {
                    buf.append(" xml:lang=\"").append(getLanguage()).append("\"")
                }
                if (getPacketID() != null) {
                    buf.append(" id=\"").append(getPacketID()).append("\"")
                }
                if (getTo() != null) {
                    buf.append(" to=\"").append(StringUtils.escapeForXML(getTo())).append("\"")
                }
                if (getFrom() != null) {
                    buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom())).append("\"")
                }
                buf.append(">")
                buf.append(this@GcmPacketExtension.toXML())
                buf.append("</message>")
                return buf.toString()
            }
        }
    }
}
