package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.GameController;
import log.Logger;
import model.MultiPlayerState;
import model.RobotState;
import service.LocalizationService;
import plugin.RobotJarLoader;
import plugin.RobotJarRunner;
import script.RobotScriptRunner;

public class GameWindow extends JInternalFrame implements RobotStateView, LocalizableView
{
    private static final String LOG_SOURCE = "gui.GameWindow";
    private final GameVisualizer visualizer;
    private final LocalizationService localizationService;
    private final JButton colorButton = new JButton();
    private final JButton scriptButton = new JButton();
    private GameController controller;
    private Thread programThread;
    private RobotState currentState = new RobotState(100.0, 100.0, 0.0, 150, 100);

    public GameWindow(LocalizationService localizationService)
    {
        super("", true, true, true, true);
        this.localizationService = localizationService;
        visualizer = new GameVisualizer();
        colorButton.addActionListener(event -> chooseRobotColor());
        scriptButton.addActionListener(event -> chooseAndRunScript());

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.add(colorButton, BorderLayout.WEST);
        toolbar.add(scriptButton, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateTexts();
    }

    public void setController(GameController controller)
    {
        this.controller = controller;
        visualizer.setController(controller);
        Logger.debug(LOG_SOURCE, "controller_bound", "Game controller bound to visualizer.");
    }

    @Override
    public void render(RobotState state)
    {
        currentState = state;
        SwingUtilities.invokeLater(() -> visualizer.render(state));
    }

    @Override
    public void renderMulti(MultiPlayerState state)
    {
        currentState = state.getOwnState();
        SwingUtilities.invokeLater(() -> visualizer.renderMulti(state));
    }

    @Override
    public void updateTexts()
    {
        setTitle(localizationService.get("window.game"));
        colorButton.setText(localizationService.get("game.change_color"));
        scriptButton.setText(localizationService.get("game.run_script"));
    }

    private void chooseRobotColor()
    {
        Color selected = JColorChooser.showDialog(
                this,
                localizationService.get("game.choose_color.title"),
                currentState.getRobotColor());
        if (selected != null && controller != null)
        {
            controller.setRobotColor(selected);
        }
    }

    private void chooseAndRunScript()
    {
        if (controller == null)
        {
            return;
        }

        JFileChooser chooser = new JFileChooser(new File("scripts"));
        chooser.setDialogTitle(localizationService.get("game.script_chooser.title"));
        chooser.setFileFilter(new FileNameExtensionFilter(localizationService.get("game.program_filter"), "lua", "jar"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        startProgram(chooser.getSelectedFile());
    }

    private synchronized void startProgram(File file)
    {
        if (programThread != null && programThread.isAlive())
        {
            programThread.interrupt();
        }

        if (isJar(file))
        {
            startJarProgram(file);
        }
        else
        {
            startScriptProgram(file);
        }
    }

    private void startScriptProgram(File file)
    {
        scriptButton.setEnabled(false);
        RobotScriptRunner runner = new RobotScriptRunner(
                file,
                controller,
                () -> SwingUtilities.invokeLater(() -> scriptButton.setEnabled(true)),
                message -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        message,
                        localizationService.get("game.script_error.title"),
                        JOptionPane.ERROR_MESSAGE)));

        programThread = new Thread(runner, "robot-script-" + file.getName());
        programThread.setDaemon(true);
        programThread.start();
        Logger.info(LOG_SOURCE, "script_started", localizationService.format("game.script_started", file.getName()));
    }

    private void startJarProgram(File file)
    {
        String className;
        try
        {
            className = chooseRobotClass(file);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    localizationService.get("game.script_error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (className == null)
        {
            return;
        }

        scriptButton.setEnabled(false);
        RobotJarRunner runner = new RobotJarRunner(
                file,
                className,
                controller,
                () -> SwingUtilities.invokeLater(() -> scriptButton.setEnabled(true)),
                message -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        message,
                        localizationService.get("game.script_error.title"),
                        JOptionPane.ERROR_MESSAGE)));

        programThread = new Thread(runner, "robot-jar-" + file.getName());
        programThread.setDaemon(true);
        programThread.start();
        Logger.info(LOG_SOURCE, "jar_started", localizationService.format("game.jar_started", file.getName(), className));
    }

    private String chooseRobotClass(File file) throws Exception
    {
        java.util.List<String> classes = RobotJarLoader.findProgramClassNames(file);
        if (classes.isEmpty())
        {
            throw new IllegalArgumentException(localizationService.get("game.jar_no_robot_classes"));
        }
        if (classes.size() == 1)
        {
            return classes.get(0);
        }
        Object selected = JOptionPane.showInputDialog(
                this,
                localizationService.get("game.jar_class_select.message"),
                localizationService.get("game.jar_class_select.title"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                classes.toArray(),
                classes.get(0));
        return selected == null ? null : selected.toString();
    }

    private static boolean isJar(File file)
    {
        return file != null && file.getName().toLowerCase().endsWith(".jar");
    }
}
