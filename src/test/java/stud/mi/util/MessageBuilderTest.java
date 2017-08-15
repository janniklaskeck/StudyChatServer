package stud.mi.util;

import org.junit.Assert;
import org.junit.Test;

import stud.mi.message.Message;
import stud.mi.server.ChatServer;

public class MessageBuilderTest
{
    @Test
    public void testBuildMessageBase()
    {
        final String type = "TEST_TYPE";
        final Message buildMessageBase = MessageBuilder.buildMessageBase(type);
        Assert.assertEquals(buildMessageBase.getType(), type);
        Assert.assertEquals(buildMessageBase.getVersion(), ChatServer.PROTOCOL_VERSION);
    }
}
