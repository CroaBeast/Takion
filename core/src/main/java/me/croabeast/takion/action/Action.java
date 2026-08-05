package me.croabeast.takion.action;

import me.croabeast.takion.token.Token;

/**
 * A message instruction that performs a side effect.
 *
 * <p>Resolves to whether the action had an effect. Unlike the other token kinds it honours
 * {@link Token#getChannels()}, so an action can be restricted to specific channels.
 */
public interface Action extends Token<Boolean> {}
