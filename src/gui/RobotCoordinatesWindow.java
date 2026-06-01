package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import log.Logger;
import service.LocalizationService;
import model.RobotState;

public class RobotCoordinatesWindow extends JInternalFrame implements RobotStateView, LocalizableView
{
    private static final String LOG_SOURCE = "gui.RobotCoordinatesWindow";
    private final LocalizationService localizationService;
    private final JLabel robotXLabel = new JLabel();
    private final JLabel robotYLabel = new JLabel();
    private final JLabel directionLabel = new JLabel();
    private final JLabel targetXLabel = new JLabel();
    private final JLabel targetYLabel = new JLabel();
    private RobotState state = new RobotState(100.0, 100.0, 0.0, 150, 100);

    public RobotCoordinatesWindow(LocalizationService localizationService)
    {
        super("", true, true, true, true);
        this.localizationService = localizationService;

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
        updateTexts();
        Logger.debug(LOG_SOURCE, "initialized", "Coordinates window initialized.");
    }

    @Override
    public void render(RobotState state)
    {
        this.state = state;
        SwingUtilities.invokeLater(() -> {
            setTitle(localizationService.get("window.coordinates"));
            robotXLabel.setText(localizationService.format("coordinates.robot_x", state.getRobotPositionX()));
            robotYLabel.setText(localizationService.format("coordinates.robot_y", state.getRobotPositionY()));
            directionLabel.setText(localizationService.format("coordinates.direction", state.getRobotDirection()));
            targetXLabel.setText(localizationService.format("coordinates.target_x", state.getTargetPositionX()));
            targetYLabel.setText(localizationService.format("coordinates.target_y", state.getTargetPositionY()));
        });
    }

    @Override
    public void updateTexts()
    {
        render(state);
    }
}
