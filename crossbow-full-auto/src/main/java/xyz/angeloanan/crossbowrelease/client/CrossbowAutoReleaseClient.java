// src/main/java/xyz/angeloanan/crossbowrelease/client/CrossbowAutoReleaseClient.java
package xyz.angeloanan.crossbowrelease.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossbowAutoReleaseClient implements ClientModInitializer {
    public static final String MOD_ID = "crossbow_release";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ModConfig config;

    private int currentTick = 0;
    private int chargeTime = 10; // Base charge time of 0.5 seconds (10 ticks)
    private boolean isCharging = false;
    private int pingCompensation = 0;
    private int pingFetchTick = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Crossbow Auto Release initializing...");

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if (!config.enabled) {
            LOGGER.info("Crossbow Auto Release is disabled via configuration.");
            return;
        }

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack handItem = player.getStackInHand(hand);

            if (!handItem.getItem().equals(Items.CROSSBOW)) return TypedActionResult.pass(handItem);
            if (CrossbowItem.isCharged(handItem)) return TypedActionResult.pass(handItem);

            currentTick = 0;
            isCharging = true;

            return TypedActionResult.pass(handItem);
        });
        LOGGER.debug("Crossbow Auto Release is now listening on item use events!");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (pingFetchTick >= 10) { // Fetch ping every 0.5 seconds (10 ticks)
                pingFetchTick = 0;
                pingCompensation = getPingCompensation(client.getNetworkHandler());
                LOGGER.debug("Ping compensation: " + pingCompensation + " ticks");
            } else {
                pingFetchTick++;
            }

            if (!isCharging) return;
            if (!(client.player != null && client.player.isUsingItem())) {
                isCharging = false;
                return;
            }

            ItemStack handItem = client.player.getActiveItem();
            if (!handItem.getItem().equals(Items.CROSSBOW)) {
                isCharging = false;
                return;
            }

            if (currentTick >= chargeTime + pingCompensation) {
                LOGGER.debug("Crossbow fully charged!");

                isCharging = false;
                currentTick = 0;

                client.player.stopUsingItem();
                client.interactionManager.stopUsingItem(client.player);

                if (config.sound.enabled) {
                    SoundEvent sound = SoundEvents.UI_BUTTON_CLICK.value();
                    float volume = config.sound.volume / 10.0F;
                    float pitch = config.sound.pitch / 10.0F;

                    client.player.playSound(sound, volume, pitch);
                }

                // Automatically reload the crossbow
                client.player.setCurrentHand(client.player.getActiveHand());
                isCharging = true;

                return;
            }

            currentTick += 1;
            LOGGER.trace("Current tick: " + currentTick);
        });
        LOGGER.debug("Crossbow Auto Release is now running fn on every client tick!");

        LOGGER.info("Crossbow Auto Release initialized!");
    }

    private int getPingCompensation(ClientPlayNetworkHandler networkHandler) {
        if (networkHandler == null) return 0;
        int pingMs = networkHandler.getPlayerListEntry(networkHandler.getProfile().getId()).getLatency();
        int roundedPingMs = (pingMs + 25) / 50 * 50; // Round to the nearest 50ms
        return roundedPingMs / 50; // Convert to ticks (50ms per tick)
    }
}