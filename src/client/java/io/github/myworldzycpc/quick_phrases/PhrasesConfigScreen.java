package io.github.myworldzycpc.quick_phrases;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class PhrasesConfigScreen extends Screen {
    private final Screen parent;
    public TextWidget title;
    public ButtonWidget confirmButton;
    public ButtonWidget openYmlButton;
    public ButtonWidget reloadButton;
    public ButtonWidget phrasesFileSelectButton;
    public PhrasesListWidget phrasesListWidget;
    public RightClickMenu rightClickMenu;
    public List<String> currentRightClickPath = null;
    public PhrasesListWidget.Entry currentRightClickEntry = null;
    public List<String> currentEditingKeyBind = null;
    public PhrasesListWidget.Entry currentEditingKeyBindEntry = null;

    public PhrasesConfigScreen(Screen parent) {
        super(Text.translatable("quick_phrases.config.title"));
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        if (QuickPhrasesClient.getPhrases().nodes.isEmpty()) {
            QuickPhrasesClient.getPhrases().put((String) null, "");
        }
        title = new TextWidget(width / 2 - 100, 5, 200, 10, Text.translatable("quick_phrases.config.title"), QuickPhrasesClient.client.textRenderer);
        confirmButton = ButtonWidget.builder(Text.translatable("quick_phrases.config.confirm"), button -> {
            QuickPhrasesClient.savePhrases();
            if (client != null) {
                client.setScreen(parent);
            }
        }).dimensions(25, height - 5 - 20, width - 50, 20).build();
        phrasesFileSelectButton = ButtonWidget.builder(Text.literal(QuickPhrasesConfig.phrasesFile), button -> {
        }).dimensions(25, 15, width - 25 - 25 - 50 - 5 - 50 - 5, 20).tooltip(Tooltip.of(Text.translatable("quick_phrases.config.phrases_file.tooltip"))).build();
        phrasesFileSelectButton.active = false;
        openYmlButton = ButtonWidget.builder(Text.translatable("quick_phrases.config.open_yml"), button -> {
            Util.getOperatingSystem().open(QuickPhrasesClient.phrasesFile);
        }).dimensions(width - 25 - 50 - 5 - 50, 15, 50, 20).build();
        reloadButton = ButtonWidget.builder(Text.translatable("quick_phrases.config.reload"), button -> {
            QuickPhrasesClient.loadPhrases();
            phrasesListWidget.setNodes(QuickPhrasesClient.getPhrases());
        }).tooltip(Tooltip.of(Text.translatable("quick_phrases.config.reload.tooltip"))).dimensions(width - 25 - 50, 15, 50, 20).build();
        phrasesListWidget = new PhrasesListWidget(client, width, height - 15 - 20 - 5 - 20 - 5 - 5, 15 + 20 + 5, 20, QuickPhrasesClient.getPhrases(), this);

        addDrawableChild(title);
        addDrawableChild(confirmButton);
        addDrawableChild(phrasesFileSelectButton);
        addDrawableChild(openYmlButton);
        addDrawableChild(reloadButton);
        addDrawableChild(phrasesListWidget);
    }

    @Override
    public void close() {
        QuickPhrasesClient.savePhrases();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    enum RightClickOptions {
        PATH("path"),
        INSERT("insert"),
        ADD_CHILD("add_child"),
        REMOVE_PHRASE("remove_phrase");

        private final String value;

        RightClickOptions(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public void openRightClickMenu(double mouseX, double mouseY, PhrasesListWidget.Entry entry) {
        rightClickMenu = new RightClickMenu(client, 100, 100, 0, 15);
        if (mouseX > width - 100) {
            mouseX = width - 100;
        }
        if (mouseY > height - 15 * 4) {
            mouseY = height - 15 * 4;
        }
        rightClickMenu.setFocused(true);
        rightClickMenu.setX((int) mouseX);
        rightClickMenu.setY((int) mouseY);
        rightClickMenu.addEntry(RightClickOptions.PATH.toString(), Text.of(String.join(".", entry.path)));
        rightClickMenu.addEntry(RightClickOptions.INSERT.toString(), Text.translatable("quick_phrases.config.insert"));
        rightClickMenu.addEntry(RightClickOptions.ADD_CHILD.toString(), Text.translatable("quick_phrases.config.add_child"));
        rightClickMenu.addEntry(RightClickOptions.REMOVE_PHRASE.toString(), Text.translatable("quick_phrases.config.remove_phrase"));
        currentRightClickPath = entry.path;
        currentRightClickEntry = entry;
    }

    public void closeRightClickMenu() {
        rightClickMenu = null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (rightClickMenu != null) {
            for (RightClickMenu.Entry entry : rightClickMenu.children()) {
                if (entry.button.mouseClicked(mouseX, mouseY, button)) {
                    System.out.println(entry.id);
                    switch (entry.id) {
                        case "path":
                            break;
                        case "insert": {
                            List<String> path = new ArrayList<>(currentRightClickPath);
                            path.set(path.size() - 1, null);
                            if (!QuickPhrasesClient.phrases.containsKey(path)) {
                                QuickPhrasesClient.phrases.insert(currentRightClickPath, null, new Leaf(""));
                                int index = this.phrasesListWidget.children().indexOf(currentRightClickEntry);
                                this.phrasesListWidget.addEmptyEntry(index, path);
                            }
                        }
                        break;
                        case "add_child": {
                            List<String> newPath = new ArrayList<>(currentRightClickPath);
                            newPath.add(null);
                            int index = this.phrasesListWidget.children().indexOf(currentRightClickEntry);
                            int indexOriginal = index;
                            if (QuickPhrasesClient.phrases.get(currentRightClickPath) instanceof Leaf leaf) {
                                PhraseNodes newNode = new PhraseNodes();
                                if (!leaf.getContent().isEmpty()) {
                                    newNode.put("ENTER", leaf);
                                    List<String> newPath2 = new ArrayList<>(currentRightClickPath);
                                    newPath2.add("ENTER");
                                    index++;
                                    this.phrasesListWidget.addEntry(index, newPath2, leaf);
                                }
                                QuickPhrasesClient.phrases.put(currentRightClickPath, newNode);
                            }
                            if (!QuickPhrasesClient.phrases.containsKey(newPath)) {
                                QuickPhrasesClient.phrases.put(newPath, new Leaf(""));
                                index++;
                                this.phrasesListWidget.addEntry(index, newPath, QuickPhrasesClient.phrases.get(newPath));
                            }

                            this.phrasesListWidget.setEntry(indexOriginal, currentRightClickPath, QuickPhrasesClient.phrases.get(currentRightClickPath));
                        }
                        break;
                        case "remove_phrase": {
                            QuickPhrasesClient.phrases.remove(currentRightClickPath);
                            int index = this.phrasesListWidget.children().indexOf(currentRightClickEntry);
                            this.phrasesListWidget.removeTree(index);
                        }
                        break;
                    }
                    closeRightClickMenu();
                    return true;
                }
            }
            closeRightClickMenu();
        }
        if (button == 1) {
            for (PhrasesListWidget.Entry entry : phrasesListWidget.children()) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    openRightClickMenu(mouseX, mouseY, entry);
                    return true;
                }
            }
        }
        currentEditingKeyBind = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (rightClickMenu != null) {
            rightClickMenu.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.currentEditingKeyBind != null) {
            if (keyCode != InputUtil.GLFW_KEY_ESCAPE && keyCode != InputUtil.GLFW_KEY_BACKSPACE) {
                String keyName = io.github.myworldzycpc.quick_phrases.Util.getKeyName(keyCode);
                if (keyName == null) {
                    keyName = String.valueOf(keyCode);
                }
                if (QuickPhrasesClient.phrases.changeKey(currentEditingKeyBind, keyName)) {
                    currentEditingKeyBindEntry.key = keyName;
                    currentEditingKeyBind.set(currentEditingKeyBind.size() - 1, keyName);
                }
            }
            currentEditingKeyBind = null;
            currentEditingKeyBindEntry = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
