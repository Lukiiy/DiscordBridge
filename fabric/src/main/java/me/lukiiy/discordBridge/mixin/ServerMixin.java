package me.lukiiy.discordBridge.mixin;

import me.lukiiy.discordBridge.DiscordBridge;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerMixin {
    @Inject(method = "loadLevel", at = @At("TAIL"))
    public void discordBridge$start(CallbackInfo ci) {
        DiscordBridge.getInstance().initBot((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void discordBridge$stop(CallbackInfo ci) {
        DiscordBridge.getInstance().shutdown();
    }
}
