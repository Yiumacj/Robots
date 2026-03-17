package controller;

import java.awt.Point;

import gui.RobotStateView;

public interface GameController
{
    void start();

    void stop();

    void addView(RobotStateView view);

    void setTargetPosition(Point point);

    void tick(double duration);
}
