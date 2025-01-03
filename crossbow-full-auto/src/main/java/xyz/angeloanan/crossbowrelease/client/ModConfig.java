// src/main/java/xyz/angeloanan/crossbowrelease/client/ModConfig.java
package xyz.angeloanan.crossbowrelease.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CrossbowAutoReleaseClient.MOD_ID)
public class ModConfig implements ConfigData {
    @Comment("Enable or disable the Crossbow Auto Release mod.")
    public boolean enabled = true;

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