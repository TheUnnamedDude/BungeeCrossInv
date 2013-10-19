package org.theunnameddude.crossinv;

import net.md_5.bungee.api.plugin.Plugin;

public class CrossInvBungeePlugin extends Plugin {
    public static final String bungeeSyncChannel = "CInv";
    public static final String bungeeCommandChannel = "CInvBungee";

    public final NetworkListener networkListener = new NetworkListener( this );

    @Override
    public void onEnable() {
        getProxy().registerChannel( bungeeSyncChannel );
        getProxy().registerChannel( bungeeCommandChannel );
        getProxy().getPluginManager().registerListener( this, networkListener );
    }

}
