package net.michanide.ai_assembler_tech;

import com.mojang.logging.LogUtils;
import net.michanide.ai_assembler_tech.integration.IntegrationManager;
import net.michanide.aiassembler.util.recipe.SpecialIngredientsRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AIAssemblerTech.MOD_ID)
public class AIAssemblerTech {

    public static final String MOD_ID = "ai_assembler_tech";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AIAssemblerTech() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
        LOGGER.info("AI Assembler Tech Support loaded");
    }

    /**
     * Registers all integrations at load-complete: after every mod's registries are populated,
     * but before AI Assembler builds its recipe matrix at server start. Registrations run on the
     * main thread via enqueueWork because the AI Assembler registries are not thread-safe.
     */
    private void onLoadComplete(FMLLoadCompleteEvent event) {
        // Fluids need no handling here: the base mod already registers every fluid id as a
        // special ingredient during its own load-complete.
        event.enqueueWork(() -> {
            IntegrationManager.runIntegrations();
            LOGGER.info("AI Assembler Tech Support ready; {} special ingredients total",
                    SpecialIngredientsRegistry.getAll().size());
        });
    }
}
