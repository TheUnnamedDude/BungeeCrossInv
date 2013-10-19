package org.theunnameddude.crossinv.inventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CrossInventory {
    @Getter
    @NonNull
    ItemStack[] inv;
    @Getter
    @NonNull
    byte inventoryType;

    public static final byte inventory = 0;

    public static CrossInventory toCrossInventory(Player player) {
        int invSize = player.getInventory().getSize();
        ItemStack[] items = new ItemStack[ invSize + 4 ];

        for ( int i = 0; i < invSize; i++ ) {
            items[i] = player.getInventory().getItem( i );
        }

        items[ invSize + 1 ] = player.getInventory().getHelmet();
        items[ invSize + 2 ] = player.getInventory().getChestplate();
        items[ invSize + 3 ] = player.getInventory().getLeggings();
        items[ invSize + 4 ] = player.getInventory().getBoots();

        return new CrossInventory( items, inventory );
    }

    // TODO Enderchests?

    public static CrossInventory readInventory(ByteBuf buf) {
        byte inventoryType = buf.readByte();
        ItemStack[] inventory = new ItemStack[ buf.readByte() ];
        int size = buf.readByte();
        for ( int i = 0; i < size; i++ ) {
            inventory[ buf.readByte() ] = CrossItemStack.toItemStack( buf );
        }
        return new CrossInventory( inventory, inventoryType );
    }

    public ByteBuf writeInventory() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte( inventoryType );
        Map<Integer, CrossItemStack> items = getMappedItems();

        buf.writeByte( inv.length ); // Size of array when reading

        buf.writeByte( items.size() );

        for ( Map.Entry<Integer, CrossItemStack> entry : items.entrySet() ) {
            buf.writeByte(entry.getKey().intValue());
            entry.getValue().appendItemStack( buf );
        }
        return buf;
    }

    private Map<Integer, CrossItemStack> getMappedItems() {
        HashMap<Integer, CrossItemStack> mappedItems = new HashMap<Integer, CrossItemStack>();
        for ( int i = 0; i < inv.length; i++ ) {
            ItemStack item = inv[ i ];
            if ( item != null && item.getType() != Material.AIR ) {
                mappedItems.put( i, new CrossItemStack( item ) );
            }
        }
        return mappedItems;
    }
}
