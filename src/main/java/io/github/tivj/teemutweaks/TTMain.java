package io.github.tivj.teemutweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TTMain.MODID, name = TTMain.NAME, version = TTMain.VERSION)
public class TTMain {
    public static final String MODID = "tttest";
    public static final String NAME = "TTTest";
    public static final String VERSION = "@MOD_VERSION@";

    public static final Logger LOGGER = LogManager.getLogger("TTTest");

    @Mod.Instance(MODID)
    public static TTMain INSTANCE;

    public TTMain() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void printDebugMessageToChat(String message) {
        try {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(message));
        } catch (NullPointerException npe) {
            TTMain.LOGGER.warn("Unable to send message \""+message+"\" to chat! NullPointerException!");
        }
    }
}