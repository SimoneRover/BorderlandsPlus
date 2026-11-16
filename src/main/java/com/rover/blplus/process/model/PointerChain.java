package com.rover.blplus.process.model;

import com.rover.blplus.process.ProcessManager;
import com.rover.blplus.process.exception.ProcessException;
import com.rover.utility.process.BrokenPointerChainException;
import com.rover.utility.process.ProcessUtils;

public class PointerChain implements ResolvablePointerChain {
    private final long baseAddress;
    private final int[] offsets;

    public PointerChain(long baseAddress, int[] offsets) {
        this.baseAddress = baseAddress;
        this.offsets = offsets;
    }

    public PointerChain(long baseAddress, int initialOffset, int[] offsets) {
        this.baseAddress = baseAddress + initialOffset;
        this.offsets = offsets;
    }

    public long resolve() throws ProcessException, BrokenPointerChainException {
        ProcessManager pm = ProcessManager.getInstance();
        if (!pm.isAttached()) throw new ProcessException("Cannot resolve dynamic address because there's no process attached");

        return ProcessUtils.resolveDynamicAddress(pm.getProcessHandle(), this.offsets, this.baseAddress);
    }
}
