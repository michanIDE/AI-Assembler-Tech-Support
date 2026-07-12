package net.michanide.ai_assembler_tech.integration.integrateddynamics;

import java.util.Arrays;
import java.util.List;

import com.mojang.datafixers.util.Either;

import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezerFacade;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezerFacade;

import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;

/**
 * Extracts {@link RecipeData} from Integrated Dynamics machine recipes:
 * <ul>
 *   <li>squeezer / mechanical squeezer — item in, chanced item outputs (expected-value weighted)
 *       and/or a fluid output</li>
 *   <li>drying basin / mechanical drying basin — item and/or fluid in, item and/or fluid out
 *       (pure fluid-to-item recipes like menril resin → crystalized menril block exist)</li>
 * </ul>
 * The mechanical variants subclass the base recipe classes, so two {@code instanceof} branches
 * cover all four types. The facade recipes registered under the squeezer types are dynamic
 * (their real output depends on the input's NBT, the declared one is a placeholder) and return
 * {@code null}. Recipe outputs may be tag-based ({@code ItemStackFromIngredient}); those expand
 * to alternatives with their declared count applied.
 */
final class IntegratedDynamicsRecipeProcessor {

    private IntegratedDynamicsRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (recipe instanceof RecipeSqueezerFacade || recipe instanceof RecipeMechanicalSqueezerFacade) {
            return null;
        }
        if (recipe instanceof RecipeSqueezer squeezer) {
            return processSqueezer(squeezer, type);
        }
        if (recipe instanceof RecipeDryingBasin basin) {
            return processDryingBasin(basin, type);
        }
        return null;
    }

    private static RecipeData processSqueezer(RecipeSqueezer recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getInputIngredient().getItems()));
        for (RecipeSqueezer.IngredientChance output : recipe.getOutputItems()) {
            b.outputItems(resolve(output.getIngredient()), output.getChance());
        }
        FluidStack outputFluid = recipe.getOutputFluid();
        if (outputFluid != null && !outputFluid.isEmpty()) {
            b.outputFluids(List.of(outputFluid));
        }
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processDryingBasin(RecipeDryingBasin recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.getInputIngredient().getItems()));
        FluidStack inputFluid = recipe.getInputFluid();
        if (inputFluid != null && !inputFluid.isEmpty()) {
            b.inputFluids(List.of(inputFluid));
        }
        Either<ItemStack, ItemStackFromIngredient> outputItem = recipe.getOutputItem();
        if (outputItem != null) {
            b.outputItems(resolve(outputItem));
        }
        FluidStack outputFluid = recipe.getOutputFluid();
        if (outputFluid != null && !outputFluid.isEmpty()) {
            b.outputFluids(List.of(outputFluid));
        }
        return b.build(recipe.getId().toString(), type);
    }

    /** Expands a concrete stack or a tag-based alternative list, applying the declared count. */
    private static List<ItemStack> resolve(Either<ItemStack, ItemStackFromIngredient> either) {
        return either.map(
                List::of,
                fromIngredient -> Arrays.stream(fromIngredient.getIngredient().getItems())
                        .map(stack -> stack.copyWithCount(stack.getCount() * fromIngredient.getCount()))
                        .toList());
    }
}
