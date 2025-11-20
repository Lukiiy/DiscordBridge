package me.lukiiy.discordBridge.mixin;

import me.lukiiy.discordBridge.DSerialMoj;
import me.lukiiy.discordBridge.DiscordBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(ServerGamePacketListenerImpl.class)
public class GamePacketImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    public void discordBridge$quit(Component component, CallbackInfo ci) {
        DiscordBridge.getInstance().sendNeutral(DSerialMoj.toDiscord(Component.translatable("multiplayer.player.left", player.getDisplayName()).withStyle(ChatFormatting.YELLOW)), false);
    }

    @Inject(method = "broadcastChatMessage", at = @At("TAIL"))
    public void discordBridge$chat(PlayerChatMessage playerChatMessage, CallbackInfo ci) {
        Component formatted = ChatType.bind(ChatType.CHAT, player).decorate(playerChatMessage.decoratedContent());

        DiscordBridge.getInstance().sendNeutral(DSerialMoj.toDiscord(formatted), true);
    }

}
