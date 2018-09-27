/*
 * Copyright (C) 2018 XHawk87
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.xhawk87.DirectPortals.listeners;

import me.xhawk87.DirectPortals.DirectPortalInfo;
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

    private static final TravelAgent TRAVEL_AGENT = new DirectTravelAgent();

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
        if (portal.getType() != Material.NETHER_PORTAL) {
            int minX = (int) Math.round(from.getX() - portal.getX() - 1);
            int maxX = minX + 1;
            int minZ = (int) Math.round(from.getZ() - portal.getZ() - 1);
            int maxZ = minZ + 1;
            find:
            for (int dx = minX; dx <= maxX; dx++) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    Block near = portal.getRelative(dx, 0, dz);
                    if (near.getType() == Material.NETHER_PORTAL) {
                        portal = near;
                        break find;
                    }
                }
            }
            if (portal.getType() != Material.NETHER_PORTAL) {
                return defaultTravelAgent; // Not travelling using a nether portal
            }
        }
        DirectPortalInfo portalInfo = DirectTravelAgent.getPortalInfo(portal);
        Location centre = portalInfo.getCentre();
        centre.setPitch(from.getPitch());
        centre.setYaw(from.getYaw());
        if (centre.getWorld().getEnvironment() == World.Environment.NETHER
                && to.getWorld().getEnvironment() == World.Environment.NORMAL) {
            to.setX(centre.getX() * 8.0);
            to.setZ(centre.getZ() * 8.0);
            TRAVEL_AGENT.setSearchRadius(8);
        } else if (centre.getWorld().getEnvironment() == World.Environment.NORMAL
                && to.getWorld().getEnvironment() == World.Environment.NETHER) {
            to.setX(centre.getX() / 8.0);
            to.setZ(centre.getZ() / 8.0);
            TRAVEL_AGENT.setSearchRadius(0);
        } else {
            return defaultTravelAgent;
        }
        to.setY(centre.getY());
        ((DirectTravelAgent) TRAVEL_AGENT).setAxis(portalInfo.getAxis());
        return TRAVEL_AGENT;
    }
}
