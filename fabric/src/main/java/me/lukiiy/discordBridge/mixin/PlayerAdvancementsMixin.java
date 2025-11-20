package me.lukiiy.discordBridge.mixin;

import me.lukiiy.discordBridge.DSerialMoj;
import me.lukiiy.discordBridge.DiscordBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.SERVER)
@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", shift = At.Shift.AFTER))
    public void discordBridge$advancement(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {
        DisplayInfo display = advancement.getDisplay();
        if (display == null) return;

        DiscordBridge.getInstance().sendNeutral(DSerialMoj.toDiscord(Component.translatable("chat.type.advancement." + display.getFrame().getName(), player.getDisplayName(), advancement.getChatComponent())), false);
    }
}
