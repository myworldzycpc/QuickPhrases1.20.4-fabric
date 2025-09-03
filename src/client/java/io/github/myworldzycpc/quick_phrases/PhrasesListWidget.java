package io.github.myworldzycpc.quick_phrases;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PhrasesListWidget extends ElementListWidget<PhrasesListWidget.Entry> {
    public PhrasesConfigScreen parent;
    public PhraseNodes nodes;

    public PhrasesListWidget(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight, PhraseNodes nodes, PhrasesConfigScreen parent) {
        super(minecraftClient, width, height, y, itemHeight);
        this.nodes = nodes;
        this.parent = parent;
        addNodes(new ArrayList<>(), nodes);
    }

    public void setNodes(PhraseNodes nodes) {
        this.nodes = nodes;
        clearEntries();
        addNodes(new ArrayList<>(), nodes);
    }

    public void addNodes(List<String> path, PhraseNodes nodes) {
        for (String key : nodes.getKeys()) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(key);
            addEntry(new Entry(newPath, nodes.get(key)));
            if (!nodes.isLeaf(key)) {
                addNodes(newPath, (PhraseNodes) nodes.get(key));
            }
        }
    }

    public void addEmptyEntry(int index, List<String> path) {
        this.children().add(index, new Entry(path, new Leaf("")));
    }

    public void addEntry(int index, List<String> path, Object value) {
        this.children().add(index, new Entry(path, value));
    }

    public void setEntry(int index, List<String> path, Object value) {
        this.children().set(index, new Entry(path, value));
    }

    public void removeTree(int index) {
        int depth = this.children().get(index).depth;
        int i = index + 1;
        while (i < this.children().size()) {
            if (this.children().get(i).depth <= depth) {
                break;
            }
            i++;
        }
        this.children().subList(index, i).clear();
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 10;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public int getScrollbarPositionX() {
        return width - 6;
    }

    public class Entry extends ElementListWidget.Entry<Entry> implements Element {
        public String key;
        public List<String> path;
        public Object value;
        public boolean isLeaf;
        public ButtonWidget keyButtonWidget;
        public TextFieldWidget valueTextWidget;
        public int depth;
        public int overrideWidthForValue = -1;
        public boolean isCollapsed;

        public Entry(List<String> path, Object value) {
            this.key = path.get(path.size() - 1);
            this.path = path;
            this.value = value;
            this.depth = path.size() - 1;
            this.keyButtonWidget = ButtonWidget.builder(Util.getKeyText(key), button -> {
                PhrasesListWidget.this.parent.currentEditingKeyBind = path;
                PhrasesListWidget.this.parent.currentEditingKeyBindEntry = this;
            }).build();
            this.valueTextWidget = new TextFieldWidget(QuickPhrasesClient.client.textRenderer, 0, 0, 1000, 0, Text.empty());
            this.valueTextWidget.setMaxLength(32767);
            if (value instanceof Leaf leaf) {
                this.isLeaf = true;
                this.valueTextWidget.setText(leaf.content);
                this.valueTextWidget.setEditable(true);
                this.valueTextWidget.setChangedListener(s -> QuickPhrasesClient.getPhrases().put(path, new Leaf(this.valueTextWidget.getText())));
            } else if (value instanceof PhraseNodes phraseNodes) {
                this.isLeaf = false;
                this.valueTextWidget.setText(Text.translatable("quick_phrases.phrase_nodes.size", phraseNodes.nodes.size()).getString());
                this.valueTextWidget.setEditable(false);
                this.valueTextWidget.setChangedListener(null);
            }
        }


        @Override
        public List<? extends Element> children() {
            return List.of(keyButtonWidget, valueTextWidget);
        }

        @Override
        public void setFocused(boolean focused) {

        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            keyButtonWidget.setX(x + depth * 10);
            keyButtonWidget.setY(y);
            keyButtonWidget.setWidth(25);
            keyButtonWidget.setHeight(entryHeight);
            keyButtonWidget.render(context, mouseX, mouseY, tickDelta);
            if (PhrasesListWidget.this.parent.currentEditingKeyBind == path) {
                keyButtonWidget.setMessage(Util.getKeyText(key).copy().formatted(Formatting.YELLOW, Formatting.UNDERLINE));
            } else {
                keyButtonWidget.setMessage(Util.getKeyText(key));
            }
            valueTextWidget.setX(x + depth * 10 + 30);
            valueTextWidget.setY(y);
            valueTextWidget.setWidth(entryWidth - depth * 10 - 30);
            valueTextWidget.setHeight(entryHeight);
            valueTextWidget.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(keyButtonWidget, valueTextWidget);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && button == 1) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
