package network;

import java.awt.Point;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import network.protocol.ServerErrorEvent;
import network.protocol.ServerFullStateEvent;
import network.protocol.ServerStateEvent;
import network.protocol.ServerWelcomeEvent;

public class RobotGameServerIntegrationTest
{
    @Test
    public void serverAcceptsMultipleClientsAndPropagatesTargetChanges() throws Exception
    {
        int port = findFreePort();
        RobotGameServer server = new RobotGameServer(port);
        TestClientListener listenerA = new TestClientListener(300, 220);
        TestClientListener listenerB = new TestClientListener(300, 220);
        RobotGameClient clientA = new RobotGameClient("localhost", port, listenerA);
        RobotGameClient clientB = new RobotGameClient("localhost", port, listenerB);

        try
        {
            server.start();
            clientA.connect();
            clientB.connect();

            Assert.assertTrue(listenerA.welcomeLatch.await(3, TimeUnit.SECONDS));
            Assert.assertTrue(listenerB.welcomeLatch.await(3, TimeUnit.SECONDS));
            Assert.assertTrue(listenerA.stateLatch.await(3, TimeUnit.SECONDS));
            Assert.assertTrue(listenerB.stateLatch.await(3, TimeUnit.SECONDS));

            
            String playerAId = listenerA.myPlayerId;

            Assert.assertTrue(clientA.sendSetTarget(new Point(300, 220)));
            listenerA.setExpectedTarget(playerAId, 300, 220);
            listenerB.setExpectedTarget(playerAId, 300, 220);
            Assert.assertTrue(listenerA.targetLatch.await(3, TimeUnit.SECONDS));
            Assert.assertTrue(listenerB.targetLatch.await(3, TimeUnit.SECONDS));
        }
        finally
        {
            clientA.close();
            clientB.close();
            server.stop();
        }
    }

    @Test
    public void lastWriteWinsForSequentialCommands() throws Exception
    {
        int port = findFreePort();
        RobotGameServer server = new RobotGameServer(port);
        TestClientListener listener = new TestClientListener(180, 140);
        RobotGameClient client = new RobotGameClient("localhost", port, listener);

        try
        {
            server.start();
            client.connect();
            Assert.assertTrue(listener.welcomeLatch.await(3, TimeUnit.SECONDS));

            Assert.assertTrue(client.sendSetTarget(new Point(120, 90)));
            Assert.assertTrue(client.sendSetTarget(new Point(180, 140)));
            listener.setExpectedTarget(listener.myPlayerId, 180, 140);
            Assert.assertTrue(listener.targetLatch.await(3, TimeUnit.SECONDS));
        }
        finally
        {
            client.close();
            server.stop();
        }
    }

    private static int findFreePort() throws IOException
    {
        ServerSocket socket = new ServerSocket(0);
        try
        {
            return socket.getLocalPort();
        }
        finally
        {
            socket.close();
        }
    }

    private static class TestClientListener implements RobotGameClientListener
    {
        
        private volatile int expectedTargetX;
        private volatile int expectedTargetY;
        private volatile String expectedPlayerId;
        volatile String myPlayerId;

        final CountDownLatch welcomeLatch = new CountDownLatch(1);
        final CountDownLatch stateLatch  = new CountDownLatch(1);
        final CountDownLatch targetLatch = new CountDownLatch(1);

        TestClientListener(int expectedTargetX, int expectedTargetY)
        {
            this.expectedTargetX = expectedTargetX;
            this.expectedTargetY = expectedTargetY;
        }

        void setExpectedTarget(String playerId, int x, int y)
        {
            this.expectedPlayerId = playerId;
            this.expectedTargetX = x;
            this.expectedTargetY = y;
        }

        @Override
        public void onWelcome(ServerWelcomeEvent event)
        {
            if (event != null)
            {
                myPlayerId = event.getClientId();
            }
            welcomeLatch.countDown();
        }

        @Override
        public void onState(ServerStateEvent event)
        {
            
        }

        @Override
        public void onFullState(ServerFullStateEvent event)
        {
            if (event == null || event.getPlayers() == null) return;
            stateLatch.countDown();

            for (ServerStateEvent playerState : event.getPlayers())
            {
                if (playerState == null) continue;
                boolean isTarget = expectedPlayerId == null
                        || expectedPlayerId.equals(playerState.getPlayerId());
                if (isTarget
                        && playerState.toRobotState().getTargetPositionX() == expectedTargetX
                        && playerState.toRobotState().getTargetPositionY() == expectedTargetY)
                {
                    targetLatch.countDown();
                    return;
                }
            }
        }

        @Override
        public void onServerError(ServerErrorEvent event)
        {
            Assert.fail("Unexpected server error: " + (event == null ? "null" : event.getMessage()));
        }

        @Override
        public void onDisconnected(String message)
        {
            
        }
    }
}
