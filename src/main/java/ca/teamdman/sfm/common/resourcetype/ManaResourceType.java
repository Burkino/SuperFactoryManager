package ca.teamdman.sfm.common.resourcetype;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.IForgeRegistry;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.stream.Stream;

import static net.minecraftforge.common.capabilities.CapabilityManager.get;

public class ManaResourceType extends ResourceType<Integer, Class<Integer>, ManaReceiver> {
    public static final Capability<ManaReceiver> CAP = get(new CapabilityToken<>() {
    });

    public ManaResourceType() {
        super(CAP);
    }

    @Override
    public long getAmount(Integer integer) {
        return integer;
    }

    @Override
    public Integer getStackInSlot(ManaReceiver manaReceiver, int slot) {
        return manaReceiver.getCurrentMana();
    }

    @Override
    public Integer extract(ManaReceiver manaReceiver, int slot, long amount, boolean simulate) {
        if (manaReceiver instanceof ManaPool pool) {
            int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;

            int mana = pool.getCurrentMana();
            int extractAmount = Math.min(finalAmount, mana);
            /// WARNING: This sucks and is only done because Botania doesn't let you simulate actions
            if (!simulate) {
                pool.receiveMana(-extractAmount);
            }
            return extractAmount;
        } else if (manaReceiver instanceof ManaCollector collector) {
            int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;

            int mana = collector.getCurrentMana();
            int extractAmount = Math.min(finalAmount, mana);
            /// WARNING: This sucks and is only done because Botania doesn't let you simulate actions
            if (!simulate) {
                collector.receiveMana(-extractAmount);
            }
            return extractAmount;
        }
        return 0;
    }

    @Override
    public int getSlots(ManaReceiver handler) {
        return 1;
    }

    @Override
    public long getMaxStackSize(Integer integer) {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getMaxStackSize(ManaReceiver manaReceiver, int slot) {
        if (manaReceiver instanceof ManaPool pool)
            return pool.getMaxMana();
        else if (manaReceiver instanceof ManaCollector collector)
            return collector.getMaxMana();
        return 0;
    }

    @Override
    public Integer insert(ManaReceiver manaReceiver, int slot, Integer stack, boolean simulate) {
        if (manaReceiver instanceof ManaPool pool) {
            int space = Math.max(0, Math.min(pool.getMaxMana() - pool.getCurrentMana(), pool.getMaxMana()));

            int accepted = Math.min(stack, space);

            /// WARNING: This sucks and is only done because Botania doesn't let you simulate actions
            if (!simulate) {
                int oldMana = pool.getCurrentMana();

                pool.receiveMana(accepted);

                int newMana = pool.getCurrentMana();
                int difference = newMana - oldMana;
                accepted -= difference;
            }
            return stack - accepted;
        } else if (manaReceiver instanceof ManaCollector collector) {
            int space = Math.max(0, Math.min(collector.getMaxMana() - collector.getCurrentMana(), collector.getMaxMana()));

            int accepted = Math.min(stack, space);
            /// WARNING: This sucks and is only done because Botania doesn't let you simulate actions
            if (!simulate) {
                int oldMana = collector.getCurrentMana();

                collector.receiveMana(accepted);

                int newMana = collector.getCurrentMana();
                int difference = newMana - oldMana;
                accepted -= difference;
            }
            return stack - accepted;
        }
        return stack;
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
        return o instanceof ManaReceiver;
    }

    @Override
    public Stream<ResourceLocation> getTagsForStack(Integer integer) {
        return Stream.empty();
    }

    @Override
    public Integer getEmptyStack() {
        return 0;
    }

    public static final ResourceLocation REGISTRY_KEY = new ResourceLocation("botania", "mana");

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
    protected Integer setCount(Integer stack, long amount) {
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
