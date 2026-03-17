package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;
import service.LocalizationService;

public class LogWindow extends JInternalFrame implements LogChangeListener, LocalizableView
{
    private final LogWindowSource logSource;
    private final TextArea logContent;
    private final LocalizationService localizationService;

    public LogWindow(LogWindowSource logSource, LocalizationService localizationService)
    {
        super("", true, true, true, true);
        this.logSource = logSource;
        this.localizationService = localizationService;
        this.logSource.registerListener(this);
        logContent = new TextArea("");
        logContent.setSize(200, 500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateTexts();
        updateLogContent();
    }

    private void updateLogContent()
    {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : logSource.all())
        {
            content.append(entry.getMessage()).append("\n");
        }
        logContent.setText(content.toString());
        logContent.invalidate();
    }

    @Override
    public void onLogChanged()
    {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public void updateTexts()
    {
        setTitle(localizationService.get("window.log"));
    }
}
