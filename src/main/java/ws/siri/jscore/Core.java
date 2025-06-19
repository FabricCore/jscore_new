package ws.siri.jscore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import ws.siri.jscore.behaviour.EvaluateCommand;
import ws.siri.jscore.behaviour.RequireCommand;
import ws.siri.jscore.behaviour.WebCommand;
import ws.siri.jscore.runtime.Runtime;
import ws.siri.yarnwrap.mapping.JavaObject;

public class Core implements ModInitializer {
    public static final String MOD_ID = "jscore";

    @Override
    public void onInitialize() {
        JavaObject.addNoWrap("org.mozilla");

        entry();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("jscore")
                    .then(ClientCommandManager.literal("eval")
                            .then(ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                                    .executes(EvaluateCommand::evaluate)))
                    .then(ClientCommandManager.literal("web")
                            .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                    .executes(WebCommand::web)))
                    .then(ClientCommandManager.literal("require")
                            .then(ClientCommandManager.literal("lazy")
                                    .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                            .executes(RequireCommand::lazy)))
                            .then(ClientCommandManager.literal("strict")
                                    .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                            .executes(RequireCommand::strict)))));
        });
    }

    public static void entry() {
        Path entryPoint = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("init.js");

        if (Files.exists(entryPoint)) {
            Runtime.call(Path.of("init"), "lazy", null);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(null instanceof Object);
        System.out.println(Path.of("./test/test").normalize());
    }
}