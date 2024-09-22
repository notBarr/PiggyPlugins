package com.example.AutoVampyres.tasks;

import com.example.AutoVampyres.AutoVampyres;
import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.Collections.EquipmentItemWidget;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.piggyplugins.PiggyUtils.API.InventoryUtil;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalkToArea extends AbstractTask<AutoVampyres, AutoVampyresConfig>
{
    private static final Logger log = LoggerFactory.getLogger(WalkToArea.class);
    Client client;
    AutoVampyresConfig config;

    public WalkToArea(AutoVampyres plugin, AutoVampyresConfig config)
    {
        super(plugin, config);
        this.client  = plugin.getClient();
        this.config = plugin.getConfig();
    }

    @Override
    public boolean validate()
    {
        return client.getGameState() == GameState.LOGGED_IN
                && Inventory.getItemAmount("Dodgy necklace") >= config.necklaceQuantity()
                //&& plugin.getPlayerUtil().hp() >= client.getRealSkillLevel(Skill.HITPOINTS)
                && client.getRealSkillLevel(Skill.THIEVING) >= 82
                && !checkLocation();
    }

    @Override
    public void execute()
    {
        plugin.setCurrentTask("WALK");
        if (isMoving())
        {
            log.info("Player is moving, timeout 1 tick.");
            plugin.timeout = 1;
            return;
        }

        if (AutoVampyres.TOB_BANK_AREA.contains(client.getLocalPlayer().getWorldLocation()))
        {
            if (!checkEquipment())
            {
                log.info("we're missing gear");
                EthanApiPlugin.sendClientMessage("MISSING GEAR! - HALT");
                EthanApiPlugin.stopPlugin(plugin);
                return;
            }

            var drakans = InventoryUtil.getItems().stream()
                    .filter(tele -> tele.getName().contains("Drakan's medallion"))
                    .findFirst().orElse(null);

            if (drakans != null)
            {
                InventoryInteraction.useItem(drakans, "Darkmeyer");
                plugin.timeout = 6;
                return;
            }

            return;
        }

        var distanceToDoor = client.getLocalPlayer().getWorldLocation().distanceTo(config.vyreType().getDoorWorldPoint());

        if (distanceToDoor > 6)
        {
            log.info("we need to walk to the door.");
            MovementPackets.queueMovement(config.vyreType().getDoorWorldPoint());
            plugin.timeout = 1;
            return;
        }

        if (distanceToDoor <= 6)
        {
            WorldPoint doorPoint = config.vyreType().getDoorWorldPoint();
            WorldPoint insidePoint = config.vyreType().getInsideWorldPoint();

            TileObjects.search().withName("Door").withAction("Open").nearestToPoint(doorPoint).ifPresent(door -> {
                if (door.getWorldLocation().distanceTo(doorPoint) <= 2)
                {
                    TileObjectInteraction.interact(door, "Open");
                    log.info("We clicked door {}", door.getId());
                    //plugin.timeout = 1;
                    return;
                }
            });

            TileObjects.search().withName("Door").withAction("Close").nearestToPoint(doorPoint).ifPresent(door -> {
                if (door.getWorldLocation().distanceTo(doorPoint) <= 2)
                {
                    log.info("Door is open.");
                    MovementPackets.queueMovement(config.vyreType().getInsideWorldPoint());
                    log.info("Walking inside house, door was already open.");
                    plugin.timeout = 1;
                    return;
                }
            });
        }

    }

    private boolean checkLocation()
    {
        var loc = config.vyreType().getNpcArea();

        for (WorldArea area : loc)
        {
            if (area.contains(client.getLocalPlayer().getWorldLocation()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isMoving() {
        return (client.getLocalPlayer().getPoseAnimation()
                == client.getLocalPlayer().getWalkAnimation()) || (client.getLocalPlayer().getPoseAnimation() == client.getLocalPlayer().getRunAnimation());
    }

    private boolean checkEquipment()
    {
        return !Equipment.search().nameContains("Vyre noble top").result().isEmpty()
                && !Equipment.search().nameContains("Vyre noble legs").result().isEmpty()
                && !Equipment.search().nameContains("Vyre noble shoes").result().isEmpty()
                && !Equipment.search().nameContains("Rogue mask").result().isEmpty()
                && !Equipment.search().nameContains("Rogue gloves").result().isEmpty()
                && !Equipment.search().nameContains("Dodgy necklace").result().isEmpty();


    }

    public boolean isAnimating() {
        return client.getLocalPlayer().getAnimation() != -1;
    }

}
