package dev.rinchan.sharedlootr;

import net.minecraftforge.fml.common.Mod;

@Mod(modid = SharedLootr.MOD_ID, name = "Shared Lootr", version = SharedLootr.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:rinlib@[0.2.0+1.12.2];required-after:lootr;required-after:mixinbooter")
public final class SharedLootr {
    public static final String MOD_ID = "shared_lootr";
    public static final String VERSION = "1.3.2+1.12.2";
}
