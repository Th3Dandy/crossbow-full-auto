package xyz.angeloanan.crossbowrelease.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CrossbowAutoReleaseClient.MOD_ID)
public class ModConfig implements ConfigData {
    @Comment("""
How much tick to add to the crossbow charge time.
This is to compensate for server ping.

Increase if your crossbow often fails to charge.
Decrease if your crossbow charges too slow. You shouldn't go below -1.""")
    int compensation = 0;
}
