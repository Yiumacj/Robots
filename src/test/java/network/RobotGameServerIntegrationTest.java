package network;

import java.awt.Point;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import network.protocol.ServerErrorEvent;
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

            Assert.assertTrue(clientA.sendSetTarget(new Point(300, 220)));
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
        private final int expectedTargetX;
        private final int expectedTargetY;
        private final CountDownLatch welcomeLatch = new CountDownLatch(1);
        private final CountDownLatch stateLatch = new CountDownLatch(1);
        private final CountDownLatch targetLatch = new CountDownLatch(1);

        private TestClientListener(int expectedTargetX, int expectedTargetY)
        {
            this.expectedTargetX = expectedTargetX;
            this.expectedTargetY = expectedTargetY;
        }

        @Override
        public void onWelcome(ServerWelcomeEvent event)
        {
            welcomeLatch.countDown();
        }

        @Override
        public void onState(ServerStateEvent event)
        {
            if (event != null)
            {
                stateLatch.countDown();
                if (event.toRobotState().getTargetPositionX() == expectedTargetX
                        && event.toRobotState().getTargetPositionY() == expectedTargetY)
                {
                    targetLatch.countDown();
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
            // Test controls lifecycle and closes clients explicitly.
        }
    }
}
