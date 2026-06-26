package com.mcsdc.addon.mixin;

import com.google.common.net.InetAddresses;
import net.minecraft.client.multiplayer.resolver.ServerAddressResolver;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ServerAddressResolver.class)
public interface AddressResolverMixin {
    @ModifyVariable(method = "lambda$static$0", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/net/InetAddress;getByName(Ljava/lang/String;)Ljava/net/InetAddress;", shift = At.Shift.AFTER))
    private static InetAddress avoidDnsResolve(InetAddress inetAddress, ServerAddress address) throws UnknownHostException {
        return patch(address.getHost(), inetAddress);
    }

    @Unique
    private static InetAddress patch(String hostName, InetAddress addr) throws UnknownHostException {
        if (InetAddresses.isInetAddress(hostName)) {
            InetAddress patched = InetAddress.getByAddress(addr.getHostAddress(), addr.getAddress());
            addr = patched;
        }

        return addr;
    }
}
