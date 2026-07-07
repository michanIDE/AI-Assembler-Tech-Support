package net.michanide.ai_assembler_tech.integration.tconstruct;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Tinkers' Construct integration. Smeltery transformations (melting incl. byproducts, alloying,
 * casting, molding) are extracted; the material/tool system (part builder, tinker station,
 * modifier worktable, material melting/casting), entity-based recipes (entity melting, severing)
 * and fuels are routed but skipped by the processor — they describe tool assembly and effects,
 * not resource transformations. Molten metals are fluids, which AI Assembler already excludes
 * from recommendations, so no special-ingredient registration is needed.
 */
public final class TinkersIntegration implements TechIntegration {

    @Override
    public void register() {
        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("tconstruct")) {
                SpecialRecipeRegistry.register(id, TinkersRecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info("Tinkers' Construct integration: {} recipe types routed", types);
    }
}
