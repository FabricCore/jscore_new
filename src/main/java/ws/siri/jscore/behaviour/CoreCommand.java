package ws.siri.jscore.behaviour;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.runtime.Runtime;

public class CoreCommand {
    public static int evaluate(CommandContext<FabricClientCommandSource> context) {
        String script = context.getArgument("expression", String.class);

        if (MinecraftClient.getInstance().player != null)
            MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(Text.literal("> ").append(script).formatted(Formatting.GREEN));

        try {
            Object res = Runtime.evaluateAt(script, new String[] { "repl" });

            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal(res.toString()).formatted(Formatting.YELLOW));
        } catch (Exception e) {
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("An error has occured when running this script:\n")
                                .append(e.toString()).formatted(Formatting.RED));
        }
        return 0;
    }
}
