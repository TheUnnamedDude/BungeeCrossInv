package org.theunnameddude.crossinv;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

@RequiredArgsConstructor
public class NetworkListener implements Listener {
    @NonNull
    CrossInvBungeePlugin main;

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if ( event.getTag().equals( CrossInvBungeePlugin.bungeeSyncChannel ) ) {
            inventories.put( ((ProxiedPlayer)event.getSender()).getName(), new CrossInventory( event.getData() ) );
        }
    }

    HashMap<String, CrossInventory> inventories = new HashMap<String, CrossInventory>();

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        CrossInventory inv = inventories.get( event.getPlayer().getName() );
        event.getPlayer().sendData( CrossInvBungeePlugin.bungeeSyncChannel, inv.getContent() );
    }

    @SneakyThrows(IOException.class)
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( byteOut );
        out.writeUTF( "GETINV" );
        event.getPlayer().sendData( CrossInvBungeePlugin.bungeeCommandChannel, byteOut.toByteArray() );
    }
}
