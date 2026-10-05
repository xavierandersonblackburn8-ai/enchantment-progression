package com.dark.enchantmentprogression.client;

import com.dark.enchantmentprogression.ClientUpgradeEligibility;
import com.dark.enchantmentprogression.PendingUpgrade;
import com.dark.enchantmentprogression.ProgressionDisplay;
import com.dark.enchantmentprogression.UpgradeSession;
import com.dark.enchantmentprogression.network.UpgradeRequest;
import com.dark.enchantmentprogression.network.UpgradeResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public final class ProgressionScreen extends Screen {
    private static final int W=500,H=270,RH=28,VISIBLE=6;
    private static final int BLACK=0xF5070507,PANEL=0xF2140A0D,PANEL2=0xF21D0D12,ROW=0xF21A0D11;
    private static final int RED=0xFF8F1321,BRIGHT=0xFFFF263E,DARK_RED=0xFF4A0D16,GOLD=0xFFFFC04A;
    private static final int TEXT=0xFFF5EEE8,MUTED=0xFF99898A,GREEN=0xFF55F078,CYAN=0xFF53DDF4;

    private final Screen parent;
    private final List<Row> rows=new ArrayList<>();
    private final PendingUpgrade pending=new PendingUpgrade();
    private final UpgradeSession session=new UpgradeSession();
    private int scroll,refreshTicks,tab;
    private Row selected,confirm;
    private Component status=Component.empty();
    private int statusColor=MUTED;

    public ProgressionScreen(Screen parent){super(Component.translatable("enchantment_progression.title"));this.parent=parent;}

    @Override protected void init(){
        super.init();
        rebuild();
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose())
                .bounds(width/2-42,Math.min(height-23,top()+H+4),84,20).build());
    }

    private int left(){return width/2-W/2;}
    private int top(){return Math.max(8,height/2-H/2-8);}

    private void rebuild(){
        String sid=selected==null?null:selected.id().toString();
        rows.clear();
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null)return;
        ItemStack stack=mc.player.getMainHandItem();
        if(stack.isEmpty())return;
        var stored=EnchantmentHelper.getEnchantmentsForCrafting(stack);
        var reg=mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        reg.listElements().forEach(h->{
            int level=EnchantmentHelper.getItemEnchantmentLevel(h,stack);
            if(level<=0&&!h.value().isSupportedItem(stack))return;
            boolean ok=true;
            for(var e:stored.entrySet())if(e.getIntValue()>0&&!e.getKey().equals(h)&&!Enchantment.areCompatible(e.getKey(),h)){ok=false;break;}
            Row r=new Row(h,level,ok);
            if(matchesTab(r,stack))rows.add(r);
        });
        rows.sort((a,b)->a.name().getString().compareToIgnoreCase(b.name().getString()));
        scroll=Math.min(scroll,Math.max(0,rows.size()-VISIBLE));
        selected=null;
        if(sid!=null)for(Row r:rows)if(r.id().toString().equals(sid))selected=r;
        if(selected==null&&!rows.isEmpty())selected=rows.get(0);
    }

    private boolean matchesTab(Row r,ItemStack held){
        if(tab==0)return true;
        ItemStack probe=probe(tab);
        return probe.isEmpty()||r.holder.value().isSupportedItem(probe);
    }

    private ItemStack probe(int n){return switch(n){
        case 1->new ItemStack(Items.DIAMOND_SWORD);
        case 2->new ItemStack(Items.DIAMOND_PICKAXE);
        case 3->new ItemStack(Items.DIAMOND_AXE);
        case 4->new ItemStack(Items.DIAMOND_SHOVEL);
        case 5->new ItemStack(Items.BOW);
        case 6->new ItemStack(Items.DIAMOND_CHESTPLATE);
        case 7->new ItemStack(Items.DIAMOND_HELMET);
        default->ItemStack.EMPTY;
    };}

    private void frame(GuiGraphicsExtractor g,int l,int t){
        g.fill(l-7,t-7,l+W+7,t+H+7,0xF8030203);
        g.outline(l-7,t-7,W+14,H+14,0xFF27060B);
        g.outline(l-4,t-4,W+8,H+8,BRIGHT);
        g.outline(l-1,t-1,W+2,H+2,DARK_RED);
        g.fill(l,t,l+W,t+H,BLACK);
        g.outline(l,t,W,H,RED);
        // Gothic corner blocks / rivets.
        int[][] p={{l-7,t-7},{l+W-5,t-7},{l-7,t+H-5},{l+W-5,t+H-5}};
        for(int[]q:p){g.fill(q[0],q[1],q[0]+12,q[1]+12,0xFF16090C);g.outline(q[0],q[1],12,12,RED);g.fill(q[0]+4,q[1]+4,q[0]+8,q[1]+8,BRIGHT);}
        // Header plaque and center crimson gem.
        g.fill(l+112,t+5,l+W-112,t+34,0xFF17090D);
        g.outline(l+112,t+5,W-224,29,RED);
        g.outline(l+118,t+9,W-236,21,DARK_RED);
        g.fill(width/2-4,t+1,width/2+4,t+7,BRIGHT);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        int l=left(),t=top();
        frame(g,l,t);
        g.fakeItem(new ItemStack(Items.ENCHANTED_BOOK),width/2-8,t-12);
        g.centeredText(font,Component.literal("ENCHANTMENT PROGRESSION"),width/2,t+14,TEXT);
        g.fill(l+12,t+41,l+118,t+H-14,PANEL);
        g.outline(l+12,t+41,106,H-55,RED);

        Minecraft mc=Minecraft.getInstance();
        ItemStack stack=mc.player==null?ItemStack.EMPTY:mc.player.getMainHandItem();
        g.fill(l+25,t+53,l+105,t+123,0xFF10080B);
        g.outline(l+25,t+53,80,70,DARK_RED);
        if(!stack.isEmpty()){
            g.fakeItem(stack,l+57,t+75);
            g.centeredText(font,stack.getHoverName(),l+65,t+128,TEXT);
        }
        if(mc.player!=null)g.centeredText(font,Component.literal("✦ "+mc.player.experienceLevel+" LEVELS"),l+65,t+143,GREEN);
        if(selected!=null){
            g.centeredText(font,selected.name(),l+65,t+164,CYAN);
            ProgressionDisplay sd=ProgressionDisplay.forLevel(selected.level);
            g.centeredText(font,Component.literal(roman(selected.level)+" → "+roman(sd.targetLevel())),l+65,t+179,TEXT);
            g.centeredText(font,Component.literal(sd.capped()?"MAX LEVEL":sd.xpCost()+" XP NEXT"),l+65,t+194,sd.capped()?MUTED:GREEN);
            g.centeredText(font,Component.literal(selected.compatible?"READY TO PROGRESS":"CONFLICT LOCKED"),l+65,t+213,selected.compatible?MUTED:0xFFFF626F);
        }

        // Item-type icon bar matching the approved concept instead of text category tabs.
        int tabsX=l+127,tabsY=t+43;
        ItemStack[] icons={new ItemStack(Items.ENCHANTED_BOOK),new ItemStack(Items.DIAMOND_SWORD),new ItemStack(Items.DIAMOND_PICKAXE),new ItemStack(Items.DIAMOND_AXE),new ItemStack(Items.DIAMOND_SHOVEL),new ItemStack(Items.BOW),new ItemStack(Items.DIAMOND_CHESTPLATE),new ItemStack(Items.DIAMOND_HELMET)};
        for(int i=0;i<icons.length;i++){
            int x=tabsX+i*43;
            g.fill(x,tabsY,x+38,tabsY+30,i==tab?0xFF4C1019:0xFF120A0D);
            g.outline(x,tabsY,38,30,i==tab?BRIGHT:0xFF482029);
            g.fakeItem(icons[i],x+11,tabsY+7);
        }

        int listX=l+127,listY=t+81,listW=350;
        int y=listY;
        for(int i=scroll,n=0;i<rows.size()&&n<VISIBLE;i++,n++,y+=RH){
            Row r=rows.get(i); ProgressionDisplay d=ProgressionDisplay.forLevel(r.level);
            boolean afford=mc.player!=null&&(mc.player.getAbilities().instabuild||mc.player.experienceLevel>=d.xpCost());
            g.fill(listX,y,listX+listW,y+24,r==selected?0xFF2B1016:ROW);
            g.outline(listX,y,listW,24,r==selected?BRIGHT:0xFF4A1A22);
            // Small enchanted-book badge gives every row the icon treatment used by the concept.
            g.fakeItem(new ItemStack(Items.ENCHANTED_BOOK),listX+7,y+4);
            g.text(font,r.name(),listX+29,y+7,TEXT,false);
            g.text(font,Component.literal(roman(r.level)+" → "+roman(d.targetLevel())),listX+151,y+7,CYAN,false);
            int costColor=(!r.compatible||d.capped())?MUTED:(afford?GREEN:0xFFFF626F);
            g.text(font,Component.literal(d.capped()?"MAX":d.xpCost()+" XP"),listX+210,y+7,costColor,false);
            int bx=listX+274;
            g.fill(bx,y+3,bx+69,y+21,r.compatible&&!d.capped()?0xFF65131D:0xFF251216);
            g.outline(bx,y+3,69,18,r.compatible&&!d.capped()?BRIGHT:0xFF53313A);
            g.centeredText(font,Component.literal(d.capped()?"MAX":r.compatible?"UPGRADE":"LOCKED"),bx+34,y+8,r.compatible?TEXT:MUTED);
        }
        // Scroll rail.
        g.fill(l+482,listY,l+486,listY+RH*VISIBLE,0xFF241217);
        if(rows.size()>VISIBLE){int track=RH*VISIBLE-18;int sy=listY+(int)((double)scroll/Math.max(1,rows.size()-VISIBLE)*track);g.fill(l+482,sy,l+486,sy+18,0xFFB51E2D);}

        if(selected!=null)drawPathStrip(g,l,t,selected);
        if(!status.getString().isEmpty())g.centeredText(font,status,width/2,t+H-10,statusColor);
        if(pending.isPending())g.centeredText(font,Component.literal("✦ CHANNELING ENCHANTMENT… ✦"),width/2,t+74,GOLD);
        if(confirm!=null)drawConfirm(g,t);
        super.extractRenderState(g,mx,my,delta);
    }

    private void drawPathStrip(GuiGraphicsExtractor g,int l,int t,Row r){
        // Compact skill-tree preview at the bottom; selecting the enchantment exposes its progression nodes.
        int x=l+127,y=t+251;
        ProgressionDisplay d=ProgressionDisplay.forLevel(r.level);
        int start=Math.max(1,r.level-2);
        for(int n=start;n<start+6;n++){
            int nx=x+(n-start)*43; boolean owned=n<=r.level,next=n==r.level+1;
            if(n>start)g.fill(nx-15,y+7,nx,y+9,owned?RED:0xFF3A2429);
            g.fill(nx,y,nx+24,y+16,owned?0xFF6B1420:next?0xFF142A31:0xFF151014);
            g.outline(nx,y,24,16,owned?BRIGHT:next?CYAN:0xFF4C353A);
            g.centeredText(font,Component.literal(roman(n)),nx+12,y+4,owned?TEXT:next?CYAN:MUTED);
        }
        g.text(font,Component.literal(d.capped()?"MAX":roman(r.level)+" → "+roman(d.targetLevel())),l+402,y+4,d.capped()?MUTED:GOLD,false);
    }

    private void drawConfirm(GuiGraphicsExtractor g,int t){
        int x=width/2-115,y=t+84; ProgressionDisplay d=ProgressionDisplay.forLevel(confirm.level);
        g.fill(x-5,y-5,x+235,y+105,0xFB040204);g.outline(x-5,y-5,240,110,BRIGHT);g.outline(x,y,230,100,DARK_RED);
        g.fill(x+6,y+6,x+224,y+29,0xFF1E0C11);g.centeredText(font,Component.literal("UPGRADE ENCHANTMENT"),width/2,y+13,TEXT);
        g.fakeItem(new ItemStack(Items.ENCHANTED_BOOK),x+22,y+39);
        g.text(font,confirm.name(),x+48,y+39,TEXT,false);g.text(font,Component.literal(roman(confirm.level)+" → "+roman(d.targetLevel())),x+48,y+54,CYAN,false);
        g.centeredText(font,Component.literal("COST: "+d.xpCost()+" XP"),width/2,y+69,GREEN);
        g.fill(x+14,y+80,x+104,y+97,0xFF65131D);g.outline(x+14,y+80,90,17,BRIGHT);g.centeredText(font,Component.literal("CONFIRM"),x+59,y+85,TEXT);
        g.fill(x+126,y+80,x+216,y+97,0xFF211519);g.outline(x+126,y+80,90,17,0xFF6A4B51);g.centeredText(font,Component.literal("CANCEL"),x+171,y+85,TEXT);
    }

    @Override public boolean mouseClicked(MouseButtonEvent c,boolean doubled){
        double mx=c.x(),my=c.y();int l=left(),t=top();
        if(confirm!=null){int x=width/2-115,y=t+84;if(my>=y+80&&my<=y+97){if(mx>=x+14&&mx<=x+104){Row r=confirm;confirm=null;send(r);return true;}if(mx>=x+126&&mx<=x+216){confirm=null;return true;}}return true;}
        int tabsX=l+127,tabsY=t+43;
        if(my>=tabsY&&my<=tabsY+30)for(int i=0;i<8;i++)if(mx>=tabsX+i*43&&mx<=tabsX+i*43+38){tab=i;scroll=0;rebuild();return true;}
        int listX=l+127,y=t+81;
        for(int i=scroll,n=0;i<rows.size()&&n<VISIBLE;i++,n++,y+=RH){Row r=rows.get(i);if(mx>=listX&&mx<=listX+350&&my>=y&&my<=y+24){selected=r;Minecraft mc=Minecraft.getInstance();boolean creative=mc.player!=null&&mc.player.getAbilities().instabuild;int xp=mc.player==null?0:mc.player.experienceLevel;if(mx>=listX+274&&ClientUpgradeEligibility.canRequest(pending.isPending(),r.compatible,creative,r.level,xp))confirm=r;return true;}}
        return super.mouseClicked(c,doubled);
    }

    private void send(Row r){Identifier id=r.id();long req=session.begin(id.toString());if(req==0)return;pending.begin();status=Component.empty();if(!ClientPlayNetworking.canSend(UpgradeRequest.TYPE)){session.complete();pending.complete();status=Component.literal("Server does not support Enchantment Progression");statusColor=0xFFFF626F;return;}ClientPlayNetworking.send(new UpgradeRequest(req,id));}
    @Override public boolean mouseScrolled(double x,double y,double h,double v){scroll=Math.max(0,Math.min(Math.max(0,rows.size()-VISIBLE),scroll+(v<0?1:-1)));return true;}
    public void acceptResult(UpgradeResult r){if(!session.accepts(r.requestId(),r.enchantmentId().toString()))return;session.complete();pending.complete();if(r.success()){status=Component.literal("✦ UPGRADE COMPLETE • LEVEL "+r.newLevel()+" ✦");statusColor=GREEN;}else{status=Component.literal("Upgrade rejected: "+r.reason().replace('_',' '));statusColor=0xFFFF626F;}rebuild();}
    @Override public void tick(){super.tick();if(++refreshTicks>=10){refreshTicks=0;rebuild();}if(pending.tick()){session.complete();status=Component.literal("Upgrade timed out — no confirmation received");statusColor=0xFFFF626F;rebuild();}}
    @Override public void onClose(){Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
    private record Row(Holder.Reference<Enchantment> holder,int level,boolean compatible){Component name(){return holder.value().description().copy();}Identifier id(){return holder.unwrapKey().orElseThrow().identifier();}}
    private static String roman(int n){if(n<=0)return"—";if(n>20)return Integer.toString(n);int[]v={10,9,5,4,1};String[]s={"X","IX","V","IV","I"};StringBuilder b=new StringBuilder();for(int i=0;i<v.length;i++)while(n>=v[i]){b.append(s[i]);n-=v[i];}return b.toString();}
}
