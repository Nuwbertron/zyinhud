package com.zyin.zyinhud.mods;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.text.WordUtils;

import com.zyin.zyinhud.ZyinHUDRenderer;
import com.zyin.zyinhud.util.Localization;

/**
 * Shows information about horses in the F3 menu.
 */
public class AnimalInfo extends ZyinHUDModBase
{
    /**
     * Enables/Disables this Mod
     */
    public static boolean Enabled;

    /**
     * Toggles this Mod on or off
     *
     * @return The state the Mod was changed to
     */
    public static boolean ToggleEnabled()
    {
    	return Enabled = !Enabled;
    }

    /**
     * The current mode for this mod
     */
    public static Modes Mode;

    /**
     * The enum for the different types of Modes this mod can have
     */
    public static enum Modes
    {
        /**
         * Off modes.
         */
        OFF(Localization.get("safeoverlay.mode.0")),
        /**
         * On modes.
         */
        ON(Localization.get("safeoverlay.mode.1"));
        
        private String friendlyName;
        
        private Modes(String friendlyName)
        {
        	this.friendlyName = friendlyName;
        }

        /**
         * Sets the next availble mode for this mod
         *
         * @return the modes
         */
        public static Modes ToggleMode()
        {
        	return Mode = Mode.ordinal() < Modes.values().length - 1 ? Modes.values()[Mode.ordinal() + 1] : Modes.values()[0];
        }

        /**
         * Gets the mode based on its internal name as written in the enum declaration
         *
         * @param modeName the mode name
         * @return modes
         */
        public static Modes GetMode(String modeName)
        {
        	try {return Modes.valueOf(modeName);}
        	catch (IllegalArgumentException e) {return values()[0];}
        }

        /**
         * Get friendly name string.
         *
         * @return the string
         */
        public String GetFriendlyName()
        {
        	return friendlyName;
        }
    }

    /**
     * The constant ShowTextBackgrounds.
     */
    public static boolean ShowTextBackgrounds;
    /**
     * The constant ShowBreedingIcons.
     */
    public static boolean ShowBreedingIcons;
    /**
     * The constant ShowHorseStatsOnF3Menu.
     */
//public static boolean ShowBreedingTimers;
    public static boolean ShowHorseStatsOnF3Menu;
    /**
     * The constant ShowHorseStatsOverlay.
     */
    public static boolean ShowHorseStatsOverlay;

    /**
     * Sets the number of decimal places that will be rendered when displaying horse stats
     */
    public static int numberOfDecimalsDisplayed = 1;
    /**
     * The constant minNumberOfDecimalsDisplayed.
     */
    public static int minNumberOfDecimalsDisplayed = 0;
    /**
     * The constant maxNumberOfDecimalsDisplayed.
     */
    public static int maxNumberOfDecimalsDisplayed = 20;
    
    private static EntityPlayer me;
    
    //values above the perfect value are aqua
    //values between the perfect and good values are green
    //values between the good and bad values are white
    //values below the bad value are red
    private static double perfectHorseSpeedThreshold = 13;	//max: 14.1?
    private static double goodHorseSpeedThreshold = 11;
    private static double badHorseSpeedThreshold = 9.5;		//min: ~7?
    
    private static double perfectHorseJumpThreshold = 5;	//max: 5.5?
    private static double goodHorseJumpThreshold = 4;
    private static double badHorseJumpThreshold = 2.5;		//min: 1.2
    
    private static int perfectHorseHPThreshold = 28;		//max: 30
    private static int goodHorseHPThreshold = 24;			
    private static int badHorseHPThreshold = 20;			//min: 15
    
    private static final int verticalSpaceBetweenLines = 10;	//space between the overlay lines (because it is more than one line)

    /**
     * Animals that are farther away than this will not have their info shown
     */
    public static int viewDistanceCutoff = 8;		//how far away we will render the overlay
    /**
     * The constant minViewDistanceCutoff.
     */
    public static int minViewDistanceCutoff = 0;
    /**
     * The constant maxViewDistanceCutoff.
     */
    public static int maxViewDistanceCutoff = 120;

    /**
     * The constant maxNumberOfOverlays.
     */
    public static final int maxNumberOfOverlays = 200;	//render only the first nearest 50 overlays

