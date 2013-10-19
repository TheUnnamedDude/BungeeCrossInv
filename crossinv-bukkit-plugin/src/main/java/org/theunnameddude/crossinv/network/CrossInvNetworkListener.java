package org.theunnameddude.crossinv.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.theunnameddude.crossinv.CrossInvBukkitPlugin;
import org.theunnameddude.crossinv.inventory.CrossInventory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class CrossInvNetworkListener implements PluginMessageListener {
    public static final String GETINV = "GETINV";
    CrossInvBukkitPlugin main;

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if ( channel.equals( CrossInvBukkitPlugin.channelName ) ) {
            ByteBuf buf = Unpooled.copiedBuffer( bytes );
            CrossInventory cInv = CrossInventory.readInventory( buf );
            if ( cInv.getInventoryType() == CrossInventory.inventory ) {
                PlayerInventory inv = player.getInventory();
                inv.clear();
                ItemStack[] items = cInv.getInv();
                int size = inv.getSize();
                for ( int i = 0; i < size; i++ ) {
                    inv.setItem( i, items[ i ] );
                }
                inv.setHelmet( items[ size - 4 ] );
                inv.setChestplate( items[ size - 3 ] );
                inv.setLeggings( items[ size - 2 ] );
                inv.setBoots( items[ size - 1 ] );
            }
        } else if ( channel.equals( CrossInvBukkitPlugin.bungeeCommandChannel ) ) {
            ByteArrayInputStream byteIn = new ByteArrayInputStream( bytes );
            DataInputStream in = new DataInputStream( byteIn );
            try {
                String command = in.readUTF();
                if ( command.equals( GETINV ) ) {
                    main.sendInventoryUpdate( player );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
