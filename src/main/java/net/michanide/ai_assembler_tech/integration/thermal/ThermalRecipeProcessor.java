package net.michanide.ai_assembler_tech.integration.thermal;

import java.util.Arrays;
import java.util.List;

import cofh.lib.common.fluid.FluidIngredient;
import cofh.thermal.lib.util.recipes.ThermalRecipe;
import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Extracts {@link RecipeData} from any {@link ThermalRecipe}: every Thermal machine recipe exposes
 * the same input-items/input-fluids/output-items(+chances)/output-fluids accessors. Chanced outputs
 * are weighted by their expected yield; Thermal encodes catalyst-immune chances as negative values,
 * so the absolute value is the actual expected multiplier. Non-{@code ThermalRecipe} entries in the
 * {@code thermal} namespace (dynamo fuels, machine catalysts, device mappings) are skipped — they
 * describe boosts or fuel values, not item transformations.
 */
final class ThermalRecipeProcessor {

    private ThermalRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (!(recipe instanceof ThermalRecipe r)) {
            return null;
        }

        RecipeDataBuilder b = new RecipeDataBuilder();
        for (Ingredient input : r.getInputItems()) {
            b.inputItems(Arrays.asList(input.getItems()));
        }
        for (FluidIngredient input : r.getInputFluids()) {
            b.inputFluids(Arrays.asList(input.getFluids()));
        }

        List<ItemStack> outputs = r.getOutputItems();
        List<Float> chances = r.getOutputItemChances();
        for (int i = 0; i < outputs.size(); i++) {
            double chance = i < chances.size() ? Math.abs(chances.get(i)) : 1.0;
            b.outputItems(List.of(outputs.get(i)), chance);
        }
        b.outputFluids(r.getOutputFluids());

        return b.build(recipe.getId().toString(), type);
    }
}
