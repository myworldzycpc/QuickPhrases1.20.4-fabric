package io.github.myworldzycpc.quick_phrases.mixin.client;

import io.github.myworldzycpc.quick_phrases.QuickPhrasesClient;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = {"onKey"}, cancellable = true, at = {@At("HEAD")})
    private void onKeyboardInput(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (QuickPhrasesClient.onKeyboardInput(key, scanCode, action, modifiers)) {
            ci.cancel();
        }
    }

}
