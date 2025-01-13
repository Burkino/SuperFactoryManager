package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.item.AbstractDiskItem;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.program.IProgram;
import ca.teamdman.sfm.common.program.LanguageMapEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SFMProgramLanguages {
    public static final ResourceLocation REGISTRY_ID = new ResourceLocation(SFM.MOD_ID, "disk_to_language");

    public static final DeferredRegister<LanguageMapEntry> LANGUAGES = DeferredRegister.create(REGISTRY_ID, SFM.MOD_ID);
    private static final Supplier<IForgeRegistry<LanguageMapEntry>> DEFERRED_LANGUAGES = LANGUAGES.makeRegistry(
            () -> new RegistryBuilder<LanguageMapEntry>().setName(REGISTRY_ID)
    );

    public static final RegistryObject<LanguageMapEntry> SFML = LANGUAGES.register(
            "sfml",
            () -> new LanguageMapEntry(DiskItem.class, DiskItem::compileAndUpdateErrorsAndWarnings)
    );

    public static BiFunction<ItemStack, @Nullable ManagerBlockEntity, ? extends IProgram> getCompileFunction(AbstractDiskItem item) {
        Optional<RegistryObject<LanguageMapEntry>> compiler = LANGUAGES.getEntries().stream()
                .filter((x) -> x.get().itemClass() == item.getClass())
                .findFirst();
        if (compiler.isPresent()) {
            return compiler.get().get().compileFunction();
        }
        throw new IllegalArgumentException("No program registered for item class: " + item.getClass().getName());
    }

    public static void register(IEventBus bus) {
        LANGUAGES.register(bus);
    }
}
