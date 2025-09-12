package io.github.myworldzycpc.quick_phrases;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

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

    public static String parseStringWithPlaceholders(String input, PlayerEntity player, int depth) throws IllegalArgumentException, IllegalStateException {
        StringBuilder output = new StringBuilder();
        StringBuilder placeholder = new StringBuilder();
        int placeholderDepth = 0;
        int leftBraceCount = 0;
        int rightBraceCount = 0;
        boolean isEscaped = false;
        for (char c : input.toCharArray()) {
            if (!isEscaped) {
                if (c == '{') {
                    if (placeholderDepth > 0) {
                        if (rightBraceCount == 1) {
                            placeholder.append('}');
                        }
                    } else {
                        if (rightBraceCount == 1) {
                            output.append('}');
                        }
                    }
                    rightBraceCount = 0;
                    leftBraceCount++;
                    if (leftBraceCount == 2) {
                        placeholderDepth++;
                        if (placeholderDepth > 1) {
                            placeholder.append('{');
                            placeholder.append('{');
                        }
                        leftBraceCount = 0;
                    }
                } else if (c == '}') {
                    if (placeholderDepth > 0) {
                        if (leftBraceCount == 1) {
                            placeholder.append('{');
                        }
                    } else {
                        if (leftBraceCount == 1) {
                            output.append('{');
                        }
                    }
                    leftBraceCount = 0;
                    rightBraceCount++;
                    if (rightBraceCount == 2) {
                        placeholderDepth--;
                        if (placeholderDepth > 0) {
                            placeholder.append('}');
                            placeholder.append('}');
                        }
                        rightBraceCount = 0;
                        if (placeholderDepth == 0) {
                            output.append(parsePlaceholder(placeholder.toString(), player, depth));
                            placeholder = new StringBuilder();
                        }
                    }
                } else if (c == '\\') {
                    if (placeholderDepth > 0) {
                        if (leftBraceCount == 1) {
                            placeholder.append('{');
                        }
                        if (rightBraceCount == 1) {
                            placeholder.append('}');
                        }
                    } else {
                        if (leftBraceCount == 1) {
                            output.append('{');
                        }
                        if (rightBraceCount == 1) {
                            output.append('}');
                        }
                    }
                    leftBraceCount = 0;
                    rightBraceCount = 0;
                    isEscaped = true;
                } else {
                    if (placeholderDepth > 0) {
                        if (leftBraceCount == 1) {
                            placeholder.append('{');
                        }
                        if (rightBraceCount == 1) {
                            placeholder.append('}');
                        }
                        placeholder.append(c);
                    } else {
                        if (leftBraceCount == 1) {
                            output.append('{');
                        }
                        if (rightBraceCount == 1) {
                            output.append('}');
                        }
                        output.append(c);
                    }
                    leftBraceCount = 0;
                    rightBraceCount = 0;
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
        if (placeholderDepth > 0) {
            if (leftBraceCount == 1) {
                placeholder.append('{');
            }
            if (rightBraceCount == 1) {
                placeholder.append('}');
            }
        } else {
            if (leftBraceCount == 1) {
                output.append('{');
            }
            if (rightBraceCount == 1) {
                output.append('}');
            }
        }
        return output.toString();
    }

    public static String parsePlaceholder(String input, PlayerEntity player, int depth) {
        StringBuilder name = new StringBuilder();
        Map<String, String> args = new HashMap<>();
        boolean isArg = false;
        boolean isArgValue = false;
        boolean isEscaped = false;
        StringBuilder argName = new StringBuilder();
        StringBuilder argValue = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (!isEscaped) {
                if (!isArg && c == '?') {
                    isArg = true;
                } else if (isArg && !isArgValue && c == '=') {
                    isArgValue = true;
                } else if (isArg && c == '&') {
                    isArgValue = false;
                    try {
                        args.put(argName.toString(), parseStringWithPlaceholders(argValue.toString(), player, depth + 1));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("IllegalArgumentException while parsing a string with placeholders \"" + argValue + "\": " + e.getMessage(), e);
                    } catch (IllegalStateException e) {
                        throw new IllegalStateException("IllegalStateException while parsing a string with placeholders \"" + argValue + "\": " + e.getMessage(), e);
                    }
                    argName = new StringBuilder();
                    argValue = new StringBuilder();
                } else if (c == '\\') {
                    isEscaped = true;
                } else {
                    if (isArg) {
                        if (isArgValue) {
                            argValue.append(c);
                        } else {
                            argName.append(c);
                        }
                    } else {
                        name.append(c);
                    }
                }
            } else {
                if (isArg) {
                    if (isArgValue) {
                        argValue.append(c);
                    } else {
                        argName.append(c);
                    }
                } else {
                    name.append(c);
                }
                isEscaped = false;
            }
        }
        if (!argName.isEmpty()) {
            try {
                args.put(argName.toString(), parseStringWithPlaceholders(argValue.toString(), player, depth + 1));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("IllegalArgumentException while parsing a string with placeholders \"" + argValue + "\": " + e.getMessage(), e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("IllegalStateException while parsing a string with placeholders \"" + argValue + "\": " + e.getMessage(), e);
            }
        }
        try {
            return getPlaceholderValue(name.toString(), player, args, depth);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("IllegalStateException while parsing placeholder {{" + input + "}}: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("IllegalArgumentException while parsing placeholder {{" + input + "}}: " + e.getMessage(), e);
        }
    }

    public static String getPlaceholderValue(String name, PlayerEntity player, Map<String, String> args, int depth) throws IllegalStateException, IllegalArgumentException {
        switch (name) {
            case "pos": {
                Vec3d pos = player.getPos();
                if (args.containsKey("block")) {
                    return String.format("%d %d %d", (int) pos.x, (int) pos.y, (int) pos.z);
                } else {
                    return String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z);
                }
            }
            case "block": {
                HitResult hitResult = player.raycast(512, 0, false);
                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    return Registries.BLOCK.getId(player.getWorld().getBlockState(blockPos).getBlock()).toString();
                } else {
                    throw new IllegalStateException("No block found");
                }
            }
            case "item": {
                ItemStack itemStack = player.getMainHandStack();
                if (itemStack.isEmpty() && !args.containsKey("allowEmpty")) {
                    throw new IllegalStateException("No item found");
                }
                String itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
                if (args.containsKey("block")) {
                    return References.blockItems.getOrDefault(itemId, itemId);
                } else {
                    return itemId;
                }
            }
            case "cursor": {
                String sel = args.getOrDefault("sel", "");
                if (depth == 0) {
                    return "[[" + sel + "]]";
                } else {
                    return sel;
                }
            }
        }
        throw new IllegalArgumentException("Invalid placeholder: " + name);
    }
}
