package com.example.AutoVampyres;

import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;


public class AutoVampyresOverlay extends Overlay {
    private final PanelComponent panelComponent = new PanelComponent();
    private final Client client;
    private final AutoVampyres plugin;

    @Inject
    private AutoVampyresOverlay(Client client, AutoVampyres plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if (!plugin.getStarted())
        {
            return null;
        }

        panelComponent.getChildren().clear();

        Duration runtime = Duration.between(plugin.getStartTime(), Instant.now());
        String overlayTitle = "[Barr] Auto Vampyres";

        panelComponent.getChildren()
                .add(TitleComponent.builder()
                .text(overlayTitle)
                .color(Color.ORANGE).build());

        panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(overlayTitle) + 120, 0));

        panelComponent.getChildren()
                .add(TitleComponent.builder()
                .text(plugin.getStarted() ? "Running" : "Paused")
                .color(plugin.getStarted() ? Color.GREEN : Color.RED)
                .build());

        if (runtime != null) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Runtime: " + (plugin.getStarted() ? formatDuration(runtime) : "0:00:00"))
                    .build());
        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Current Task:")
                .right(plugin.getCurrentTask())
                .rightColor(Color.GREEN)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Delay:")
                .right(String.valueOf(plugin.timeout))
                .rightColor(Color.ORANGE)
                .build());

        panelComponent.setBackgroundColor(new Color(30, 30, 30, 175));

        return panelComponent.render(graphics);
    }

    /**
     * Builds a line component with the given left and right text
     *
     * @param left
     * @param right
     * @return Returns a built line component with White left text and Yellow right text
     */
    private LineComponent buildLine(String left, String right) {
        return LineComponent.builder()
                .left(left)
                .right(right)
                .leftColor(Color.WHITE)
                .rightColor(Color.YELLOW)
                .build();
    }


    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
