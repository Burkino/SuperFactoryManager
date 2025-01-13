package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.item.AbstractDiskItem;
import ca.teamdman.sfm.common.registry.SFMProgramLanguages;
import ca.teamdman.sfml.ast.SFMProgram;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public record ServerboundAbstractDiskItemSetProgramPacket(
        String programString,
        InteractionHand hand
) implements SFMPacket {
    public static class Daddy implements SFMPacketDaddy<ServerboundAbstractDiskItemSetProgramPacket> {
        @Override
        public PacketDirection getPacketDirection() {
            return PacketDirection.SERVERBOUND;
        }
        @Override
        public void encode(
                ServerboundAbstractDiskItemSetProgramPacket msg,
                FriendlyByteBuf buf
        ) {
            buf.writeUtf(msg.programString, SFMProgram.MAX_PROGRAM_LENGTH);
            buf.writeEnum(msg.hand);
        }

        @Override
        public ServerboundAbstractDiskItemSetProgramPacket decode(FriendlyByteBuf buf) {
            return new ServerboundAbstractDiskItemSetProgramPacket(
                    buf.readUtf(SFMProgram.MAX_PROGRAM_LENGTH),
                    buf.readEnum(InteractionHand.class)
            );
        }

        @Override
        public void handle(
                ServerboundAbstractDiskItemSetProgramPacket msg,
                SFMPacketHandlingContext context
        ) {
            var sender = context.sender();
            if (sender == null) {
                return;
            }
            var stack = sender.getItemInHand(msg.hand);
            if (stack.getItem() instanceof AbstractDiskItem disk) {
                AbstractDiskItem.setProgram(stack, msg.programString);
                SFMProgramLanguages.getCompileFunction(disk).apply(stack, null);
            }
        }

        @Override
        public Class<ServerboundAbstractDiskItemSetProgramPacket> getPacketClass() {
            return ServerboundAbstractDiskItemSetProgramPacket.class;
        }
    }
}
