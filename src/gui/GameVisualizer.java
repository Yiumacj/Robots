package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import controller.GameController;
import log.Logger;
import model.RobotState;

public class GameVisualizer extends JPanel
{
    private static final String LOG_SOURCE = "gui.GameVisualizer";
    private static final RobotState DEFAULT_STATE = new RobotState(100.0, 100.0, 0.0, 150, 100);

    private GameController controller;
    private RobotState state = DEFAULT_STATE;

    public GameVisualizer()
    {
        addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                if (controller != null)
                {
                    Logger.debug(LOG_SOURCE, "mouse_clicked", "x=" + e.getX() + ", y=" + e.getY());
                    controller.setTargetPosition(e.getPoint());
                }
            }
        });
        setDoubleBuffered(true);
    }

    public void setController(GameController controller)
    {
        this.controller = controller;
    }

    public void render(RobotState state)
    {
        this.state = state;
        repaint();
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawRobot(g2d, round(state.getRobotPositionX()), round(state.getRobotPositionY()), state.getRobotDirection());
        drawTarget(g2d, state.getTargetPositionX(), state.getTargetPositionY());
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        AffineTransform transform = AffineTransform.getRotateInstance(direction, x, y);
        g.setTransform(transform);
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, x + 10, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x + 10, y, 5, 5);
    }

    private static void drawTarget(Graphics2D g, int x, int y)
    {
        g.setTransform(AffineTransform.getRotateInstance(0, 0, 0));
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }
}
