package net.michanide.ai_assembler_tech.integration.industrialforegoing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.buuz135.industrial.recipe.CrusherRecipe;
import com.buuz135.industrial.recipe.DissolutionChamberRecipe;
import com.buuz135.industrial.recipe.FluidExtractorRecipe;
import com.buuz135.industrial.recipe.StoneWorkGenerateRecipe;

import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Extracts {@link RecipeData} from Industrial Foregoing machine recipes:
 * <ul>
 *   <li>dissolution chamber — item slots + fluid → item and/or fluid</li>
 *   <li>fluid extractor — a placed block yields fluid per operation and breaks with
 *       {@code breakChance}, so the block (and the block it degrades into, when any) is
 *       weighted by that chance as expected cost/return per operation</li>
 *   <li>crusher — item → item, both sides given as {@link Ingredient} alternatives</li>
 *   <li>stonework generation — consumed water/lava → generated block; recipes that only
 *       require but never consume fluids have no ingredient cost and are skipped</li>
 * </ul>
 * Laser drill recipes return {@code null}: the lens catalyst is not consumed, so there is no
 * meaningful ingredient side.
 */
final class IndustrialForegoingRecipeProcessor {

    private static final double MILLIBUCKETS_PER_UNIT = RecipeDataBuilder.MILLIBUCKETS_PER_UNIT;

    private IndustrialForegoingRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (recipe instanceof DissolutionChamberRecipe dissolution) {
            return processDissolution(dissolution, type);
        }
        if (recipe instanceof FluidExtractorRecipe extractor) {
            return processFluidExtractor(extractor, type);
        }
        if (recipe instanceof CrusherRecipe crusher) {
            return processCrusher(crusher, type);
        }
        if (recipe instanceof StoneWorkGenerateRecipe stonework) {
            return processStonework(stonework, type);
        }
        return null;
    }

    private static RecipeData processDissolution(DissolutionChamberRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        for (Ingredient.Value slot : recipe.input) {
            b.inputItems(new ArrayList<>(slot.getItems()));
        }
        if (recipe.inputFluid != null && !recipe.inputFluid.isEmpty()) {
            b.inputFluids(List.of(recipe.inputFluid));
        }
        if (recipe.output != null && !recipe.output.isEmpty()) {
            b.outputItems(List.of(recipe.output));
        }
        if (recipe.outputFluid != null && !recipe.outputFluid.isEmpty()) {
            b.outputFluids(List.of(recipe.outputFluid));
        }
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processFluidExtractor(FluidExtractorRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(new ArrayList<>(recipe.input.getItems()), recipe.breakChance);
        // What the block degrades into survives the operation; an item-less result (air) adds nothing.
        b.outputItems(List.of(new ItemStack(recipe.result)), recipe.breakChance);
        b.outputFluids(List.of(recipe.output));
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processCrusher(CrusherRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        b.inputItems(Arrays.asList(recipe.input.getItems()));
        b.outputItems(Arrays.asList(recipe.output.getItems()));
        return b.build(recipe.getId().toString(), type);
    }

    private static RecipeData processStonework(StoneWorkGenerateRecipe recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();
        if (recipe.waterConsume > 0) {
            b.input("minecraft:water", recipe.waterConsume / MILLIBUCKETS_PER_UNIT);
        }
        if (recipe.lavaConsume > 0) {
            b.input("minecraft:lava", recipe.lavaConsume / MILLIBUCKETS_PER_UNIT);
        }
        b.outputItems(List.of(recipe.output));
        return b.build(recipe.getId().toString(), type);
    }
}
