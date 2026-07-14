package net.michanide.ai_assembler_tech.integration.fluxnetworks;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.ExtraRecipeRegistry;
import net.michanide.aiassembler.util.recipe.RecipeData;

/**
 * Flux Networks integration. All of its crafting recipes work via the default path, but flux
 * dust itself is made in-world — a dropped redstone dust item crushed between bedrock and
 * obsidian — which is hardcoded block logic, not a RecipeManager recipe. Without it, flux dust
 * (the root of every Flux Networks recipe) has no producer. Registered as a synthetic recipe:
 * 1 redstone → 1 flux dust; the bedrock and obsidian act as the machine and are not consumed.
 */
public final class FluxNetworksIntegration implements TechIntegration {

    @Override
    public void register() {
        ExtraRecipeRegistry.register(new RecipeData(
                "ai_assembler_tech:fluxnetworks/flux_dust", "fluxnetworks:bedrock_compression",
                Map.of("minecraft:redstone", 1.0),
                Map.of("fluxnetworks:flux_dust", 1.0)));
        AIAssemblerTech.LOGGER.info("Flux Networks integration: flux dust synthetic recipe registered");
    }
}
