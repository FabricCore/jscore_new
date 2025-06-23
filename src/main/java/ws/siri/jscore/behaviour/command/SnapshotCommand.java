package ws.siri.jscore.behaviour.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.behaviour.Snapshot;

class ListSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context,
            SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> snapshots = Snapshot.list();
        for (String snapshot : snapshots) {
            builder.suggest(snapshot);
        }

        return builder.buildFuture();
    }

}

public class SnapshotCommand {
    public static ListSuggestionProvider listProvider() {
        return new ListSuggestionProvider();
    }

    public static int snapNamed(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            try {
                String name = context.getArgument("name", String.class);

                if (name.endsWith(".zip")) {
                    name = name.substring(0, name.length() - 4);
                }

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("> Creating snapshot").formatted(Formatting.GREEN));

                Snapshot.snap(name);

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(
                                    Text.literal(String.format("Snapshot saved to %s.zip", name))
                                            .formatted(Formatting.YELLOW));
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when creating snapshot:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }
        });
        return 0;
    }

    public static int snap(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            try {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("> Creating snapshot").formatted(Formatting.GREEN));

                String res = Snapshot.snap();

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(
                                    Text.literal(String.format("Snapshot saved to %s", res))
                                            .formatted(Formatting.YELLOW));
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when creating snapshot:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }
        });
        return 0;
    }

    public static int restore(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            try {
                String fileName = context.getArgument("file", String.class);

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("> Loading snapshot " + fileName).formatted(Formatting.GREEN));

                boolean loaded = Snapshot.load(fileName);

                if (loaded) {
                    if (MinecraftClient.getInstance().player != null)
                        MinecraftClient.getInstance().inGameHud.getChatHud()
                                .addMessage(
                                        Text.literal("Snapshot restored")
                                                .formatted(Formatting.YELLOW));
                } else {
                    if (MinecraftClient.getInstance().player != null)
                        MinecraftClient.getInstance().inGameHud.getChatHud()
                                .addMessage(
                                        Text.literal("Snapshot not restored: not snapshot with that name")
                                                .formatted(Formatting.RED));
                }
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when creating snapshot:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }
        });
        return 0;
    }

    public static int pull(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            try {
                String url = context.getArgument("url", String.class);

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("> Pulling snapshot").formatted(Formatting.GREEN));

                Snapshot.pull(url);

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(
                                    Text.literal("Snapshot pulled")
                                            .formatted(Formatting.YELLOW));
            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when pulling snapshot:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }
        });
        return 0;
    }

    public static int delete(CommandContext<FabricClientCommandSource> context) {
        CompletableFuture.runAsync(() -> {
            try {
                String name = context.getArgument("file", String.class);

                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("> Deleting snapshot " + name).formatted(Formatting.GREEN));

                if (Snapshot.delete(name)) {
                    if (MinecraftClient.getInstance().player != null)
                        MinecraftClient.getInstance().inGameHud.getChatHud()
                                .addMessage(
                                        Text.literal("Snapshot deleted")
                                                .formatted(Formatting.YELLOW));
                } else {
                    if (MinecraftClient.getInstance().player != null)
                        MinecraftClient.getInstance().inGameHud.getChatHud()
                                .addMessage(
                                        Text.literal("Nothing deleted, snapshot does not exist")
                                                .formatted(Formatting.YELLOW));
                }

            } catch (Exception e) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("An error has occured when deleting snapshot:\n")
                                    .append(e.toString()).formatted(Formatting.RED));
            }
        });
        return 0;
    }
}
