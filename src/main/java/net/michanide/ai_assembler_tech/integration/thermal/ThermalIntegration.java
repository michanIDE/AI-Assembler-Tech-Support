package net.michanide.ai_assembler_tech.integration.thermal;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Thermal Series integration. All Thermal machine recipes (pulverizer, sawmill, induction smelter,
 * insolator, ...) subclass one base with uniform accessors, so a single processor covers every
 * machine — including ones registered by other Thermal modules (Expansion, Innovation) or CoFH
 * addons, since they all share the {@code thermal} recipe namespace. Fluids need no extra
 * handling: AI Assembler already excludes all fluids from recommendations.
 */
public final class ThermalIntegration implements TechIntegration {

    @Override
    public void register() {
        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("thermal")) {
                SpecialRecipeRegistry.register(id, ThermalRecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info("Thermal integration: {} recipe types routed", types);
    }
}
