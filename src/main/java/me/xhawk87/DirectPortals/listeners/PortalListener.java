/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.DirectPortals.listeners;

import me.xhawk87.DirectPortals.DirectTravelAgent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author XHawk87
 */
public class PortalListener implements Listener {

    private TravelAgent travelAgent = new DirectTravelAgent();

    public void registerEvents(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.useTravelAgent()) {
            event.setPortalTravelAgent(useDirectTravel(event.getPortalTravelAgent(), event.getFrom(), event.getTo()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.useTravelAgent()) {
            event.setPortalTravelAgent(useDirectTravel(event.getPortalTravelAgent(), event.getFrom(), event.getTo()));
        }
    }

    private TravelAgent useDirectTravel(TravelAgent defaultTravelAgent, Location from, Location to) {
        Block portal = from.getBlock();
        if (portal.getType() != Material.PORTAL) {
            int minX = (int) Math.round(from.getX() - portal.getX() - 1);
            int maxX = minX + 1;
            int minZ = (int) Math.round(from.getZ() - portal.getZ() - 1);
            int maxZ = minZ + 1;
            find:
            for (int dx = minX; dx <= maxX; dx++) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    Block near = portal.getRelative(dx, 0, dz);
                    if (near.getType() == Material.PORTAL) {
                        portal = near;
                        break find;
                    }
                }
            }
            if (portal.getType() != Material.PORTAL) {
                return defaultTravelAgent; // Not travelling using a nether portal
            }
        }
        Location centre = DirectTravelAgent.findPortalCentre(portal);
        centre.setPitch(from.getPitch());
        centre.setYaw(from.getYaw());
        if (centre.getWorld().getEnvironment() == World.Environment.NETHER
                && to.getWorld().getEnvironment() == World.Environment.NORMAL) {
            to.setX(centre.getX() * 8.0);
            to.setZ(centre.getZ() * 8.0);
            travelAgent.setSearchRadius(8);
        } else if (centre.getWorld().getEnvironment() == World.Environment.NORMAL
                && to.getWorld().getEnvironment() == World.Environment.NETHER) {
            to.setX(centre.getX() / 8.0);
            to.setZ(centre.getZ() / 8.0);
            travelAgent.setSearchRadius(0);
        } else {
            return defaultTravelAgent;
        }
        to.setY(centre.getY());
        return travelAgent;
    }
}
