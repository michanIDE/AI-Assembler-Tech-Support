package net.michanide.ai_assembler_tech.integration.integrateddynamics;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Integrated Dynamics integration. Squeezer, mechanical squeezer, drying basin and mechanical
 * drying basin recipes are extracted; the dynamic facade recipes that share the squeezer types
 * are skipped. Everything else ID ships (crafting, smelting, the energy-container/NBT-clear
 * specials) uses vanilla recipe types and goes through the default path. Menril resin and the
 * other ID fluids need no registration: the base mod excludes all fluids.
 */
public final class IntegratedDynamicsIntegration implements TechIntegration {

    @Override
    public void register() {
        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("integrateddynamics")) {
                SpecialRecipeRegistry.register(id, IntegratedDynamicsRecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info("Integrated Dynamics integration: {} recipe types routed", types);
    }
}
