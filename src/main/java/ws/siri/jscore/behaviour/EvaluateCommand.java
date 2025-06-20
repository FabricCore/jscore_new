package ws.siri.jscore.behaviour;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.runtime.Runtime;

/**
 * /jscore eval &lt;script&gt;
 * 
 * Evaluate script content at scope "repo"
 */
public class EvaluateCommand {
    public static int evaluate(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            String script = context.getArgument("expression", String.class);

            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("> ").append(script).formatted(Formatting.GREEN));

            try {
                Object res = Runtime.evaluate(script, List.of("repl"), true);

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

        return 0;
    }
}
