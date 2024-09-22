package com.example.AutoVampyres.data;

import lombok.Getter;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

@Getter
public enum Vampyres
{
    NAKASA("Nakasa Jovkai",
            List.of(new WorldArea(3608, 3322, 5, 7, 0)),
            new WorldPoint(3607, 3323, 0),
            new WorldPoint(3608, 3323, 0),
            new WorldPoint(3611, 3323, 1)),

    VLAD_D("Vald Diaemus",
            List.of(new WorldArea(3608, 3392, 6, 5, 0)), //house
            new WorldPoint(3610, 3391, 0), //door
            new WorldPoint(3610, 3392, 0)), //inside, otherside of door

    PIPI("Pipistrelle Draynar",
            List.of(new WorldArea(3606, 3381, 7, 7, 0)),
            new WorldPoint(3609, 3388, 0),
            new WorldPoint(3609, 3387, 0)),

    CANI("Caninelle Draynar",
            List.of(new WorldArea(3606, 3381, 7, 7, 0)),
            new WorldPoint(3609, 3388, 0),
            new WorldPoint(3609, 3387, 0)),

    VLAD_B("Vlad Bechstein",
            List.of(new WorldArea(3640, 3376, 8, 5, 0)),
            new WorldPoint(3645, 3381, 0),
            new WorldPoint(3645, 3380, 0)),

    DIPH("Diphylla Bechstein",
            List.of(new WorldArea(3640, 3376, 8, 5, 0)),
            new WorldPoint(3645, 3381, 0),
            new WorldPoint(3645, 3380, 0)),

    DRACONIS("Draconis Sanguine",
            List.of(new WorldArea(3626, 3378, 7, 7, 0), new WorldArea(3632, 3378, 5, 3, 0)),
            new WorldPoint(3633, 3383, 0),
            new WorldPoint(3632, 3383, 0)),

    VIOLETTA("Violetta Sanguine",
            List.of(new WorldArea(3626, 3378, 7, 7, 0), new WorldArea(3632, 3378, 5, 3, 0)),
            new WorldPoint(3633, 3383, 0),
            new WorldPoint(3632, 3383, 0)),

    VONNETTA("Vonnetta Varnis",
            List.of(new WorldArea(3640, 3384, 8, 6, 0)),
            new WorldPoint(3642, 3383, 0),
            new WorldPoint(3642, 3385, 0)),

    CRIMSONETTE("Crimsonette van Marr",
            List.of(new WorldArea(3594, 3387, 9, 10, 0), new WorldArea(3602, 3391, 4, 6, 0)),
            new WorldPoint(3603, 3389, 0),
            new WorldPoint(3601, 3389, 0)),

    GRIGOR("Grigor Rasputin",
            List.of(new WorldArea(3594, 3387, 9, 10, 0), new WorldArea(3602, 3391, 4, 6, 0)),
            new WorldPoint(3603, 3389, 0),
            new WorldPoint(3602, 3389, 0)),

    VORMAR("Vormar Vakan",
            List.of(new WorldArea(3594, 3387, 9, 10, 0), new WorldArea(3602, 3391, 4, 6, 0)),
            new WorldPoint(3603, 3389, 0),
            new WorldPoint(3602, 3389, 0));

    private final String npcName;
    private final List<WorldArea> npcArea;
    private final WorldPoint doorWorldPoint;
    private final WorldPoint insideWorldPoint;
    private final WorldPoint upstairsWorldPoint;

    Vampyres(String npcName, List<WorldArea> npcArea, WorldPoint doorWorldPoint, WorldPoint insideWorldPoint, WorldPoint upstairsWorldPoint)
    {
        this.npcName = npcName;
        this.npcArea = npcArea;
        this.doorWorldPoint = doorWorldPoint;
        this.insideWorldPoint = insideWorldPoint;
        this.upstairsWorldPoint = upstairsWorldPoint;

    }

    Vampyres(String npcName, List<WorldArea> npcArea, WorldPoint doorWorldPoint, WorldPoint insideWorldPoint)
    {
        this.npcName = npcName;
        this.npcArea = npcArea;
        this.doorWorldPoint = doorWorldPoint;
        this.insideWorldPoint = insideWorldPoint;
        this.upstairsWorldPoint = null;
    }

    public boolean hasUpstairsWorldPoint()
    {
        return this.upstairsWorldPoint != null;
    }

}
