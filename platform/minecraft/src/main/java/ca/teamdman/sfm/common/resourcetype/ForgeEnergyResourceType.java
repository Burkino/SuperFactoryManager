package ca.teamdman.sfm.common.resourcetype;

import ca.teamdman.sfm.common.blockentity.BufferBlockEntityContents;
import ca.teamdman.sfm.common.capability.SFMWellKnownCapabilities;
import ca.teamdman.sfm.common.util.SFMResourceLocation;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ForgeEnergyResourceType extends IntegerResourceType<EnergyHandler> {
    public ForgeEnergyResourceType() {
        super(
                SFMWellKnownCapabilities.ENERGY,
                SFMResourceLocation.fromNamespaceAndPath("forge", "energy")
        );
    }

    /**
     * @return stack that was extracted
     */
    @Override
    public Integer extract(
            EnergyHandler handler,
            int slot,
            long amount,
            TransactionContext tx
    ) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        try (var ctx = Transaction.open(tx)) {
            int extracted = handler.extract(finalAmount, ctx);
            ctx.commit();

            return extracted;
        }
    }

    @Override
    public boolean canExtract(EnergyHandler handler, int slot) {
        try (var ctx = Transaction.openRoot()) {
            return handler.extract(Integer.MAX_VALUE, ctx) > 0;
        }
    }

    @Override
    public int getSlots(EnergyHandler _handler) {
        return 1;
    }

    /**
     * @return remaining stack that was not inserted
     */
    @Override
    public Integer insert(
            EnergyHandler handler,
            int slot,
            Integer stack,
            TransactionContext tx
    ) {
        try (var ctx = Transaction.open(tx)) {
            int accepted = handler.insert(stack, ctx);
            ctx.commit();

            return stack - accepted;
        }
    }

    @Override
    public boolean canInsert(EnergyHandler handler, int slot) {
        try (var ctx = Transaction.openRoot()) {
            return handler.insert(Integer.MAX_VALUE, ctx) > 0;
        }
    }

    @Override
    public boolean matchesCapabilityHandler(Object o) {
        return o instanceof EnergyHandler;
    }

    @Override
    public long getMaxStackSizeForSlot(
            EnergyHandler handler,
            int slot
    ) {
        return handler.getCapacityAsLong();
    }

    @Override
    public EnergyHandler createHandlerForBufferBlock(BufferBlockEntityContents contents) {
        return new SimpleEnergyHandler(contents.tier.getIntScalarMaxStackSize()) {

/*            @Override
            public boolean canReceive() {
                boolean isValid = this.energy > 0 || contents.isEmpty();
                if (isValid) {
                    contents.lastUsedResource = BufferBlock.ContainedResource.Energy;
                }
                return isValid;
            }*/
        };
    }

    @Override
    public Integer getStackInSlot(
            EnergyHandler handler,
            int slot
    ) {
        return handler.getAmountAsInt();
    }
}
