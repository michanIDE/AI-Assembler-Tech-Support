package net.michanide.ai_assembler_tech.integration;

/**
 * One tech mod integration: registers special ingredients and special recipe processors
 * with AI Assembler for a single optional dependency.
 *
 * <p>Implementations live in {@code integration.<modname>} packages and may import classes
 * of their tech mod freely — {@link IntegrationManager} only instantiates them after
 * confirming the mod is loaded, so those classes never reach the classloader otherwise.
 * Always-loaded code must not reference implementation classes directly; register them
 * through a {@code Supplier} in {@link IntegrationManager}.</p>
 */
public interface TechIntegration {

    /**
     * Registers this mod's special ingredients and recipe processors.
     * Called once on the main thread during {@code FMLLoadCompleteEvent},
     * before AI Assembler builds its recipe matrix at server start.
     */
    void register();
}
