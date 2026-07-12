package net.michanide.ai_assembler_tech.integration.draconicevolution;

import java.util.Arrays;

import com.brandon3055.draconicevolution.api.crafting.IFusionRecipe;

import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Extracts {@link RecipeData} from Draconic Evolution fusion crafting recipes: the catalyst (the
 * item on the core pedestal, transformed into the result) plus every injector ingredient whose
 * {@code consume()} flag is set. Non-consumed injector ingredients survive the craft and are
 * excluded, like AE2's press plates. Energy cost is not a resource and is ignored.
 *
 * <p>DE's {@code IngredientStack} (used for counted catalysts such as 4x draconium block →
 * 4x awakened block) applies its count to the stacks returned by {@code getItems()}, so the
 * builder picks counts up without special handling.</p>
 */
final class DraconicRecipeProcessor {

    private DraconicRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (recipe instanceof IFusionRecipe fusion) {
            return processFusion(fusion, type);
        }
        return null;
    }

    private static RecipeData processFusion(IFusionRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getCatalyst().getItems()));
        for (IFusionRecipe.IFusionIngredient ingredient : recipe.fusionIngredients()) {
            if (ingredient.consume()) {
                b.inputItems(Arrays.asList(ingredient.get().getItems()));
            }
        }
        // FusionRecipe returns its stored result stack and ignores the registry argument.
        b.outputItems(Arrays.asList(recipe.getResultItem(RegistryAccess.EMPTY)));
        return b.build(recipe.getId().toString(), type);
    }
}
