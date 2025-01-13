package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.item.AbstractDiskItem;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.registry.SFMProgramLanguages;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BiFunction;

public interface IProgram {
    static @Nullable IProgram compileAndUpdateErrorsAndWarnings(ItemStack disk, ManagerBlockEntity manager) {
        BiFunction<ItemStack, @Nullable ManagerBlockEntity, ? extends @Nullable IProgram> compiler = SFMProgramLanguages.getCompileFunction((AbstractDiskItem) disk.getItem());
        return compiler.apply(disk, manager);
    }

    int configRevision();

    boolean tick(ManagerBlockEntity manager);

    Set<String> referencedLabels();
}
