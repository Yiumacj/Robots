package model;

import org.junit.Assert;
import org.junit.Test;

public class RobotModelTest
{
    @Test
    public void updateMovesRobotTowardsTargetAhead()
    {
        RobotModel model = new RobotModel();
        RobotState before = model.getState();

        model.update(10);

        RobotState after = model.getState();
        Assert.assertTrue(after.getRobotPositionX() > before.getRobotPositionX());
    }

    @Test
    public void normalizedRadiansStayWithinAbsoluteRange()
    {
        double angle = RobotModel.asNormalizedRadians(-Math.PI / 2);

        Assert.assertTrue(angle >= 0);
        Assert.assertTrue(angle < 2 * Math.PI);
    }

    @Test
    public void updateDoesNotMoveWhenTargetReached()
    {
        RobotModel model = new RobotModel();
        RobotState initial = model.getState();
        model.setTargetPosition((int) initial.getRobotPositionX(), (int) initial.getRobotPositionY());

        model.update(10);

        RobotState after = model.getState();
        Assert.assertEquals(initial.getRobotPositionX(), after.getRobotPositionX(), 0.00001);
        Assert.assertEquals(initial.getRobotPositionY(), after.getRobotPositionY(), 0.00001);
    }
}
