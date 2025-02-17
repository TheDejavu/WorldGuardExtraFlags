package net.goldtreeservers.worldguardextraflags.wg.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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

public class CommandOnEntryFlagHandler extends Handler
{

	public static final Factory FACTORY(Plugin plugin)
	{
		return new Factory(plugin);
	}
	
    public static class Factory extends Handler.Factory<CommandOnEntryFlagHandler>
    {
		private final Plugin plugin;

		public Factory(Plugin plugin)
		{
			this.plugin = plugin;
		}

		@Override
        public CommandOnEntryFlagHandler create( Session session)
        {
            return new CommandOnEntryFlagHandler(this.plugin, session);
        }
    }

	private final Plugin plugin;

	private Collection<Set<String>> lastCommands;
	    
	protected CommandOnEntryFlagHandler(Plugin plugin, Session session)
	{
		super(session);

		this.plugin = plugin;

		this.lastCommands = new ArrayList<>();
	}

	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType)
	{
		Collection<Set<String>> commands = toSet.queryAllValues(player, Flags.COMMAND_ON_ENTRY);

		if (!this.getSession().getManager().hasBypass(player, (World) to.getExtent()))
		{
			for(Set<String> commands_ : commands)
			{
				if (!this.lastCommands.contains(commands_) && commands_.size() > 0)
				{
					for(String command : commands_)
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
		
		this.lastCommands = new ArrayList(commands);
		
		if (!this.lastCommands.isEmpty())
		{
			for (ProtectedRegion region : toSet)
			{
                Set<String> commands_ = region.getFlag(Flags.COMMAND_ON_ENTRY);
                if (commands_ != null)
                {
                	this.lastCommands.add(commands_);
                }
            }
		}
		
		return true;
	}
}
