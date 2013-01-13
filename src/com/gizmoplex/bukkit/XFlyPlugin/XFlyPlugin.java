package com.gizmoplex.bukkit.XFlyPlugin;


import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.gizmoplex.bukkit.PluginDataAdapter;


public final class XFlyPlugin extends JavaPlugin implements Listener
{


  private HashMap<String, FlyState> _playerFlyState;

  private PluginDataAdapter<HashMap<String, FlyState>> _playerFlyStateAdapter;


  public enum FlyState
  {
    FlyDisabled, FlyEnabled;
  }


  /***
   * Called when the the plugin is disabled.
   */
  @Override
  public void onDisable()
  {
    super.onDisable();

    // Save plugin data
    if (!SavePluginData())
    {
      getLogger().severe("Unable to save plugin data.");
    }

    getLogger().info("XFly plugin disabled.");

  }


  /***
   * Called when the plugin is enabled.
   */
  @Override
  public void onEnable()
  {
    super.onEnable();

    // Init plugin data adapters
    InitPluginDataAdapters();

    // Load plugin data from files
    if (!LoadPluginData())
    {
      getLogger().severe("Unable to load plugin data.");
      setEnabled(false);
      return;
    }

    // Register events
    getServer().getPluginManager().registerEvents(this, this);

    // Register commands
    getCommand("fly").setExecutor(new FlyCommandExecutor());
    getCommand("fly-stop").setExecutor(new FlyStopCommandExecutor());

    // Log message the plugin has been loaded
    getLogger().info("XFly plugin enabled.");

  }


  /***
   * Initializes plugin data adapters.
   */
  private void InitPluginDataAdapters()
  {

    File folder;

    // Get the data folder and create it if necessary
    folder = getDataFolder();
    if (!folder.exists())
    {
      try
      {
        folder.mkdir();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    // Create adapters for plugin data
    _playerFlyStateAdapter = new PluginDataAdapter<HashMap<String, FlyState>>(getDataFolder() + File.separator + "playerFlyState.bin");

  }


  /***
   * Loads the plugin data from binary files. If the files do not exist, new
   * data objects are created.
   * 
   * @return
   */
  private boolean LoadPluginData()
  {
    // Load player fly state
    if (_playerFlyStateAdapter.FileExists())
    {
      if (_playerFlyStateAdapter.LoadObject())
      {
        _playerFlyState = _playerFlyStateAdapter.GetObject();
      }
      else
      {
        return (false);
      }
    }
    else
    {
      _playerFlyState = new HashMap<String, FlyState>();
      _playerFlyStateAdapter.SetObject(_playerFlyState);
    }

    // Return successfully
    return (true);
  }


  /***
   * Saves plugin data to binary files.
   * 
   * @return If successful, true is returned. Otherwise, false is returned.
   */
  private boolean SavePluginData()
  {
    boolean ret = true;

    // Save player invincible state
    if (!_playerFlyStateAdapter.Save())
      ret = false;

    // Return status
    return (ret);

  }


  /***
   * Returns the player fly state hash map.
   * 
   * @return
   */
  public HashMap<String, FlyState> GetPlayerFlyState()
  {
    return (_playerFlyState);
  }


  /***
   * Handler for PlayerJoin event
   * 
   * @param Event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent Event)
  {
    HashMap<String, FlyState> playerFlyState;
    String playerName;

    // Get player fly state hash map
    playerFlyState = GetPlayerFlyState();

    // Get the player's name
    playerName = Event.getPlayer().getName();

    // If hash map contains entry for the player
    if (playerFlyState.containsKey(playerName))
    {
      if (playerFlyState.get(playerName) == FlyState.FlyEnabled)
        Event.getPlayer().setAllowFlight(true);
      else
        Event.getPlayer().setAllowFlight(false);
    }
    else
    {
      Event.getPlayer().setAllowFlight(false);
    }

  }


  /***
   * Class for fly command
   * 
   */
  private class FlyCommandExecutor implements CommandExecutor
  {


    /***
     * Handles the "fly" command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
        String[] args)
    {
      Player player;
      HashMap<String, FlyState> playerFlyState;

      if (sender instanceof Player)
      {
        player = (Player) sender;

        // Must be no arguments
        if (args.length != 0)
        {
          player.sendMessage("Invalid number of arguments.");
          return (false);
        }

        // Get the player fly state hash map
        playerFlyState = GetPlayerFlyState();

        // Save the fly state
        playerFlyState.put(player.getName(), FlyState.FlyEnabled);

        // Enable flying
        player.setAllowFlight(true);

        // Message
        player.sendMessage("Flying has been enabled.");

      }
      else
      {
        sender.sendMessage("This command can only be executed by a player.");
        return (true);
      }

      return (true);
    }

  }


  /***
   * Class for fly-stop command
   */
  private class FlyStopCommandExecutor implements CommandExecutor
  {


    /***
     * Handles the "fly-stop" command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
        String[] args)
    {
      Player player;
      HashMap<String, FlyState> playerFlyState;

      if (sender instanceof Player)
      {
        player = (Player) sender;

        // Must be no arguments
        if (args.length != 0)
        {
          player.sendMessage("Invalid number of arguments.");
          return (false);
        }

        // Get the player fly state hash map
        playerFlyState = GetPlayerFlyState();

        // Save the fly state
        playerFlyState.put(player.getName(), FlyState.FlyDisabled);

        // Enable flying
        player.setAllowFlight(false);

        // Message
        player.sendMessage("Flying has been disabled.");

      }
      else
      {
        sender.sendMessage("This command can only be executed by a player.");
        return (true);
      }

      return (true);
    }

  }

}