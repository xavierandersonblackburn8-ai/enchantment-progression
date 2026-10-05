from pathlib import Path
p=Path('project/src/client/java/com/dark/enchantmentprogression/client/ProgressionScreen.java')
s=p.read_text()
s=s.replace('import net.minecraft.client.input.MouseButtonEvent;','import net.minecraft.client.input.MouseButtonEvent;\nimport net.minecraft.client.renderer.RenderPipelines;')
s=s.replace('    private static final int TEXT=0xFFF5EEE8,MUTED=0xFF99898A,GREEN=0xFF55F078,CYAN=0xFF53DDF4;','    private static final int TEXT=0xFFF5EEE8,MUTED=0xFF99898A,GREEN=0xFF55F078,CYAN=0xFF53DDF4;\n    private static final Identifier GOTHIC_UI=Identifier.fromNamespaceAndPath("enchantment_progression","textures/gui/gothic_progression.png");')
start=s.index('    private void frame(GuiGraphicsExtractor g,int l,int t){')
end=s.index('\n    @Override public void extractRenderState',start)
new='''    private void frame(GuiGraphicsExtractor g,int l,int t){\n        // The approved mockup is now represented by a real packaged GUI texture rather\n        // than approximated with flat fill()/outline() calls. Dynamic item/enchantment\n        // content is rendered over this 500x270 Gothic plate.\n        g.blit(RenderPipelines.GUI_TEXTURED,GOTHIC_UI,l,t,0,0,W,H,W,H);\n    }\n'''
s=s[:start]+new+s[end:]
p.write_text(s)
