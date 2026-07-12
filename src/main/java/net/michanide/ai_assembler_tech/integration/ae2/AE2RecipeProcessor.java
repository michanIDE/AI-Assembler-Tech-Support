package net.michanide.ai_assembler_tech.integration.ae2;

import java.util.Arrays;

import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;

import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Extracts {@link RecipeData} from AE2 machine recipes:
 * <ul>
 *   <li>inscriber — the middle input is always consumed; the top/bottom inputs are consumed only
 *       in {@code PRESS} mode (processor assembly). In {@code INSCRIBE} mode they are press
 *       plates, reusable catalysts that must not count as ingredients.</li>
 *   <li>charger — one item in, one item out.</li>
 *   <li>in-world transform — all listed ingredients are consumed (thrown into the world);
 *       the circumstance (a water bath or an explosion) is a condition, not a resource,
 *       matching how AE2 itself models it.</li>
 * </ul>
 * Entropy manipulator recipes return {@code null}: they transform block/fluid states in the
 * world without consuming an item. Matter cannon ammo entries return {@code null}: they only
 * assign a projectile weight to existing items and produce nothing.
 */
final class AE2RecipeProcessor {

    private AE2RecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (recipe instanceof InscriberRecipe inscriber) {
            return processInscriber(inscriber, type);
        }
        if (recipe instanceof ChargerRecipe charger) {
            return processCharger(charger, type);
        }
        if (recipe instanceof TransformRecipe transform) {
            return processTransform(transform, type);
        }
        return null;
    }

    private static RecipeData processInscriber(InscriberRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getMiddleInput().getItems()));
        if (recipe.getProcessType() == InscriberProcessType.PRESS) {
            b.inputItems(Arrays.asList(recipe.getTopOptional().getItems()));
            b.inputItems(Arrays.asList(recipe.getBottomOptional().getItems()));
        }
        b.outputItems(Arrays.asList(recipe.getResultItem()));
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processCharger(ChargerRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getIngredient().getItems()));
        b.outputItems(Arrays.asList(recipe.getResultItem()));
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processTransform(TransformRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        for (Ingredient ingredient : recipe.ingredients) {
            b.inputItems(Arrays.asList(ingredient.getItems()));
        }
        b.outputItems(Arrays.asList(recipe.output));
        return b.build(recipe.getId().toString(), type);
    }
}
