package net.michanide.ai_assembler_tech.integration.mekanism;

import java.util.List;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Extracts {@link RecipeData} from Mekanism recipes by dispatching on the recipe API base classes,
 * so one processor covers every machine. Chemicals appear in the data under their registry ids
 * (e.g. {@code mekanism:oxygen}) with amounts scaled from mB to buckets; they are all registered
 * as special ingredients by {@link MekanismIntegration}, so they contribute recipe relationships
 * without ever being vectorized or recommended.
 */
final class MekanismRecipeProcessor {

    private MekanismRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        RecipeDataBuilder b = new RecipeDataBuilder();

        if (recipe instanceof ItemStackToItemStackRecipe r) { // enriching, crushing, smelting
            b.inputItems(r.getInput().getRepresentations());
            b.outputItems(r.getOutputDefinition());
        } else if (recipe instanceof SawmillRecipe r) {
            b.inputItems(r.getInput().getRepresentations());
            b.outputItems(r.getMainOutputDefinition());
            b.outputItems(r.getSecondaryOutputDefinition(), r.getSecondaryChance());
        } else if (recipe instanceof CombinerRecipe r) {
            b.inputItems(r.getMainInput().getRepresentations());
            b.inputItems(r.getExtraInput().getRepresentations());
            b.outputItems(r.getOutputDefinition());
        } else if (recipe instanceof ItemStackChemicalToItemStackRecipe<?, ?, ?> r) {
            // purifying, injecting, compressing, metallurgic infusing, nucleosynthesizing, painting
            b.inputItems(r.getItemInput().getRepresentations());
            chemicalInput(b, r.getChemicalInput());
            b.outputItems(r.getOutputDefinition());
        } else if (recipe instanceof ChemicalCrystallizerRecipe r) {
            chemicalInput(b, r.getInput());
            b.outputItems(r.getOutputDefinition());
        } else if (recipe instanceof ChemicalDissolutionRecipe r) {
            b.inputItems(r.getItemInput().getRepresentations());
            chemicalInput(b, r.getGasInput());
            for (BoxedChemicalStack out : r.getOutputDefinition()) {
                chemicalOutput(b, out.getChemicalStack());
            }
        } else if (recipe instanceof ChemicalChemicalToChemicalRecipe<?, ?, ?> r) { // chemical infusing, pigment mixing
            chemicalInput(b, r.getLeftInput());
            chemicalInput(b, r.getRightInput());
            chemicalOutputs(b, r.getOutputDefinition());
        } else if (recipe instanceof FluidChemicalToChemicalRecipe<?, ?, ?> r) { // washing
            b.inputFluids(r.getFluidInput().getRepresentations());
            chemicalInput(b, r.getChemicalInput());
            chemicalOutputs(b, r.getOutputDefinition());
        } else if (recipe instanceof ChemicalToChemicalRecipe<?, ?, ?> r) { // activating, centrifuging
            chemicalInput(b, r.getInput());
            chemicalOutputs(b, r.getOutputDefinition());
        } else if (recipe instanceof ItemStackToChemicalRecipe<?, ?> r) {
            // oxidizing, pigment extracting, gas/infusion conversion
            b.inputItems(r.getInput().getRepresentations());
            chemicalOutputs(b, r.getOutputDefinition());
        } else if (recipe instanceof ItemStackToFluidRecipe r) { // nutritional liquification
            b.inputItems(r.getInput().getRepresentations());
            b.outputFluids(r.getOutputDefinition());
        } else if (recipe instanceof FluidToFluidRecipe r) { // evaporating
            b.inputFluids(r.getInput().getRepresentations());
            b.outputFluids(r.getOutputDefinition());
        } else if (recipe instanceof ElectrolysisRecipe r) { // separating
            b.inputFluids(r.getInput().getRepresentations());
            for (ElectrolysisRecipe.ElectrolysisRecipeOutput out : r.getOutputDefinition()) {
                chemicalOutput(b, out.left());
                chemicalOutput(b, out.right());
            }
        } else if (recipe instanceof PressurizedReactionRecipe r) {
            b.inputItems(r.getInputSolid().getRepresentations());
            b.inputFluids(r.getInputFluid().getRepresentations());
            chemicalInput(b, r.getInputGas());
            for (PressurizedReactionRecipe.PressurizedReactionRecipeOutput out : r.getOutputDefinition()) {
                if (!out.item().isEmpty()) {
                    b.outputItems(List.of(out.item()));
                }
                if (!out.gas().isEmpty()) {
                    chemicalOutput(b, out.gas());
                }
            }
        } else if (recipe instanceof RotaryRecipe) {
            // Phase conversion between a fluid and its gas form. Mekanism gives both forms the same
            // registry id, so the extracted data would be a self-loop (x -> x); nothing to relate.
            return null;
        } else if (recipe instanceof ItemStackToEnergyRecipe) {
            return null; // produces energy, not a resource
        } else {
            AIAssemblerTech.LOGGER.debug("No Mekanism extraction rule for {} ({}); skipping",
                    recipe.getId(), recipe.getClass().getName());
            return null;
        }

        return b.build(recipe.getId().toString(), type);
    }

    /** One chemical input slot, averaged across its alternatives, amounts in buckets. */
    private static void chemicalInput(RecipeDataBuilder b, ChemicalStackIngredient<?, ?> ingredient) {
        List<? extends ChemicalStack<?>> alternatives = ingredient.getRepresentations();
        long valid = alternatives.stream().filter(s -> !s.isEmpty()).count();
        for (ChemicalStack<?> stack : alternatives) {
            if (!stack.isEmpty()) {
                b.input(stack.getTypeRegistryName().toString(),
                        stack.getAmount() / RecipeDataBuilder.MILLIBUCKETS_PER_UNIT / valid);
            }
        }
    }

    /** One chemical output whose concrete chemical may be any of the given alternatives. */
    private static void chemicalOutputs(RecipeDataBuilder b, List<? extends ChemicalStack<?>> alternatives) {
        long valid = alternatives.stream().filter(s -> !s.isEmpty()).count();
        for (ChemicalStack<?> stack : alternatives) {
            if (!stack.isEmpty()) {
                b.output(stack.getTypeRegistryName().toString(),
                        stack.getAmount() / RecipeDataBuilder.MILLIBUCKETS_PER_UNIT / valid);
            }
        }
    }

    private static void chemicalOutput(RecipeDataBuilder b, ChemicalStack<?> stack) {
        if (!stack.isEmpty()) {
            b.output(stack.getTypeRegistryName().toString(),
                    stack.getAmount() / RecipeDataBuilder.MILLIBUCKETS_PER_UNIT);
        }
    }
}
