package com.example.AutoVampyres;

import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.example.AutoVampyres.data.WorldHopper;
import com.example.AutoVampyres.tasks.*;
import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.piggyplugins.PiggyUtils.API.PlayerUtil;
import com.piggyplugins.PiggyUtils.BreakHandler.ReflectBreakHandler;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import com.piggyplugins.PiggyUtils.strategy.TaskManager;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

@PluginDescriptor(
        name = "<html><font color=\"#FF9DF9\">[Barr]</font>AutoVampyres</html>",
        description = "Sharded My Pants",
        enabledByDefault = false,
        tags = {"barr", "plugin", "vampyres", "blood", "bloodshards", "vamp"}
)
public class AutoVampyres extends Plugin {
    public static final Logger log = LoggerFactory.getLogger(AutoVampyres.class);
    public static final WorldArea TOB_BANK_AREA = new WorldArea(3640, 3200, 50, 50, 0);

    @Getter @Inject private Client client;
    @Inject private KeyManager keyManager;
    @Getter @Inject private AutoVampyresConfig config;
    @Inject private AutoVampyresOverlay overlay;
    @Inject private OverlayManager overlayManager;
    @Getter @Inject private WorldHopper worldHopper;
    @Getter @Inject private ReflectBreakHandler breakHandler;
    @Getter @Inject private PlayerUtil playerUtil;
    @Inject private ClientThread clientThread;

    private final TaskManager taskManager = new TaskManager();
    @Getter private Instant startTime;
    @Getter @Setter public boolean hopWorld;
    private boolean started = false;
    public int timeout = 0;
    @Setter @Getter private String currentTask = "NULL";

    @Provides
    private AutoVampyresConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoVampyresConfig.class);
    }

    @Override
    protected void startUp() {
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
        breakHandler.registerPlugin(this);
        log.info("AutoVyres registered with breakhandler.");

    }

    @Override
    protected void shutDown() {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
        breakHandler.unregisterPlugin(this);
        log.info("AutoVyres deregistered from breakhandler.");
        timeout = 0;
        started = false;
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        if (client.getGameState() != GameState.LOGGED_IN || !started || breakHandler.isBreakActive(this))
        {
            return;
        }

        if (timeout > 0)
        {
            timeout--;
            log.info("timeout tick");
            return;
        }

        if (taskManager.hasTasks())
        {
            for (AbstractTask t : taskManager.getTasks())
            {
                if (t.validate() && !breakHandler.isBreakActive(this))
                {
                    t.execute();
                    return;
                }
            }
        }
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public void toggle() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        started = !started;

        if (!started) {
            breakHandler.stopPlugin(this);
        } else {
            breakHandler.startPlugin(this);
            startTime = Instant.now();
            initializeTasks();
        }
    }

    private void initializeTasks() {
        taskManager.addTask(new Break(this, config));
        taskManager.addTask(new Eat(this, config));
        taskManager.addTask(new Banking(this, config));
        taskManager.addTask(new InventorySpace(this, config));
        taskManager.addTask(new WalkToArea(this, config));
        taskManager.addTask(new Robbery(this, config));
    }

    // Getters and setters
    public boolean getStarted() { return started; }

}