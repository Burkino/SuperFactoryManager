package ca.teamdman.sfm.common.resourcetype;

import ca.teamdman.sfm.common.block.BufferBlock;
import ca.teamdman.sfm.common.blockentity.BufferBlockEntityContents;
import ca.teamdman.sfm.common.capability.SFMWellKnownCapabilities;
import ca.teamdman.sfm.common.registry.SFMRegistryWrapper;
import ca.teamdman.sfm.common.registry.SFMWellKnownRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.stream.Stream;

public class ItemResourceType extends RegistryBackedResourceType<ResourceStack<ItemResource>, Item, ResourceHandler<ItemResource>> {
    private static final ResourceStack<ItemResource> EMPTY_STACK = new ResourceStack<>(ItemResource.EMPTY, 0);

    public ItemResourceType() {
        super(SFMWellKnownCapabilities.ITEM_HANDLER);
    }

    @Override
    public SFMRegistryWrapper<Item> getRegistry() {
        return SFMWellKnownRegistries.ITEMS;
    }


    @Override
    public Item getItem(ResourceStack<ItemResource> itemStack) {
        return itemStack.resource().getItem();
    }

    @Override
    public ResourceStack<ItemResource> copy(ResourceStack<ItemResource> stack) {
        return new ResourceStack<>(stack.resource(), stack.amount());
    }

    @Override
    public ResourceHandler<ItemResource> createHandlerForBufferBlock(BufferBlockEntityContents contents) {
        return new ItemStacksResourceHandler(contents.tier.numSlots) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                boolean isValid = (this.getAmountAsInt(0) == 0) || contents.isEmpty();
                if (isValid) {
                    contents.lastUsedResource = BufferBlock.ContainedResource.Item;
                }
                return isValid;
            }
        };
    }

    @Override
    public long getAmount(ResourceStack<ItemResource> stack) {
        return stack.amount();
    }

    @Override
    public ResourceStack<ItemResource> getStackInSlot(
            ResourceHandler<ItemResource> handler,
            int slot
    ) {
        return new ResourceStack<>(handler.getResource(slot), handler.getAmountAsInt(slot));
    }

    /**
     * @return stack that was extracted
     */
    @Override
    public ResourceStack<ItemResource> extract(
            ResourceHandler<ItemResource> handler,
            int slot,
            long amount,
            TransactionContext tx
    ) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        // Mekanism bin (not creative?) intentionally only returns stacks with count 64, avoiding going past the max stack size
        // https://github.com/mekanism/Mekanism/blob/f92b48a49e0766cd3aa78e95c9c4a47ba90402f5/src/main/java/mekanism/common/inventory/slot/BasicInventorySlot.java#L174-L175
        try (var ctx = Transaction.open(tx)) {
            ItemResource resource = handler.getResource(slot);
            int extracted = handler.extract(slot, resource, finalAmount, ctx);

            ctx.commit();
            return new ResourceStack<>(resource, extracted);
        }
    }

    @Override
    public boolean matchesStackType(Object o) {
        return o instanceof ResourceStack<?> stack && stack.resource() instanceof ItemResource;
    }

    @Override
    public boolean matchesCapabilityHandler(Object o) {
        return o instanceof ResourceHandler<?>;
    }

    /**
     * We want to also return block tags here.
     * <p>
     * <a href="https://github.com/CoFH/CoFHCore/blob/58b83bd0ef1676783323dce54788c3161faab49d/src/main/java/cofh/core/event/CoreClientEvents.java#L127">CoFH Core adds the "Press Ctrl for Tags" tooltip</a>
     * See: {@link cofh.core.event.CoreClientEvents#handleItemTooltipEvent(ItemTooltipEvent)}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public Stream<Identifier> getTagsForStack(ResourceStack<ItemResource> _itemStack) {
        ItemResource itemStack = _itemStack.resource();
        // Get block tags
        Stream<TagKey<Block>> blockTagKeys;
        if (!itemStack.isEmpty()) {
            Block block = Block.byItem(itemStack.getItem());
            if (block != Blocks.AIR) {
                //noinspection deprecation
                blockTagKeys = block.builtInRegistryHolder().tags();
            } else {
                blockTagKeys = Stream.empty();
            }
        } else {
            blockTagKeys = Stream.empty();
        }

        // Get item tags
        //noinspection deprecation
        Stream<TagKey<Item>> itemTagKeys = itemStack.getItem().builtInRegistryHolder().tags();

        // Return union
        return Stream.concat(itemTagKeys, blockTagKeys).map(TagKey::location);
    }

    @Override
    public int getSlots(ResourceHandler<ItemResource> handler) {
        return handler.size();
    }

    @Override
    public long getMaxStackSize(ResourceStack<ItemResource> itemStack) {
        return itemStack.resource().getMaxStackSize();
    }

    @Override
    public long getMaxStackSizeForSlot(
            ResourceHandler<ItemResource> handler,
            int slot
    ) {
        return handler.getCapacityAsLong(slot, ItemResource.EMPTY);
    }

    /**
     * @return remaining stack that was not inserted
     */
    @Override
    public ResourceStack<ItemResource> insert(
            ResourceHandler<ItemResource> handler,
            int slot,
            ResourceStack<ItemResource> stack,
            TransactionContext tx
    ) {
        try (var ctx = Transaction.open(tx)) {
            int inserted = handler.insert(slot, stack.resource(), stack.amount(), ctx);
            ctx.commit();

            return new ResourceStack<>(stack.resource(), stack.amount() - inserted);
        }
    }

    @Override
    public boolean isEmpty(ResourceStack<ItemResource> stack) {
        return stack.isEmpty();
    }

    @Override
    public ResourceStack<ItemResource> getEmptyStack() {
        return EMPTY_STACK;
    }

    @Override
    protected ResourceStack<ItemResource> setCount(
            ResourceStack<ItemResource> stack,
            long amount
    ) {
        return new ResourceStack<>(stack.resource(), (int) Math.min(amount, Integer.MAX_VALUE));
    }

}
