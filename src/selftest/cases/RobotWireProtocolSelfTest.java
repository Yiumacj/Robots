package selftest.cases;

import network.protocol.ClientSetTargetCommand;
import network.protocol.RobotWireProtocol;
import network.protocol.ServerStateEvent;
import network.protocol.WireMessage;
import selftest.SelfTestCase;
import selftest.SelfTestContext;

public class RobotWireProtocolSelfTest implements SelfTestCase
{
    @Override
    public String getName()
    {
        return "Wire protocol encode/decode";
    }

    @Override
    public void run(SelfTestContext context)
    {
        String setTargetJson = RobotWireProtocol.encodeMessage(
                RobotWireProtocol.TYPE_CLIENT_SET_TARGET,
                new ClientSetTargetCommand(140, 230));
        WireMessage setTargetMessage = RobotWireProtocol.decodeMessage(setTargetJson);
        context.assertEquals(RobotWireProtocol.TYPE_CLIENT_SET_TARGET, setTargetMessage.getType(), "SetTarget type mismatch.");
        ClientSetTargetCommand setTarget = RobotWireProtocol.decodePayload(setTargetMessage, ClientSetTargetCommand.class);
        context.assertNotNull(setTarget, "SetTarget payload should not be null.");
        context.assertEquals(Integer.valueOf(140), Integer.valueOf(setTarget.getX()), "SetTarget x mismatch.");
        context.assertEquals(Integer.valueOf(230), Integer.valueOf(setTarget.getY()), "SetTarget y mismatch.");

        String stateJson = RobotWireProtocol.encodeMessage(
                RobotWireProtocol.TYPE_SERVER_STATE,
                new ServerStateEvent(10.5, 20.5, 0.8, 111, 222, 13L));
        WireMessage stateMessage = RobotWireProtocol.decodeMessage(stateJson);
        context.assertEquals(RobotWireProtocol.TYPE_SERVER_STATE, stateMessage.getType(), "State type mismatch.");
        ServerStateEvent state = RobotWireProtocol.decodePayload(stateMessage, ServerStateEvent.class);
        context.assertNotNull(state, "State payload should not be null.");
        context.assertEquals(Long.valueOf(13L), Long.valueOf(state.getServerTick()), "Server tick mismatch.");
        context.assertEquals(Integer.valueOf(111), Integer.valueOf(state.toRobotState().getTargetPositionX()), "Target x mismatch.");
        context.assertEquals(Integer.valueOf(222), Integer.valueOf(state.toRobotState().getTargetPositionY()), "Target y mismatch.");
    }
}
