package net.michanide.ai_assembler_tech.integration.industrialforegoing;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Industrial Foregoing integration. Dissolution chamber, fluid extractor, crusher and stonework
 * generation are extracted; laser drill recipes are routed but skipped — their lens catalyst is
 * never consumed, so they generate resources without ingredients. The washing factory /
 * fermentation station / fluid sieving "ore fluid" entries are plain static lists, not
 * {@code RecipeManager} recipes, so they never reach AI Assembler in the first place. Fluids
 * (latex, meat, ether gas, ...) need no registration: the base mod excludes all fluids.
 */
public final class IndustrialForegoingIntegration implements TechIntegration {

    @Override
    public void register() {
        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("industrialforegoing")) {
                SpecialRecipeRegistry.register(id, IndustrialForegoingRecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info("Industrial Foregoing integration: {} recipe types routed", types);
    }
}
