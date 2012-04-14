package com.mrhen.NoMoreRain;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author mrhen
 * 
 * Simple plugin that allows automatic suppression of all weather effects.
 * Suppressing Rain while allowing Thunder seems to confuse the event system a
 * bit but it works itself out without issue. Suppressing Lightning seems to
 * have a similar end result as suppressing Thunder but this plugin is meant as
 * a direct interaction with Minecraft+Bukkit.
 */
public class NoMoreRain extends JavaPlugin implements Listener {

  protected static final String commandRoot = "nomorerain";

  protected static final String commandDebug = "debug";

  protected static final String commandStart = "set";

  protected static final String commandSuppress = "suppress";

  protected static final String configSuppress = "nomorerain.suppress";

  protected static final String messagePrefix = "[NoMoreRain] ";

  protected static final String paramFalse = "0";

  protected static final String paramLightning = "lightning";

  protected static final String paramRain = "rain";
  
  protected static final String paramThunder = "thunder";
  
  private HashSet<CommandSender> debugPlayers;
    
  protected Logger log = null;
  
  public void onEnable(){ 
    this.log = this.getLogger();
    this.log.info(this.getName() + " has been enabled.");
    getServer().getPluginManager().registerEvents(this, this);
  }
   
  public void onDisable(){ 
    this.log.info(this.getName() + " has been disabled.");
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    String permissionsString = "";
    if(cmd.getName().equalsIgnoreCase(NoMoreRain.commandRoot)) {
      permissionsString += NoMoreRain.commandRoot;
      
      if(args.length >= 1) {
        if(args[0].equalsIgnoreCase(NoMoreRain.commandSuppress)) {
          permissionsString += "." + NoMoreRain.commandSuppress;
          if(args.length >= 4) {
            if(sender.hasPermission(permissionsString)) {
              //TODO usage
            } else {
              return false;
            }
          }

          boolean suppress = args.length < 3 || !args[2].equalsIgnoreCase(NoMoreRain.paramFalse);
          
          if(args[1].equalsIgnoreCase(NoMoreRain.paramLightning)) {
            if(sender.hasPermission(permissionsString + "." + NoMoreRain.paramLightning)) {
              this.suppressLightning(suppress, sender);
            }
          } else if(args[1].equalsIgnoreCase(NoMoreRain.paramThunder)) {
            if(sender.hasPermission(permissionsString + "." + NoMoreRain.paramThunder)) {
              this.suppressThunder(suppress, sender);
            }
          } else if(args[1].equalsIgnoreCase(NoMoreRain.paramRain)) {
            if(sender.hasPermission(permissionsString + "." + NoMoreRain.paramRain)) {
              this.suppressRain(suppress, sender);
            }
          } else {
            //TODO usage
          }

        } else if(args[0].equalsIgnoreCase(NoMoreRain.commandStart)) {
          permissionsString += "." + NoMoreRain.commandStart;
          if(args.length >= 4) {
            if(sender.hasPermission(permissionsString)) {
              //TODO usage
            } else {
              return false;
            }
          }

          boolean start = args.length < 3 || !args[2].equalsIgnoreCase(NoMoreRain.paramFalse);
          
          //TODO split permissions into on/off
          if(args[1].equalsIgnoreCase(NoMoreRain.paramRain)) {
            if(sender.hasPermission(permissionsString + "." + NoMoreRain.paramRain)) {
              this.startRain(start, sender);
            }
          } else if(args[1].equalsIgnoreCase(NoMoreRain.paramThunder)) {
            if(sender.hasPermission(permissionsString + "." + NoMoreRain.paramThunder)) {
              this.startThunder(start, sender);
            }
          } else {
            //TODO usage
          }
        } else if(args[0].equalsIgnoreCase(NoMoreRain.commandDebug)) {
          if(sender.hasPermission(permissionsString + "." + NoMoreRain.commandDebug)) {
            if(args.length >= 3) {
              //TODO usage
            }
            
            if(args.length < 2 || !args[1].equalsIgnoreCase(NoMoreRain.paramFalse)) {
              this.addDebugPlayer(sender);
            } else {
              this.removeDebugPlayer(sender);
            }
          }
        }
      } else {
        if(sender.hasPermission(permissionsString)) {
          //TODO usage
        }
      }
      return true;
    }
    return false; 
  }
  
  protected void addDebugPlayer(CommandSender player) {
    if(this.debugPlayers == null) debugPlayers = new HashSet<CommandSender>();
    
    this.debugPlayers.add(player);
    this.sendOutput("Now sending debug messages to " + player.getName(), player, true, false);
  }

