package net.michanide.ai_assembler_tech.integration.tconstruct;

import java.util.Arrays;
import java.util.List;

import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;

/**
 * Extracts {@link RecipeData} from Tinkers' smeltery recipes:
 * <ul>
 *   <li>melting — item → fluid plus byproduct fluids ({@link MeltingRecipe}, covering ore and
 *       damageable variants; material melting of tool parts is skipped)</li>
 *   <li>alloying — fluids → fluid; catalyst fluids are required but not consumed, so they are
 *       excluded from the ingredient map</li>
 *   <li>casting — fluid (+ cast) → item ({@link ItemCastingRecipe}); the cast counts as an
 *       ingredient only when the recipe consumes it, otherwise it is a reusable mold</li>
 *   <li>molding — item (+ pattern) → item, same consumed-only rule for the pattern</li>
 * </ul>
 * Everything else in the {@code tconstruct} namespace returns {@code null}: the material/tool
 * system (part builder, tinker station, modifier worktable), entity melting/severing, melting
 * fuels, and dynamic casting (potions, container filling) have no fixed item↔resource mapping.
 */
final class TinkersRecipeProcessor {

    private TinkersRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (recipe instanceof MeltingRecipe melting) {
            return processMelting(melting, type);
        }
        if (recipe instanceof AlloyRecipe alloying) {
            return processAlloying(alloying, type);
        }
        if (recipe instanceof ItemCastingRecipe casting) {
            return processCasting(casting, type);
        }
        if (recipe instanceof MoldingRecipe molding) {
            return processMolding(molding, type);
        }
        return null;
    }

    private static RecipeData processMelting(MeltingRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getInput().getItems()));
        // Structured as [[output], [byproduct], ...] with singleton inner lists; byproducts have
        // no direct accessor, so this display helper is the supported way to reach them.
        for (List<FluidStack> output : recipe.getOutputWithByproducts()) {
            if (!output.isEmpty()) {
                b.outputFluids(List.of(output.get(0)));
            }
        }
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processAlloying(AlloyRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        for (AlloyRecipe.AlloyIngredient input : recipe.getInputs()) {
            if (input.catalyst()) {
                continue;
            }
            b.inputFluids(input.fluid().getFluids());
        }
        b.outputFluids(List.of(recipe.getOutput()));
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processCasting(ItemCastingRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputFluids(recipe.getFluids());
        if (recipe.isConsumed()) {
            b.inputItems(Arrays.asList(recipe.getCast().getItems()));
        }
        b.outputItems(List.of(recipe.getOutput()));
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processMolding(MoldingRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getMaterial().getItems()));
        if (recipe.isPatternConsumed()) {
            b.inputItems(Arrays.asList(recipe.getPattern().getItems()));
        }
        b.outputItems(List.of(recipe.getResultItem(RegistryAccess.EMPTY)));
        return b.build(recipe.getId().toString(), type);
    }
}
