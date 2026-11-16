package com.rover.blplus;

import com.rover.blplus.process.model.DynamicPointerChain;
import com.rover.blplus.process.model.RelativePointerChain;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

public class Addresses {
    public static RelativePointerChain player = new RelativePointerChain(0x025EC648, new int[]{0x10, 0x670, 0x10, 0x260, 0x0});
    public static DynamicPointerChain currentWeapon = new DynamicPointerChain(player, 0x584, new int[]{0x0});
    public static DynamicPointerChain combatXpMult = new DynamicPointerChain(player, 0xD4, new int[]{0x450, 0x268, 0x164});
    public static DynamicPointerChain maxHealth = new DynamicPointerChain(player, 0x4DC, new int[]{0x364, 0x80});
    public static DynamicPointerChain currentHealth = new DynamicPointerChain(player, 0x4DC, new int[]{0x364, 0x98});
    public static DynamicPointerChain maxShield = new DynamicPointerChain(player, 0x7FC, new int[]{0x80});
    public static DynamicPointerChain currentShield = new DynamicPointerChain(player, 0x7FC, new int[]{0x98});
    public static DynamicPointerChain currentSkillCooldown = new DynamicPointerChain(player, 0xD4, new int[]{0x450, 0x270, 0x98});
    public static DynamicPointerChain timeToShopReset = new DynamicPointerChain(player, 0x11C, new int[]{0x48C, 0x608});
    public static DynamicPointerChain cash = new DynamicPointerChain(player, 0x4DC, new int[]{0x350});
    public static RelativePointerChain keys = new RelativePointerChain(0x025616B0, new int[]{0x94, 0x64, 0x300});

    public static int ammoCostOffset = 0x4A8;

    public static byte[] longAddressToByteArray(long address) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(address);
        byte[] array = buffer.array();
        ArrayUtils.reverse(array);
        return array;
    }
}
