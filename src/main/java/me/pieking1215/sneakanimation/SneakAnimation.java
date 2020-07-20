package me.pieking1215.sneakanimation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod("sneakanimation")
public class SneakAnimation {
    private static final Logger LOGGER = LogManager.getLogger();

    Field ActiveRenderInfo_height;
    Field ActiveRenderInfo_previousHeight;

    float start_height;
    float start_previousHeight;

    public SneakAnimation() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(this);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.spec);
            Config.registerClothConfig();

            try{
                // field_216801_m = height
                ActiveRenderInfo_height = ObfuscationReflectionHelper.findField(ActiveRenderInfo.class, "field_216801_m");
                ActiveRenderInfo_height.setAccessible(true);
                // field_216802_n = previousHeight
                ActiveRenderInfo_previousHeight = ObfuscationReflectionHelper.findField(ActiveRenderInfo.class, "field_216802_n");
                ActiveRenderInfo_previousHeight.setAccessible(true);
            }catch(Exception e){
                e.printStackTrace();
            }

        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        // see ActiveRenderInfo::interpolateHeight

        // test if we should do anything
        if(Config.getBoolSafe(Config.GENERAL.enabled, true)) {
            if (!Minecraft.getInstance().isGamePaused()) {
                ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
                Entity renderViewEntity = renderInfo.getRenderViewEntity();
                if (renderViewEntity != null) {
                    // tweak the animation

                    if(Config.getBoolSafe(Config.GENERAL.disableAnimation, false)){
                        if (event.phase == TickEvent.Phase.END) {
                            // skip the lerp by setting the previous and current to the target
                            float targetHeight = renderViewEntity.getEyeHeight();
                            setHeight(renderInfo, targetHeight);
                            setPreviousHeight(renderInfo, targetHeight);
                        }
                    }else {
                        if (event.phase == TickEvent.Phase.START) {
                            // save the values before the vanilla animation happens

                            start_height = getHeight(renderInfo);
                            start_previousHeight = getPreviousHeight(renderInfo);
                        } else {
                            // perform the modified animation using the values from before the vanilla one

                            start_previousHeight = start_height;

                            // get and clamp the speed modifier
                            double mod = Config.getDoubleSafe(Config.GENERAL.animationSpeed, 1.0);
                            if(mod < Config.General.ANIM_MIN) mod = Config.General.ANIM_MIN;
                            if(mod > Config.General.ANIM_MAX) mod = Config.General.ANIM_MAX;

                            start_height += (renderViewEntity.getEyeHeight() - start_height) * 0.5f * mod;

                            // override the values in the ActiveRenderInfo
                            setHeight(renderInfo, start_height);
                            setPreviousHeight(renderInfo, start_previousHeight);
                        }
                    }
                }
            }
        }

    }

    // utility methods for getting/setting private fields in ActiveRenderInfo

    public float getHeight(ActiveRenderInfo render){
        try{
            return (float) ActiveRenderInfo_height.get(render);
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public float getPreviousHeight(ActiveRenderInfo render){
        try{
            return (float) ActiveRenderInfo_previousHeight.get(render);
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public void setHeight(ActiveRenderInfo render, float val){
        try{
            ActiveRenderInfo_height.set(render, val);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setPreviousHeight(ActiveRenderInfo render, float val){
        try{
            ActiveRenderInfo_previousHeight.set(render, val);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
