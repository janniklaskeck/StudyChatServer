package stud.mi.server.user;

import org.junit.Assert;
import org.junit.Test;

import stud.mi.message.Message;
import stud.mi.message.MessageType;
import stud.mi.util.MessageBuilder;

public class UserRegistryTest
{

    @Test
    public void testRegisterUser()
    {
        final Message message = MessageBuilder.buildMessageBase(MessageType.USER_JOIN);
        message.getContent().addProperty("userName", "testName");
        Assert.assertNotNull(UserRegistry.getInstance().registerUser(null, message));
        Assert.assertNull(UserRegistry.getInstance().registerUser(null, message));
    }

    @Test
    public void testGenerateUserID()
    {
        Assert.assertTrue(UserRegistry.generateUserID() > 0L);
    }

}
