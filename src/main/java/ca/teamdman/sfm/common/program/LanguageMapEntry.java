package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.item.AbstractDiskItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public record LanguageMapEntry(Class<? extends AbstractDiskItem> itemClass,
                               BiFunction<ItemStack, @Nullable ManagerBlockEntity, ? extends @Nullable IProgram> compileFunction) {
}