  protected void removeDebugPlayer(CommandSender player) {
    if(this.debugPlayers != null) {
        this.debugPlayers.remove(player);
    }
    this.sendOutput("Not sending debug messages to " + player.getName(), player, true, false);
  }

  protected void sendOutput(String message) {
    this.sendOutput(message, null, true, true);
  }
  
  protected void sendOutput(String message, CommandSender player) {
    this.sendOutput(message, player, true, true);
  }
  
  protected void sendOutput(String message, boolean sendToLog, boolean sendToDebug) {
    this.sendOutput(message, null, sendToLog, sendToDebug);
  }

  protected void sendOutput(String message, CommandSender player, boolean sendToLog, boolean sendToDebug) {
    if(player != null) {
      player.sendMessage(NoMoreRain.messagePrefix + message);
    }
    
    if(sendToLog) {
      // do not prefix messagePrefix for console
      this.log.info(message);
    }
    
    if(sendToDebug && this.debugPlayers != null) {
      for (CommandSender sender : this.debugPlayers) {
        // don't send the same message to the same player twice
        if(sender != player) {
          sender.sendMessage(NoMoreRain.messagePrefix + message);
        }
      }      
    }
  }

  protected void startRain(boolean start, CommandSender source) {
    this.sendOutput((start ? "Starting" : "Ending") + " rain", source);
    this.getServer().getWorlds().get(0).setStorm(start);
  }
  
  protected void startThunder(boolean start, CommandSender source) {
    this.sendOutput((start ? "Starting" : "Ending") + " thunder", source);
    this.getServer().getWorlds().get(0).setThundering(start);
  }

  protected void suppressLightning(boolean suppress, CommandSender source) {
    this.sendOutput("Will " + (suppress ? "now" : "not") + " suppress lightning", source);
    this.getConfig().set(NoMoreRain.configSuppress + "." + NoMoreRain.paramLightning, suppress);
  }

  protected void suppressRain(boolean suppress, CommandSender source) {
    this.sendOutput("Will " + (suppress ? "now" : "not") + " suppress rain", source);
    this.getConfig().set(NoMoreRain.configSuppress + "." + NoMoreRain.paramRain, suppress);
    World world = this.getServer().getWorlds().get(0);
    if(world.hasStorm()) {
      this.sendOutput("Automatically stopping rain");
      world.setStorm(false);
    }
    this.saveConfig();
  }
  
  protected void suppressThunder(boolean suppress, CommandSender source) {
    this.sendOutput("Will " + (suppress ? "now" : "not") + " suppress thunder", source);
    this.getConfig().set(NoMoreRain.configSuppress + "." + NoMoreRain.paramThunder, suppress);
    World world = this.getServer().getWorlds().get(0);
    if(world.isThundering()) {
      this.sendOutput("Automatically stopping thunder");
      world.setThundering(false);
    }
    this.saveConfig();
  }

  /**
   * If we are suppressing lightning than this will cancel all lightning strike 
   * events
   * 
   * @param event A lightning strike event
   */
  @EventHandler(ignoreCancelled = true)
  public void onLightningStrike(LightningStrikeEvent event) {
    if(this.getConfig().getBoolean(NoMoreRain.configSuppress + "." + NoMoreRain.paramLightning)) {
      this.sendOutput("Cancelling " + event.getEventName());
      event.setCancelled(true);
    } else {
      this.sendOutput("Allowing " + event.getEventName());
    }
    this.saveConfig();
  }

  /**
   * If we are suppressing thunder than this will cancel all thunder change
   * events
   * 
   * @param event A thunder change event
   */
  @EventHandler(ignoreCancelled = true)
  public void onThunderChange(ThunderChangeEvent event) {
    if(event.toThunderState()) {
      if(this.getConfig().getBoolean(NoMoreRain.configSuppress + "." + NoMoreRain.paramLightning)) {
        this.sendOutput("Cancelling " + event.getEventName() + " (detected thunder)");
        event.setCancelled(true);
      } else {
        this.sendOutput("Allowing " + event.getEventName() + " (detected thunder)");
      }
    }
  }

  /**
   * If we are suppressing rain than this will cancel all rain change
   * events
   * 
   * @param event A weather change event
   */
  @EventHandler(ignoreCancelled = true)
  public void onWeatherChange(WeatherChangeEvent event) {
    if(event.toWeatherState()) {
      if(this.getConfig().getBoolean(NoMoreRain.configSuppress + "." + NoMoreRain.paramLightning)) {
        this.sendOutput("Cancelling " + event.getEventName() + " (detected rain)");
        event.setCancelled(true);
      } else {
        this.sendOutput("Allowing " + event.getEventName() + " (detected rain)");
      }
    }
  }
}
