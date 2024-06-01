package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.ClientStuff;
import ca.teamdman.sfm.common.containermenu.ManagerContainerMenu;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.net.ServerboundManagerFixPacket;
import ca.teamdman.sfm.common.net.ServerboundManagerProgramPacket;
import ca.teamdman.sfm.common.net.ServerboundManagerResetPacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static ca.teamdman.sfm.common.Constants.LocalizationKeys.*;

public class ManagerScreen extends AbstractContainerScreen<ManagerContainerMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE_LOCATION = new ResourceLocation(
            SFM.MOD_ID,
            "textures/gui/container/manager.png"
    );
    private final float STATUS_DURATION = 40;
    private Component status = Component.empty();
    private float statusCountdown = 0;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton diagButton;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton clipboardPasteButton;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton clipboardCopyButton;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton resetButton;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton editButton;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton examplesButton;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private ExtendedButton logsButton;

    public ManagerScreen(ManagerContainerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    public List<ExtendedButton> getButtonsForJEIExclusionZones() {
        return List.of(
                clipboardPasteButton,
                editButton,
                examplesButton,
                clipboardCopyButton,
                logsButton
        );
    }

    public boolean isReadOnly() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null || player.isSpectator();
    }

    public void updateVisibilities() {
        boolean diskPresent = menu.getSlot(0).hasItem();
        diagButton.visible = shouldShowDiagButton();
        clipboardCopyButton.visible = diskPresent;
        logsButton.visible = diskPresent;
        clipboardPasteButton.visible = diskPresent && !isReadOnly();
        resetButton.visible = diskPresent && !isReadOnly();
        editButton.visible = diskPresent && !isReadOnly();
    }

    private Button.OnTooltip buildTooltip(LocalizationEntry entry) {
        return (btn, pose, mx, my) -> renderTooltip(
                pose,
                font.split(
                        entry.getComponent(),
                        Math.max(
                                width
                                / 2
                                - 43,
                                170
                        )
                ),
                mx,
                my
        );
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 120;
        clipboardPasteButton = this.addRenderableWidget(new ExtendedButtonWithTooltip(
                (this.width - this.imageWidth) / 2 - buttonWidth,
                (this.height - this.imageHeight) / 2 + 16,
                buttonWidth,
                16,
                MANAGER_GUI_PASTE_FROM_CLIPBOARD_BUTTON.getComponent(),
                button -> this.onLoadClipboard(),
                buildTooltip(MANAGER_GUI_PASTE_FROM_CLIPBOARD_BUTTON_TOOLTIP)
        ));
        editButton = this.addRenderableWidget(new ExtendedButtonWithTooltip(
                (this.width - this.imageWidth) / 2 - buttonWidth,
                (this.height - this.imageHeight) / 2 + 16 + 50,
                buttonWidth,
                16,
                MANAGER_GUI_EDIT_BUTTON.getComponent(),
                button -> onEdit(),
                buildTooltip(MANAGER_GUI_EDIT_BUTTON_TOOLTIP)
        ));
        examplesButton = this.addRenderableWidget(new ExtendedButtonWithTooltip(
                (this.width - this.imageWidth) / 2 - buttonWidth,
                (this.height - this.imageHeight) / 2 + 16 * 2 + 50,
                buttonWidth,
                16,
                MANAGER_GUI_VIEW_EXAMPLES_BUTTON.getComponent(),
                button -> onShowExamples(),
                buildTooltip(MANAGER_GUI_VIEW_EXAMPLES_BUTTON_TOOLTIP)
        ));
        clipboardCopyButton = this.addRenderableWidget(new ExtendedButton(
                (this.width - this.imageWidth) / 2 - buttonWidth,
                (this.height - this.imageHeight) / 2 + 128,
                buttonWidth,
                16,
                MANAGER_GUI_COPY_TO_CLIPBOARD_BUTTON.getComponent(),
                button -> this.onSaveClipboard()
        ));
        logsButton = this.addRenderableWidget(new ExtendedButton(
                (this.width - this.imageWidth) / 2 - buttonWidth,
                (this.height - this.imageHeight) / 2 + 16 * 9,
                buttonWidth,
                16,
                MANAGER_GUI_VIEW_LOGS_BUTTON.getComponent(),
                button -> onShowLogs()
        ));
        resetButton = this.addRenderableWidget(new ExtendedButtonWithTooltip(
                (this.width - this.imageWidth) / 2 + 120,
                (this.height - this.imageHeight) / 2 + 10,
                50,
                12,
                MANAGER_GUI_RESET_BUTTON.getComponent(),
                button -> sendReset(),
                buildTooltip(MANAGER_GUI_RESET_BUTTON_TOOLTIP)
        ));
        diagButton = this.addRenderableWidget(new ExtendedButtonWithTooltip(
                (this.width - this.imageWidth) / 2 + 35,
                (this.height - this.imageHeight) / 2 + 48,
                12,
                14,
                Component.literal("!"),
                button -> {
                    if (Screen.hasShiftDown() && !isReadOnly()) {
                        sendAttemptFix();
                    } else {
                        this.onSaveDiagClipboard();
                    }
                },
                buildTooltip(isReadOnly()
                             ? MANAGER_GUI_WARNING_BUTTON_TOOLTIP_READ_ONLY
                             : MANAGER_GUI_WARNING_BUTTON_TOOLTIP)
        ));
        updateVisibilities();
    }

    private void onEdit() {
        ClientStuff.showProgramEditScreen(DiskItem.getProgram(menu.getDisk()), this::sendProgram);
    }

    private void onShowExamples() {
        ClientStuff.showExampleListScreen(DiskItem.getProgram(menu.getDisk()), this::sendProgram);
    }

    private void onShowLogs() {
        ClientStuff.showLogsScreen(menu);
    }

    private void sendReset() {
        SFMPackets.MANAGER_CHANNEL.sendToServer(new ServerboundManagerResetPacket(
                menu.containerId,
                menu.MANAGER_POSITION
        ));
        status = MANAGER_GUI_STATUS_RESET.getComponent();
        statusCountdown = STATUS_DURATION;
    }

    private void sendAttemptFix() {
        SFMPackets.MANAGER_CHANNEL.sendToServer(new ServerboundManagerFixPacket(
                menu.containerId,
                menu.MANAGER_POSITION
        ));
        status = MANAGER_GUI_STATUS_FIX.getComponent();
        statusCountdown = STATUS_DURATION;
    }

    private void sendProgram(String program) {
        SFMPackets.MANAGER_CHANNEL.sendToServer(new ServerboundManagerProgramPacket(
                menu.containerId,
                menu.MANAGER_POSITION,
                program
        ));
        menu.program = program;
        status = MANAGER_GUI_STATUS_LOADED_CLIPBOARD.getComponent();
        statusCountdown = STATUS_DURATION;
    }

    private void onSaveClipboard() {
        try {
            Minecraft.getInstance().keyboardHandler.setClipboard(menu.program);
            status = MANAGER_GUI_STATUS_SAVED_CLIPBOARD.getComponent();
            statusCountdown = STATUS_DURATION;
        } catch (Throwable t) {
            SFM.LOGGER.error("failed to save clipboard", t);
        }
    }

    private boolean shouldShowDiagButton() {
        var disk = menu.getDisk();
        if (!(disk.getItem() instanceof DiskItem)) return false;
        var errors = DiskItem.getErrors(disk);
        var warnings = DiskItem.getWarnings(disk);
        return !errors.isEmpty() || !warnings.isEmpty();
    }

    private void onSaveDiagClipboard() {
        try {
            var disk = menu.CONTAINER.getItem(0);
            if (!(disk.getItem() instanceof DiskItem)) return;
            StringBuilder content = new StringBuilder(menu.program);

            content
                    .append("\n\n-- Diagnostic info --\n");

            content.append("-- DateTime: ")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").format(new java.util.Date()))
                    .append('\n');

            assert this.minecraft != null;
            content
                    .append("-- Game Version: ")
                    .append("Minecraft ")
                    .append(SharedConstants.getCurrentVersion().getName())
                    .append(" (")
                    .append(this.minecraft.getLaunchedVersion())
                    .append("/")
                    .append(ClientBrandRetriever.getClientModName())
                    .append(")")
                    .append('\n');

            content.append("-- Forge Version: ")
                    .append(ForgeVersion.getVersion())
                    .append('\n');

            //noinspection CodeBlock2Expr
            ModList.get().getModContainerById(SFM.MOD_ID).ifPresent(mod -> {
                content.append("-- SFM Version: ")
                        .append(mod.getModInfo().getVersion())
                        .append('\n');
            });

            var errors = DiskItem.getErrors(disk);
            if (!errors.isEmpty()) {
                content.append("\n-- Errors\n");
                for (var error : errors) {
                    content.append("-- * ").append(I18n.get(error.getKey(), error.getArgs())).append("\n");
                }
            }

            var warnings = DiskItem.getWarnings(disk);
            if (!warnings.isEmpty()) {
                content.append("\n-- Warnings\n");
                for (var warning : warnings) {
                    content.append("-- * ").append(I18n.get(warning.getKey(), warning.getArgs())).append("\n");
                }
            }

            Minecraft.getInstance().keyboardHandler.setClipboard(content.toString());
            status = MANAGER_GUI_STATUS_SAVED_CLIPBOARD.getComponent();
            statusCountdown = STATUS_DURATION;
        } catch (Throwable t) {
            SFM.LOGGER.error("failed saving clipboard", t);
        }
    }

    private void onLoadClipboard() {
        try {
            String contents = Minecraft.getInstance().keyboardHandler.getClipboard();
            sendProgram(contents);
        } catch (Throwable t) {
            SFM.LOGGER.error("failed loading clipboard", t);
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (Screen.isPaste(pKeyCode) && clipboardPasteButton.visible) {
            onLoadClipboard();
            return true;
        } else if (Screen.isCopy(pKeyCode) && clipboardCopyButton.visible) {
            onSaveClipboard();
            return true;
        } else if (pKeyCode == GLFW.GLFW_KEY_E
                   && Screen.hasControlDown()
                   && Screen.hasShiftDown()
                   && examplesButton.visible) {
            onShowExamples();
            return true;
        } else if (pKeyCode == GLFW.GLFW_KEY_E && Screen.hasControlDown() && editButton.visible) {
            onEdit();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mx, int my) {
        // draw title
        super.renderLabels(poseStack, mx, my);

        // draw state string
        var state = menu.state;
        this.font.draw(
                poseStack,
                MANAGER_GUI_STATE.getComponent(state.LOC.getComponent().withStyle(state.COLOR)),
                titleLabelX,
                20,
                0
        );

        // draw log level
        if (!menu.logLevel.equals(Level.OFF.name())) {
            poseStack.pushPose();
            poseStack.translate(
                    titleLabelX,
                    font.lineHeight * 1.5,
                    0f
            );
            poseStack.scale(0.5f, 0.5f, 1f);
            this.font.draw(
                    poseStack,
                    Component
                            .literal(menu.logLevel),
                    0f,
                    0f,
                    0
            );
            poseStack.popPose();
        }

        // draw status string
        if (statusCountdown > 0) {
            this.font.draw(
                    poseStack,
                    status,
                    inventoryLabelX + font.width(playerInventoryTitle.getString()) + 5,
                    inventoryLabelY,
                    0
            );
        }

        // Find the maximum tick time for normalization
        long peakTickTimeNanoseconds = 0;
        for (int i = 0; i < menu.tickTimeNanos.length; i++) {
            peakTickTimeNanoseconds = Long.max(peakTickTimeNanoseconds, menu.tickTimeNanos[i]);
        }
        long yMax = Long.max(peakTickTimeNanoseconds, 50000000); // Start with max at 50ms but allow it to grow

        // Constants for the plot size and position
        final int plotX = titleLabelX + 45;
        final int plotY = 40;
        final int spaceBetweenPoints = 6;
        final int plotWidth = spaceBetweenPoints * (menu.tickTimeNanos.length - 1);
        final int plotHeight = 30;


        // Set up rendering
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f pose = poseStack.last().pose();
        BufferBuilder bufferbuilder;

        // Draw the plot background
        bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(pose, plotX, plotY, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.vertex(pose, plotX + plotWidth, plotY, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.vertex(pose, plotX + plotWidth, plotY + plotHeight, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.vertex(pose, plotX, plotY + plotHeight, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.vertex(pose, plotX, plotY, 0).color(0, 0, 0, 0.5f).endVertex();
        tesselator.end();

        // Draw lines for each data point
        bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int mouseTickTimeIndex = -1;
        for (int i = 0; i < menu.tickTimeNanos.length; i++) {
            long y = menu.tickTimeNanos[i];
            float normalizedTickTime = y == 0 ? 0 : (float) (Math.log10(y) / Math.log10(yMax));
            int plotPosY = plotY + plotHeight - (int) (normalizedTickTime * plotHeight);

            int plotPosX = plotX + spaceBetweenPoints * i;

            // Color the lines based on their tick times (green to red)
            var c = getMillisecondColour(y / 1_000_000f);
            //noinspection DataFlowIssue
            float red = ((c.getColor() >> 16) & 0xFF) / 255f;
            float green = ((c.getColor() >> 8) & 0xFF) / 255f;
            float blue = (c.getColor() & 0xFF) / 255f;

            bufferbuilder
                    .vertex(pose, (float) plotPosX, (float) plotPosY, (float) getBlitOffset())
                    .color(red, green, blue, 1f)
                    .endVertex();

            // Check if the mouse is hovering over this line
            if (mx - leftPos >= plotPosX - spaceBetweenPoints / 2
                && mx - leftPos <= plotPosX + spaceBetweenPoints / 2
                && my - topPos >= plotY - 2
                && my - topPos <= plotY + plotHeight + 2) {
                mouseTickTimeIndex = i;
            }
        }
        tesselator.end();

        // Draw the tick time text
        var format = new DecimalFormat("0.000");
        if (mouseTickTimeIndex != -1) { // We are hovering over the plot
            // Draw the tick time text for the hovered point instead of peak
            long hoveredTickTimeNanoseconds = menu.tickTimeNanos[mouseTickTimeIndex];
            var hoveredTickTimeMilliseconds = hoveredTickTimeNanoseconds / 1_000_000f;

            this.font.draw(
                    poseStack,
                    MANAGER_GUI_HOVERED_TICK_TIME.getComponent(Component
                                                                       .literal(format.format(
                                                                               hoveredTickTimeMilliseconds))
                                                                       .withStyle(getMillisecondColour(
                                                                               hoveredTickTimeMilliseconds))),
                    titleLabelX,
                    20f + font.lineHeight + 0.1f,
                    0
            );

            // draw a vertical line
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            tesselator = Tesselator.getInstance();
            bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            pose = poseStack.last().pose();

            int x = plotX + spaceBetweenPoints * mouseTickTimeIndex;
            bufferbuilder
                    .vertex(pose, (float) x, (float) plotY, (float) getBlitOffset())
                    .color(1f, 1f, 1f, 1f)
                    .endVertex();
            bufferbuilder
                    .vertex(pose, (float) x, (float) plotY + plotHeight, (float) getBlitOffset())
                    .color(1f, 1f, 1f, 1f)
                    .endVertex();
            tesselator.end();
        } else {
            // Draw the tick time text for peak value
            var peakTickTimeMilliseconds = peakTickTimeNanoseconds / 1_000_000f;
            this.font.draw(
                    poseStack,
                    MANAGER_GUI_PEAK_TICK_TIME.getComponent(Component
                                                                    .literal(format.format(peakTickTimeMilliseconds))
                                                                    .withStyle(getMillisecondColour(
                                                                            peakTickTimeMilliseconds))),
                    titleLabelX,
                    20f + font.lineHeight + 0.1f,
                    0
            );
        }

        // Restore stuff
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public ChatFormatting getMillisecondColour(float ms) {
        if (ms <= 5) {
            return ChatFormatting.GREEN;
        } else if (ms <= 15) {
            return ChatFormatting.YELLOW;
        } else {
            return ChatFormatting.RED;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mx, int my, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mx, my, partialTicks);
        this.renderTooltip(poseStack, mx, my);

        updateVisibilities();

        // update status countdown
        statusCountdown -= partialTicks;
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mx, int my) {
        super.renderTooltip(pose, mx, my);
        this.renderables
                .stream()
                .filter(ExtendedButtonWithTooltip.class::isInstance)
                .map(ExtendedButtonWithTooltip.class::cast)
                .forEach(x -> x.renderToolTip(pose, mx, my));

    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mx, int my) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (!menu.logLevel.equals(Level.OFF.name())) {
            RenderSystem.setShaderColor(0.2f, 0.8f, 1f, 1f);
        } else {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}
