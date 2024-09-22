package com.example.AutoVampyres.tasks;

import com.example.AutoVampyres.AutoVampyres;
import com.example.AutoVampyres.configs.AutoVampyresConfig;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import net.runelite.api.Client;

public class Break extends AbstractTask<AutoVampyres, AutoVampyresConfig>
{
    Client client;
    AutoVampyresConfig config;

    public Break(AutoVampyres plugin, AutoVampyresConfig config)
    {
        super(plugin, config);
        this.client = plugin.getClient();
        this.config = config;
    }


    @Override
    public boolean validate()
    {
        return plugin.getBreakHandler().shouldBreak(plugin);
    }

    @Override
    public void execute()
    {
        plugin.setCurrentTask("BREAKING");
        plugin.getBreakHandler().startBreak(plugin);
    }
}