    private static DecimalFormat decimalFormat = GetDecimalFormat();
    private static DecimalFormat twoDigitFormat = new DecimalFormat("00");
    
    
    
    /**
     * Gets the amount of decimals that should be displayed with a DecimalFormat object.
     * @return
     */
    private static DecimalFormat GetDecimalFormat()
    {
    	if(numberOfDecimalsDisplayed < 1)
    		return new DecimalFormat("#");
    	
    	String format = "#.";
    	for(int i = 1; i <= numberOfDecimalsDisplayed; i++)
    		format += "#";
    	
    	return new DecimalFormat(format);
    }

    /**
     * Gets the number of deciamls used to display the horse stats.
     *
     * @return int
     */
    public static int GetNumberOfDecimalsDisplayed()
    {
    	return numberOfDecimalsDisplayed;
    }

    /**
     * Sets the number of deciamls used to display the horse stats.
     *
     * @param numDecimals the num decimals
     * @return
     */
    public static void SetNumberOfDecimalsDisplayed(int numDecimals)
    {
    	numberOfDecimalsDisplayed = numDecimals;
    	decimalFormat = GetDecimalFormat();
    }

    /**
     * Renders a horse's speed, hit points, and jump strength on the F3 menu when the player is riding it.
     */
    public static void RenderOntoDebugMenu()
    {
    	//if F3 is shown
        if (AnimalInfo.Enabled && ShowHorseStatsOnF3Menu && mc.gameSettings.showDebugInfo)
        {
            if (mc.thePlayer.isRidingHorse())
            {
                EntityHorse horse = (EntityHorse) mc.thePlayer.getRidingEntity();
                String horseSpeedMessage = Localization.get("animalinfo.debug.speed") + " " + GetHorseSpeedText(horse) + " m/s";
                String horseJumpMessage = Localization.get("animalinfo.debug.jump") + " " + GetHorseJumpText(horse) + " blocks";
                String horseHPMessage = Localization.get("animalinfo.debug.hp") + " " + GetHorseHPText(horse);
                String horseColor = Localization.get("animalinfo.debug.color") + " " + GetHorseColoringText(horse);
                String horseMarking = Localization.get("animalinfo.debug.markings") + " " + GetHorseMarkingText(horse);
                
            	List list = new ArrayList<String>(5);
            	
            	list.add(horseSpeedMessage);
            	list.add(horseJumpMessage);
            	list.add(horseHPMessage);

                if (horse.getHorseVariant() == 0)    //not a donkey
                {
                	list.add(horseColor);
                	list.add(horseMarking);
                }

                for (int i = 0; i < list.size(); ++i)
                {
                    String s = (String)list.get(i);
                    
                    int height = mc.fontRendererObj.FONT_HEIGHT;
                    int width = mc.fontRendererObj.getStringWidth(s);
                    int y = 2 + height * i + 144;
                    Gui.drawRect(1, y - 1, 2 + width + 1, y + height - 1, -0xa9AFAFB0);
                    mc.fontRendererObj.drawString(s, 2, y, 0xE0E0E0);
                }
            }
        }
    }


    /**
     * Renders information about an entity into the game world.
     *
     * @param entity          the entity
     * @param partialTickTime the partial tick time
     */
    public static void RenderEntityInfoInWorld(Entity entity, float partialTickTime)
    {
    	if (!(entity instanceof EntityAgeable))
        {
            return;    //we only care about ageable entities
        }
    	
    	int i = 0;

        //if the player is in the world
        //and not looking at a menu
        //and F3 not pressed
        if (AnimalInfo.Enabled && Mode == Modes.ON &&
                (mc.inGameHasFocus || mc.currentScreen == null || mc.currentScreen instanceof GuiChat)
                && !mc.gameSettings.showDebugInfo)
        {
        	if(i > maxNumberOfOverlays)
        		return;
        	
            EntityAgeable animal = (EntityAgeable)entity;

            if (animal.isBeingRidden())// instanceof EntityPlayer
            {
                return;    //don't render stats of the horse/animal we are currently riding
            }

            //only show entities that are close by
            double distanceFromMe = mc.thePlayer.getDistanceToEntity(animal);

            if (distanceFromMe > maxViewDistanceCutoff
                    || distanceFromMe > viewDistanceCutoff)
            {
                return;
            }
            
            RenderAnimalOverlay(animal, partialTickTime);
            i++;
        }
    }


