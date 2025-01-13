package ca.teamdman.sfm.common.item;

import ca.teamdman.sfm.client.ClientKeyHelpers;
import ca.teamdman.sfm.client.ProgramSyntaxHighlightingHelper;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.program.linting.ProgramLinter;
import ca.teamdman.sfm.common.util.SFMItemUtils;
import ca.teamdman.sfml.ast.SFMProgram;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DiskItem extends AbstractDiskItem {
    public static @Nullable SFMProgram compileAndUpdateErrorsAndWarnings(ItemStack stack, @Nullable ManagerBlockEntity manager) {
        if (manager != null) {
            manager.logger.info(x -> x.accept(LocalizationKeys.PROGRAM_COMPILE_FROM_DISK_BEGIN.get()));
        }
        AtomicReference<SFMProgram> rtn = new AtomicReference<>(null);

        SFMProgram.compile(
                getProgram(stack),
                successProgram -> {
                    ArrayList<TranslatableContents> warnings = ProgramLinter.gatherWarnings(successProgram, LabelPositionHolder.from(stack), manager);

                    // Log to disk
                    if (manager != null) {
                        manager.logger.info(x -> x.accept(LocalizationKeys.PROGRAM_COMPILE_SUCCEEDED_WITH_WARNINGS.get(
                                successProgram.name(),
                                warnings.size()
                        )));
                        manager.logger.warn(warnings::forEach);
                    }

                    // Update disk properties
                    setProgramName(stack, successProgram.name());
                    setWarnings(stack, warnings);
                    setErrors(stack, Collections.emptyList());

                    // Track result
                    rtn.set(successProgram);
                },
                errors -> {
                    List<TranslatableContents> warnings = Collections.emptyList();

                    // Log to disk
                    if (manager != null) {
                        manager.logger.error(x -> x.accept(LocalizationKeys.PROGRAM_COMPILE_FAILED_WITH_ERRORS.get(
                                errors.size())));
                        manager.logger.error(errors::forEach);
                    }

                    // Update disk properties
                    setWarnings(stack, warnings);
                    setErrors(stack, errors);
                }
        );
        return rtn.get();
    }

    @Override
    public Component getName(ItemStack stack) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (ClientKeyHelpers.isKeyDown(SFMKeyMappings.MORE_INFO_TOOLTIP_KEY)) return super.getName(stack);
        }
        var name = getProgramName(stack);
        if (name.isEmpty()) return super.getName(stack);
        return Component.literal(name).withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag detail
    ) {

        if (stack.hasTag()) {
            if (SFMItemUtils.isClientAndMoreInfoKeyPressed()) {
                // show the program
                var program = getProgram(stack);
                if (!program.isEmpty()) {
                    lines.add(SFMItemUtils.getRainbow(getName(stack).getString().length()));
                    lines.addAll(ProgramSyntaxHighlightingHelper.withSyntaxHighlighting(program, false));
                }
            } else {
                lines.addAll(LabelPositionHolder.from(stack).asHoverText());
                getErrors(stack)
                        .stream()
                        .map(MutableComponent::create)
                        .map(line -> line.withStyle(ChatFormatting.RED))
                        .forEach(lines::add);
                getWarnings(stack)
                        .stream()
                        .map(MutableComponent::create)
                        .map(line -> line.withStyle(ChatFormatting.YELLOW))
                        .forEach(lines::add);
                SFMItemUtils.appendMoreInfoKeyReminderTextIfOnClient(lines);
            }
        } else {
            lines.add(LocalizationKeys.DISK_EDIT_IN_HAND_TOOLTIP.getComponent().withStyle(ChatFormatting.GRAY));
        }
    }
}
