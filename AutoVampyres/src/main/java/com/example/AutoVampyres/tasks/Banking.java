package com.example.AutoVampyres.tasks;

import com.example.AutoVampyres.AutoVampyres;
import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.PlayerQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.piggyplugins.PiggyUtils.API.BankUtil;
import com.piggyplugins.PiggyUtils.API.EquipmentUtil;
import com.piggyplugins.PiggyUtils.API.InventoryUtil;
import com.piggyplugins.PiggyUtils.API.PlayerUtil;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Banking extends AbstractTask<AutoVampyres, AutoVampyresConfig>
{
    Client client;
    AutoVampyresConfig config;
    List<String> requiredItems;
    List<String> requiredGear;

    public Banking(AutoVampyres plugin, AutoVampyresConfig config)
    {
        super(plugin, config);
        this.client  = plugin.getClient();
        this.config = plugin.getConfig();
        requiredItems = new ArrayList<>(List.of("<col=ff9040>Rogue mask</col>", "<col=ff9040>Rogue gloves</col>", "<col=ff9040>Rogue boots</col>", "<col=ff9040>Rogue top</col>", "<col=ff9040>Rogue trousers</col>", "<col=ff9040>Drakan's medallion</col>", "<col=ff9040>Vyre noble top</col>", "<col=ff9040>Vyre noble legs</col>", "<col=ff9040>Vyre noble shoes</col>", "<col=ff9040>Dodgy necklace</col>", "<col=ff9040>" + config.foodName() + "</col>"));

    }

    @Override
    public boolean validate()
    {
        return client.getGameState() == GameState.LOGGED_IN
                && (Inventory.getItemAmount("Blood shard") > 0 && TileItems.search().nameContains("Blood shard").empty())
                || Inventory.getItemAmount(config.foodName()) <= 0
                || (AutoVampyres.TOB_BANK_AREA.contains(client.getLocalPlayer().getWorldLocation()) && plugin.getPlayerUtil().hp() < client.getRealSkillLevel(Skill.HITPOINTS))
                || (Inventory.getItemAmount("Dodgy necklace") <= 0 && config.shouldEquipDodgy())
                || (AutoVampyres.TOB_BANK_AREA.contains(client.getLocalPlayer().getWorldLocation()) && !EquipmentUtil.hasItem("Dodgy necklace"))
                || (AutoVampyres.TOB_BANK_AREA.contains(client.getLocalPlayer().getWorldLocation()) && Inventory.getItemAmount(config.foodName()) != config.foodAmount())
                || Bank.isOpen();
    }

    @Override
    public void execute()
    {

        if (plugin.hopWorld)
        {
            plugin.getWorldHopper().hopWorlds();
            plugin.setHopWorld(false);
            return;
        }

        plugin.setCurrentTask("BANK");

        var coinPouch = InventoryUtil.getItems().stream()
                .filter(coin -> coin.getName().contains("Coin pouch"))
                .findFirst().orElse(null);

        if (isMoving())
        {
            log.info("Player is moving, returning.");
            plugin.timeout = 1;
            return;
        }

        if (coinPouch != null)
        {
            if (Bank.isOpen())
            {
                log.info("Opening coin pouches with bank screen open.");
                BankInventoryInteraction.useItem(coinPouch, "Open-all");
                plugin.timeout = 1;
                return;
            }

            log.info("Opening coin pouches.");
            InventoryInteraction.useItem(coinPouch, "Open-all");
            plugin.timeout = 1;
            return;
        }

        client.runScript(101, -1);

        if (Bank.isOpen())
        {
            log.info("Bank interface is open.");
            depositAllExcept(requiredItems);

            if (config.shouldEat())
            {
                int currentFoodCount = Inventory.getItemAmount(config.foodName());
                int amountToWithdrawFood = config.foodAmount() - currentFoodCount;

                if (amountToWithdrawFood > 0)
                {
                    var foodInBank = BankUtil.nameContainsNoCase(config.foodName()).first();
                    foodInBank.ifPresent(food -> {
                        log.info("Withdrawing food: {} with quantity of: {}", food.getName(), amountToWithdrawFood);
                        MousePackets.queueClickPacket();
                        BankInteraction.withdrawX(food, amountToWithdrawFood);
                    });

                    plugin.timeout = 1;
                    return;
                }
            }

            if (config.shouldEquipDodgy())
            {
                int currentNecklaceCount = Inventory.getItemAmount("Dodgy necklace");
                int amountToWithdrawNecklace = config.necklaceQuantity() - currentNecklaceCount;

                if (amountToWithdrawNecklace > 0)
                {
                    var necklaceInBank = BankUtil.nameContainsNoCase("Dodgy necklace").first();
                    necklaceInBank.ifPresent(necklace -> {
                        log.info("Withdrawing necklace: {} with quantity of: {}", necklace.getName(), amountToWithdrawNecklace);
                        MousePackets.queueClickPacket();
                        BankInteraction.withdrawX(necklace, amountToWithdrawNecklace);
                    });

                    plugin.timeout = 1;
                    return;
                }
            }

            if (!checkGear())
            {
                List<Widget> inventoryItems = InventoryUtil.getItems().stream()
                        .filter(item -> item.getName().toLowerCase().contains("rogue") || item.getName().toLowerCase().contains("vyre"))
                        .collect(Collectors.toList());

                List<EquipmentItemWidget> equipmentItems = Equipment.search()
                        .filter(item -> item.getName().toLowerCase().contains("rogue") || item.getName().toLowerCase().contains("vyre"))
                        .result();

                List<String> filteredItemNames = new ArrayList<>();

                log.info("Inventory Size: {}", inventoryItems.size());
                log.info("Equipment Size: {}", equipmentItems.size());

                for (Widget item : inventoryItems)
                {
                    filteredItemNames.add(item.getName());
                    log.info("Inventory Name: {}", item.getName());
                }

                for (EquipmentItemWidget item : equipmentItems)
                {
                    filteredItemNames.add(item.getName());
                    log.info("Equipment Name: {}", item.getName());
                }

                for (String gear : requiredItems.subList(0, 9))
                {
                    if (filteredItemNames.contains(gear))
                    {
                        filteredItemNames.remove(gear);
                        continue;
                    }

                    log.warn("Missing {}, withdrawing from Bank.", gear);

                    var missingGear = BankUtil.nameContainsNoCase(gear.replaceAll("<[^>]*>", "")).first();
                    missingGear.ifPresent(item -> {
                        MousePackets.queueClickPacket();
                        BankInteraction.withdrawX(item, 1);
                    });
                }

                plugin.timeout = 1;
                return;
            }

            if (!EquipmentUtil.hasItem("Dodgy necklace") && InventoryUtil.hasItem("Dodgy necklace"))
            {
                InventoryUtil.getItems().stream()
                        .filter(item -> item.getName().toLowerCase().contains("dodgy necklace"))
                        .findFirst()
                        .ifPresent(item -> BankInventoryInteraction.useItem(item.getName(), "Wear"));

                plugin.timeout = 1;
                return;
            }

            if (!checkVyreEquipped())
            {
                InventoryUtil.getItems().stream()
                        .filter(item -> item.getName().toLowerCase().contains("vyre"))
                        .forEach(item -> InventoryInteraction.useItem(item, "Wear"));

                InventoryUtil.getItems().stream()
                        .filter(item -> item.getName().toLowerCase().contains("rogue mask") || item.getName().toLowerCase().contains("rogue gloves"))
                        .forEach(item -> InventoryInteraction.useItem(item, "Wear"));

                plugin.timeout = 2;
                return;
            }

            sendKey(KeyEvent.VK_ESCAPE);
            client.runScript(101, -1);
            return;
        }

        if (checkGear() && Inventory.getItemAmount(config.foodName()) == config.foodAmount()
                && Inventory.getItemAmount("Dodgy necklace") == config.necklaceQuantity()
                && Inventory.getItemAmount("Blood shard") == 0)
        {
            log.info("We have all equipment and items, teleporting to darkmeyer");
            equipVyre();

            var drakans = InventoryUtil.getItems().stream()
                    .filter(tele -> tele.getName().contains("Drakan's medallion"))
                    .findFirst().orElse(null);

            if (drakans != null)
            {
                InventoryInteraction.useItem(drakans, "Darkmeyer");
                plugin.timeout = 6;
                return;
            }
        }

        if (!AutoVampyres.TOB_BANK_AREA.contains(client.getLocalPlayer().getWorldLocation()))
        {
            log.info("We're not in the banking area, teleporting.");
            var drakans = InventoryUtil.getItems().stream()
                    .filter(tele -> tele.getName().contains("Drakan's medallion"))
                    .findFirst().orElse(null);

            if (drakans != null)
            {
                InventoryInteraction.useItem(drakans, "Ver Sinhaza");
                plugin.timeout = 6;
                return;
            }
        }

        if (!Bank.isOpen())
        {
            log.info("Opening Bank.");
            findBank();
            plugin.timeout = 1;
        }

        return;
    }

    public boolean isMoving() {
        return (client.getLocalPlayer().getPoseAnimation()
                == client.getLocalPlayer().getWalkAnimation()) || (client.getLocalPlayer().getPoseAnimation() == client.getLocalPlayer().getRunAnimation());
    }

    private void findBank() {
        Optional<NPC> banker = NPCs.search().withAction("Bank").nearestToPlayer();
        Optional<TileObject> bank = TileObjects.search().withAction("Bank").nearestToPlayer();
        if (!Bank.isOpen()) {
            if (banker.isPresent()) {
                interactNpc(banker.get(), "Bank");
                plugin.timeout = 2;
                //plugin.timeout = config.tickDelay() == 0 ? 1 : config.tickDelay();
            } else if (bank.isPresent()) {
                interactObject(bank.get(), "Bank");
                plugin.timeout = 2;
                //plugin.timeout = config.tickDelay() == 0 ? 1 : config.tickDelay();
            } else {
                EthanApiPlugin.sendClientMessage("Couldn't find bank or banker");
                EthanApiPlugin.stopPlugin(plugin);
            }
        }
    }

    public void depositAllExcept(List<String> itemNames)
    {
        List<Widget> truncatedItems;

        var listOfItems = BankInventory.search()
                .filter(item -> !itemNames.contains(item.getName()))
                .result();

        if (listOfItems.size() > 10)
        {
            truncatedItems = new ArrayList<>(listOfItems.subList(0, 10));

            truncatedItems.forEach(item -> {
                if (!item.getName().contains(config.foodName()))
                {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(item, "Deposit-All");
                    log.info("More than 10 item(s) found, depositing: {}", item.getName());
                }
            });

            plugin.timeout = 2;
            return;
        }

        listOfItems.forEach(item -> {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(item, "Deposit-All");
            log.info("Depositing {} unwanted item(s): {}", listOfItems.size(), item.getName());
            plugin.timeout = 2;
        });

        return;
    }

    private void equipVyre()
    {
        Inventory.search().nameContains("vyre")
                .result().forEach(item -> {
                    InventoryInteraction.useItem(item, "Wear");
                });
    }

    private boolean checkGear()
    {
        return (EquipmentUtil.hasItem(5556) || InventoryUtil.hasItem(5556))
                && (EquipmentUtil.hasItem(5554) || InventoryUtil.hasItem(5554))
                && (EquipmentUtil.hasItem(5557) || InventoryUtil.hasItem(5557))
                && (EquipmentUtil.hasItem(5553) || InventoryUtil.hasItem(5553))
                && (EquipmentUtil.hasItem(5555) || InventoryUtil.hasItem(5555))
                && (EquipmentUtil.hasItem(24676) || InventoryUtil.hasItem(24676))
                && (EquipmentUtil.hasItem(24678) || InventoryUtil.hasItem(24678))
                && (EquipmentUtil.hasItem(24680) || InventoryUtil.hasItem(24680))
                && InventoryUtil.hasItem(22400);
    }

    private boolean checkVyreEquipped()
    {
        return !(InventoryUtil.hasItem("Vyre noble top")
                && InventoryUtil.hasItem("Vyre noble legs")
                && InventoryUtil.hasItem("Vyre noble shoes")
                && InventoryUtil.hasItem("Rogue mask")
                && InventoryUtil.hasItem("Rogue gloves"))

                && (EquipmentUtil.hasItem("Vyre noble top")
                && EquipmentUtil.hasItem("Vyre noble legs")
                && EquipmentUtil.hasItem("Vyre noble shoes")
                && EquipmentUtil.hasItem("Rogue mask")
                && EquipmentUtil.hasItem("Rogue gloves"));

    }

    private void sendKey(int key) {
        keyEvent(KeyEvent.KEY_PRESSED, key);
        keyEvent(KeyEvent.KEY_RELEASED, key);
    }

    private void keyEvent(int id, int key) {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, key, KeyEvent.CHAR_UNDEFINED
        );

        client.getCanvas().dispatchEvent(e);
    }
}
