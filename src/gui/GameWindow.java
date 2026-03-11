package gui;

import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class GameWindow extends JInternalFrame
{
    private final RobotModel model;
    private final GameVisualizer visualizer;
    private final Timer timer = initTimer();

    public GameWindow(RobotModel model)
    {
        super("Игровое поле", true, true, true, true);
        this.model = model;
        visualizer = new GameVisualizer(model);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        startModelUpdates();
    }

    private static Timer initTimer()
    {
        return new Timer("robot model timer", true);
    }

    private void startModelUpdates()
    {
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                model.update(10);
            }
        }, 0, 10);
    }
}