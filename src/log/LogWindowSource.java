package log;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он 
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено 
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений 
 * ограниченного размера) 
 */
public class LogWindowSource
{
    private final int m_iQueueLength;
    private final ArrayDeque<LogEntry> m_messages;
    private final CopyOnWriteArrayList<WeakReference<LogChangeListener>> m_listeners;

    public LogWindowSource(int iQueueLength) 
    {
        m_iQueueLength = iQueueLength;
        m_messages = new ArrayDeque<LogEntry>(iQueueLength);
        m_listeners = new CopyOnWriteArrayList<WeakReference<LogChangeListener>>();
    }
    
    public void registerListener(LogChangeListener listener)
    {
        if (listener == null)
        {
            return;
        }
        m_listeners.add(new WeakReference<LogChangeListener>(listener));
        cleanupListeners();
    }
    
    public void unregisterListener(LogChangeListener listener)
    {
        if (listener == null)
        {
            return;
        }
        for (WeakReference<LogChangeListener> reference : m_listeners)
        {
            LogChangeListener candidate = reference.get();
            if (candidate == null || candidate == listener)
            {
                m_listeners.remove(reference);
            }
        }
    }
    
    public void append(LogLevel logLevel, String strMessage)
    {
        append(new LogEntry(logLevel, strMessage));
    }

    public void append(LogEntry entry)
    {
        synchronized (this)
        {
            if (m_messages.size() >= m_iQueueLength)
            {
                m_messages.removeFirst();
            }
            m_messages.addLast(entry);
        }
        for (WeakReference<LogChangeListener> reference : m_listeners)
        {
            LogChangeListener listener = reference.get();
            if (listener != null)
            {
                listener.onLogChanged();
            }
            else
            {
                m_listeners.remove(reference);
            }
        }
    }
    
    public synchronized int size()
    {
        return m_messages.size();
    }

    public synchronized Iterable<LogEntry> range(int startFrom, int count)
    {
        if (startFrom < 0 || startFrom >= m_messages.size())
        {
            return Collections.emptyList();
        }
        List<LogEntry> snapshot = new ArrayList<LogEntry>(m_messages);
        int indexTo = Math.min(startFrom + count, snapshot.size());
        return snapshot.subList(startFrom, indexTo);
    }

    public synchronized Iterable<LogEntry> all()
    {
        return new ArrayList<LogEntry>(m_messages);
    }

    private void cleanupListeners()
    {
        for (WeakReference<LogChangeListener> reference : m_listeners)
        {
            if (reference.get() == null)
            {
                m_listeners.remove(reference);
            }
        }
    }
}
