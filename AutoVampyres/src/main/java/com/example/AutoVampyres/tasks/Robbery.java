package com.example.AutoVampyres.tasks;

import com.example.AutoVampyres.AutoVampyres;
import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.example.AutoVampyres.data.WorldHopper;
import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.TileItemPackets;
import com.piggyplugins.PiggyUtils.API.InventoryUtil;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Robbery extends AbstractTask<AutoVampyres, AutoVampyresConfig>
{
    private static final Logger log = LoggerFactory.getLogger(Robbery.class);
    Client client;
    AutoVampyresConfig config;
    List<String> status = new ArrayList<>(List.of("MOST HONEST TURK(ROACH)", "JEWISH MORAL ACTION", "SERBIAN PATRIOTIC ACTS", "MOST HONEST BOSNIAN", "13% -> 50%",
                "SWISS NEUTRAL PRECAUTIONS", "BELGIAN BLACK HAND ACTS", "FRENCH PEOPLE ARENT WHITE", "ITALIANS ARE BLACKS"));
    Random random;

    public Robbery(AutoVampyres plugin, AutoVampyresConfig config)
    {
        super(plugin, config);
        this.client  = plugin.getClient();
        this.config = plugin.getConfig();
        random = new Random();
    }

    @Override
    public boolean validate()
    {
        return checkLocation();
    }

    @Override
    public void execute()
    {
        plugin.setCurrentTask(status.get(random.nextInt(status.size()+1)));

        if (escape())
        {
            log.info("Interaction with Vyrewatch Sentinel detected, leaving.");

            plugin.timeout = 1;
            return;
        }

        if (isMoving() || isAnimating())
        {
            plugin.timeout = 1;
            return;
        }

        if (isStunned())
        {
            log.info("Stunned, sleep 1 tick.");
            plugin.timeout = 1;
            return;
        }

        if (isPlayerPresent())
        {
            plugin.getWorldHopper().hopWorlds();
            log.info("Player is present, world hopping.");
            plugin.timeout = 2;
            return;
        }

        if (enemyInArea())
        {
            log.info("Enemy vyre in area, handling.");
            equipVyre();
            teleportToBank();
            plugin.setHopWorld(true);
            plugin.timeout = 1;
            return;
        }

        if (!isDoorClosed() && (isTargetAvailable() != null))
        {
            log.info("Door not closed, target is in building - closing.");
            equipVyre();
            closeDoor();
            plugin.timeout = 2;
            return;
        }

        TileItems.search()
                .withName("Blood shard")
                .nearestToPoint(client.getLocalPlayer().getWorldLocation())
                .ifPresent(shards -> {
                    if (Inventory.full())
                    {
                        Inventory.search().withAction("Eat").first().ifPresent(food -> InventoryInteraction.useItem(food, "Eat"));
                        Inventory.search().withAction("Drink").first().ifPresent(food -> InventoryInteraction.useItem(food, "Eat"));
                        return;
                    } else
                    {
                        MousePackets.queueClickPacket();
                        TileItemPackets.queueTileItemAction(shards, false);
                        return;
                    }
                });

        if (!enemyInArea())
        {
            if (!hasNecklace() && inventoryContains(ItemID.DODGY_NECKLACE))
            {
                log.info("Don't have necklace equipped.");
                Inventory.search().nameContains("Dodgy necklace")
                        .result()
                        .stream()
                        .findFirst()
                        .ifPresent(item -> InventoryInteraction.useItem(item, "Wear"));
            }

            Inventory.search().nameContains("Coin pouch").first().ifPresent(item -> {

                if (item.getItemQuantity() >= 27)
                {
                    InventoryInteraction.useItem(item, "Open-all");
                    plugin.timeout = 1;
                    return;
                }

                if (item.getItemQuantity() > 10 && random.nextBoolean() && random.nextBoolean() && random.nextBoolean())
                {
                    InventoryInteraction.useItem(item, "Open-all");
                    plugin.timeout = 1;
                    return;
                }

                if (item.getItemQuantity() > 15 && random.nextBoolean() && random.nextBoolean())
                {
                    InventoryInteraction.useItem(item, "Open-all");
                    plugin.timeout = 1;
                    return;
                }

                if (item.getItemQuantity() > 20 && random.nextBoolean())
                {
                    InventoryInteraction.useItem(item, "Open-all");
                    plugin.timeout = 1;
                    return;
                }

            });

            if (isDoorClosed() && (isTargetAvailable() != null))
            {
                log.info("Door is closed and we have target available.");

                if (!isWearingRogues())
                {
                    equipRogues();
                    plugin.timeout = 2;
                }

                NPCInteraction.interact(isTargetAvailable(), "Pickpocket");

                plugin.timeout = 1;
                return;
            }

        }

    }

    private boolean inventoryContains(int itemID)
    {
        return Inventory.getItemAmount(itemID) > 0;
    }

    private boolean checkLocation()
    {
        var loc = config.vyreType().getNpcArea();

        for (WorldArea area : loc)
        {
            if (area.contains(client.getLocalPlayer().getWorldLocation()))
            {
                log.info("we're in target house.");
                return true;
            }
        }
        return false;
    }

    private void equipVyre()
    {
        Inventory.search().nameContains("vyre")
                .result().forEach(item -> {
                    InventoryInteraction.useItem(item, "Wear");
                });
    }

    private boolean isWearingRogues()
    {
        return Equipment.search().nameContainsNoCase("rogue").result().size() >= 5;
    }

    private boolean hasNecklace()
    {
        return !Equipment.search().nameContains("Dodgy necklace").result().isEmpty();
    }

    private void equipRogues()
    {
        Inventory.search().nameContains("Rogue")
                .result().forEach(item -> {
                    InventoryInteraction.useItem(item, "Wear");
                });
    }

    private boolean enemyInArea()
    {
       var loc = config.vyreType().getNpcArea();

       return !NPCs.search()
               .filter(npc -> npc.getName().toLowerCase().contains("vyrewatch sentinel"))
               .filter(npc -> loc.stream().anyMatch(area -> area.contains(npc.getWorldLocation())))
               .empty();
    }

    private NPC isTargetAvailable()
    {
        var loc = config.vyreType().getNpcArea();

        return NPCs.search()
                .filter(npc -> loc.stream().anyMatch(area -> area.contains(npc.getWorldLocation())))
                .withAction("Pickpocket")
                .first()
                .orElse(null);

    }

    private boolean escape()
    {
        if (!NPCs.search().interactingWith(client.getLocalPlayer()).nameContains("Vyrewatch Sentinel").empty())
        {
            teleportToBank();
            log.warn("Drakan's medallion is null! We will most likely die.");
            EthanApiPlugin.sendClientMessage("Teleport out cannot be found, we can die!");

            return true;
        }
        return false;
    }

    private void teleportToBank()
    {
        var drakans = InventoryUtil.getItems().stream()
                .filter(tele -> tele.getName().contains("Drakan's medallion"))
                .findFirst().orElse(null);

        if (drakans != null)
        {
            InventoryInteraction.useItem(drakans, "Ver Sinhaza");
            plugin.timeout = 4;
        }
    }

    private boolean isMoving() {
        return (client.getLocalPlayer().getPoseAnimation()
                == client.getLocalPlayer().getWalkAnimation()) || (client.getLocalPlayer().getPoseAnimation() == client.getLocalPlayer().getRunAnimation());
    }

    private boolean isAnimating() {
        return client.getLocalPlayer().getAnimation() != -1;
    }

    private boolean isStunned()
    {
        return client.getLocalPlayer().getAnimation() == 245;
    }

    private boolean isPlayerPresent()
    {
        var loc = config.vyreType().getNpcArea();

        return Players.search()
                .filter(player -> player != client.getLocalPlayer())
                .filter(player -> loc.stream().anyMatch(area -> area.contains(player.getWorldLocation())))
                .first().isPresent();

    }

    private boolean isDoorClosed()
    {
        WorldPoint doorPoint = config.vyreType().getDoorWorldPoint();
        WorldPoint insidePoint = config.vyreType().getInsideWorldPoint();
        AtomicBoolean result = new AtomicBoolean(false);

        TileObjects.search().withName("Door").withAction("Open").nearestToPoint(doorPoint).ifPresent(door -> {
            if (door.getWorldLocation().distanceTo(doorPoint) <= 2)
            {
                //TileObjectInteraction.interact(door, "Open");
                log.info("door {} is CLOSED at {}", door.getId(), door.getWorldLocation());
                plugin.timeout = 4;
                result.set(true);
                return;
            }
        });

        TileObjects.search().withName("Door").withAction("Close").nearestToPoint(doorPoint).ifPresent(door -> {
            if (door.getWorldLocation().distanceTo(doorPoint) <= 2)
            {
                log.info("door {} is OPEN at {}", door.getId(), door.getWorldLocation());
                plugin.timeout = 2;
                result.set(false);
                return;
            }
        });

        return result.get();
    }

    private void closeDoor()
    {
        WorldPoint doorPoint = config.vyreType().getDoorWorldPoint();

        TileObjects.search().withName("Door").withAction("Close").nearestToPoint(doorPoint).ifPresent(door -> {
            log.info("Door is open.");
            TileObjectInteraction.interact(door, "Close");
            log.info("We clicked door {}", door.getId());
            plugin.timeout = 2;
            return;
        });
    }
}
