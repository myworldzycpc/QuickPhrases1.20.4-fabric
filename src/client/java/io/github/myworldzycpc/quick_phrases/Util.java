package io.github.myworldzycpc.quick_phrases;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class Util {
    public static Text getKeyText(String key) {
        InputUtil.Key key1 = getInputUtilKey(key);
        if (key1 == null) {
            return Text.of(key);
        }
        return key1.getLocalizedText();
    }

    public static Integer getKeyCode(String key) {
        if (key == null) {
            return null;
        }
        key = key.toUpperCase();
        return References.KEY_CODE_MAP.get(key);
    }

    public static String getKeyName(int keyCode) {
        return References.KEY_CODE_MAP.inverse().get(keyCode);
    }

    public static InputUtil.Key getInputUtilKey(String key) {
        Integer keyCode = getKeyCode(key);
        if (keyCode == null) {
            return null;
        }
        return InputUtil.fromKeyCode(keyCode, 0);
    }

    public static void sendMessage(String message) {
        String[] messages = message.split("\n");
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        for (String singleMessage : messages) {
            if (singleMessage.startsWith("/")) {
                player.networkHandler.sendChatCommand(message.substring(1));
            } else {
                player.networkHandler.sendChatMessage(message);
            }
        }
    }

    public static String parseStringWithPlaceholders(String input, PlayerEntity player) {
        StringBuilder output = new StringBuilder();
        StringBuilder placeholder = new StringBuilder();
        int placeholderDepth = 0;
        int leftBraceCount = 0;
        int rightBraceCount = 0;
        boolean isEscaped = false;
        for (char c : input.toCharArray()) {
            if (!isEscaped) {
                if (c == '{') {
                    leftBraceCount++;
                    if (leftBraceCount == 2) {
                        placeholderDepth++;
                        leftBraceCount = 0;
                    }
                } else if (c == '}') {
                    rightBraceCount++;
                    if (rightBraceCount == 2) {
                        placeholderDepth--;
                        rightBraceCount = 0;
                        if (placeholderDepth == 0) {
                            output.append(parsePlaceholder(placeholder.toString(), player));
                            placeholder = new StringBuilder();
                        }
                    }
                } else if (c == '\\') {
                    leftBraceCount = 0;
                    rightBraceCount = 0;
                    isEscaped = true;
                } else {
                    leftBraceCount = 0;
                    rightBraceCount = 0;
                    if (placeholderDepth > 0) {
                        placeholder.append(c);
                    } else {
                        output.append(c);
                    }
                }
            } else {
                if (placeholderDepth > 0) {
                    placeholder.append(c);
                } else {
                    output.append(c);
                }
                isEscaped = false;
            }
        }
        return output.toString();
    }

    public static String parsePlaceholder(String input, PlayerEntity player) {

    }
}
