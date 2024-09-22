package com.example.AutoVampyres.tasks;

import com.example.AutoVampyres.AutoVampyres;
import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.example.EthanApiPlugin.Collections.Bank;
import com.example.EthanApiPlugin.Collections.BankInventory;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import net.runelite.api.Client;
import net.runelite.api.Skill;

public class Eat extends AbstractTask<AutoVampyres, AutoVampyresConfig>
{
    Client client;
    AutoVampyresConfig config;


    public Eat(AutoVampyres plugin, AutoVampyresConfig config)
    {
        super(plugin, config);
        this.client = plugin.getClient();
        this.config = plugin.getConfig();
    }

    @Override
    public boolean validate()
    {
        return (plugin.getPlayerUtil().hp() < config.eatHp())
                && (Inventory.search().withAction("Eat").first().isPresent() || Inventory.search().withAction("Drink").first().isPresent());
    }

    @Override
    public void execute()
    {
        plugin.setCurrentTask("EAT");
        if (Bank.isOpen())
        {
            BankInventory.search().withAction("Eat").first().ifPresent(item -> BankInventoryInteraction.useItem(item, "Eat"));
            BankInventory.search().withAction("Drink").nameContains("wine").first().ifPresent(item -> BankInventoryInteraction.useItem(item, "Drink"));
            plugin.timeout = 1;
            return;
        }

        Inventory.search().withAction("Eat").first().ifPresent(item -> InventoryInteraction.useItem(item, "Eat"));
        Inventory.search().withAction("Drink").nameContains("wine").first().ifPresent(item -> InventoryInteraction.useItem(item, "Drink"));
        plugin.timeout = 1;
        return;

    }
}
