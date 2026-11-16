package com.rover.blplus.process.model;

import com.rover.blplus.process.ProcessManager;
import com.rover.blplus.process.exception.ProcessException;
import com.rover.utility.process.BrokenPointerChainException;
import com.rover.utility.process.ProcessUtils;

public class DynamicPointerChain implements ResolvablePointerChain {

    private final ResolvablePointerChain pointerChain;
    private final int initialOffset;
    private final int[] offsets;

    public DynamicPointerChain(ResolvablePointerChain pointerChain, int initialOffset, int[] offsets) {
        this.pointerChain = pointerChain;
        this.initialOffset = initialOffset;
        this.offsets = offsets;
    }

    @Override
    public long resolve() throws ProcessException, BrokenPointerChainException {
        ProcessManager pm = ProcessManager.getInstance();
        if (!pm.isAttached()) throw new ProcessException("Cannot resolve dynamic address because there's no process attached");

        long baseAddress = pointerChain.resolve();
        return ProcessUtils.resolveDynamicAddress(pm.getProcessHandle(), this.offsets, baseAddress + this.initialOffset);
    }
}
