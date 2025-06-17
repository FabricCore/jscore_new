package ws.siri.jscore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

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

    public static void main(String[] args) throws IOException {
        String resourceName = "fabric.mod.json"; // Name of the resource file

        // Use the class loader to check for the resource
        InputStream inputStream = org.apache.commons.codec.BinaryDecoder.class.getClassLoader().getResourceAsStream(resourceName);

        
        var s = IOUtils.toString(inputStream, Charset.defaultCharset());
        System.out.println(s);
    }
}