package wxdgaming.boot.starter.net.server.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.NetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class SslUtils {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslUtils.class);

    // See https://tools.ietf.org/html/rfc8446#appendix-B.4
    static final Set<String> TLSV13_CIPHERS = Collections.unmodifiableSet(new LinkedHashSet<String>(
            asList("TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256",
                    "TLS_AES_128_GCM_SHA256", "TLS_AES_128_CCM_8_SHA256",
                    "TLS_AES_128_CCM_SHA256")));

    static final short DTLS_1_0 = (short) 0xFEFF;
    static final short DTLS_1_2 = (short) 0xFEFD;
    static final short DTLS_1_3 = (short) 0xFEFC;
    static final short DTLS_RECORD_HEADER_LENGTH = 13;

    /**
     * GMSSL Protocol Version
     */
    static final int GMSSL_PROTOCOL_VERSION = 0x101;

    static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";

    /**
     * change cipher spec
     */
    static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;

    /**
     * alert
     */
    static final int SSL_CONTENT_TYPE_ALERT = 21;

    /**
     * handshake
     */
    static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;

    /**
     * application data
     */
    static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;

    /**
     * HeartBeat Extension
     */
    static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24;

    /**
     * the length of the ssl record header (in bytes)
     */
    static final int SSL_RECORD_HEADER_LENGTH = 5;

    /**
     * Not enough data in buffer to parse the record length
     */
    static final int NOT_ENOUGH_DATA = -1;

    /**
     * data is not encrypted
     */
    static final int NOT_ENCRYPTED = -2;

    static int getEncryptedPacketLength(ByteBuf buffer, int offset) {
        int packetLength = 0;

        // SSLv3 or TLS - Check ContentType
        boolean tls;
        switch (buffer.getUnsignedByte(offset)) {
            case SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
            case SSL_CONTENT_TYPE_ALERT:
            case SSL_CONTENT_TYPE_HANDSHAKE:
            case SSL_CONTENT_TYPE_APPLICATION_DATA:
            case SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT:
                tls = true;
                break;
            default:
                // SSLv2 or bad data
                tls = false;
        }

        if (tls) {
            // SSLv3 or TLS or GMSSLv1.0 or GMSSLv1.1 - Check ProtocolVersion
            int majorVersion = buffer.getUnsignedByte(offset + 1);
            int version = buffer.getShort(offset + 1);
            if (majorVersion == 3 || version == GMSSL_PROTOCOL_VERSION) {
                // SSLv3 or TLS or GMSSLv1.0 or GMSSLv1.1
                packetLength = unsignedShortBE(buffer, offset + 3) + SSL_RECORD_HEADER_LENGTH;
                if (packetLength <= SSL_RECORD_HEADER_LENGTH) {
                    // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                    tls = false;
                }
            } else if (version == DTLS_1_0 || version == DTLS_1_2 || version == DTLS_1_3) {
                if (buffer.readableBytes() < offset + DTLS_RECORD_HEADER_LENGTH) {
                    return NOT_ENOUGH_DATA;
                }
                // length is the last 2 bytes in the 13 byte header.
                packetLength = unsignedShortBE(buffer, offset + DTLS_RECORD_HEADER_LENGTH - 2) +
                        DTLS_RECORD_HEADER_LENGTH;
            } else {
                // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                tls = false;
            }
        }

        if (!tls) {
            // SSLv2 or bad data - Check the version
            int headerLength = (buffer.getUnsignedByte(offset) & 0x80) != 0 ? 2 : 3;
            int majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
            if (majorVersion == 2 || majorVersion == 3) {
                // SSLv2
                packetLength = headerLength == 2 ?
                        (shortBE(buffer, offset) & 0x7FFF) + 2 : (shortBE(buffer, offset) & 0x3FFF) + 3;
                if (packetLength <= headerLength) {
                    return NOT_ENOUGH_DATA;
                }
            } else {
                return NOT_ENCRYPTED;
            }
        }
        return packetLength;
    }

    // Reads a big-endian unsigned short integer from the buffer
    @SuppressWarnings("deprecation")
    private static int unsignedShortBE(ByteBuf buffer, int offset) {
        int value = buffer.getUnsignedShort(offset);
        if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
            value = Integer.reverseBytes(value) >>> Short.SIZE;
        }
        return value;
    }

    // Reads a big-endian short integer from the buffer
    @SuppressWarnings("deprecation")
    private static short shortBE(ByteBuf buffer, int offset) {
        short value = buffer.getShort(offset);
        if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
            value = Short.reverseBytes(value);
        }
        return value;
    }

    private static short unsignedByte(byte b) {
        return (short) (b & 0xFF);
    }

    // Reads a big-endian unsigned short integer from the buffer
    private static int unsignedShortBE(ByteBuffer buffer, int offset) {
        return shortBE(buffer, offset) & 0xFFFF;
    }

    // Reads a big-endian short integer from the buffer
    private static short shortBE(ByteBuffer buffer, int offset) {
        return buffer.order() == ByteOrder.BIG_ENDIAN ?
                buffer.getShort(offset) : ByteBufUtil.swapShort(buffer.getShort(offset));
    }

    static int getEncryptedPacketLength(ByteBuffer[] buffers, int offset) {
        ByteBuffer buffer = buffers[offset];

        // Check if everything we need is in one ByteBuffer. If so we can make use of the fast-path.
        if (buffer.remaining() >= SSL_RECORD_HEADER_LENGTH) {
            return getEncryptedPacketLength(buffer);
        }

        // We need to copy 5 bytes into a temporary buffer so we can parse out the packet length easily.
        ByteBuffer tmp = ByteBuffer.allocate(5);

        do {
            buffer = buffers[offset++].duplicate();
            if (buffer.remaining() > tmp.remaining()) {
                buffer.limit(buffer.position() + tmp.remaining());
            }
            tmp.put(buffer);
        } while (tmp.hasRemaining());

        // Done, flip the buffer so we can read from it.
        tmp.flip();
        return getEncryptedPacketLength(tmp);
    }

    private static int getEncryptedPacketLength(ByteBuffer buffer) {
        int packetLength = 0;
        int pos = buffer.position();
        // SSLv3 or TLS - Check ContentType
        boolean tls;
        switch (unsignedByte(buffer.get(pos))) {
            case SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
            case SSL_CONTENT_TYPE_ALERT:
            case SSL_CONTENT_TYPE_HANDSHAKE:
            case SSL_CONTENT_TYPE_APPLICATION_DATA:
            case SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT:
                tls = true;
                break;
            default:
                // SSLv2 or bad data
                tls = false;
        }

        if (tls) {
            // SSLv3 or TLS or GMSSLv1.0 or GMSSLv1.1 - Check ProtocolVersion
            int majorVersion = unsignedByte(buffer.get(pos + 1));
            if (majorVersion == 3 || buffer.getShort(pos + 1) == GMSSL_PROTOCOL_VERSION) {
                // SSLv3 or TLS or GMSSLv1.0 or GMSSLv1.1
                packetLength = unsignedShortBE(buffer, pos + 3) + SSL_RECORD_HEADER_LENGTH;
                if (packetLength <= SSL_RECORD_HEADER_LENGTH) {
                    // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                    tls = false;
                }
            } else {
                // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                tls = false;
            }
        }

        if (!tls) {
            // SSLv2 or bad data - Check the version
            int headerLength = (unsignedByte(buffer.get(pos)) & 0x80) != 0 ? 2 : 3;
            int majorVersion = unsignedByte(buffer.get(pos + headerLength + 1));
            if (majorVersion == 2 || majorVersion == 3) {
                // SSLv2
                packetLength = headerLength == 2 ?
                        (shortBE(buffer, pos) & 0x7FFF) + 2 : (shortBE(buffer, pos) & 0x3FFF) + 3;
                if (packetLength <= headerLength) {
                    return NOT_ENOUGH_DATA;
                }
            } else {
                return NOT_ENCRYPTED;
            }
        }
        return packetLength;
    }

    static void handleHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean notify) {
        // We have may haven written some parts of data before an exception was thrown so ensure we always flush.
        // See https://github.com/netty/netty/issues/3900#issuecomment-172481830
        ctx.flush();
        if (notify) {
            ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause));
        }
        ctx.close();
    }

    /**
     * Fills the {@link ByteBuf} with zero bytes.
     */
    static void zeroout(ByteBuf buffer) {
        if (!buffer.isReadOnly()) {
            buffer.setZero(0, buffer.capacity());
        }
    }

    /**
     * Fills the {@link ByteBuf} with zero bytes and releases it.
     */
    static void zerooutAndRelease(ByteBuf buffer) {
        zeroout(buffer);
        buffer.release();
    }

    /**
     * Same as {@link Base64#encode(ByteBuf, boolean)} but allows the use of a custom {@link ByteBufAllocator}.
     *
     * @see Base64#encode(ByteBuf, boolean)
     */
    static ByteBuf toBase64(ByteBufAllocator allocator, ByteBuf src) {
        ByteBuf dst = Base64.encode(src, src.readerIndex(),
                src.readableBytes(), true, Base64Dialect.STANDARD, allocator);
        src.readerIndex(src.writerIndex());
        return dst;
    }

    /**
     * Validate that the given hostname can be used in SNI extension.
     */
    static boolean isValidHostNameForSNI(String hostname) {
        // See  https://datatracker.ietf.org/doc/html/rfc6066#section-3
        return hostname != null &&
                // SNI HostName has to be a FQDN according to TLS SNI Extension spec (see [1]),
                // which means that is has to have at least a host name and a domain part.
                hostname.indexOf('.') > 0 &&
                !hostname.endsWith(".") && !hostname.startsWith("/") &&
                !NetUtil.isValidIpV4Address(hostname) &&
                !NetUtil.isValidIpV6Address(hostname);
    }

    /**
     * Returns {@code true} if the given cipher (in openssl format) is for TLSv1.3, {@code false} otherwise.
     */
    static boolean isTLSv13Cipher(String cipher) {
        // See https://tools.ietf.org/html/rfc8446#appendix-B.4
        return TLSV13_CIPHERS.contains(cipher);
    }
}