    /**
     * Renders an overlay in the game world for the specified animal.
     *
     * @param animal          the animal
     * @param partialTickTime the partial tick time
     */
    protected static void RenderAnimalOverlay(EntityAgeable animal, float partialTickTime)
    {
    	float x = (float)animal.posX;
        float y = (float)animal.posY;
        float z = (float)animal.posZ;
        
        //a positive value means the horse has bred recently
        int animalGrowingAge = animal.getGrowingAge();
    	
    	ArrayList multilineOverlayArrayList = new ArrayList(4);
    	
    	if(ShowHorseStatsOverlay && animal instanceof EntityHorse)
    	{
    		EntityHorse horse = (EntityHorse)animal;
    		
    		multilineOverlayArrayList.add(GetHorseSpeedText(horse) + " " + Localization.get("animalinfo.overlay.speed"));
    		multilineOverlayArrayList.add(GetHorseHPText(horse) + " " + Localization.get("animalinfo.overlay.hp"));
    		multilineOverlayArrayList.add(GetHorseJumpText(horse) + " " + Localization.get("animalinfo.overlay.jump"));
    		
    		if (animalGrowingAge < 0)
        		multilineOverlayArrayList.add(GetHorseBabyGrowingAgeAsPercent(horse) + "%");
    	}
    	/* Breeding timer info no longer available on client in 1.8
    	if(ShowBreedingTimers && animal instanceof EntityAgeable)
        {
            if (animalGrowingAge > 0)	//if the animal has recently bred
                multilineOverlayArrayList.add(GetTimeUntilBreedAgain(animal));
        }
        */
    	
    	String[] multilineOverlayMessage = new String[1];
        multilineOverlayMessage = (String[])multilineOverlayArrayList.toArray(multilineOverlayMessage);
        
        if(multilineOverlayMessage[0] != null)
        {
            //render the overlay message
            ZyinHUDRenderer.RenderFloatingText(multilineOverlayMessage, x, y, z, 0xFFFFFF, ShowTextBackgrounds, partialTickTime);
        }
        
		if(ShowBreedingIcons && 
				animalGrowingAge == 0 && 			//animal is an adult that is ready to breed
				animal instanceof EntityAnimal && 	//animal is not a villager
				!((EntityAnimal)animal).isInLove())	//animal is not currently breeding
		{
	        //render the overlay icon
			if(animal instanceof EntityHorse && ((EntityHorse)animal).isTame())
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.golden_carrot, partialTickTime);
			else if(animal instanceof EntityCow)
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.wheat, partialTickTime);
			else if(animal instanceof EntitySheep)
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.wheat, partialTickTime);
			else if(animal instanceof EntityPig)
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.carrot, partialTickTime);
			else if(animal instanceof EntityChicken)
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.wheat_seeds, partialTickTime);
			else if(animal instanceof EntityRabbit)
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.carrot, partialTickTime);
			else if(animal instanceof EntityWolf && ((EntityWolf)animal).isTamed())
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.beef, partialTickTime);
			else if(animal instanceof EntityWolf && !((EntityWolf)animal).isTamed())
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.bone, partialTickTime);
			else if(animal instanceof EntityOcelot)
				ZyinHUDRenderer.RenderFloatingItemIcon(x, y + animal.height, z, Items.fish, partialTickTime);
		}
		
		
        /* EntityVillager profession is not available on client
        if(animal instanceof EntityVillager)
        {
			String getDisplayName = ((EntityVillager)animal).getDisplayName().getFormattedText();
            ZyinHUDRenderer.RenderFloatingText(getDisplayName, x, y + animal.getEyeHeight() + 0.8f, z, 0xFFFFFF, ShowTextBackgrounds, partialTickTime);
			
        }
		*/
    }

    /**
     * Gets the status of the Animal Info
     *
     * @return the string "animals" if Animal Info is enabled, otherwise "".
     */
    public static String CalculateMessageForInfoLine()
    {
        if (Mode == Modes.OFF || !AnimalInfo.Enabled)
        {
            return "";
        }
        else if (Mode == Modes.ON)
        {
            return TextFormatting.WHITE + Localization.get("animalinfo.infoline");
        }
        else
        {
            return TextFormatting.WHITE + "???";
        }
    }

    /**
     * Gets the baby horses age ranging from 0 to 100.
     * @param horse
     * @return
     */
    private static int GetHorseBabyGrowingAgeAsPercent(EntityHorse horse)
    {
        float horseGrowingAge = horse.getHorseSize();	//horse size ranges from 0.5 to 1
        return (int)((horseGrowingAge - 0.5f) * 2.0f * 100f);
    }
    
	/**
	 * Gets the time remaining before this animal can breed again
	 * @param animal
	 * @return null if the animal ready to breed or is a baby, otherwise "#:##" formatted string
	 */
	private static String GetTimeUntilBreedAgain(EntityAgeable animal)
	{
	    int animalBreedingTime = animal.getGrowingAge();
	    
	    if(animalBreedingTime <= 0)
	    	return null;
	    
	    int seconds = animalBreedingTime / 20;
	    int minutes = seconds / 60;
	    
	    return minutes + ":" + twoDigitFormat.format(seconds % 60);
	}

    /**
     * Gets a horses speed, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "13.5"<br>green "12.5"<br>white "11.3"<br>red "7.0"
     */
    private static String GetHorseSpeedText(EntityHorse horse)
    {
        double horseSpeed = GetEntityMaxSpeed(horse);
        String horseSpeedString = decimalFormat.format(horseSpeed);

        if (horseSpeed > perfectHorseSpeedThreshold)
            horseSpeedString = TextFormatting.AQUA + horseSpeedString + TextFormatting.WHITE;
        else if (horseSpeed > goodHorseSpeedThreshold)
            horseSpeedString = TextFormatting.GREEN + horseSpeedString + TextFormatting.WHITE;
        else if (horseSpeed < badHorseSpeedThreshold)
            horseSpeedString = TextFormatting.RED + horseSpeedString + TextFormatting.WHITE;

        return horseSpeedString;
    }

    /**
     * Gets a horses HP, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "28"<br>green "26"<br>white "22"<br>red "18"
     */
    private static String GetHorseHPText(EntityHorse horse)
    {
        int horseHP = GetEntityMaxHP(horse);
        String horseHPString = decimalFormat.format(GetEntityMaxHP(horse));

        if (horseHP > perfectHorseHPThreshold)
            horseHPString = TextFormatting.AQUA + horseHPString + TextFormatting.WHITE;
        else if (horseHP > goodHorseHPThreshold)
            horseHPString = TextFormatting.GREEN + horseHPString + TextFormatting.WHITE;
        else if (horseHP < badHorseHPThreshold)
            horseHPString = TextFormatting.RED + horseHPString + TextFormatting.WHITE;

        return horseHPString;
    }
    
    /**
     * Gets a horses hearts, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "15"<br>green "13"<br>white "11"<br>red "9"
     */
    private static String GetHorseHeartsText(EntityHorse horse)
    {
        int horseHP = GetEntityMaxHP(horse);
        int horseHearts = GetEntityMaxHearts(horse);
        String horseHeartsString = "" + horseHearts;

        if (horseHP > perfectHorseHPThreshold)
            horseHeartsString = TextFormatting.AQUA + horseHeartsString + TextFormatting.WHITE;
        else if (horseHP > goodHorseHPThreshold)
            horseHeartsString = TextFormatting.GREEN + horseHeartsString + TextFormatting.WHITE;
        else if (horseHP < badHorseHPThreshold)
            horseHeartsString = TextFormatting.RED + horseHeartsString + TextFormatting.WHITE;

        return horseHeartsString;
    }

    /**
     * Gets a horses jump height, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "5.4"<br>green "4"<br>white "3"<br>red "1.5"
     */
    private static String GetHorseJumpText(EntityHorse horse)
    {
        double horseJump = GetHorseMaxJump(horse);
        String horseJumpString = decimalFormat.format(horseJump);

        if (horseJump > perfectHorseJumpThreshold)
            horseJumpString = TextFormatting.AQUA + horseJumpString + TextFormatting.WHITE;
        else if (horseJump > goodHorseJumpThreshold)
            horseJumpString = TextFormatting.GREEN + horseJumpString + TextFormatting.WHITE;
        else if (horseJump < badHorseJumpThreshold)
            horseJumpString = TextFormatting.RED + horseJumpString + TextFormatting.WHITE;

        return horseJumpString;
    }

    /**
     * Gets a horses primary coloring
     * @param horse
     * @return empty string if there is no coloring (for donkeys)
     */
    private static String GetHorseColoringText(EntityHorse horse)
    {
        String texture = horse.getVariantTexturePaths()[0];
        
        if(texture == null || texture.isEmpty())
        	return "";
        
        String[] textureArray = texture.split("/");			//"textures/entity/horse/horse_creamy.png"
        texture = textureArray[textureArray.length-1];		//"horse_creamy.png"
        texture = texture.substring(6, texture.length()-4);	//"creamy"
        texture = WordUtils.capitalize(texture);			//"Creamy"
        
        return texture;
    }

    /**
     * Gets a horses secondary coloring
     * @param horse
     * @return empty string if there is no secondary coloring (for donkeys)
     */
    private static String GetHorseMarkingText(EntityHorse horse)
    {
        String texture = horse.getVariantTexturePaths()[1];
        
        if(texture == null || texture.isEmpty())
        	return "";
        
        String[] textureArray = texture.split("/");				//"textures/entity/horse/horse_markings_blackdots.png"
        texture = textureArray[textureArray.length-1];			//"horse_markings_blackdots.png"
        texture = texture.substring(15, texture.length()-4);	//"blackdots"
        texture = WordUtils.capitalize(texture);				//"Blackdots"
        
        return texture;
    }

    /**
     * Gets the max height a horse can jump when the jump bar is fully charged.
     * @param horse
     * @return e.x. 1.2?-5.5?
     */
    private static double GetHorseMaxJump(EntityHorse horse)
    {
    	//simulate gravity and air resistance to determine the jump height
    	double yVelocity = horse.getHorseJumpStrength();	//horses's jump strength attribute
    	double jumpHeight = 0;
    	while (yVelocity > 0)
    	{
    		jumpHeight += yVelocity;
    		yVelocity -= 0.08;
    		yVelocity *= 0.98;
    	}
    	return jumpHeight;
    }

    /**
     * Gets an entity's max hit points
     * @param entity
     * @return e.x. Steve = 20 hit points
     */
    private static int GetEntityMaxHP(EntityLivingBase entity)
    {
        return (int) entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
    }

    /**
     * Gets the max hearts an entity has
     * @param entity
     * @return e.x. Steve = 20 hit points
     */
    private static int GetEntityMaxHearts(EntityLivingBase entity)
    {
        return (int) Math.round(entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() / 2);
    }

    /**
     * Gets an entity's max run speed in meters(blocks) per second
     * @param entity
     * @return e.x. Steve = 4.3 m/s. Horses ~7-13
     */
    private	 static double GetEntityMaxSpeed(EntityLivingBase entity)
    {
        //Steve has a movement speed of 0.1 and walks 4.3 blocks per second,
        //so multiply this result by 43 to convert to blocks per second
        return entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 43;
    }

    /**
     * Toggle showing horse stats on the F3 menu
     *
     * @return the new F3 render boolean
     */
    public static boolean ToggleShowHorseStatsOnF3Menu()
    {
    	return ShowHorseStatsOnF3Menu = !ShowHorseStatsOnF3Menu;
    }

    /**
     * Toggle showing horse stats on the overlay
     *
     * @return the new overlay render boolean
     */
    public static boolean ToggleShowHorseStatsOverlay()
    {
    	return ShowHorseStatsOverlay = !ShowHorseStatsOverlay;
    }

    /**
     * Toggle showing black text backgrounds on overlayed text
     *
     * @return the new text background boolean
     */
    public static boolean ToggleShowTextBackgrounds()
    {
    	return ShowTextBackgrounds = !ShowTextBackgrounds;
    }

    /**
     * Toggles showing breeding icons
     *
     * @return the new boolean
     */
    public static boolean ToggleShowBreedingIcons()
    {
    	return ShowBreedingIcons = !ShowBreedingIcons;
    }
    /**
     * Toggles showing breeding timers
     * @return the new boolean
     */
    /*
    public static boolean ToggleShowBreedingTimers()
    {
    	return ShowBreedingTimers = !ShowBreedingTimers;
    }
    */
}
