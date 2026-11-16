package com.rover.blplus.process;

import com.rover.blplus.Config;
import com.rover.blplus.process.exception.AttachException;
import com.rover.utility.process.ProcessConst;
import com.rover.utility.process.ProcessUtils;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProcessManager {

    private final Logger log;

    private int pid;
    private WinNT.HANDLE processHandle;
    private static ProcessManager instance;

    private ProcessManager() {
        this.log = LoggerFactory.getLogger(ProcessManager.class);
    }

    public static ProcessManager getInstance() {
        if (instance == null) instance = new ProcessManager();
        return instance;
    }

    public void attach() throws AttachException {
        boolean done = false;
        List<String> processedWindows = new ArrayList<>();

        while(!done) {
            String windowName = findWindowName(processedWindows);
            if (windowName == null) throw new AttachException("Borderlands window is not open");

            log.trace("Attaching to window {}", windowName);

            this.pid = ProcessUtils.getProcessId(windowName);
            if (this.pid == 0) throw new AttachException("Process not found");
            log.trace("Attached to process with PID {}", pid);

            String exeName = ProcessUtils.getExecutableName(pid);
            if (!Config.exeName.equals(exeName)) {
                processedWindows.add(windowName);
                log.trace("Executable name of attached process don't match the correct one, skipping it");
                continue;
            }

            this.processHandle = ProcessUtils.openProcess(this.pid);
            if (this.processHandle.getPointer().equals(Pointer.createConstant(ProcessConst.INVALID_HANDLE_VALUE)))
                throw new AttachException("Could not get handle to process");

            log.info("Correctly attached to process {} (PID = {}, windowName = {})", exeName, pid, windowName);
            done = true;
        }
    }

    public WinNT.HANDLE getProcessHandle() {
        return this.processHandle;
    }

    public int getPid() {
        return this.pid;
    }

    public boolean isAttached() {
        return this.processHandle != null && !this.processHandle.getPointer().equals(Pointer.createConstant(ProcessConst.INVALID_HANDLE_VALUE));
    }

    private String findWindowName() {
        for (String name : ProcessUtils.listWindows()) {
            if (!name.startsWith("Borderlands")) continue;
            if (name.startsWith("Borderlands+") || name.startsWith("BorderlandsPlus")) continue;

            return name;
        }

        return null;
    }

    private String findWindowName(List<String> filter) {
        for (String name : ProcessUtils.listWindows()) {
            if (!name.startsWith("Borderlands")) continue;
            if (name.startsWith("Borderlands+") || name.startsWith("BorderlandsPlus")) continue;
            if (filter.contains(name)) continue;

            return name;
        }

        return null;
    }

    private byte[] toByteArray(long value) {
        byte[] ret = new byte[8];

        ret[0] = (byte)(value & 0x00000000000000FFL);
        ret[1] = (byte)((value & 0x000000000000FF00L) >> 8);
        ret[2] = (byte)((value & 0x0000000000FF0000L) >> 2*8);
        ret[3] = (byte)((value & 0x00000000FF000000L) >> 3*8);
        ret[4] = (byte)((value & 0x000000FF00000000L) >> 4*8);
        ret[5] = (byte)((value & 0x0000FF0000000000L) >> 5*8);
        ret[6] = (byte)((value & 0x00FF000000000000L) >> 6*8);
        ret[7] = (byte)((value & 0xFF00000000000000L) >> 7*8);

        return ret;
    }
}
