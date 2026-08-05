package me.croabeast.takion.tag;

import me.croabeast.takion.token.Token;

/**
 * A semantic text tag that inserts content into a message.
 *
 * <p>Resolves to a {@link Token.Rendered}, so besides the visible output it carries the measurement
 * the alignment pass needs and neutral data for optional renderers.
 */
public interface Tag extends Token<Token.Rendered> {}
