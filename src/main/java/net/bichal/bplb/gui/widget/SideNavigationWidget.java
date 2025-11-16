package net.bichal.bplb.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

public class SideNavigationWidget extends ClickableWidget {
    private final List<SectionDefinition> sections;
    private final Consumer<Integer> onSectionClick;
    private float expandProgress = 0f;
    private final float[] sectionHoverProgress;
    private static final int COLLAPSED_WIDTH = 4;
    private static final int EXPANDED_WIDTH = 120;
    
    public SideNavigationWidget(int x, int y, int height, List<SectionDefinition> sections, Consumer<Integer> onSectionClick) {
        super(x, y, COLLAPSED_WIDTH, height, Text.empty());
        this.sections = sections;
        this.onSectionClick = onSectionClick;
        this.sectionHoverProgress = new float[sections.size()];
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hovered = mouseX >= this.getX() && mouseX < this.getX() + EXPANDED_WIDTH && mouseY >= this.getY() && mouseY < this.getY() + this.height;
        float targetExpand = hovered ? 1f : 0f;
        expandProgress = MathHelper.lerp(0.15f, expandProgress, targetExpand);
        
        int currentWidth = Math.round(MathHelper.lerp(expandProgress, COLLAPSED_WIDTH, EXPANDED_WIDTH));
        
        context.fill(this.getX(), this.getY(), this.getX() + currentWidth, this.getY() + this.height, 0xE0000000);
        context.drawBorder(this.getX(), this.getY(), currentWidth, this.height, 0xFF505050);
        
        if (expandProgress > 0.01f) {
            int sectionHeight = 30;
            int spacing = 5;
            int startY = this.getY() + 10;
            
            for (int i = 0; i < sections.size(); i++) {
                int sectionY = startY + i * (sectionHeight + spacing);
                boolean sectionHovered = hovered && mouseY >= sectionY && mouseY < sectionY + sectionHeight;
                
                float targetHover = sectionHovered ? 1f : 0f;
                sectionHoverProgress[i] = MathHelper.lerp(0.2f, sectionHoverProgress[i], targetHover);
                
                int bgAlpha = (int) (sectionHoverProgress[i] * 80);
                context.fill(this.getX() + 4, sectionY, this.getX() + currentWidth - 4, sectionY + sectionHeight, 0x40FFFFFF | (bgAlpha << 24));
                
                Text text = sections.get(i).text();
                int textAlpha = (int) (expandProgress * 255);
                context.drawTextWithShadow(client.textRenderer, text, this.getX() + 8, sectionY + 11, 0xFFFFFF | (textAlpha << 24));
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (expandProgress < 0.5f) return;
        
        int sectionHeight = 30;
        int spacing = 5;
        int startY = this.getY() + 10;
        
        for (int i = 0; i < sections.size(); i++) {
            int sectionY = startY + i * (sectionHeight + spacing);
            if (mouseY >= sectionY && mouseY < sectionY + sectionHeight) {
                onSectionClick.accept(sections.get(i).targetY());
                break;
            }
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    
    public record SectionDefinition(Text text, int targetY) {}
}