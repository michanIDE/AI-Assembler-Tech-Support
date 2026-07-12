package net.michanide.ai_assembler_tech.integration.ae2;

import java.util.Map;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Applied Energistics 2 integration. Inscriber, charger and in-world transform recipes are
 * extracted; entropy manipulator recipes (in-world block/fluid state changes, no consumed items)
 * and matter cannon ammo definitions (weight lookup tables, no output) are routed but skipped.
 * AE2's regular crafting/smelting recipes go through the default path, and it adds no
 * non-item resources, so no special-ingredient registration is needed.
 */
public final class AE2Integration implements TechIntegration {

    @Override
    public void register() {
        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("ae2")) {
                SpecialRecipeRegistry.register(id, AE2RecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info("Applied Energistics 2 integration: {} recipe types routed", types);
    }
}
