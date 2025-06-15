package ws.siri.jscore;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import ws.siri.jscore.behaviour.CoreCommand;

public class Core implements ModInitializer {
    public static final String MOD_ID = "jscore";

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("jscore")
                    .then(ClientCommandManager.literal("eval")
                            .then(ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                                    .executes(CoreCommand::evaluate))));
        });
    }
}