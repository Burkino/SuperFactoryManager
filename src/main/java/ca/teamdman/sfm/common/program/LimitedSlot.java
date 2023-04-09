package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.resourcetype.ResourceType;

public abstract class LimitedSlot<STACK, CAP, T extends ResourceTracker<STACK, CAP>> {
    public final ResourceType<STACK, CAP> TYPE;
    public final CAP                      HANDLER;
    public final int                      SLOT;
    public final T                        TRACKER;
    private      boolean                  done = false;

    public LimitedSlot(CAP handler, ResourceType<STACK, CAP> type, int slot, T matcher) {
        this.TYPE    = type;
        this.HANDLER = handler;
        this.SLOT    = slot;
        this.TRACKER = matcher;
    }

    public boolean isDone() {
        return done || TRACKER.isDone();
    }

    protected void setDone() {
        this.done = true;
    }

    public STACK getStackInSlot() {
        return TYPE.getStackInSlot(HANDLER, SLOT);
    }

    public STACK extract(long amount, boolean simulate) {
        return TYPE.extract(HANDLER, SLOT, amount, simulate);
    }

    public STACK insert(STACK stack, boolean simulate) {
        return TYPE.insert(HANDLER, SLOT, stack, simulate);
    }

}
