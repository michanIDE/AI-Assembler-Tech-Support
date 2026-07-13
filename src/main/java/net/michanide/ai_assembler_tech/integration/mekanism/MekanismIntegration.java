package net.michanide.ai_assembler_tech.integration.mekanism;

import java.util.Map;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.TechIntegration;
import net.michanide.aiassembler.util.recipe.ExtraRecipeRegistry;
import net.michanide.aiassembler.util.recipe.RecipeData;
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
        registerSyntheticRecipes();

        AIAssemblerTech.LOGGER.info(
                "Mekanism integration: {} chemicals registered as special ingredients, {} recipe types routed",
                chemicals, types);
    }

    /**
     * Conversions hardcoded in multiblock machine logic rather than data-driven recipes: the
     * fission reactor burns fissile fuel into nuclear waste (1:1 mB) and the SPS condenses
     * 1000 mB of polonium into 1 mB of antimatter. Neither exists in the RecipeManager, so
     * without these the production chain from uranium to antimatter pellets is disconnected.
     * Amounts are in buckets, matching {@link MekanismRecipeProcessor}'s mB scaling.
     */
    private static void registerSyntheticRecipes() {
        ExtraRecipeRegistry.register(new RecipeData(
                "ai_assembler_tech:mekanism/fission_reactor", "mekanism:fission_reactor",
                Map.of("mekanism:fissile_fuel", 0.001),
                Map.of("mekanism:nuclear_waste", 0.001)));
        ExtraRecipeRegistry.register(new RecipeData(
                "ai_assembler_tech:mekanism/sps", "mekanism:sps",
                Map.of("mekanism:polonium", 1.0),
                Map.of("mekanism:antimatter", 0.001)));
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
