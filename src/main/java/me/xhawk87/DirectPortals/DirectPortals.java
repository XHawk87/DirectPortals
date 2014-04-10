/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.DirectPortals;

import me.xhawk87.DirectPortals.listeners.PortalListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author XHawk87
 */
public class DirectPortals extends JavaPlugin {

    @Override
    public void onEnable() {
        new PortalListener().registerEvents(this);
    }
}
