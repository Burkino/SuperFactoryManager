package ca.teamdman.sfm.common.resourcetype;

import ca.teamdman.sfm.common.blockentity.BufferBlockEntityContents;
import ca.teamdman.sfm.common.capability.SFMBlockCapabilityKind;
import ca.teamdman.sfm.common.registry.SFMRegistryWrapper;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.stream.Stream;

public class ChemicalResourceType extends RegistryBackedResourceType<ResourceStack<ChemicalResource>, Chemical, ResourceHandler<ChemicalResource>> {
    private static final ResourceStack<ChemicalResource> EMPTY_STACK = new ResourceStack<>(ChemicalResource.EMPTY, 0);

    public static final SFMBlockCapabilityKind<ResourceHandler<ChemicalResource>> CAP = new SFMBlockCapabilityKind<>(
            Capabilities.CHEMICAL.block()
    );

    public ChemicalResourceType() {
        super(CAP);
    }

    @Override
    public ResourceHandler<ChemicalResource> createHandlerForBufferBlock(BufferBlockEntityContents contents) {
        return BasicChemicalTank.create(
                contents.tier.getLongScalarMaxStackSize(),
                null
        );
    }

    @Override
    public long getAmount(ResourceStack<ChemicalResource> stack) {
        return stack.amount();
    }

    @Override
    public ResourceStack<ChemicalResource> getStackInSlot(
            ResourceHandler<ChemicalResource> handler,
            int slot
    ) {
        return new ResourceStack<>(handler.getResource(slot), handler.getAmountAsInt(slot));
    }

    @Override
    public Stream<Identifier> getTagsForStack(ResourceStack<ChemicalResource> stack) {
        return stack.resource().tags().map(TagKey::location);
    }

    @Override
    public ResourceStack<ChemicalResource> extract(
            ResourceHandler<ChemicalResource> handler,
            int slot,
            long amount,
            TransactionContext tx
    ) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        try (var ctx = Transaction.open(tx)) {
            ChemicalResource resource = handler.getResource(slot);
            int extracted = handler.extract(slot, resource, finalAmount, ctx);

            ctx.commit();
            return new ResourceStack<>(resource, extracted);
        }
    }

    @Override
    public int getSlots(ResourceHandler<ChemicalResource> handler) {
        return handler.size();
    }

    @Override
    public long getMaxStackSize(ResourceStack<ChemicalResource> stack) {
        return Long.MAX_VALUE;
    }

    @Override
    public long getMaxStackSizeForSlot(
            ResourceHandler<ChemicalResource> handler,
            int slot
    ) {
        return handler.getCapacityAsLong(slot, ChemicalResource.EMPTY);
    }

    @Override
    public ResourceStack<ChemicalResource> insert(
            ResourceHandler<ChemicalResource> handler,
            int slot,
            ResourceStack<ChemicalResource> stack,
            TransactionContext tx
    ) {
        try (var ctx = Transaction.open(tx)) {
            int inserted = handler.insert(slot, stack.resource(), stack.amount(), ctx);
            ctx.commit();

            return new ResourceStack<>(stack.resource(), stack.amount() - inserted);
        }
    }

    @Override
    public boolean isEmpty(ResourceStack<ChemicalResource> stack) {
        return stack.isEmpty();
    }

    @Override
    public ResourceStack<ChemicalResource> getEmptyStack() {
        return EMPTY_STACK;
    }

    @Override
    public boolean matchesStackType(Object o) {
        return o instanceof ChemicalStack;
    }

    @Override
    public boolean matchesCapabilityHandler(Object o) {
        return o instanceof ResourceHandler<?>;
    }

    @Override
    public SFMRegistryWrapper<Chemical> getRegistry() {
        return new SFMRegistryWrapper<>(MekanismAPI.CHEMICAL_REGISTRY);
    }

    @Override
    public Chemical getItem(ResourceStack<ChemicalResource> stack) {
        return stack.resource().getChemical();
    }

    @Override
    public ResourceStack<ChemicalResource> copy(ResourceStack<ChemicalResource> stack) {
        return new ResourceStack<>(stack.resource(), stack.amount());
    }

    @Override
    protected ResourceStack<ChemicalResource> setCount(
            ResourceStack<ChemicalResource> stack,
            long amount
    ) {
        return new ResourceStack<>(stack.resource(), (int) Math.min(amount, Integer.MAX_VALUE));

    }
}
