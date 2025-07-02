package ws.siri.jscore.behaviour.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.runtime.Runtime;

/**
 * /jscore reset
 * 
 */
public class RestartCommand {
    public static int restart(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("Restarting JSCore")
                                .formatted(Formatting.YELLOW));

            try {
                Runtime.stop();
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when running the stop script:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }

            try {
                Runtime.init();
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when running the init script:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }

            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("JSCore restarted")
                                .formatted(Formatting.GREEN));
        });

        return 0;
    }
}
