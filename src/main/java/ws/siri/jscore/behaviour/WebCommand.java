package ws.siri.jscore.behaviour;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.runtime.Runtime;

public class WebCommand {
    private static HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static int web(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            String url = context.getArgument("url", String.class);

            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("Running from ").append(url).formatted(Formatting.GREEN));

            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).build();
            String script;
            try {
                script = client.send(req, BodyHandlers.ofString()).body();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                Object res = Runtime.evaluate(script, List.of("repl"));

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
