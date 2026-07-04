package net.michanide.ai_assembler_tech.integration.mekanism;

import java.util.Map;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.SpecialIngredientsRegistry;
import net.michanide.aiassembler.util.recipe.SpecialRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Mekanism integration: registers all chemicals (gases, infuse types, pigments, slurries) as
 * special ingredients and routes every {@code mekanism*}-namespaced recipe type through
 * {@link MekanismRecipeProcessor}. Enumerating the registries instead of hardcoding names also
 * covers chemicals and recipe types added by Mekanism Generators or third-party Mekanism addons.
 */
public final class MekanismIntegration implements TechIntegration {

    @Override
    public void register() {
        int chemicals = 0;
        chemicals += registerChemicals(MekanismAPI.gasRegistry());
        chemicals += registerChemicals(MekanismAPI.infuseTypeRegistry());
        chemicals += registerChemicals(MekanismAPI.pigmentRegistry());
        chemicals += registerChemicals(MekanismAPI.slurryRegistry());

        int types = 0;
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : ForgeRegistries.RECIPE_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().startsWith("mekanism")) {
                SpecialRecipeRegistry.register(id, MekanismRecipeProcessor::process);
                types++;
            }
        }
        AIAssemblerTech.LOGGER.info(
                "Mekanism integration: {} chemicals registered as special ingredients, {} recipe types routed",
                chemicals, types);
    }

    private static int registerChemicals(IForgeRegistry<? extends Chemical<?>> registry) {
        int count = 0;
        for (Chemical<?> chemical : registry) {
            if (!chemical.isEmptyType()) {
                SpecialIngredientsRegistry.register(chemical.getRegistryName());
                count++;
            }
        }
        return count;
    }
}
