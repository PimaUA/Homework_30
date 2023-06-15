package client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    @Test
    public void nettyTest() {
        EmbeddedChannel channel = new EmbeddedChannel(new StringDecoder(StandardCharsets.UTF_8));
        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{(byte) 0xE2, (byte) 0x98, (byte) 0xA2}));
        String myObject = channel.readInbound();
        // Perform checks on your object
        assertEquals("☢", myObject);
    }

    @Test
    public void testIncompleteLinesStrippedDelimiters() {
        EmbeddedChannel ch = new EmbeddedChannel(new DelimiterBasedFrameDecoder(8192, true,
                Delimiters.lineDelimiter()));
        ch.writeInbound(Unpooled.copiedBuffer("Test", Charset.defaultCharset()));
        assertNull(ch.readInbound());
        ch.writeInbound(Unpooled.copiedBuffer("Line\r\ng\r\n", Charset.defaultCharset()));
        assertEquals("TestLine", ((ByteBuf) ch.readInbound()).toString(Charset.defaultCharset()));
        assertEquals("g", ((ByteBuf) ch.readInbound()).toString(Charset.defaultCharset()));
        assertNull(ch.readInbound());
        ch.finish();
    }

    @Test
    public void testDecode() throws Exception {
        EmbeddedChannel ch = new EmbeddedChannel(
                new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter()));

        ch.writeInbound(Unpooled.copiedBuffer("first\r\nsecond\nthird", CharsetUtil.US_ASCII));
        assertEquals("first", ((ByteBuf) ch.readInbound()).toString(CharsetUtil.US_ASCII));
        assertEquals("second", ((ByteBuf) ch.readInbound()).toString(CharsetUtil.US_ASCII));
        assertNull(ch.readInbound());
        ch.finish();

        ReferenceCountUtil.release(ch.readInbound());
    }
}