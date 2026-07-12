package net.michanide.ai_assembler_tech.integration.avaritia;

import java.util.Arrays;

import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;

import net.michanide.ai_assembler_tech.integration.RecipeDataBuilder;
import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Extracts {@link RecipeData} from Re:Avaritia neutron compressor recipes: one input slot
 * consumed {@code getInputCount()} times per operation (passed as the weight, so tag
 * alternatives still average) → one result. Time cost is not a resource and is ignored.
 */
final class AvaritiaRecipeProcessor {

    private AvaritiaRecipeProcessor() {
    }

    static RecipeData process(Recipe<?> recipe, String type) {
        if (recipe instanceof ICompressorRecipe compressor) {
            RecipeDataBuilder b = new RecipeDataBuilder();
            b.inputItems(Arrays.asList(compressor.getInput().getItems()), compressor.getInputCount());
            // CompressorRecipe returns its stored result stack and ignores the registry argument.
            b.outputItems(Arrays.asList(compressor.getResultItem(RegistryAccess.EMPTY)));
            return b.build(compressor.getId().toString(), type);
        }
        return null;
    }
}
