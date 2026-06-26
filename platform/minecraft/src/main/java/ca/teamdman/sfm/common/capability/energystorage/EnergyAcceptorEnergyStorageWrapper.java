package ca.teamdman.sfm.common.capability.energystorage;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record EnergyAcceptorEnergyStorageWrapper(
        EnergyHandler inner
) implements EnergyHandler {
    @Override
    public long getAmountAsLong() {
        return inner.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return inner.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext tx) {
        return inner.insert(amount, tx);
    }

    @Override
    public int extract(int amount, TransactionContext tx) {
        return inner.extract(amount, tx);
    }
}
