package com.rover.blplus.process.model;

import com.rover.blplus.process.exception.ProcessException;
import com.rover.utility.process.BrokenPointerChainException;

public interface ResolvablePointerChain {
    public long resolve() throws ProcessException, BrokenPointerChainException;
}
