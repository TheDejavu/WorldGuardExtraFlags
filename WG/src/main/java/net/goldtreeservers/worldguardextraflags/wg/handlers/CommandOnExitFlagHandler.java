package net.goldtreeservers.worldguardextraflags.wg.handlers;

import java.util.*;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;

import net.goldtreeservers.worldguardextraflags.flags.Flags;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandOnExitFlagHandler extends Handler
{
	public static final Factory FACTORY(Plugin plugin)
	{
		return new Factory(plugin);
	}
	
    public static class Factory extends Handler.Factory<CommandOnExitFlagHandler>
    {
		private final Plugin plugin;

		public Factory(Plugin plugin)
		{
			this.plugin = plugin;
		}

		@Override
        public CommandOnExitFlagHandler create(Session session)
        {
            return new CommandOnExitFlagHandler(this.plugin, session);
        }
    }

	private final Plugin plugin;

	private Collection<Set<String>> lastCommands;
	    
	protected CommandOnExitFlagHandler(Plugin plugin, Session session)
	{
		super(session);

		this.plugin = plugin;

		this.lastCommands = new ArrayList<>();
	}
	
    @Override
	public void initialize(LocalPlayer player, Location current, ApplicableRegionSet set)
    {
    	this.lastCommands = set.queryAllValues(player, Flags.COMMAND_ON_EXIT);
    }
    	
	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType)
	{
		Collection<Set<String>> commands = Lists.newArrayList(toSet.queryAllValues(player, Flags.COMMAND_ON_EXIT));
		
		if (!commands.isEmpty())
		{
			for (ProtectedRegion region : toSet)
			{
                Set<String> commands_ = region.getFlag(Flags.COMMAND_ON_EXIT);
                if (commands_ != null)
                {
                	commands.add(commands_);
                }
            }
		}

		Collection<Set<String>> lastCommands = this.lastCommands;

		this.lastCommands = commands;

		if (!this.getSession().getManager().hasBypass(player, (World) to.getExtent()))
		{
			for (Set<String> commands_ : lastCommands)
			{
				if (!commands.contains(commands_) && commands_.size() > 0)
				{
					for (String command : commands_)
					{
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								Bukkit.getServer().dispatchCommand(((BukkitPlayer) player).getPlayer(), command.substring(1).replace("%username%", player.getName())); //TODO: Make this better

							}
						}.runTask(plugin);
					}

					break;
				}
			}
		}
		
		return true;
	}
}
