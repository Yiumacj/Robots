package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.List;

import javax.swing.JPanel;

import controller.GameController;
import log.Logger;
import model.MultiPlayerState;
import model.RobotState;

public class GameVisualizer extends JPanel
{
    private static final String LOG_SOURCE = "gui.GameVisualizer";
    private static final RobotState DEFAULT_STATE = new RobotState(100.0, 100.0, 0.0, 150, 100);

    private GameController controller;
    private RobotState state = DEFAULT_STATE;
    private MultiPlayerState multiState = null;

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
        this.multiState = null;
        repaint();
    }

    
    public void renderMulti(MultiPlayerState mState)
    {
        this.multiState = mState;
        this.state = mState.getOwnState();
        repaint();
    }

    private static int round(double value)
    {
        return (int) (value + 0.5);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (multiState != null)
        {
            
            List<RobotState> opponents = multiState.getOpponentStates();
            for (RobotState opponent : opponents)
            {
                drawRobot(g2d,
                        round(opponent.getRobotPositionX()),
                        round(opponent.getRobotPositionY()),
                        opponent.getRobotDirection(),
                        opponent.getRobotColor());
                drawTarget(g2d, opponent.getTargetPositionX(), opponent.getTargetPositionY(), Color.BLUE);
            }
            
            RobotState own = multiState.getOwnState();
            drawRobot(g2d,
                    round(own.getRobotPositionX()),
                    round(own.getRobotPositionY()),
                    own.getRobotDirection(),
                    own.getRobotColor());
            drawTarget(g2d, own.getTargetPositionX(), own.getTargetPositionY(), Color.GREEN);
        }
        else
        {
            drawRobot(g2d,
                    round(state.getRobotPositionX()),
                    round(state.getRobotPositionY()),
                    state.getRobotDirection(),
                    state.getRobotColor());
            drawTarget(g2d, state.getTargetPositionX(), state.getTargetPositionY(), Color.GREEN);
        }
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawRobot(Graphics2D g, int x, int y, double direction, Color bodyColor)
    {
        AffineTransform saved = g.getTransform();
        AffineTransform transform = AffineTransform.getRotateInstance(direction, x, y);
        g.setTransform(transform);
        g.setColor(bodyColor);
        fillOval(g, x, y, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, x + 10, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x + 10, y, 5, 5);
        g.setTransform(saved);
    }

    private static void drawTarget(Graphics2D g, int x, int y, Color color)
    {
        AffineTransform saved = g.getTransform();
        g.setTransform(new AffineTransform());
        g.setColor(color);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
        g.setTransform(saved);
    }
}
