package ca.teamdman.sfm.common.resourcetype;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.NotImplementedException;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;

import java.util.stream.Stream;

import static net.minecraftforge.common.capabilities.CapabilityManager.get;

public class SourceResourceType extends ResourceType<Integer, Class<Integer>, ISourceTile> {
    public static final Capability<ISourceTile> CAP = get(new CapabilityToken<>() {
    });

    public SourceResourceType() {
        super(CAP);
    }

    @Override
    public long getAmount(Integer integer) {
        return integer;
    }

    @Override
    public Integer getStackInSlot(
            ISourceTile sourceTile,
            int slot
    ) {
        return sourceTile.getSource();
    }

    @Override
    public Integer extract(
            ISourceTile sourceTile,
            int slot,
            long amount,
            boolean simulate
    ) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;

        int source = sourceTile.getSource();
        int extractAmount = Math.min(finalAmount, source);
        /// WARNING: This sucks and is only done because Ars doesn't let you simulate actions
        if (!simulate) {
            sourceTile.removeSource(extractAmount);
        }
        return extractAmount;
    }

    @Override
    public Stream<ResourceLocation> getTagsForStack(Integer integer) {
        return Stream.empty();
    }

    @Override
    public int getSlots(ISourceTile handler) {
        return 1;
    }

    @Override
    public long getMaxStackSize(Integer integer) {
        return Long.MAX_VALUE;
    }

    @Override
    public long getMaxStackSizeForSlot(
            ISourceTile sourceTile,
            int slot
    ) {
        return sourceTile.getMaxSource();
    }

    @Override
    public Integer insert(
            ISourceTile sourceTile,
            int slot,
            Integer stack,
            boolean simulate
    ) {
        int space = Mth.clamp(sourceTile.getMaxSource() - sourceTile.getSource(), 0, sourceTile.getMaxSource());

        int accepted = Math.min(stack, space);

        /// WARNING: This sucks and is only done because Ars doesn't let you simulate actions
        if (!simulate) {
            int oldSource = sourceTile.getSource();

            sourceTile.addSource(accepted);

            int newSource = sourceTile.getSource();
            int difference = newSource - oldSource;
            accepted -= difference;
        }
        return stack - accepted;
    }

    @Override
    public boolean isEmpty(Integer stack) {
        return stack == 0;
    }

    @Override
    public boolean matchesStackType(Object o) {
        return o instanceof Integer;
    }

    @Override
    public boolean matchesCapabilityType(Object o) {
        return o instanceof ISourceTile;
    }

    @Override
    public Integer getEmptyStack() {
        return 0;
    }

    public static final ResourceLocation REGISTRY_KEY = new ResourceLocation("ars_nouveau", "source");

    @Override
    public ResourceLocation getRegistryKey(Integer stack) {
        return REGISTRY_KEY;
    }

    @Override
    public IForgeRegistry<Class<Integer>> getRegistry() {
        throw new NotImplementedException();
    }

    @Override
    public boolean registryKeyExists(ResourceLocation location) {
        return location.equals(REGISTRY_KEY);
    }

    @Override
    public Class<Integer> getItem(Integer stack) {
        return Integer.class;
    }

    @Override
    public Integer copy(Integer stack) {
        return stack;
    }

    @Override
    protected Integer setCount(
            Integer stack,
            long amount
    ) {
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
