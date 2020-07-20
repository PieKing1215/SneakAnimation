package me.pieking1215.sneakanimation;

import me.shedaniel.forge.clothconfig2.api.ConfigBuilder;
import me.shedaniel.forge.clothconfig2.api.ConfigCategory;
import me.shedaniel.forge.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Boolean> enabled;
        public static final double ANIM_MIN = 0.1;
        public static final double ANIM_MAX = 2.5;
        public final ForgeConfigSpec.ConfigValue<Double> animationSpeed;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableAnimation;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            enabled = builder
                    .comment("Enables/Disables the whole Mod [false/true|default:true]")
                    .translation("enable.sneakanimation.config")
                    .define("enableMod", true);
            animationSpeed = builder
                    .comment("Modifier for the sneak animation speed [false/true|default:true]")
                    .translation("speed.sneakanimation.config")
                    .defineInRange("animationSpeed", 1.0, ANIM_MIN, ANIM_MAX);
            disableAnimation = builder
                    .comment("Disables the smooth sneak animation [false/true|default:false]")
                    .translation("disableanimation.sneakanimation.config")
                    .define("disableAnimation", false);
            builder.pop();
        }
    }

    public static void registerClothConfig() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> {
            ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("config.sneakanimation.title");
            builder.setDefaultBackgroundTexture(new ResourceLocation("minecraft:textures/block/spruce_planks.png"));
            builder.transparentBackground();

            ConfigEntryBuilder eb = builder.getEntryBuilder();
            ConfigCategory general = builder.getOrCreateCategory("key.sneakanimation.category.general");
            general.addEntry(eb.startBooleanToggle("config.sneakanimation.enable", getBoolSafe(GENERAL.enabled, true)).setDefaultValue(true).setSaveConsumer(GENERAL.enabled::set).setTooltip(I18n.format("tooltip.config.sneakanimation.enable").split("\n")).build());
            //general.addEntry(eb.startDoubleField("config.sneakanimation.animationSpeed", getDoubleSafe(GENERAL.animationSpeed, 1.0)).setDefaultValue(1.0).setMin(0.1).setMax(2.5).setSaveConsumer(GENERAL.animationSpeed::set).setTooltip(I18n.format("tooltip.config.sneakanimation.animationSpeed").split("\n")).build());

            int nTicks = (int) ((General.ANIM_MAX - General.ANIM_MIN) / 0.1) + 1;

            // map [ANIM_MIN, ANIM_MAX] to [0, nTicks]
            int animV = (int) (((getDoubleSafe(GENERAL.animationSpeed, 1.0) - General.ANIM_MIN) / (General.ANIM_MAX - General.ANIM_MIN)) * nTicks);
            int animDef = (int) (((1.0 - General.ANIM_MIN) / (General.ANIM_MAX - General.ANIM_MIN)) * nTicks);

            general.addEntry(eb.startIntSlider("config.sneakanimation.animationSpeed", animV, 0, nTicks).setDefaultValue(animDef).setSaveConsumer((i) -> {
                // map [0, nTicks] to [ANIM_MIN, ANIM_MAX]
                double thru = i / (double)nTicks;
                double v = General.ANIM_MIN + (thru * (General.ANIM_MAX - General.ANIM_MIN));
                v = Math.round(v * 20.0) / 20.0;
                GENERAL.animationSpeed.set(v);
            }).setTextGetter((i) -> {
                // map [0, nTicks] to [ANIM_MIN * 100, ANIM_MAX * 100]
                double thru = i / (double)nTicks;
                double v = General.ANIM_MIN + (thru * (General.ANIM_MAX - General.ANIM_MIN));
                v = Math.round(v * 20.0) / 20.0;
                int percent = (int) (100 * v);

                percent = (int) (Math.round(percent/10.0) * 10);
                return percent + "%";
            }).setTooltip(I18n.format("tooltip.config.sneakanimation.animationSpeed").split("\n")).build());

            general.addEntry(eb.startBooleanToggle("config.sneakanimation.disableAnimation", getBoolSafe(GENERAL.disableAnimation, false)).setDefaultValue(false).setSaveConsumer(GENERAL.disableAnimation::set).setTooltip(I18n.format("tooltip.config.sneakanimation.disableAnimation").split("\n")).build());

            return builder.setSavingRunnable(spec::save).build();
        });
    }

    public static boolean getBoolSafe(ForgeConfigSpec.ConfigValue<Boolean> bool, boolean defaultValue){
        Object o = bool.get();
        if(!(o instanceof Boolean)){
            //java thinks this if will never be true but we know it can be
            //noinspection ConstantConditions
            if(o instanceof String){
                String st = (String)o;

                // an invalid string (eg. "foo") should revert to the default
                if(defaultValue){
                    bool.set(!st.equalsIgnoreCase("false"));
                }else{
                    bool.set(st.equalsIgnoreCase("true"));
                }
            }else{
                bool.set(defaultValue);
            }
        }
        return bool.get();
    }

    public static double getDoubleSafe(ForgeConfigSpec.ConfigValue<Double> bool, double defaultValue){
        Object o = bool.get();
        if(!(o instanceof Double)){
            //java thinks this if will never be true but we know it can be
            //noinspection ConstantConditions
            if(o instanceof Float){
                bool.set((Double)o);
            }else if(o instanceof String){
                String st = (String)o;

                // an invalid string (eg. "foo") should revert to the default
                try{
                    double d = Double.parseDouble(st);
                    bool.set(d);
                }catch(Exception e){
                    e.printStackTrace();
                    bool.set(defaultValue);
                }
            }else{
                bool.set(defaultValue);
            }
        }
        return bool.get();
    }
}