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
package me.xhawk87.DirectPortals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;

/**
 *
 * @author XHawk87
 */
public class DirectTravelAgent implements TravelAgent {

    private static final Material[][][] PORTAL_TEMPLATE = new Material[4][3][5];

    static {
        // Base
        PORTAL_TEMPLATE[0][1][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[1][1][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[2][1][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][1][0] = Material.OBSIDIAN;

        // Frame
        PORTAL_TEMPLATE[0][1][1] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[0][1][2] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[0][1][3] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][1][1] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][1][2] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][1][3] = Material.OBSIDIAN;

        // Top
        PORTAL_TEMPLATE[0][1][4] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[1][1][4] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[2][1][4] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][1][4] = Material.OBSIDIAN;

        // Portal
        PORTAL_TEMPLATE[1][1][1] = Material.PORTAL;
        PORTAL_TEMPLATE[1][1][2] = Material.PORTAL;
        PORTAL_TEMPLATE[1][1][3] = Material.PORTAL;
        PORTAL_TEMPLATE[2][1][1] = Material.PORTAL;
        PORTAL_TEMPLATE[2][1][2] = Material.PORTAL;
        PORTAL_TEMPLATE[2][1][3] = Material.PORTAL;

        // Ledges
        PORTAL_TEMPLATE[0][0][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[1][0][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[2][0][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][0][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[0][2][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[1][2][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[2][2][0] = Material.OBSIDIAN;
        PORTAL_TEMPLATE[3][2][0] = Material.OBSIDIAN;

        // Roof
        PORTAL_TEMPLATE[0][0][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[1][0][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[2][0][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[3][0][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[0][2][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[1][2][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[2][2][4] = Material.COBBLESTONE;
        PORTAL_TEMPLATE[3][2][4] = Material.COBBLESTONE;
    }
    private int searchRadius = 0;

    @Override
    public TravelAgent setSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }

    @Override
    public int getSearchRadius() {
        return searchRadius;
    }

    @Override
    public TravelAgent setCreationRadius(int radius) {
        return this;
    }

    @Override
    public int getCreationRadius() {
        return 0;
    }

    @Override
    public boolean getCanCreatePortal() {
        return true;
    }

    @Override
    public void setCanCreatePortal(boolean create) {
        // ignore
    }

    @Override
    public Location findOrCreate(Location location) {
        Location destination = findPortal(location);
        if (destination == null) {
            destination = location.clone();
            createPortal(destination);
        }
        return destination;
    }

    @Override
    public Location findPortal(Location location) {
        Block block = location.getBlock();
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                Block portal = block.getRelative(dx, 0, dz);
                if (portal.getType() == Material.PORTAL) {
                    // Find portal centre
                    Location centre = findPortalCentre(portal);
                    centre.setYaw(location.getYaw());
                    centre.setYaw(location.getPitch());
                    return centre;
                }
            }
        }
        return null;
    }

    @Override
    public boolean createPortal(Location location) {
        float yaw = location.getYaw();
        int xFacing = 0;
        int zFacing = 0;
        if ((yaw > -45 && yaw <= 45) || (yaw > 135 && yaw <= 225)) {
            // Facing along the X-axis
            xFacing = 1;
        } else {
            // Facing along the Z-axis
            zFacing = 1;
        }
        for (int length = 0; length < PORTAL_TEMPLATE.length; length++) {
            for (int depth = 0; depth < PORTAL_TEMPLATE[0].length; depth++) {
                for (int height = 0; height < PORTAL_TEMPLATE[0][0].length; height++) {
                    Material material = PORTAL_TEMPLATE[length][depth][height];
                    if (material == null) {
                        material = Material.AIR;
                    }
                    int xOffset = (xFacing * (length - 1)) + (zFacing * (depth - 1));
                    int yOffset = height - 1;
                    int zOffset = (zFacing * (length - 1)) + (xFacing * (depth - 1));
                    location.getBlock().getRelative(xOffset, yOffset, zOffset).setTypeId(material.getId(), false);
                }
            }
        }
        return true;
    }

    public static Location findPortalCentre(Block portal) {
        if (portal.getType() != Material.PORTAL) {
            throw new IllegalArgumentException("There is no portal at " + portal.toString());
        }
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        double minZ = 0;
        double maxZ = 0;
        while (portal.getRelative((int) minX - 1, 0, 0).getType() == Material.PORTAL) {
            minX--;
        }
        if (portal.getRelative((int) minX - 1, 0, 0).getType() == Material.OBSIDIAN) {
            while (portal.getRelative((int) maxX + 1, 0, 0).getType() == Material.PORTAL) {
                maxX++;
            }
            if (portal.getRelative((int) minX - 1, 0, 0).getType() != Material.OBSIDIAN) {
                minX = 0;
                maxX = 0;
            }
        } else {
            minX = 0;
            maxX = 0;
        }
        while (portal.getRelative(0, (int) minY - 1, 0).getType() == Material.PORTAL) {
            minY--;
        }
        if (portal.getRelative(0, (int) minY - 1, 0).getType() == Material.OBSIDIAN) {
            while (portal.getRelative(0, (int) maxY + 1, 0).getType() == Material.PORTAL) {
                maxY++;
            }
            if (portal.getRelative(0, (int) minY - 1, 0).getType() != Material.OBSIDIAN) {
                minY = 0;
                maxY = 0;
            }
        } else {
            minY = 0;
            maxY = 0;
        }
        while (portal.getRelative(0, 0, (int) minZ - 1).getType() == Material.PORTAL) {
            minZ--;
        }
        if (portal.getRelative(0, 0, (int) minZ - 1).getType() == Material.OBSIDIAN) {
            while (portal.getRelative(0, 0, (int) maxZ + 1).getType() == Material.PORTAL) {
                maxZ++;
            }
            if (portal.getRelative(0, 0, (int) minZ - 1).getType() != Material.OBSIDIAN) {
                minZ = 0;
                maxZ = 0;
            }
        } else {
            minZ = 0;
            maxZ = 0;
        }
        double x = portal.getX() + ((maxX + 1.0) + minX) / 2.0;
        double y = portal.getY() + minY;
        double z = portal.getZ() + ((maxZ + 1.0) + minZ) / 2.0;
        return new Location(portal.getWorld(), x, y, z);
    }
}
