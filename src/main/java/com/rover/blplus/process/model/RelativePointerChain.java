package com.rover.blplus.process.model;

import com.rover.blplus.process.ProcessManager;
import com.rover.blplus.process.exception.ProcessException;
import com.rover.utility.process.BrokenPointerChainException;
import com.rover.utility.process.ProcessUtils;

public class RelativePointerChain implements ResolvablePointerChain {
    private final int initialOffset;
    private final int[] offsets;

    public RelativePointerChain(int initialOffset, int[] offsets) {
        this.initialOffset = initialOffset;
        this.offsets = offsets;
    }

    public RelativePointerChain(int[] offsets) {
        this.initialOffset = 0;
        this.offsets = offsets;
    }

    public long resolve() throws ProcessException, BrokenPointerChainException {
        ProcessManager pm = ProcessManager.getInstance();
        if (!pm.isAttached()) throw new ProcessException("Cannot resolve dynamic address because there's no process attached");

        long baseAddress = ProcessUtils.getProcessBaseAddress(pm.getPid());
        return ProcessUtils.resolveDynamicAddress(pm.getProcessHandle(), this.offsets, baseAddress + this.initialOffset);
    }
}
