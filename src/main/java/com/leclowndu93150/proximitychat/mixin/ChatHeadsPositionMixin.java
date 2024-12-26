package com.leclowndu93150.proximitychat.mixin;

import dzwdz.chat_heads.ChatHeads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ChatHeads.class, remap = false)
public abstract class ChatHeadsPositionMixin {
    @ModifyVariable(method = "scanForPlayerName", at = @At("HEAD"), argsOnly = true)
    private static String adjustScanPosition(String message) {
        int distanceEnd = message.indexOf(") ");
        if (distanceEnd != -1) {
            int bracketIndex = message.indexOf("[", distanceEnd);
            if (bracketIndex != -1) {
                return message.substring(bracketIndex + 1);
            }
        }
        return message;
    }
}