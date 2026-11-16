package com.rover.blplus.process.hack;

public class HackManager {

    private boolean unlimitedAmmoEnabled;
    private boolean godModeEnabled;
    private boolean maxWeaponDmgEnabled;
    private boolean noSkillCooldownEnabled;

    private static HackManager instance;

    private HackManager() {
        unlimitedAmmoEnabled = false;
        godModeEnabled = false;
        maxWeaponDmgEnabled = false;
        noSkillCooldownEnabled = false;
    }

    public static HackManager getInstance() {
        if (instance == null) instance = new HackManager();
        return instance;
    }

    public void toggleGodMode() {
        this.godModeEnabled = !this.godModeEnabled;
    }
    public void toggleUnlimitedAmmo() {
        this.unlimitedAmmoEnabled = !this.unlimitedAmmoEnabled;
    }
    public void toggleMaxWeaponDmg() {
        this.maxWeaponDmgEnabled = !this.maxWeaponDmgEnabled;
    }
    public void toggleNoSkillCooldown() {
        this.noSkillCooldownEnabled = !this.noSkillCooldownEnabled;
    }

    public boolean isUnlimitedAmmoEnabled() {
        return this.unlimitedAmmoEnabled;
    }
    public void setUnlimitedAmmoEnabled(boolean unlimitedAmmoEnabled) {
        this.unlimitedAmmoEnabled = unlimitedAmmoEnabled;
    }

    public boolean isGodModeEnabled() {
        return godModeEnabled;
    }
    public void setGodModeEnabled(boolean godModeEnabled) {
        this.godModeEnabled = godModeEnabled;
    }

    public boolean isMaxWeaponDmgEnabled() {
        return maxWeaponDmgEnabled;
    }
    public void setMaxWeaponDmgEnabled(boolean maxWeaponDmgEnabled) {
        this.maxWeaponDmgEnabled = maxWeaponDmgEnabled;
    }

    public boolean isNoSkillCooldownEnabled() {
        return noSkillCooldownEnabled;
    }
    public void setNoSkillCooldownEnabled(boolean noSkillCooldownEnabled) {
        this.noSkillCooldownEnabled = noSkillCooldownEnabled;
    }
}
