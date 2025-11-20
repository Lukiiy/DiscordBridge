package me.lukiiy.discordBridge.mixin;

import com.mojang.authlib.GameProfile;
import me.lukiiy.discordBridge.DSerialMoj;
import me.lukiiy.discordBridge.DiscordBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Environment(EnvType.SERVER)
@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void discordBridge$join(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci, GameProfile gameProfile, GameProfileCache gameProfileCache, Optional optional, String string, CompoundTag compoundTag, ResourceKey resourceKey, ServerLevel serverLevel, ServerLevel serverLevel2, String string2, LevelData levelData, ServerGamePacketListenerImpl serverGamePacketListenerImpl, GameRules gameRules, boolean bl, boolean bl2, MutableComponent mutableComponent) {
        DiscordBridge.getInstance().sendNeutral(DSerialMoj.toDiscord(mutableComponent), false);
    }
}
