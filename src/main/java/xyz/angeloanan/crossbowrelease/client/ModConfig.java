package xyz.angeloanan.crossbowrelease.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CrossbowAutoReleaseClient.MOD_ID)
public class ModConfig implements ConfigData {
    @Comment("""
How much tick to add to the crossbow charge time.
This is to compensate for server ping.

Increase if your crossbow often fails to charge.
Decrease if your crossbow charges too slow. You shouldn't go below -1.""")
    public int compensation = 0;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    SoundFeedback sound = new SoundFeedback();

    public static class SoundFeedback {
        @Comment("Enable sound feedback when crossbow is fully charged.")
        boolean enabled = true;

        @Comment("Volume of the sound feedback.")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
        int volume = 5;

        @Comment("Pitch of the sound feedback.")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
        int pitch = 20;
    }
}
