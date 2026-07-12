package net.michanide.ai_assembler_tech.integration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.michanide.ai_assembler_tech.AIAssemblerTech;
import net.minecraftforge.fml.ModList;

/**
 * Declares every supported tech mod and runs the integrations whose mod is actually present.
 *
 * <p>Integrations are held as {@link Supplier}s so that an implementation class (which imports
 * its tech mod's classes) is only loaded by the JVM after {@link ModList#isLoaded} confirms the
 * mod exists. Adding a new integration is one {@code add(...)} line in the static block.</p>
 */
public final class IntegrationManager {

    private static final Map<String, Supplier<TechIntegration>> INTEGRATIONS = new LinkedHashMap<>();

    static {
        // Deliberately lambdas, not constructor references: a method reference would resolve (and
        // classload) the integration class as soon as this static block runs, defeating the gate.
        add("mekanism", () -> new net.michanide.ai_assembler_tech.integration.mekanism.MekanismIntegration());
        add("thermal_foundation", () -> new net.michanide.ai_assembler_tech.integration.thermal.ThermalIntegration());
        add("tconstruct", () -> new net.michanide.ai_assembler_tech.integration.tconstruct.TinkersIntegration());
        add("industrialforegoing", () -> new net.michanide.ai_assembler_tech.integration.industrialforegoing.IndustrialForegoingIntegration());
        add("ae2", () -> new net.michanide.ai_assembler_tech.integration.ae2.AE2Integration());
        add("draconicevolution", () -> new net.michanide.ai_assembler_tech.integration.draconicevolution.DraconicIntegration());
        add("integrateddynamics", () -> new net.michanide.ai_assembler_tech.integration.integrateddynamics.IntegratedDynamicsIntegration());
    }

    private IntegrationManager() {
    }

    private static void add(String modId, Supplier<TechIntegration> factory) {
        INTEGRATIONS.put(modId, factory);
    }

    /**
     * Instantiates and runs every integration whose mod is loaded. Called on the main thread
     * during {@code FMLLoadCompleteEvent}. A failing integration is logged and skipped so one
     * broken mod version cannot take down the others.
     */
    public static void runIntegrations() {
        for (Map.Entry<String, Supplier<TechIntegration>> entry : INTEGRATIONS.entrySet()) {
            String modId = entry.getKey();
            if (!ModList.get().isLoaded(modId)) {
                AIAssemblerTech.LOGGER.debug("Skipping AI Assembler integration for '{}' (mod not loaded)", modId);
                continue;
            }
            try {
                entry.getValue().get().register();
                AIAssemblerTech.LOGGER.info("Registered AI Assembler integration for '{}'", modId);
            } catch (Throwable t) {
                AIAssemblerTech.LOGGER.error(
                        "AI Assembler integration for '{}' failed; its recipes will use default processing", modId, t);
            }
        }
    }
}
