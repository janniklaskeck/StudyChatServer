package stud.mi.server.user;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stud.mi.server.channel.Channel;

public class RemoteUserTest
{

    private RemoteUser user;
    private Channel channel;

    @Before
    public void init()
    {
        this.user = new RemoteUser(null, "testUser", 12345L);
        this.channel = new Channel("testChannel");
    }

    @Test
    public void testIsDead()
    {
        this.user.lastHeartBeat = LocalDateTime.now();
        Assert.assertFalse(this.user.isDead());
        this.user.lastHeartBeat = this.user.lastHeartBeat.minusSeconds(RemoteUser.MAX_ALIVE_SECONDS + 1);
        Assert.assertTrue(this.user.isDead());
    }

    @Test
    public void testJoinChannel()
    {
        final boolean couldJoin = this.user.joinChannel(this.channel);
        final boolean couldJoinAgain = this.user.joinChannel(this.channel);
        Assert.assertTrue(couldJoin);
        Assert.assertTrue(couldJoinAgain);
    }

    @Test
    public void testExitChannel()
    {
        Assert.assertFalse(this.user.exitChannel());
        this.user.joinChannel(this.channel);
        Assert.assertTrue(this.user.exitChannel());
    }
}
