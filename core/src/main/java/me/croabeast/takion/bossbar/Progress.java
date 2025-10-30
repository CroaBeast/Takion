package me.croabeast.takion.bossbar;

/**
 * Represents the animation progress of a bossbar animation.
 *
 * <p> This enum defines the possible states of progress during an animation:
 * <ul>
 *     <li>{@link #INCREASE} - Progress increases from 0.0 to 1.0.</li>
 *     <li>{@link #DECREASE} - Progress decreases from 1.0 to 0.0.</li>
 *     <li>{@link #STATIC} - Progress remains constant at a specified value.</li>
 * </ul>
 *
 * <p> The progress can be used to control the visual representation of the bossbar,
 * allowing for smooth transitions and animations.
 */
public enum Progress {
    /**
     * Progress increases from 0.0 to 1.0.
     */
    INCREASE,
    /**
     * Progress decreases from 1.0 to 0.0.
     */
    DECREASE,
    /**
     * Progress remains constant at {@code staleProgress}.
     */
    STATIC
}
