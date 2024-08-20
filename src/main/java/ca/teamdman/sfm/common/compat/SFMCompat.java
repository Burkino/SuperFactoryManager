package ca.teamdman.sfm.common.compat;

import ca.teamdman.sfm.common.resourcetype.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class SFMCompat {
    public static boolean isMekanismLoaded() {
        return ModList.get().getModContainerById("mekanism").isPresent();
    }
    public static boolean isBotaniaLoaded() {
        return ModList.get().getModContainerById("botania").isPresent();
    }

    public static List<Capability<?>> getCapabilities() {
        List<Capability<?>> capabilities = List.of(
                ForgeCapabilities.ITEM_HANDLER,
                ForgeCapabilities.FLUID_HANDLER,
                ForgeCapabilities.ENERGY
        );

        if (isMekanismLoaded()) {
            capabilities.addAll(List.of(
                    GasResourceType.CAP,
                    InfuseResourceType.CAP,
                    PigmentResourceType.CAP,
                    SlurryResourceType.CAP
            ));
        }

        if (isBotaniaLoaded()) {
            capabilities.add(ManaResourceType.CAP);
        }

        return capabilities;
    }
}
