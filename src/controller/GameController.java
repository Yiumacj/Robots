package controller;

import java.awt.Color;
import java.awt.Point;

import gui.RobotStateView;

public interface GameController
{
    void start();

    void stop();

    void addView(RobotStateView view);

    void setTargetPosition(Point point);

    void setRobotColor(Color color);

    void tick(double duration);
}
