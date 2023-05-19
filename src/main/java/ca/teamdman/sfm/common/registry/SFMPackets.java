package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.net.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SFMPackets {
    public static final String MANAGER_CHANNEL_VERSION = "1";
    public static final String LABEL_GUN_ITEM_CHANNEL_VERSION = "1";
    public static final String DISK_ITEM_CHANNEL_VERSION = "1";
    public static final SimpleChannel MANAGER_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SFM.MOD_ID, "manager"),
            MANAGER_CHANNEL_VERSION::toString,
            MANAGER_CHANNEL_VERSION::equals,
            MANAGER_CHANNEL_VERSION::equals
    );
    public static final SimpleChannel LABEL_GUN_ITEM_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SFM.MOD_ID, "labelgun"),
            LABEL_GUN_ITEM_CHANNEL_VERSION::toString,
            LABEL_GUN_ITEM_CHANNEL_VERSION::equals,
            LABEL_GUN_ITEM_CHANNEL_VERSION::equals
    );
    public static final SimpleChannel DISK_ITEM_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SFM.MOD_ID, "disk"),
            DISK_ITEM_CHANNEL_VERSION::toString,
            DISK_ITEM_CHANNEL_VERSION::equals,
            DISK_ITEM_CHANNEL_VERSION::equals
    );

    public static void register() {
        MANAGER_CHANNEL.registerMessage(
                0,
                ServerboundManagerProgramPacket.class,
                ServerboundManagerProgramPacket::encode,
                ServerboundManagerProgramPacket::decode,
                ServerboundManagerProgramPacket::handle
        );
        MANAGER_CHANNEL.registerMessage(
                1,
                ServerboundManagerResetPacket.class,
                ServerboundManagerResetPacket::encode,
                ServerboundManagerResetPacket::decode,
                ServerboundManagerResetPacket::handle
        );
        MANAGER_CHANNEL.registerMessage(
                2,
                ServerboundManagerFixPacket.class,
                ServerboundManagerFixPacket::encode,
                ServerboundManagerFixPacket::decode,
                ServerboundManagerFixPacket::handle
        );
        MANAGER_CHANNEL.registerMessage(
                3,
                ClientboundManagerGuiPacket.class,
                ClientboundManagerGuiPacket::encode,
                ClientboundManagerGuiPacket::decode,
                ClientboundManagerGuiPacket::handle
        );


        LABEL_GUN_ITEM_CHANNEL.registerMessage(
                0,
                ServerboundLabelGunUpdatePacket.class,
                ServerboundLabelGunUpdatePacket::encode,
                ServerboundLabelGunUpdatePacket::decode,
                ServerboundLabelGunUpdatePacket::handle
        );
        LABEL_GUN_ITEM_CHANNEL.registerMessage(
                1,
                ServerboundLabelGunPrunePacket.class,
                ServerboundLabelGunPrunePacket::encode,
                ServerboundLabelGunPrunePacket::decode,
                ServerboundLabelGunPrunePacket::handle
        );
        LABEL_GUN_ITEM_CHANNEL.registerMessage(
                2,
                ServerboundLabelGunClearPacket.class,
                ServerboundLabelGunClearPacket::encode,
                ServerboundLabelGunClearPacket::decode,
                ServerboundLabelGunClearPacket::handle
        );

        DISK_ITEM_CHANNEL.registerMessage(
                0,
                ServerboundDiskItemSetProgramPacket.class,
                ServerboundDiskItemSetProgramPacket::encode,
                ServerboundDiskItemSetProgramPacket::decode,
                ServerboundDiskItemSetProgramPacket::handle
        );
    }

    public static <MENU extends AbstractContainerMenu, BE extends BlockEntity> void handleServerboundContainerPacket(
            @Nullable Supplier<NetworkEvent.Context> ctxSupplier,
            Class<MENU> menuClass,
            Class<BE> blockEntityClass,
            BlockPos pos,
            int containerId,
            BiConsumer<MENU, BE> callback
    ) {
        if (ctxSupplier == null) return;

        var ctx = ctxSupplier.get();
        if (ctx == null) return;
        ctx.enqueueWork(() -> {
            var sender = ctx.getSender();
            if (sender == null) return;
            if (sender.isSpectator()) return; // ignore packets from spectators

            var menu = sender.containerMenu;
            if (!menuClass.isInstance(menu)) return;
            if (menu.containerId != containerId) return;

            var level = sender.getLevel();
            //noinspection ConstantValue
            if (level == null) return;
            if (!level.isLoaded(pos)) return;

            var blockEntity = level.getBlockEntity(pos);
            if (!blockEntityClass.isInstance(blockEntity)) return;
            //noinspection unchecked
            callback.accept((MENU) menu, (BE) blockEntity);
        });
    }
}
