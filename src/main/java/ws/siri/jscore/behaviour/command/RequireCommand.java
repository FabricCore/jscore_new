package ws.siri.jscore.behaviour.command;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.runtime.Runtime;

/**
 * Require [strict/lazy] &lt;path&gt;
 * 
 * - strict always loads the file
 * - lazy only loads the file if not previously loaded
 */
public class RequireCommand {
    public static int lazy(CommandContext<FabricClientCommandSource> context) {
        String path = context.getArgument("path", String.class);
        run(path, "lazy");
        return 0;
    }

    public static int strict(CommandContext<FabricClientCommandSource> context) {
        String path = context.getArgument("path", String.class);
        run(path, "strict");
        return 0;
    }

    private static void run(String path, String mode) {
        CompletableFuture.runAsync(() -> {
            try {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(
                                    Text.literal(String.format("Run %s (%s)", path, mode)).formatted(Formatting.GREEN));

                Path filePath = Path.of(path).normalize();

                if (filePath.isAbsolute()) {
                    filePath = filePath.subpath(0, filePath.getNameCount());
                }
                Object res = Runtime.call(filePath, mode, null);

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(
                                    Text.literal(res == null ? "null" : res.toString()).formatted(Formatting.YELLOW));
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when running this script:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }
        });
    }
}
