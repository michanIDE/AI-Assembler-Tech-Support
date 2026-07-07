package net.michanide.ai_assembler_tech.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.michanide.aiassembler.util.recipe.RecipeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Accumulates ingredient/result entries for one recipe and produces a {@link RecipeData}.
 *
 * <p>Alternative-averaging follows {@code RecipeFetcher.buildRecipeData}: when one slot accepts N
 * alternatives, each alternative contributes 1/N of its amount, so the slot still costs one choice
 * in total. Fluid (and chemical) amounts are given in mB and scaled to buckets so they stay on the
 * same order of magnitude as item counts in the recipe matrix.</p>
 *
 * <p>Uses only vanilla/Forge types, so it is safe to load without any tech mod present; mod-specific
 * glue (e.g. Mekanism chemicals) converts to plain {@link #input(String, double)} /
 * {@link #output(String, double)} calls in the gated integration packages.</p>
 */
public final class RecipeDataBuilder {

    public static final double MILLIBUCKETS_PER_UNIT = 1000.0;

    private final Map<String, Double> ingredients = new HashMap<>();
    private final Map<String, Double> results = new HashMap<>();

    /** Adds a raw ingredient entry (registry id of an item, fluid, gas, ...), summed over calls. */
    public RecipeDataBuilder input(String id, double amount) {
        merge(ingredients, id, amount);
        return this;
    }

    /** Adds a raw result entry, summed over calls. */
    public RecipeDataBuilder output(String id, double amount) {
        merge(results, id, amount);
        return this;
    }

    /** Adds one input slot that accepts any of the given item alternatives. */
    public RecipeDataBuilder inputItems(List<ItemStack> alternatives) {
        addItems(ingredients, alternatives, 1.0);
        return this;
    }

    /** Adds an input consumed with the given probability per operation (expected cost weighting). */
    public RecipeDataBuilder inputItems(List<ItemStack> alternatives, double chance) {
        addItems(ingredients, alternatives, chance);
        return this;
    }

    /** Adds one input slot that accepts any of the given fluid alternatives. */
    public RecipeDataBuilder inputFluids(List<FluidStack> alternatives) {
        addFluids(ingredients, alternatives);
        return this;
    }

    /** Adds one output whose concrete item may be any of the given alternatives. */
    public RecipeDataBuilder outputItems(List<ItemStack> alternatives) {
        addItems(results, alternatives, 1.0);
        return this;
    }

    /** Adds a chanced output (e.g. a sawmill secondary output), weighted by its chance. */
    public RecipeDataBuilder outputItems(List<ItemStack> alternatives, double chance) {
        addItems(results, alternatives, chance);
        return this;
    }

    /** Adds one output whose concrete fluid may be any of the given alternatives. */
    public RecipeDataBuilder outputFluids(List<FluidStack> alternatives) {
        addFluids(results, alternatives);
        return this;
    }

    /** @return the built data, or {@code null} when either side is empty (recipe should be skipped). */
    public RecipeData build(String recipeId, String type) {
        if (ingredients.isEmpty() || results.isEmpty()) {
            return null;
        }
        return new RecipeData(recipeId, type, ingredients, results);
    }

    private static void addItems(Map<String, Double> target, List<ItemStack> alternatives, double weight) {
        List<Map.Entry<String, Double>> valid = new ArrayList<>(alternatives.size());
        for (ItemStack stack : alternatives) {
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null) {
                valid.add(Map.entry(id.toString(), (double) stack.getCount()));
            }
        }
        for (Map.Entry<String, Double> entry : valid) {
            merge(target, entry.getKey(), entry.getValue() * weight / valid.size());
        }
    }

    private static void addFluids(Map<String, Double> target, List<FluidStack> alternatives) {
        List<Map.Entry<String, Double>> valid = new ArrayList<>(alternatives.size());
        for (FluidStack stack : alternatives) {
            // Ingredient representations list flowing variants alongside the source fluid
            // (water + flowing_water); only the source form is a meaningful resource.
            if (stack.isEmpty() || !stack.getFluid().defaultFluidState().isSource()) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
            if (id != null) {
                valid.add(Map.entry(id.toString(), stack.getAmount() / MILLIBUCKETS_PER_UNIT));
            }
        }
        for (Map.Entry<String, Double> entry : valid) {
            merge(target, entry.getKey(), entry.getValue() / valid.size());
        }
    }

    private static void merge(Map<String, Double> target, String id, double amount) {
        if (id != null && amount > 0) {
            target.merge(id, amount, Double::sum);
        }
    }
}
