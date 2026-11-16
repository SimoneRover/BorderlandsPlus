package com.rover.blplus;

import com.rover.blplus.process.model.DynamicPointerChain;
import com.rover.blplus.process.model.RelativePointerChain;
import com.rover.utility.process.ProcessUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    public static String exeName = "BorderlandsGOTY.exe";

    public static List<Byte> maxDamageAOBBytes = Arrays.asList((byte)0xF3, (byte)0x0F, (byte)0x10, (byte)0x86, (byte)0x94,
            (byte)0x03, (byte)0x00, (byte)0x00);
}
