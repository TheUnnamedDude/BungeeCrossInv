package org.theunnameddude.crossinv.inventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Map;

public class CrossItemStack {
    ItemStack itemStack;

    private static final byte fBall = 0;
    private static final byte fBallLarge = 1;
    private static final byte fStar = 2;
    private static final byte fBurst = 3;
    private static final byte fCreeper = 4;

    private static final byte book = 0;
    private static final byte enchantmentstorage = 1;
    private static final byte fireworkeffect = 2;
    private static final byte firework = 3;
    private static final byte leatherarmor = 4;
    private static final byte map = 5;
    private static final byte potion = 6;
    private static final byte repairable = 7;
    private static final byte skull = 8;

    public CrossItemStack(ItemStack item) {
        this.itemStack = item;
    }

    public static ItemStack toItemStack(ByteBuf buf) {
        Material material = Material.getMaterial( readString( buf ) );
        //ItemStack item = new ItemStack( buf.readShort(), buf.readByte() );
        ItemStack item = new ItemStack( material, buf.readByte() );
        item.setDurability(buf.readShort());

        byte enchSize = buf.readByte();
        for ( int i = 0; i < enchSize; i++ ) {
            Enchantment enchantment = Enchantment.getByName( readString( buf ) );
            //item.addUnsafeEnchantment(Enchantment.getById(buf.readInt()), buf.readInt());
            item.addUnsafeEnchantment( enchantment, buf.readInt() );
        }

        while ( buf.readableBytes() > 0 ) {
            item = readMeta( buf.readByte(), buf, item );
        }

        return item;
    }

    public ByteBuf getItemStack() {
        ByteBuf buf = Unpooled.buffer();
        return appendItemStack( buf );
    }

    public ByteBuf appendItemStack(ByteBuf buf) {
        // buf.writeShort(itemStack.getTypeId());
        writeString( itemStack.getType().name(), buf );
        buf.writeByte(itemStack.getAmount());
        buf.writeShort(itemStack.getDurability());


        int enchSize = itemStack.getEnchantments().size();
        buf.writeByte(enchSize);
        for ( Map.Entry<Enchantment, Integer> enchantment : itemStack.getEnchantments().entrySet() ) {
            writeString( enchantment.getKey().getName(), buf );
            //buf.writeInt( enchantment.getKey().getId() );
            buf.writeInt( enchantment.getValue().intValue() );
        }

        writeMeta( itemStack.getItemMeta(), buf );
        return buf;
    }

    private static ItemStack readMeta(int metaid, ByteBuf buf, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if ( metaid == book ) {
            BookMeta book = (BookMeta)meta;
            int size = buf.readByte();
            ArrayList<String> pages = new ArrayList<String>();
            for ( int i = 0; i < size; i++ ) {
                pages.add( readString( buf ) );
            }
            book.setPages( pages );
        } else if ( metaid == enchantmentstorage ) {
            EnchantmentStorageMeta ench = (EnchantmentStorageMeta)meta;
            int size = buf.readByte();
            for ( int i = 0; i < size; i++ ) {
                Enchantment enchantment = Enchantment.getByName( readString( buf ) );
                ench.addStoredEnchant( enchantment, buf.readByte(), false );
                //ench.addStoredEnchant( Enchantment.getById( buf.readByte() ), buf.readByte(), false );
            }
        } else if ( metaid == fireworkeffect ) {
            FireworkEffectMeta firework = (FireworkEffectMeta)meta;
            firework.setEffect( readEffect( buf ) );
        } else if ( metaid == firework ) {
            FireworkMeta firework = (FireworkMeta)meta;
            ArrayList<FireworkEffect> effects = new ArrayList<FireworkEffect>();
            int size = buf.readByte();
            for ( int i = 0; i < size; i++ ) {
                effects.add( readEffect( buf ) );
            }
        } else if ( metaid == leatherarmor ) {
            LeatherArmorMeta leather = (LeatherArmorMeta)meta;
            leather.setColor( Color.fromRGB( buf.readInt() ) );
        } else if ( metaid == map ) {
            MapMeta map = (MapMeta)meta;
            map.setScaling( buf.readBoolean() );
        } else if ( metaid == potion ) {
            PotionMeta potion = (PotionMeta)meta;
            int size = buf.readByte();
            for ( int i = 0; i < size; i++ ) {
                PotionEffectType effectType = PotionEffectType.getByName( readString( buf ) );
                //potion.addCustomEffect( new PotionEffect( PotionEffectType.getById( buf.readShort() ), buf.readByte()
                //        , buf.readInt() ), true );
                potion.addCustomEffect( new PotionEffect( effectType, buf.readByte(), buf.readInt() ), true );
            }
        } else if ( metaid == skull ) {
            SkullMeta skull = (SkullMeta)meta;
            String owner = readString( buf );
            if ( owner != null ) {
                skull.setOwner( owner );
            }
        } else if ( metaid == repairable ) {
            ((Repairable)item).setRepairCost( buf.readByte() );
        }

        item.setItemMeta( meta );
        return item;
    }

