/* ========================================================================================================
 * 
 * README
 * 
 * Zyin's HUD
 * 
 * This code is all open source and you are free to, and encouraged to, do whatever you want with it.
 * 
 * Adding your own functionality is (relatively) simple. First make a class in com.zyin.zyinhud.mods
 * which contains all of your mods logic. Then you need a way to interact with your mod. You can
 * do this with a Tick Handler (already setup for you in ZyinHUDRenderer.java), a Hotkey (follow the
 * examples in ZyinHUDKeyHandlers.java), or a single-player only command (see com.zyin.zyinhud.command).
 * 
 * To add configurable options to your mod, you need to add a new tab to GuiZyinHUDOptions.java.
 * You do this by modifing the tabbedButtonNames and tabbedButtonIDs variables. Then add your new button 
 * actions in the actionPerformed() method. To have these configurable options persist after logging out,
 * you need to follow the examples in ZyinHUDConfig.java to write your data to the config file.
 * 
 * That's it! Make sure to check out the other classes as they have useful helper functions. If you don't
 * know how to do something, just look at how another mod does something similar to it.
 * 
 * ========================================================================================================
 */

package com.zyin.zyinhud;

import com.zyin.zyinhud.command.CommandFps;
import com.zyin.zyinhud.command.CommandZyinHUDOptions;
import com.zyin.zyinhud.mods.HealthMonitor;
import com.zyin.zyinhud.mods.Miscellaneous;
import com.zyin.zyinhud.util.ModCompatibility;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.File;

/**
 * The type Zyin hud.
 */
@Mod(modid = ZyinHUD.MODID, version = ZyinHUD.VERSION, name = ZyinHUD.MODNAME, clientSideOnly = true, canBeDeactivated = true, dependencies = ZyinHUD.dependencies, updateJSON = ZyinHUD.updateJSON)
public class ZyinHUD {
    /**
     * Version number must be changed in 3 spots before releasing a build:
     * <br><ol>
     * <li>VERSION
     * <li>src/main/resources/mcmod.info:"version", "mcversion"
     * <li>build.gradle:version
     * </ol>
     * If incrementing the Minecraft version, also update "curseFilenameParser" in AddVersionChecker()
     */
    public static final String VERSION = "@VERSION@";
    /**
     * The constant MODID.
     */
    public static final String MODID = "zyinhud";
    /**
     * The constant MODNAME.
     */
    public static final String MODNAME = "Zyin's HUD";
    
    public static final String updateJSON = "https://raw.githubusercontent.com/cyilin/zyinhud-update/master/update.json";

    public static final String dependencies = "required-after:forge@[14.23.1.2554,);";
    
    public static final String buildTime = "@BUILD_TIME@";
    
    /**
     * The constant proxy.
     */
    @SidedProxy(clientSide = "com.zyin.zyinhud.ClientProxy", serverSide = "com.zyin.zyinhud.CommonProxy")
    public static CommonProxy proxy;

    /**
     * The constant mc.
     */
    protected static final Minecraft mc = Minecraft.getMinecraft();

    private File configFile;


    /**
     * Instantiates a new Zyin hud.
     */
    public ZyinHUD() {

    }


    /**
     * Pre init.
     *
     * @param event the event
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configFile = event.getSuggestedConfigurationFile();

        //AddVersionChecker();
    }

    /**
     * Init.
     *
     * @param event the event
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        log(String.format("version: %s (%s)", VERSION, buildTime));
        //load all our Key Handlers
        //FMLCommonHandler.instance().bus().register(ZyinHUDKeyHandlers.instance);
        MinecraftForge.EVENT_BUS.register(ZyinHUDKeyHandlers.instance);

        //load configuration settings from the ZyinHUD.cfg file
        ZyinHUDConfig.LoadConfigSettings(configFile);

        //needed for @SubscribeEvent method subscriptions:
        //   MinecraftForge.EVENT_BUS.register()          --> is used for net.minecraftforge events
        //   FMLCommonHandler.instance().bus().register() --> is used for cpw.mods.fml events
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ZyinHUDGuiEvents.instance);
        MinecraftForge.EVENT_BUS.register(ZyinHUDRenderer.instance);
        MinecraftForge.EVENT_BUS.register(Miscellaneous.instance);
        MinecraftForge.EVENT_BUS.register(HealthMonitor.instance);
        //FMLCommonHandler.instance().bus().register(Miscellaneous.instance);
        //FMLCommonHandler.instance().bus().register(HealthMonitor.instance);
    }

    /**
     * Post init.
     *
     * @param event the event
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModCompatibility.TConstruct.isLoaded = Loader.isModLoaded("TConstruct");
    }

    /**
     * Server starting.
     *
     * @param event the event
     */
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        //THIS EVENT IS NOT FIRED ON SMP SERVERS
        event.registerServerCommand(new CommandFps());
        event.registerServerCommand(new CommandZyinHUDOptions());
    }

    /**
     * Adds support for the Version Checker mod.
     *
     * @link http ://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2091981-version-checker-auto-update-mods-and-clean
     */
    public void AddVersionChecker() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("curseProjectName", "59953-zyins-hud");    //http://minecraft.curseforge.com/mc-mods/59953-zyins-hud
        compound.setString("curseFilenameParser", "ZyinsHUD-(1.9)-v.[].jar");
        FMLInterModComms.sendRuntimeMessage(ZyinHUD.MODID, "VersionChecker", "addCurseCheck", compound);
    }

    public static void log(String msg) {
        LogManager.getLogger(MODID).log(Level.INFO, msg);
    }

}

