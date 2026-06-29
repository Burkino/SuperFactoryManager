package ca.teamdman.sfm.common.resourcetype;

import ca.teamdman.sfm.common.block.BufferBlock;
import ca.teamdman.sfm.common.blockentity.BufferBlockEntityContents;
import ca.teamdman.sfm.common.capability.SFMWellKnownCapabilities;
import ca.teamdman.sfm.common.registry.SFMRegistryWrapper;
import ca.teamdman.sfm.common.registry.SFMWellKnownRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.stream.Stream;

public class FluidResourceType extends RegistryBackedResourceType<ResourceStack<FluidResource>, Fluid, ResourceHandler<FluidResource>> {
    private static final ResourceStack<FluidResource> EMPTY_STACK = new ResourceStack<>(FluidResource.EMPTY, 0);

    public FluidResourceType() {
        super(SFMWellKnownCapabilities.FLUID_HANDLER);
    }

    @Override
    public SFMRegistryWrapper<Fluid> getRegistry() {
        return SFMWellKnownRegistries.FLUIDS;
    }

    @Override
    public Fluid getItem(ResourceStack<FluidResource> fluidStack) {
        return fluidStack.resource().getFluid();
    }

    @Override
    public ResourceStack<FluidResource> copy(ResourceStack<FluidResource> fluidStack) {
        return new ResourceStack<>(fluidStack.resource(), fluidStack.amount());
    }

    @Override
    public Stream<Identifier> getTagsForStack(ResourceStack<FluidResource> fluidStack) {
        //noinspection deprecation
        return fluidStack.resource().getFluid().builtInRegistryHolder().tags().map(TagKey::location);
    }

    @Override
    protected ResourceStack<FluidResource> setCount(ResourceStack<FluidResource> fluidStack, long amount) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        return new ResourceStack<>(fluidStack.resource(), finalAmount);
    }

    @Override
    public ResourceHandler<FluidResource> createHandlerForBufferBlock(BufferBlockEntityContents contents) {
        return new FluidStacksResourceHandler(1, contents.tier.getIntMaxStackSize()) {
            @Override
            public boolean isValid(int index, FluidResource resource) {
                boolean isValid = this.getAmountAsInt(index) > 0 || contents.isEmpty();
                if (isValid) {
                    contents.lastUsedResource = BufferBlock.ContainedResource.Fluid;
                }
                return isValid;
            }
        };
    }

    @Override
    public long getAmount(ResourceStack<FluidResource> stack) {
        return stack.amount();
    }

    @Override
    public ResourceStack<FluidResource> getStackInSlot(ResourceHandler<FluidResource> handler, int slot) {
        return new ResourceStack<>(handler.getResource(slot), handler.getAmountAsInt(slot));
    }

    /**
     * @return stack that was extracted
     */
    @Override
    public ResourceStack<FluidResource> extract(
            ResourceHandler<FluidResource> handler,
            int slot,
            long amount_long,
            TransactionContext tx
    ) {
        int finalAmount = amount_long > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount_long;

        try (var ctx = Transaction.open(tx)) {
            FluidResource resource = handler.getResource(slot);
            int extracted = handler.extract(slot, resource, finalAmount, ctx);

            ctx.commit();
            return new ResourceStack<>(resource, extracted);
        }
    }

    @Override
    public boolean matchesStackType(Object o) {
        return o instanceof ResourceStack<?> stack && stack.resource() instanceof FluidResource;
    }

    @Override
    public boolean matchesCapabilityHandler(Object o) {
        return o instanceof ResourceHandler<?>;
    }

    @Override
    public int getSlots(ResourceHandler<FluidResource> handler) {
        return handler.size();
    }

    @Override
    public long getMaxStackSize(ResourceStack<FluidResource> fluidStack) {
        return Long.MAX_VALUE;
    }

    @Override
    public long getMaxStackSizeForSlot(ResourceHandler<FluidResource> handler, int slot) {
        return handler.getCapacityAsLong(slot, FluidResource.EMPTY);
    }

    /**
     * @return remaining stack that was not inserted
     */
    @Override
    public ResourceStack<FluidResource> insert(ResourceHandler<FluidResource> handler, int slot, ResourceStack<FluidResource> stack, TransactionContext tx) {
        try (var ctx = Transaction.open(tx)) {
            var inserted = handler.insert(slot, stack.resource(), stack.amount(), ctx);
            ctx.commit();

            return new ResourceStack<>(stack.resource(), stack.amount() - inserted);
        }
    }

    @Override
    public boolean isEmpty(ResourceStack<FluidResource> stack) {
        return stack.isEmpty();
    }

    @Override
    public ResourceStack<FluidResource> getEmptyStack() {
        return EMPTY_STACK;
    }
}