    public void writeMeta(ItemMeta meta, ByteBuf buf) {
        writeString( meta.getDisplayName(), buf );
        buf.writeByte(meta.getLore().size());
        for ( String lore : meta.getLore() ) {
            writeString(lore, buf);
        }
        if ( meta instanceof BookMeta ) {
            // ID, Author, title, pages(byte array)
            BookMeta book = (BookMeta)meta;
            buf.writeByte( this.book );
            writeString(book.getAuthor(), buf);
            writeString( book.getTitle(), buf );
            buf.writeByte( book.getPageCount() );
            for ( String page : book.getPages() ) {
                writeString( page, buf );
            }
        }
        if ( meta instanceof EnchantmentStorageMeta ) {
            // ID, enchantments(byte array)
            EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta)meta;
            buf.writeByte( enchantmentstorage );
            int size = enchantmentStorage.getStoredEnchants().size();
            buf.writeByte( size );
            for ( Map.Entry<Enchantment, Integer> enchantment : enchantmentStorage.getStoredEnchants().entrySet() ) {
                writeString( enchantment.getKey().getName(), buf );
                // buf.writeByte(enchantment.getKey().getId());
                buf.writeByte( enchantment.getValue().intValue() );
            }
        }
        if ( meta instanceof FireworkEffectMeta ) {
            // ID, fireworkeffect
            FireworkEffectMeta firework = (FireworkEffectMeta)meta;
            buf.writeByte( fireworkeffect );
            writeFirework(firework.getEffect(), buf);
        }
        if ( meta instanceof FireworkMeta ) {
            // ID, fireworkeffect(byte array)
            buf.writeByte( firework );
            FireworkMeta firework = (FireworkMeta)meta;
            buf.writeByte( firework.getPower() );
            buf.writeByte( firework.getEffectsSize() );
            for ( FireworkEffect effect : firework.getEffects() ) {
                writeFirework( effect, buf );
            }
        }
        if ( meta instanceof LeatherArmorMeta ) {
            // ID and color as RGB
            LeatherArmorMeta leather = (LeatherArmorMeta)meta;
            buf.writeByte( leatherarmor );
            buf.writeInt( leather.getColor().asRGB() );
        }
        if ( meta instanceof MapMeta ) {
            // ID and boolean
            MapMeta map = (MapMeta)meta;
            buf.writeByte( this.map );
            buf.writeBoolean( map.isScaling() );
        }
        if ( meta instanceof PotionMeta ) {
            // ID and potioneffet(bytearray)
            buf.writeByte( potion );
            PotionMeta potion = (PotionMeta)meta;
            buf.writeByte( potion.getCustomEffects().size() );
            for ( PotionEffect effect : potion.getCustomEffects() ) {
                writeString( effect.getType().getName(), buf );
                // buf.writeShort( effect.getType().getId() );
                buf.writeByte( effect.getAmplifier() );
                buf.writeInt( effect.getDuration() );
            }
        }
        if ( meta instanceof SkullMeta ) {
            // ID and skullowner
            buf.writeByte( skull );
            SkullMeta skull = (SkullMeta)meta;
            writeString( skull.getOwner(), buf );
        }
        if ( itemStack instanceof Repairable ) {
            // ID and repair cost
            buf.writeByte( repairable );
            Repairable repairable = (Repairable)itemStack;
            buf.writeByte( repairable.getRepairCost() );
        }
    }
    /*
    BookMeta	Represents a book (Material.BOOK_AND_QUILL or Material.WRITTEN_BOOK) that can have a title, an author, and pages.
    EnchantmentStorageMeta	EnchantmentMeta is specific to items that can store enchantments, as opposed to being enchanted.
    FireworkEffectMeta	Represents a meta that can store a single FireworkEffect.
    FireworkMeta	Represents a Material.FIREWORK and its effects.
    ItemMeta	This type represents the storage mechanism for auxiliary item data.
    LeatherArmorMeta	Represents leather armor (Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET, or Material.LEATHER_LEGGINGS) that can be colored.
    MapMeta	Represents a map that can be scalable.
    PotionMeta	Represents a potion (Material.POTION) that can have custom effects.
    Repairable	Represents an item that can be repaired at an anvil.
    SkullMeta	Represents a skull (Material.SKULL_ITEM) that can have an owner.
     */

    private static void writeString(String string, ByteBuf buf) {
        if ( string == null ) {
            buf.writeShort( 0 );
        } else {
            int size = string.length();
            buf.writeShort( size );
            for ( char c : string.toCharArray() ) {
                buf.writeByte( c );
            }
        }
    }


    static String readString(ByteBuf buf) {
        int size = buf.readShort();
        byte[] chars = new byte[size];
        for ( int i = 0; i < size; i++ ) {
            chars[i] = buf.readByte();
        }
        return new String( chars );
    }

    private static FireworkEffect readEffect(ByteBuf buf) {
        FireworkEffect.Type type = byteToEffect(buf.readByte());
        boolean flicker = buf.readBoolean();
        boolean trail = buf.readBoolean();
        FireworkEffect.Builder builder = FireworkEffect.builder().flicker( flicker ).trail( trail ).with( type );
        int colorCount = buf.readByte();
        for ( int i = 0; i < colorCount; i++ ) {
            builder.withColor( Color.fromRGB( buf.readInt() ) );
        }
        int fadedColorCount = buf.readByte();
        for ( int i = 0; i < colorCount; i++ ) {
            builder.withFade(Color.fromRGB(buf.readInt()));
        }
        return builder.build();
    }

    private void writeFirework(FireworkEffect effect, ByteBuf buf) {
        buf.writeByte( effectToByte( effect.getType() ) );
        buf.writeBoolean( effect.hasFlicker() );
        buf.writeBoolean( effect.hasTrail() );
        buf.writeInt( effect.getColors().size() );
        for ( Color color : effect.getColors() ) {
            buf.writeInt( color.asRGB() );
        }
        buf.writeInt( effect.getFadeColors().size() );
        for ( Color color : effect.getColors() ) {
            buf.writeInt( color.asRGB() );
        }
    }

    private static FireworkEffect.Type byteToEffect(byte b) {
        if ( b == fBall ) {
            return FireworkEffect.Type.BALL;
        } else if ( b == fBallLarge ) {
            return FireworkEffect.Type.BALL_LARGE;
        } else if ( b == fBurst ) {
            return FireworkEffect.Type.BURST;
        } else if ( b == fCreeper ) {
            return FireworkEffect.Type.CREEPER;
        } else if ( b == fStar ) {
            return FireworkEffect.Type.STAR;
        } else {
            return FireworkEffect.Type.BALL;
        }
    }

    private byte effectToByte(FireworkEffect.Type type) {
        if ( type == FireworkEffect.Type.BALL ) {
            return fBall;
        } else if ( type == FireworkEffect.Type.BALL_LARGE ) {
            return fBallLarge;
        } else if ( type == FireworkEffect.Type.BURST ) {
            return fBurst;
        } else if ( type == FireworkEffect.Type.CREEPER ) {
            return fCreeper;
        } else if ( type == FireworkEffect.Type.STAR ) {
            return fStar;
        } else {
            return fBall;
        }
    }
}
