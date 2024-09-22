package com.example.AutoVampyres.tasks;

import com.example.AutoVampyres.AutoVampyres;
import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.InteractionApi.InventoryInteraction;
import com.piggyplugins.PiggyUtils.API.InventoryUtil;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;

@Slf4j
public class InventorySpace extends AbstractTask<AutoVampyres, AutoVampyresConfig>
{
    Client client;
    AutoVampyresConfig config;

    public InventorySpace(AutoVampyres plugin, AutoVampyresConfig config)
    {
        super(plugin, config);
        this.client = plugin.getClient();
        this.config = plugin.getConfig();
    }

    @Override
    public boolean validate()
    {
        return (Inventory.getEmptySlots() < 2);
    }

    @Override
    public void execute()
    {

        //Inventory.search().withName("Jug").first().ifPresent(item -> InventoryInteraction.useItem(item, "Drop"));

        if (inventoryContains(ItemID.COOKED_MYSTERY_MEAT))
        {
            log.info("Inventory contains Cooked Mystery Meat");

           Inventory.search()
                   .withName("Cooked mystery meat")
                   .result()
                   .forEach(item -> InventoryInteraction.useItem(item, "Eat"));

           //plugin.timeout = 1;
           return;
        }

        if (inventoryContains(ItemID.BLOOD_PINT))
        {
            log.info("Inventory contains Blood Pint");

            Inventory.search()
                    .withName("Blood pint")
                    .result()
                    .forEach(item -> InventoryInteraction.useItem(item, "Drop"));

            plugin.timeout = 1;
            return;
        }

        if (inventoryContains(ItemID.JUG))
        {
            log.info("Inventory contains Jug");

            Inventory.search()
                    .withName("Jug")
                    .result()
                    .forEach(item -> InventoryInteraction.useItem(item, "Drop"));

            plugin.timeout = 1;
            return;
        }

        if (inventoryContains(ItemID.DIAMOND))
        {
            log.info("Inventory contains Diamond");

            Inventory.search()
                    .withName("Diamond")
                    .result()
                    .forEach(item -> InventoryInteraction.useItem(item, "Drop"));

            plugin.timeout = 1;
            return;
        }

        if (inventoryContains(ItemID.UNCUT_RUBY))
        {
            log.info("Inventory contains Uncut Ruby");

            Inventory.search()
                    .withName("Uncut ruby")
                    .result()
                    .forEach(item -> InventoryInteraction.useItem(item, "Drop"));

            plugin.timeout = 1;
            return;
        }

        if (inventoryContains(ItemID.DEATH_RUNE))
        {
            log.info("Inventory contains Death Runes");

            Inventory.search()
                    .withName("Death rune")
                    .result()
                    .forEach(item -> InventoryInteraction.useItem(item, "Drop"));

            plugin.timeout = 1;
            return;
        }

        var coinPouch = Inventory.search().nameContains("Coin pouch").first().orElse(null);
        if (coinPouch != null)
        {
            InventoryInteraction.useItem(coinPouch, "Open-all");
            plugin.timeout = 1;
            return;
        }

        if (inventoryContains(config.foodName()))
        {
            log.info("Inventory contains {}", config.foodName());

            Inventory.search()
                    .withName(config.foodName())
                    .result()
                    .forEach(item -> InventoryInteraction.useItem(item, "Eat", "Drink"));

            plugin.timeout = 1;
            return;
        }


    }

    private boolean isStunned()
    {
        return client.getLocalPlayer().getAnimation() == 245;
    }

    private boolean inventoryContains(String itemName)
    {
        return Inventory.getItemAmount(itemName) > 0;
    }

    private boolean inventoryContains(int itemID)
    {
        return Inventory.getItemAmount(itemID) > 0;
    }

}
