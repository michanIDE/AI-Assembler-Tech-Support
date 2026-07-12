package net.michanide.ai_assembler_tech.integration.draconicevolution;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Draconic Evolution integration. Fusion crafting recipes are extracted; everything else DE ships
 * (crafting table recipes, smelting) already uses vanilla recipe types and goes through the
 * default path. DE adds no gases or custom fluids, so no special-ingredient registration is
 * needed.
 */
public final class DraconicIntegration implements TechIntegration {

    @Override
    public void register() {
        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("draconicevolution")) {
                SpecialRecipeRegistry.register(id, DraconicRecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info("Draconic Evolution integration: {} recipe types routed", types);
    }
}
