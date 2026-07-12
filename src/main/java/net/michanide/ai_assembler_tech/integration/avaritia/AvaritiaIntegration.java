package net.michanide.ai_assembler_tech.integration.avaritia;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Re:Avaritia integration (modid {@code avaritia}). Only the neutron compressor type gets a
 * processor: its recipes need {@code getInputCount()} applied (e.g. 10000 deepslate → bedrock),
 * which the default {@code getIngredients()} path cannot see. The extreme crafting table type
 * ({@code avaritia:crafting_table_recipe}) deliberately gets NO processor — a registered
 * processor returning {@code null} would skip the recipe rather than fall back, and the default
 * path already extracts those correctly, including the dynamic catalyst/singularity ones.
 * Known limitation: all material singularities share the item id {@code avaritia:singularity}
 * (the material lives in NBT), so their compressor recipes collapse onto one output item.
 */
public final class AvaritiaIntegration implements TechIntegration {

    @Override
    public void register() {
        SpecialRecipeRegistry.register(
                new ResourceLocation("avaritia", "compressor_recipe"), AvaritiaRecipeProcessor::process);
        AIAssemblerTech.LOGGER.info("Re:Avaritia integration: compressor recipe type routed");
    }
}
