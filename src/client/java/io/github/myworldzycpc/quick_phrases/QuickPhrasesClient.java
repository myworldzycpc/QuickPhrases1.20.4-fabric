package io.github.myworldzycpc.quick_phrases;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuickPhrasesClient implements ClientModInitializer {
    public static File phrasesFile;
    public static final File configFile = new File(MinecraftClient.getInstance().runDirectory, "config/quick_phrases/config.yaml");
    public static PhraseNodes phrases;
    private static KeyBinding keyConfig;
    public static String phrasesDirectory = "phrases";
    public static String defaultPhrasesFile = "default";
    public static List<String> currentKeys = new ArrayList<>();
    public static MinecraftClient client;

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        phrasesFile = new File(MinecraftClient.getInstance().runDirectory, "config/quick_phrases/" + phrasesDirectory + "/" + defaultPhrasesFile + ".yaml");
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        // 加载短语
        try {
            phrases = PhraseNodesSerializer.load(phrasesFile);

            // 如果文件不存在则创建默认
            if (phrases.nodes.isEmpty()) {
                initDefaultPhrases();
                savePhrases();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.quick_phrases.config", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_G, // The keycode of the key
                "category.quick_phrases" // The translation key of the keybinding's category.
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyConfig.wasPressed()) {
                client.setScreen(new PhrasesConfigScreen(client.currentScreen));
            }
        });
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            int x = 20;
            int y = 10;
            final int HEIGHT = 10;
            if (!currentKeys.isEmpty()) {
                Text pathText = Text.translatable("quick_phrases.current_keys", Text.of(String.join(".", currentKeys)).copy().formatted(Formatting.DARK_AQUA)).formatted(Formatting.YELLOW);
                drawContext.drawTextWithShadow(client.textRenderer, pathText, x, y += HEIGHT, 0xffffff);
                int width = client.textRenderer.getWidth(pathText);
                int height = HEIGHT;
                if (phrases.get(currentKeys) instanceof PhraseNodes nodes) {
                    for (String key : nodes.getKeys()) {
                        Object child = nodes.get(key);
                        Text textToDraw = null;
                        if (child instanceof Leaf leaf) {
                            textToDraw = Util.getKeyText(key).copy().append(": ").formatted(Formatting.DARK_AQUA).append(Text.of(leaf.getContent()).copy().formatted(Formatting.WHITE));
                        } else if (child instanceof PhraseNodes childNodes) {
                            textToDraw = Util.getKeyText(key).copy().append(": ").formatted(Formatting.DARK_AQUA).append(Text.translatable("quick_phrases.phrase_nodes.size", childNodes.getKeys().size()).formatted(Formatting.GRAY));
                        }
                        if (textToDraw != null) {
                            drawContext.drawTextWithShadow(client.textRenderer, textToDraw, x + 10, y += HEIGHT, 0xffffff);
                            width = Math.max(width, client.textRenderer.getWidth(textToDraw) + 10);
                            height += HEIGHT;
                        }
                    }
                }
                drawContext.fill(15, 15, 25 + width, 23 + height, 0x80000000);
            }
        });
    }

    // 创建默认短语结构
    private void initDefaultPhrases() {
        phrases = new PhraseNodes();

        // 添加示例数据 (对应你的YAML结构)
        phrases.put("J", "hello world!");

        PhraseNodes bNode = new PhraseNodes();
        bNode.put("1", "Come on!");
        bNode.put("2", "Goodbye!");
        phrases.put("K", bNode);
    }

    // 获取短语实例 (供其他代码使用)
    public static PhraseNodes getPhrases() {
        return phrases;
    }

    public static void savePhrases() {
        try {
            PhraseNodesSerializer.save(phrasesFile, phrases);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadPhrases() {
        try {
            phrases = PhraseNodesSerializer.load(phrasesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean onKeyboardInput(int key, int scanCode, int action, int modifiers) {
        if (action == GLFW.GLFW_PRESS) {
            if (client.currentScreen == null) {
                if (key == InputUtil.GLFW_KEY_ESCAPE) {
                    if (!currentKeys.isEmpty()) {
                        currentKeys = new ArrayList<>();
                        return true;
                    }
                } else if (key == InputUtil.GLFW_KEY_BACKSPACE) {
                    if (!currentKeys.isEmpty()) {
                        currentKeys.remove(currentKeys.size() - 1);
                    }
                } else {
                    List<String> newPath = new ArrayList<>(currentKeys);
                    newPath.add(Util.getKeyName(key));
                    if (phrases.containsKey(newPath)) {
                        Object value = phrases.get(newPath);
                        if (value instanceof Leaf leaf) {
                            try {
                                Util.sendMessage(Util.parseStringWithPlaceholders(leaf.getContent(), client.player));
                            } catch (IllegalArgumentException e) {
                                if (client.player != null) {
                                    client.player.sendMessage(Text.of("IllegalArgumentException: " + e.getMessage()).copy().formatted(Formatting.RED), false);
                                }
                            } catch (IllegalStateException e) {
                                if (client.player != null) {
                                    client.player.sendMessage(Text.of("IllegalStateException: " + e.getMessage()).copy().formatted(Formatting.RED), false);
                                }
                            }
                            currentKeys = new ArrayList<>();
                        } else if (value instanceof PhraseNodes) {
                            currentKeys = newPath;
                            System.out.println(currentKeys);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}