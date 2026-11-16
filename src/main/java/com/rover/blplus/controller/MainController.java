package com.rover.blplus.controller;

import com.rover.blplus.Addresses;
import com.rover.blplus.Config;
import com.rover.blplus.gui.MainApp;
import com.rover.blplus.process.ProcessManager;
import com.rover.blplus.process.exception.ProcessException;
import com.rover.blplus.process.hack.HackManager;
import com.rover.blplus.process.hack.HackThread;
import com.rover.utility.Conversion;
import com.rover.utility.array.ArrayUtils;
import com.rover.utility.process.BrokenPointerChainException;
import com.rover.utility.process.ProcessUtils;

import com.sun.jna.Pointer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class MainController {

    @FXML
    private Button godMode;
    @FXML
    private Button unlimitedAmmo;
    @FXML
    private Button maxWeaponDmg;
    @FXML
    private Button noSkillCd;

    @FXML
    private Button maxCash;
    @FXML
    private Button maxKeys;
    @FXML
    private Button resetShops;

    @FXML
    private ComboBox<Float> xpMult;
    @FXML
    private TextArea prompt;

    private final MainApp app;
    private final HackThread thread;
    private long allocBaseAddress;
    private long injectionAddress;

    public MainController(MainApp app) {
        this.app = app;
        this.thread = new HackThread();
        this.allocBaseAddress = 0;
        this.injectionAddress = 0;
    }

    @FXML
    public void initialize() {
        // Init prompt
        prompt.setEditable(false);

        // Output debug info
        out("Currently attached to process " + Config.exeName + " (PID = " + ProcessManager.getInstance().getPid() + ")");
        out("Process base address = " + Pointer.createConstant(ProcessUtils.getProcessBaseAddress(ProcessManager.getInstance().getPid())));

        // Init XP Mult ComboBox
        xpMult.getItems().addAll(0.2F, 0.5F, 1.0F, 2.0F, 10.0F, 1000.0F, 999999.0F);
        xpMult.getSelectionModel().select(1.0F);
        xpMult.getSelectionModel().selectedItemProperty().addListener( (observableValue, oldValue, newValue) -> {
            if (oldValue.equals(newValue)) return;

            try {
                out("Setting combat XP multipler to " + newValue);
                ProcessManager pm = ProcessManager.getInstance();
                long xpMultAddress = Addresses.combatXpMult.resolve();
                if (ProcessUtils.writeFloat(pm.getProcessHandle(), xpMultAddress, newValue)) {
                    out("Combat XP multiplier: OK");
                } else {
                    out("Could not write combat XP multiplier to address " + Pointer.createConstant(xpMultAddress));
                }
            } catch (ProcessException e) {
                out("Could not set combat XP multiplier: " + e.getMessage());
            } catch (BrokenPointerChainException e) {
                out("Could not resolve address to modify: " + e.getMessage());
            }
        });

        // Scan for injection point
        out("Scanning for max weapon damage injection point");
        injectionAddress = ProcessUtils.aobScan(
                ProcessManager.getInstance().getProcessHandle(),
                ProcessUtils.getProcessBaseAddress(ProcessManager.getInstance().getPid()),
                ProcessUtils.getModuleSize(ProcessManager.getInstance().getPid()),
                Config.maxDamageAOBBytes);
        if (injectionAddress == 0L) {
            out("Could not find injection address, try closing the game and restarting Borderlands+");
        } else {
            out("Found injection point at address " + Pointer.createConstant(injectionAddress));

            // Init injection for max weapon ammo
            allocBaseAddress = ProcessUtils.memoryAlloc(ProcessManager.getInstance().getProcessHandle(),100);
            if (allocBaseAddress == 0L) {
                out("ERROR", "Could not allocate memory region");
                out("DEBUG", "Last error code = " + ProcessUtils.getLastError());
            } else {
                out("Allocated memory region at address " + Pointer.createConstant(allocBaseAddress));
                writeBytesInNewMem();
            }
        }

        // Hack thread
        this.thread.setDaemon(true);
        this.thread.start();
        out("Started hack thread");

        app.getPrimaryStage().setOnCloseRequest(windowEvent -> {
            out("Stopping hack thread");
            this.thread.doStop();
            try {
                this.thread.join(5000);
                out("Hack thread stopped successfully");
            } catch (InterruptedException ignored) {
                out("Program terminated suddenly");
            }

            if (HackManager.getInstance().isMaxWeaponDmgEnabled()) {
                out("Disabling injection for max weapon damage");
                restoreOriginalCode();
                out("Injection removed");
            }

            if (allocBaseAddress != 0L) {
                out("Cleaning allocated memory");
                ProcessUtils.memoryFree(ProcessManager.getInstance().getProcessHandle(), allocBaseAddress, 100);
                out("Memory cleaned");
            }

            out("Closing application");
            app.exit();
        });
    }

    public void toggleGodMode(MouseEvent mouseEvent) {
        HackManager.getInstance().toggleGodMode();
        out("Toggled god mode");
        if (HackManager.getInstance().isGodModeEnabled()) {
            godMode.setTextFill(Color.GREEN);
        } else {
            godMode.setTextFill(Color.RED);
        }
    }

    public void toggleUnlimitedAmmo(MouseEvent mouseEvent) {
        HackManager.getInstance().toggleUnlimitedAmmo();
        out("Toggled unlimited ammo");
        if (HackManager.getInstance().isUnlimitedAmmoEnabled()) {
            unlimitedAmmo.setTextFill(Color.GREEN);
        } else {
            unlimitedAmmo.setTextFill(Color.RED);
        }
    }

    public void toggleMaxWeaponDmg(MouseEvent mouseEvent) {
        HackManager.getInstance().toggleMaxWeaponDmg();
        out("Toggled max weapon damage");
        if (HackManager.getInstance().isMaxWeaponDmgEnabled()) {
            maxWeaponDmg.setTextFill(Color.GREEN);
            writeInjectionJump();
        } else {
            maxWeaponDmg.setTextFill(Color.RED);
            restoreOriginalCode();
        }
    }

    public void toggleNoSkillCooldown(MouseEvent mouseEvent) {
        HackManager.getInstance().toggleNoSkillCooldown();
        out("Toggled no skill cooldown");
        if (HackManager.getInstance().isNoSkillCooldownEnabled()) {
            noSkillCd.setTextFill(Color.GREEN);
        } else {
            noSkillCd.setTextFill(Color.RED);
        }
    }

    public void maxCash(MouseEvent mouseEvent) {
        try {
            ProcessUtils.writeInt(ProcessManager.getInstance().getProcessHandle(), Addresses.cash.resolve(), 99999999);
            out("Cash set to 99,999,999");
        } catch (ProcessException e) {
            out("ERROR", "Could not resolve cash pointer chain: " + e.getMessage());
        } catch (BrokenPointerChainException e) {
            out("ERROR", "Broken pointer chain for cash: " + e.getMessage());
        }
    }

    public void maxKeys(MouseEvent mouseEvent) {
        try {
            ProcessUtils.writeInt(ProcessManager.getInstance().getProcessHandle(), Addresses.keys.resolve(), 9999);
            out("Golden keys set to 9,999");
        } catch (ProcessException e) {
            out("ERROR", "Could not resolve shop timer pointer chain: " + e.getMessage());
        } catch (BrokenPointerChainException e) {
            out("ERROR", "Broken pointer chain for shop reset: " + e.getMessage());
        }
    }

    public void resetShops(MouseEvent mouseEvent) {
        try {
            ProcessUtils.writeFloat(ProcessManager.getInstance().getProcessHandle(), Addresses.timeToShopReset.resolve(), 0);
            out("Shop inventories have changed");
        } catch (ProcessException e) {
            out("ERROR", "Could not resolve shop timer pointer chain: " + e.getMessage());
        } catch (BrokenPointerChainException e) {
            out("ERROR", "Broken pointer chain for shop reset: " + e.getMessage());
        }
    }

    private void out(String message) {
        this.prompt.appendText( LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + " " + "INFO" + " - " + message + "\n");
    }

    private void out(String severity, String message) {
        this.prompt.appendText( LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + " " + severity.toUpperCase() + " - " + message + "\n");
    }

    private void writeBytesInNewMem() {
        long returnAddress = injectionAddress + 15;
        byte[] cheatMov = new byte[]{(byte)0xC7, (byte)0x86, (byte)0x94, (byte)0x03, (byte)0x00, (byte)0x00,
                (byte)0x7F, (byte)0x96, (byte)0x18, (byte)0x4B}; // mov [rsi+0x394],0x4B18967F - Puts 999999 as dmg
        byte[] originalMov = new byte[]{(byte)0xF3, (byte)0x0F, (byte)0x10, (byte)0x86, (byte)0x94, (byte)0x03,
                (byte)0x00, (byte)0x00}; // movss xmm0,[rsi+00000394]
        byte[] xor = new byte[]{(byte)0x45, (byte)0x33, (byte)0xC0}; // xor r8d,r8d
        byte[] movRdiPointer = new byte[]{(byte)0x48, (byte)0x8B, (byte)0x3F}; // mov rdi,[rdi]
        byte[] movRcxRax = new byte[]{(byte)0x48, (byte)0x8B, (byte)0xC8}; // mov rcx,rax
        byte[] movabs = new byte[]{(byte)0x49, (byte)0xBF};
        byte[] returnAddressBytes = Addresses.longAddressToByteArray(returnAddress);
        out("DEBUG", "Return address = " + Conversion.toHexString(returnAddressBytes));
        byte[] jmp = new byte[]{(byte)0x41, (byte)0xFF, (byte)0xE7};
        byte[] toWrite = ArrayUtils.mergeByteArrays(cheatMov, originalMov, xor, movRdiPointer, movRcxRax, movabs, returnAddressBytes, jmp);
        ProcessUtils.writeBytes(
                ProcessManager.getInstance().getProcessHandle(),
                allocBaseAddress,
                toWrite,
                toWrite.length
        );
    }

    private void writeInjectionJump() {
        byte[] injectionBytes = ArrayUtils.mergeByteArrays(
                new byte[]{(byte)0x41, (byte)0x57, (byte)0x49, (byte)0xbf},
                Addresses.longAddressToByteArray(allocBaseAddress),
                new byte[]{(byte)0x41, (byte)0xff, (byte)0xe7, (byte)0x41, (byte)0x5f}
        );
        ProcessUtils.writeBytes(ProcessManager.getInstance().getProcessHandle(),
                injectionAddress, injectionBytes, injectionBytes.length);
    }

    private void restoreOriginalCode() {
        byte[] originalBytes = new byte[]{(byte)0xF3, (byte)0x0F, (byte)0x10, (byte)0x86, (byte)0x94, (byte)0x03, (byte)0x00,
                (byte)0x00, (byte)0x45, (byte)0x33, (byte)0xC0, (byte)0x48, (byte)0x8B, (byte)0x3F, (byte)0x48, (byte)0x8B,
                (byte)0xC8};
        ProcessUtils.writeBytes(ProcessManager.getInstance().getProcessHandle(),
                injectionAddress, originalBytes, originalBytes.length);
    }
}
