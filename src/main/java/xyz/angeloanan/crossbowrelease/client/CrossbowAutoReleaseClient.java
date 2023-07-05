package xyz.angeloanan.crossbowrelease.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
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
    private int chargeTime = 0;
    private boolean isCharging = false;
    private boolean shouldIgnoreRightClick = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Crossbow Auto Release initializing...");

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack handItem = player.getStackInHand(hand);

            // CASE: After crossbow is fully charge and code has "released" the charge and player is still holding right-click
            //       This is to prevent the crossbow from rapid firing
            if (shouldIgnoreRightClick) return TypedActionResult.fail(player.getStackInHand(hand));

            // CASE: Player is not holding a crossbow
            if (!handItem.getItem().equals(Items.CROSSBOW)) return TypedActionResult.pass(handItem);

            // CASE: Crossbow is already fully charged and is going to be fired
            if (CrossbowItem.isCharged(handItem)) return TypedActionResult.pass(handItem);

            int pullTime = CrossbowItem.getPullTime(handItem);
            LOGGER.debug("Current crossbow pull time: " + pullTime);
            LOGGER.debug("Compensation: " + config.compensation);

            chargeTime = pullTime + config.compensation;
            currentTick = 0;
            isCharging = true;

            return TypedActionResult.pass(handItem);
        });
        LOGGER.debug("Crossbow Auto Release is now listening on item use events!");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // Reset ignoreRightClick state if player stops holding right-click
            if (shouldIgnoreRightClick && !client.mouse.wasRightButtonClicked()) {
                shouldIgnoreRightClick = false;
                return;
            }

            // CASE: Crossbow is not being charged
            if (!isCharging) return;

            // CASE: Player is not holding a crossbow
            if (!(client.player != null && client.player.isUsingItem())) {
                isCharging = false;
                return;
            }

            // CASE: Crossbow is already fully charged
            if (currentTick >= chargeTime) {
                LOGGER.debug("Crossbow fully charged!");

                // Reset charging state
                isCharging = false;
                currentTick = 0;
                chargeTime = 0;

                // Visually release the crossbow
                client.player.stopUsingItem();

                // Send crossbow release packet
                client.interactionManager.stopUsingItem(client.player);

                // Play XP sound on master channel
                SoundEvent sound = SoundEvents.UI_BUTTON_CLICK.value();
                client.player.playSound(sound, SoundCategory.MASTER, 0.5F, 2.0F);

                // Ignore right click until player releases right click
                // If this is false, crossbow rapid fire go brrrrr
                shouldIgnoreRightClick = true;

                return;
            }

            currentTick += 1;
            LOGGER.trace("Current tick: " + currentTick);
        });
        LOGGER.debug("Crossbow Auto Release is now running fn on every client tick!");

        LOGGER.info("Crossbow Auto Release initialized!");
    }
}
