package ca.teamdman.sfm.common.item;

import ca.teamdman.sfm.client.ClientScreenHelpers;
import ca.teamdman.sfm.common.net.ServerboundAbstractDiskItemSetProgramPacket;
import ca.teamdman.sfm.common.registry.SFMItems;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMTranslationUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractDiskItem extends Item {
    public AbstractDiskItem() {
        super(new Item.Properties().tab(SFMItems.TAB));
    }

    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        var stack = pPlayer.getItemInHand(pUsedHand);
        if (pLevel.isClientSide) {
            ClientScreenHelpers.showProgramEditScreen(
                    getProgram(stack),
                    programString -> SFMPackets.sendToServer(new ServerboundAbstractDiskItemSetProgramPacket(
                            programString,
                            pUsedHand
                    ))
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    public static String getProgram(ItemStack stack) {
        return stack
                .getOrCreateTag()
                .getString("sfm:program");
    }

    public static void setProgram(ItemStack stack, String program) {
        stack
                .getOrCreateTag()
                .putString("sfm:program", program.replaceAll("\r", ""));

    }

    public static List<TranslatableContents> getErrors(ItemStack stack) {
        return stack
                .getOrCreateTag()
                .getList("sfm:errors", Tag.TAG_COMPOUND)
                .stream()
                .map(CompoundTag.class::cast)
                .map(SFMTranslationUtils::deserializeTranslation)
                .toList();
    }

    public static void setErrors(ItemStack stack, List<TranslatableContents> errors) {
        stack
                .getOrCreateTag()
                .put(
                        "sfm:errors",
                        errors
                                .stream()
                                .map(SFMTranslationUtils::serializeTranslation)
                                .collect(ListTag::new, ListTag::add, ListTag::addAll)
                );
    }

    public static List<TranslatableContents> getWarnings(ItemStack stack) {
        return stack
                .getOrCreateTag()
                .getList("sfm:warnings", Tag.TAG_COMPOUND)
                .stream()
                .map(CompoundTag.class::cast)
                .map(SFMTranslationUtils::deserializeTranslation)
                .collect(
                        Collectors.toList());
    }

    public static void setWarnings(ItemStack stack, List<TranslatableContents> warnings) {
        stack
                .getOrCreateTag()
                .put(
                        "sfm:warnings",
                        warnings
                                .stream()
                                .map(SFMTranslationUtils::serializeTranslation)
                                .collect(ListTag::new, ListTag::add, ListTag::addAll)
                );
    }

    public static String getProgramName(ItemStack stack) {
        return stack
                .getOrCreateTag()
                .getString("sfm:name");
    }

    public static void setProgramName(ItemStack stack, String name) {
        if (stack.getItem() instanceof AbstractDiskItem) {
            stack
                    .getOrCreateTag()
                    .putString("sfm:name", name);
        }
    }
}