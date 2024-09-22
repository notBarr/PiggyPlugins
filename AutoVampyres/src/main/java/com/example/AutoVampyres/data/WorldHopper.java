package com.example.AutoVampyres.data;

import com.example.EthanApiPlugin.EthanApiPlugin;
import lombok.NoArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.WorldService;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@NoArgsConstructor
public class WorldHopper
{
    private final Client client = RuneLite.getInjector().getInstance(Client.class);
    private final WorldService worldService = RuneLite.getInjector().getProvider(WorldService.class).get();
    private final ClientThread clientThread = RuneLite.getInjector().getInstance(ClientThread.class);
    private net.runelite.api.World quickHopTargetWorld = null;
    private int displaySwitcherAttempts = 0;

    public void setupHop() {
        WorldResult worldResult = worldService.getWorlds();

        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        List<World> worlds = worldResult.getWorlds();
        Collections.shuffle(worlds); // Shuffle the list of worlds

        World currentWorld = worldResult.findWorld(client.getWorld());

        if (currentWorld == null) {
            return;
        }

        EnumSet<WorldType> currentWorldTypes = currentWorld.getTypes().clone();

        // Remove unwanted world types
        currentWorldTypes.remove(WorldType.PVP);
        currentWorldTypes.remove(WorldType.FRESH_START_WORLD);
        currentWorldTypes.remove(WorldType.HIGH_RISK);
        currentWorldTypes.remove(WorldType.BETA_WORLD);
        currentWorldTypes.remove(WorldType.LAST_MAN_STANDING);
        currentWorldTypes.remove(WorldType.SKILL_TOTAL);
        currentWorldTypes.remove(WorldType.PVP_ARENA);
        currentWorldTypes.remove(WorldType.QUEST_SPEEDRUNNING);
        currentWorldTypes.remove(WorldType.SEASONAL);
        currentWorldTypes.remove(WorldType.DEADMAN);
        currentWorldTypes.remove(WorldType.BOUNTY);
        currentWorldTypes.remove(WorldType.NOSAVE_MODE);
        currentWorldTypes.remove(WorldType.TOURNAMENT);

        for (World world : worlds) {
            EnumSet<WorldType> types = world.getTypes().clone();

            types.remove(WorldType.BOUNTY);
            types.remove(WorldType.LAST_MAN_STANDING);

            if (world.getPlayers() >= 1800 || world.getPlayers() < 0 || world.getId() == 400) {
                continue; // Skip worlds with near-max population or invalid IDs
            }

            // Break out if we've found a good world to hop to
            if (currentWorldTypes.equals(types)) {
                World newWorld = worldResult.findWorld(world.getId());
                if (newWorld != null) {
                    clientThread.invoke(() -> hop(client, newWorld));
                    return; // Exit the method after hopping to the first suitable world
                }
            }
        }

        EthanApiPlugin.sendClientMessage("Couldn't find world to hop to.");
    }

    public void setupHopF2P() {
        WorldResult worldResult = worldService.getWorlds();

        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        World currentWorld = worldResult.findWorld(client.getWorld());

        if (currentWorld == null) {
            return;
        }

        EnumSet<WorldType> currentWorldTypes = currentWorld.getTypes().clone();

        currentWorldTypes.remove(WorldType.PVP);
        currentWorldTypes.remove(WorldType.HIGH_RISK);
        currentWorldTypes.remove(WorldType.BOUNTY);
        currentWorldTypes.remove(WorldType.SKILL_TOTAL);
        currentWorldTypes.remove(WorldType.LAST_MAN_STANDING);
        currentWorldTypes.remove(WorldType.MEMBERS);

        List<World> worlds = worldResult.getWorlds();

        int worldIdx = worlds.indexOf(currentWorld);

        World world;
        do {
            worldIdx++;

            if (worldIdx >= worlds.size()) {
                worldIdx = 0;
            }

            world = worlds.get(worldIdx);

            EnumSet<WorldType> types = world.getTypes().clone();

            types.remove(WorldType.BOUNTY);
            types.remove(WorldType.LAST_MAN_STANDING);

            // Avoid switching to near-max population worlds, as it will refuse to allow the hop if the world is full
            if (world.getPlayers() >= 1800) {
                continue;
            }

            if (world.getPlayers() < 0) {
                continue;
            }

            // Break out if we've found a good world to hop to
            if (currentWorldTypes.equals(types)) {
                break;
            }
        }
        while (world != currentWorld);

        if (world == currentWorld) {

        } else {
            World newWorld = worldResult.findWorld(world.getId());
            if (newWorld != null) {
                hop(client, newWorld);
            }
        }
    }

    public void setupHopTo(Integer id) {
        WorldResult worldResult = worldService.getWorlds();

        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        World currentWorld = worldResult.findWorld(client.getWorld());

        if (currentWorld == null) {
            return;
        }

        List<World> worlds = worldResult.getWorlds();

        int worldIdx = worlds.indexOf(currentWorld);

        World world;
        do {
            worldIdx++;

            if (worldIdx >= worlds.size()) {
                worldIdx = 0;
            }

            world = worlds.get(worldIdx);

            if (world.getId() == id) {
                break;
            }
        }
        while (world != currentWorld);

        if (world == currentWorld) {

        } else {
            World newWorld = worldResult.findWorld(world.getId());
            if (newWorld != null) {
                hop(client, newWorld);
            }
        }
    }

    public void hopToSpecificWorld(int worldNumber) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            EthanApiPlugin.sendClientMessage("Not logged in!");
            return;
        }

        WorldResult worldResult = worldService.getWorlds();

        if (worldResult == null) {
            EthanApiPlugin.sendClientMessage("Failed to retrieve world list!");
            return;
        }

        List<World> worlds = worldResult.getWorlds();

        for (World world : worlds) {
            if (world.getId() == worldNumber) {
                World newWorld = worldResult.findWorld(world.getId());
                if (newWorld != null) {
                    hop(client, newWorld);
                    return;
                }
            }
        }

        EthanApiPlugin.sendClientMessage("World " + worldNumber + " not found!");
    }

    private void hop(Client client, World world) {
        assert client.isClientThread();

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        if (client.getGameState() == GameState.LOGIN_SCREEN) {
            // on the login screen we can just change the world by ourselves
            client.changeWorld(rsWorld);
            return;
        }

        quickHopTargetWorld = rsWorld;
        displaySwitcherAttempts = 0;
    }

    public void hopWorlds() {
        if (quickHopTargetWorld == null) {
            setupHop();
        }

        if (client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) == null) {
            client.openWorldHopper();

            if (++displaySwitcherAttempts >= 10) {
                EthanApiPlugin.sendClientMessage("Couldn't hop!");

                displaySwitcherAttempts = 0;
                quickHopTargetWorld = null;
            }
        } else {
            client.hopToWorld(quickHopTargetWorld);
            displaySwitcherAttempts = 0;
            quickHopTargetWorld = null;
        }

    }

}
