package io.github.myworldzycpc.quick_phrases;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

public class RightClickMenu extends ElementListWidget<RightClickMenu.Entry> {
    public RightClickMenu(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
        setRenderBackground(false);
    }

    public int addEntry(String id, Text name) {
        return addEntry(new Entry(id, name));
    }

    @Override
    public int getRowWidth() {
        return getWidth();
    }


    @Override
    public int getRowLeft() {
        return getX();
    }

    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(index) - 4;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    public static class Entry extends ElementListWidget.Entry<RightClickMenu.Entry> {

        public ButtonWidget button;
        public Text name;
        public String id;

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(button);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(button);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            button.setX(x);
            button.setY(y);
            button.setWidth(entryWidth);
            button.setHeight(entryHeight + 4);
            button.render(context, mouseX, mouseY, tickDelta);
        }

        public Entry(String id, Text name) {
            this.id = id;
            this.name = name;
            this.button = ButtonWidget.builder(name, button1 -> {}).build();
        }
    }
}
