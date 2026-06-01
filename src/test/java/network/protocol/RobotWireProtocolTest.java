package network.protocol;

import org.junit.Assert;
import org.junit.Test;

public class RobotWireProtocolTest
{
    @Test
    public void encodesAndDecodesSetTargetCommand()
    {
        String json = RobotWireProtocol.encodeMessage(
                RobotWireProtocol.TYPE_CLIENT_SET_TARGET,
                new ClientSetTargetCommand(120, 80));

        WireMessage decoded = RobotWireProtocol.decodeMessage(json);
        ClientSetTargetCommand payload = RobotWireProtocol.decodePayload(decoded, ClientSetTargetCommand.class);

        Assert.assertEquals(RobotWireProtocol.TYPE_CLIENT_SET_TARGET, decoded.getType());
        Assert.assertEquals(120, payload.getX());
        Assert.assertEquals(80, payload.getY());
    }

    @Test
    public void encodesAndDecodesStateEventWithTick()
    {
        ServerStateEvent event = new ServerStateEvent(10.5, 17.25, 0.5, 100, 120, 42L, "test-player-id");
        String json = RobotWireProtocol.encodeMessage(RobotWireProtocol.TYPE_SERVER_STATE, event);

        WireMessage decoded = RobotWireProtocol.decodeMessage(json);
        ServerStateEvent payload = RobotWireProtocol.decodePayload(decoded, ServerStateEvent.class);

        Assert.assertEquals(RobotWireProtocol.TYPE_SERVER_STATE, decoded.getType());
        Assert.assertEquals(42L, payload.getServerTick());
        Assert.assertEquals(100, payload.toRobotState().getTargetPositionX());
        Assert.assertEquals(120, payload.toRobotState().getTargetPositionY());
    }
}
