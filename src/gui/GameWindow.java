package gui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.GameController;
import model.RobotState;
import service.LocalizationService;

public class GameWindow extends JInternalFrame implements RobotStateView, LocalizableView
{
    private final GameVisualizer visualizer;
    private final LocalizationService localizationService;

    public GameWindow(LocalizationService localizationService)
    {
        super("", true, true, true, true);
        this.localizationService = localizationService;
        visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateTexts();
    }

    public void setController(GameController controller)
    {
        visualizer.setController(controller);
    }

    @Override
    public void render(RobotState state)
    {
        SwingUtilities.invokeLater(() -> visualizer.render(state));
    }

    @Override
    public void updateTexts()
    {
        setTitle(localizationService.get("window.game"));
    }
}
