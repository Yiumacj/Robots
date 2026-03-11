package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RobotCoordinatesWindow extends JInternalFrame implements Observer
{
    private final RobotModel model;
    private final JLabel robotXLabel = new JLabel();
    private final JLabel robotYLabel = new JLabel();
    private final JLabel directionLabel = new JLabel();
    private final JLabel targetXLabel = new JLabel();
    private final JLabel targetYLabel = new JLabel();

    public RobotCoordinatesWindow(RobotModel model)
    {
        super("Координаты робота", true, true, true, true);
        this.model = model;
        this.model.addObserver(this);

        JPanel content = new JPanel(new GridLayout(0, 1, 5, 5));
        content.add(robotXLabel);
        content.add(robotYLabel);
        content.add(directionLabel);
        content.add(targetXLabel);
        content.add(targetYLabel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.NORTH);
        getContentPane().add(wrapper);

        setSize(260, 160);
        updateLabels();
    }

    @Override
    public void update(Observable o, Object arg)
    {
        SwingUtilities.invokeLater(this::updateLabels);
    }

    private void updateLabels()
    {
        robotXLabel.setText(String.format(Locale.US, "X робота: %.2f", model.getRobotPositionX()));
        robotYLabel.setText(String.format(Locale.US, "Y робота: %.2f", model.getRobotPositionY()));
        directionLabel.setText(String.format(Locale.US, "Направление: %.4f рад", model.getRobotDirection()));
        targetXLabel.setText(String.format(Locale.US, "X цели: %d", model.getTargetPositionX()));
        targetYLabel.setText(String.format(Locale.US, "Y цели: %d", model.getTargetPositionY()));
    }
}