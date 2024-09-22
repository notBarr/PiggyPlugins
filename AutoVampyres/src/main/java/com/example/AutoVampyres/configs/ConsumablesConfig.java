package com.example.AutoVampyres.configs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("bVyres")
public interface ConsumablesConfig extends Config
{

    @ConfigSection(
            name = "Consumable Settings",
            description = "Food and gear",
            closedByDefault = true,
            position = 5
    )
    String ConsumablesSection = "Consumable Settings";

    @ConfigItem(
            keyName = "eat",
            name = "Should we eat?",
            section = ConsumablesSection,
            description = "Enable the use food",
            position = 5
    )
    default boolean shouldEat()
    {
        return false;
    }

    @ConfigItem(
            keyName = "foodName",
            name = "Food name",
            description = "",
            position = 6,
            section = ConsumablesSection,
            hidden = true
            //unhide = "eat"
    )
    default String foodName()
    {
        return "Jug of wine";
    }

    @ConfigItem(
            keyName = "foodAmount",
            name = "Food withdraw amount",
            description = "",
            section = ConsumablesSection,
            position = 7,
            hidden = true
            //unhide = "eat"
    )
    default int foodAmount()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "eatHp",
            name = "Eat at X",
            description = "",
            section = ConsumablesSection,
            position = 8,
            hidden = true
            //unhide = "eat"
    )
    default int eatHp()
    {
        return 11;
    }

    @ConfigItem(
            keyName = "dodgy",
            name = "Use dodgy necklaces?",
            section = ConsumablesSection,
            description = "Enable the use of Dodgy Necklace's",
            position = 9
    )
    default boolean shouldEquipDodgy()
    {
        return false;
    }

    @ConfigItem(
            keyName = "dodgyQuantity",
            name = "Quantity to withdraw",
            description = "",
            section = ConsumablesSection,
            position = 10,
            hidden = true
            //unhide = "dodgy"
    )
    default int necklaceQuantity()
    {
        return 3;
    }

//    @ConfigItem(
//            keyName = "junk",
//            name = "Items to drop",
//            section = ConsumablesSection,
//            description = "",
//            position = 11
//    )
//    default String junk()
//    {
//        return "Jug, Blood pint";
//    }

}
