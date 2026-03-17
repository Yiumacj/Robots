package controller;

import java.awt.Point;

import org.junit.Assert;
import org.junit.Test;

import gui.RobotStateView;
import model.RobotModel;
import model.RobotState;

public class DefaultGameControllerTest
{
    @Test
    public void clickUpdatesTargetThroughController()
    {
        RobotModel model = new RobotModel();
        DefaultGameController controller = new DefaultGameController(model);
        CapturingView view = new CapturingView();
        controller.addView(view);

        controller.setTargetPosition(new Point(222, 111));

        Assert.assertNotNull(view.lastState);
        Assert.assertEquals(222, view.lastState.getTargetPositionX());
        Assert.assertEquals(111, view.lastState.getTargetPositionY());
    }

    @Test
    public void tickPublishesUpdatedState()
    {
        RobotModel model = new RobotModel();
        DefaultGameController controller = new DefaultGameController(model);
        CapturingView view = new CapturingView();
        controller.addView(view);
        double initialX = view.lastState.getRobotPositionX();

        controller.tick(10);

        Assert.assertTrue(view.renderCount >= 2);
        Assert.assertTrue(view.lastState.getRobotPositionX() > initialX);
    }

    private static class CapturingView implements RobotStateView
    {
        private RobotState lastState;
        private int renderCount;

        @Override
        public void render(RobotState state)
        {
            lastState = state;
            renderCount++;
        }
    }
}
