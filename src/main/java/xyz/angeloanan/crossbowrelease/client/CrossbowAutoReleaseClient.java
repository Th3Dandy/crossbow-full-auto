package xyz.angeloanan.crossbowrelease.client;

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
import xyz.angeloanan.crossbowrelease.CrossbowAutoRelease;

public class CrossbowAutoReleaseClient implements ClientModInitializer {
    private int currentTick = 0;
    private int chargeTime = 0;
    private boolean isCharging = false;
    private boolean shouldIgnoreRightClick = false;

    @Override
    public void onInitializeClient() {
        CrossbowAutoRelease.LOGGER.info("Crossbow Auto Release initializing...");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack handItem = player.getStackInHand(hand);

            // CASE: After crossbow is fully charge and code has "released" the charge and player is still holding right-click
            //       This is to prevent the crossbow from rapid firing
            if (shouldIgnoreRightClick) return TypedActionResult.fail(player.getStackInHand(hand));

            // CASE: Player is not holding a crossbow
            if (!handItem.getItem().equals(Items.CROSSBOW)) return TypedActionResult.pass(handItem);

            // CASE: Crossbow is already fully charged
            if (CrossbowItem.isCharged(handItem)) return TypedActionResult.pass(handItem);

            int pullTime = CrossbowItem.getPullTime(handItem);
            CrossbowAutoRelease.LOGGER.debug("Current crossbow pull time: " + pullTime);

            chargeTime = pullTime;
            currentTick = 0;
            isCharging = true;

            return TypedActionResult.pass(handItem);
        });
        CrossbowAutoRelease.LOGGER.debug("Crossbow Auto Release is now listening on item use events!");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (shouldIgnoreRightClick && !client.mouse.wasRightButtonClicked()) {
                shouldIgnoreRightClick = false;
                return;
            }

            if (!isCharging) return;

            if (!(client.player != null && client.player.isUsingItem())) {
                isCharging = false;
                return;
            }

            if (currentTick >= chargeTime) {
                CrossbowAutoRelease.LOGGER.debug("Crossbow fully charged!");

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
            CrossbowAutoRelease.LOGGER.trace("Current tick: " + currentTick);
        });
        CrossbowAutoRelease.LOGGER.debug("Crossbow Auto Release is now running fn on every client tick!");

        CrossbowAutoRelease.LOGGER.info("Crossbow Auto Release initialized!");
    }
}
