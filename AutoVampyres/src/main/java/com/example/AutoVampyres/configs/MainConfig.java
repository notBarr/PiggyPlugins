package com.example.AutoVampyres.configs;

import com.example.AutoVampyres.data.Vampyres;
import net.runelite.client.config.*;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bVyres")
public interface MainConfig extends Config, ConsumablesConfig
{
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = "",
            position = 0
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "vyreType",
            name = "Vyre to pickpocket",
            description = "Which vyre are we stealing from, location is different",
            position = 1
    )
    default Vampyres vyreType() {
        return Vampyres.NAKASA;
    }



}