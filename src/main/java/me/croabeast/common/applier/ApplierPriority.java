package me.croabeast.common.applier;

public enum ApplierPriority {
    /**
     * Operators with this priority will be called last.
     */
    LOWEST,
    /**
     * Operators have a low importance to be applied.
     */
    LOW,
    /**
     * Operators are neither important nor unimportant.
     */
    NORMAL,
    /**
     * Operators have a high importance to be applied.
     */
    HIGH,
    /**
     * Operators with this priority will be called first.
     */
    HIGHEST
}
