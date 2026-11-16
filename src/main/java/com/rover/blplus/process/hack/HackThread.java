package com.rover.blplus.process.hack;

import com.rover.blplus.Addresses;
import com.rover.blplus.Config;
import com.rover.blplus.process.ProcessManager;
import com.rover.blplus.process.exception.ProcessException;
import com.rover.utility.process.BrokenPointerChainException;
import com.rover.utility.process.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class HackThread extends Thread {

    private final Logger log;
    private final Set<Long> ammoCostAddresses;
    private boolean stop;

    public HackThread() {
        log = LoggerFactory.getLogger(HackThread.class);
        ammoCostAddresses = new HashSet<>();
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            // Unlimited ammo
            try {
                long currentAddress = Addresses.currentWeapon.resolve() + Addresses.ammoCostOffset;
                ammoCostAddresses.add(currentAddress);

                for (long address : ammoCostAddresses) {
                    ProcessUtils.writeInt(
                            ProcessManager.getInstance().getProcessHandle(),
                            address,
                            HackManager.getInstance().isUnlimitedAmmoEnabled() ? 0 : 1);
                }
            } catch (ProcessException e) {
                log.error("Could not resolve ammoCost pointer chain", e);
            } catch (BrokenPointerChainException e) {
                log.trace("Broken pointer chain for ammo", e);
            }

            // Infinite health/shield
            if (HackManager.getInstance().isGodModeEnabled()) {
                try {
                    float maxHealth = ProcessUtils.readFloat(ProcessManager.getInstance().getProcessHandle(), Addresses.maxHealth.resolve());
                    if (maxHealth <= 0) throw new IllegalStateException("Max health has an incorrect value = " + maxHealth);
                    ProcessUtils.writeFloat(
                            ProcessManager.getInstance().getProcessHandle(),
                            Addresses.currentHealth.resolve(),
                            maxHealth);

                    float maxShield = ProcessUtils.readFloat(ProcessManager.getInstance().getProcessHandle(), Addresses.maxShield.resolve());
                    if (maxShield < 0) throw new IllegalStateException("Max shield has an incorrect value = " + maxShield);
                    ProcessUtils.writeFloat(
                            ProcessManager.getInstance().getProcessHandle(),
                            Addresses.currentShield.resolve(),
                            maxShield);
                } catch (ProcessException e) {
                    log.error("Could not resolve health pointer chain", e);
                } catch (IllegalStateException ise) {
                    log.trace("Some variable does not have the expected value", ise);
                } catch (BrokenPointerChainException e) {
                    log.trace("Broken pointer chain for health", e);
                }
            }

            // No skill cooldown
            if (HackManager.getInstance().isNoSkillCooldownEnabled()) {
                try {
                    ProcessUtils.writeFloat(
                            ProcessManager.getInstance().getProcessHandle(),
                            Addresses.currentSkillCooldown.resolve(),
                            0
                    );
                } catch (ProcessException e) {
                    log.error("Could not resolve skill cooldown pointer chain", e);
                } catch (BrokenPointerChainException e) {
                    log.trace("Broken pointer chain for skill cooldown", e);
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
                log.info("Caught interruption in hack thread loop, terminating");
                doStop();
            }
        }

        // Restore ammo cost(s)
        for (long address : ammoCostAddresses) {
            ProcessUtils.writeInt(
                    ProcessManager.getInstance().getProcessHandle(),
                    address,
                    1);
        }
    }

    public void doStop() {
        stop = true;
    }
}
