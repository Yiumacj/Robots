package controller;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gui.RobotStateView;
import log.Logger;
import model.MultiPlayerState;
import model.RobotModel;
import model.RobotState;








public class LocalMultiplayerGameSession
{
    private static final String LOG_SOURCE = "controller.LocalMultiplayerGameSession";
    private static final long TICK_MS = 10L;

    private final List<PlayerSlot> players = new ArrayList<PlayerSlot>();
    private Timer timer;

    public LocalMultiplayerGameSession(int playerCount)
    {
        for (int i = 0; i < playerCount; i++)
        {
            double startX = 100 + i * 120;
            double startY = 100;
            players.add(new PlayerSlot(new RobotModel(startX, startY)));
        }
    }

    public GameController createController(int playerIndex)
    {
        if (playerIndex < 0 || playerIndex >= players.size())
        {
            throw new IllegalArgumentException("Invalid player index: " + playerIndex);
        }
        return new Controller(playerIndex);
    }

    private synchronized void startTimerIfNeeded()
    {
        if (timer != null)
        {
            return;
        }
        Logger.info(LOG_SOURCE, "start", "Starting local multiplayer timer.");
        timer = new Timer("local-multiplayer-robot-timer", true);
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                tickAll(TICK_MS);
            }
        }, 0L, TICK_MS);
    }

    private synchronized void stopTimerIfNoActivePlayers()
    {
        for (PlayerSlot player : players)
        {
            if (player.active)
            {
                return;
            }
        }
        if (timer != null)
        {
            timer.cancel();
            timer = null;
            Logger.info(LOG_SOURCE, "stop", "Local multiplayer timer stopped.");
        }
    }

    private synchronized void tickAll(double duration)
    {
        for (PlayerSlot player : players)
        {
            if (player.active)
            {
                player.model.update(duration);
            }
        }
        publishAll();
    }

    private synchronized void publishAll()
    {
        for (int i = 0; i < players.size(); i++)
        {
            PlayerSlot recipient = players.get(i);
            if (!recipient.active)
            {
                continue;
            }

            RobotState own = recipient.model.getState();
            List<RobotState> opponents = new ArrayList<RobotState>();
            for (int j = 0; j < players.size(); j++)
            {
                if (i == j)
                {
                    continue;
                }
                PlayerSlot opponent = players.get(j);
                if (opponent.active)
                {
                    opponents.add(opponent.model.getState());
                }
            }

            MultiPlayerState state = new MultiPlayerState(own, opponents);
            for (RobotStateView view : recipient.views)
            {
                view.renderMulti(state);
            }
        }
    }

    private static class PlayerSlot
    {
        private final RobotModel model;
        private final List<RobotStateView> views = new ArrayList<RobotStateView>();
        private boolean active;

        private PlayerSlot(RobotModel model)
        {
            this.model = model;
        }
    }

    private class Controller implements GameController
    {
        private final int playerIndex;

        private Controller(int playerIndex)
        {
            this.playerIndex = playerIndex;
        }

        @Override
        public void start()
        {
            synchronized (LocalMultiplayerGameSession.this)
            {
                players.get(playerIndex).active = true;
                startTimerIfNeeded();
                publishAll();
            }
        }

        @Override
        public void stop()
        {
            synchronized (LocalMultiplayerGameSession.this)
            {
                players.get(playerIndex).active = false;
                publishAll();
                stopTimerIfNoActivePlayers();
            }
        }

        @Override
        public void addView(RobotStateView view)
        {
            synchronized (LocalMultiplayerGameSession.this)
            {
                players.get(playerIndex).views.add(view);
                
                
                
                
                publishAll();
            }
        }

        @Override
        public void setTargetPosition(Point point)
        {
            if (point == null)
            {
                return;
            }
            synchronized (LocalMultiplayerGameSession.this)
            {
                players.get(playerIndex).model.setTargetPosition(point);
                publishAll();
            }
        }

        @Override
        public void setRobotColor(Color color)
        {
            if (color == null)
            {
                return;
            }
            synchronized (LocalMultiplayerGameSession.this)
            {
                players.get(playerIndex).model.setRobotColorRgb(color.getRGB());
                publishAll();
            }
        }

        @Override
        public void tick(double duration)
        {
            synchronized (LocalMultiplayerGameSession.this)
            {
                players.get(playerIndex).model.update(duration);
                publishAll();
            }
        }
    }
}
