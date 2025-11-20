package me.lukiiy.discordBridge.mixin;

import me.lukiiy.discordBridge.DSerialMoj;
import me.lukiiy.discordBridge.DiscordBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void discordBridge$death(DamageSource damageSource, CallbackInfo ci, boolean bl, Component component, Team team) {
        DiscordBridge.getInstance().sendNeutral(DSerialMoj.toDiscord(component), false);
    }
}
