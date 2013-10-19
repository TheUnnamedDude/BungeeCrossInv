package org.theunnameddude.crossinv;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.theunnameddude.crossinv.inventory.CrossInventory;
import org.theunnameddude.crossinv.network.CrossInvNetworkListener;

public class CrossInvBukkitPlugin extends JavaPlugin {
    public static final String channelName = "CInv";
    public static final String bungeeCommandChannel = "CInvBungee";
    public CrossInvNetworkListener networkListener = new CrossInvNetworkListener();

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel( this, channelName, networkListener );
        getServer().getMessenger().registerIncomingPluginChannel( this, bungeeCommandChannel, networkListener );
        getServer().getMessenger().registerOutgoingPluginChannel( this, channelName );
    }

    public void sendInventoryUpdate(Player player) {
        player.sendPluginMessage( this, channelName, CrossInventory.toCrossInventory( player ).writeInventory().array() );
    }
}
