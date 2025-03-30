package me.croabeast.takion.character;

import lombok.Getter;

import java.util.Locale;

/**
 * An enumeration of default characters along with their associated display widths.
 * <p>
 * This enum implements {@link CharacterInfo} and provides a predefined list of characters
 * (letters, numbers, and symbols) with configurable length values for text formatting purposes.
 * </p>
 * Each enum constant represents a character with a default or custom length, which can be used
 * to determine how much space the character occupies when rendered (for example, in custom chat formats or UI elements).
 */
@Getter
public enum DefaultCharacter implements CharacterInfo {
    // Alphabet characters with default length (5) unless specified
    A('A'),
    a('a'),
    B('B'),
    b('b'),
    C('C'),
    c('c'),
    D('D'),
    d('d'),
    E('E'),
    e('e'),
    F('F'),
    f('f', 4),
    G('G'),
    g('g'),
    H('H'),
    h('h'),
    I('I', 3),
    i('i', 1),
    J('J'),
    j('j'),
    K('K'),
    k('k', 4),
    L('L'),
    l('l', 1),
    M('M'),
    m('m'),
    N('N'),
    n('n'),
    O('O'),
    o('o'),
    P('P'),
    p('p'),
    Q('Q'),
    q('q'),
    R('R'),
    r('r'),
    S('S'),
    s('s'),
    T('T'),
    t('t', 4),
    U('U'),
    u('u'),
    V('V'),
    v('v'),
    W('W'),
    w('w'),
    X('X'),
    x('x'),
    Y('Y'),
    y('y'),
    Z('Z'),
    z('z'),

    // Numeric characters
    NUM_1('1'),
    NUM_2('2'),
    NUM_3('3'),
    NUM_4('4'),
    NUM_5('5'),
    NUM_6('6'),
    NUM_7('7'),
    NUM_8('8'),
    NUM_9('9'),
    NUM_0('0'),

    // Special characters with optional custom lengths
    EXCLAMATION_POINT('!', 1),
    AT_SYMBOL('@', 6),
    NUM_SIGN('#'),
    DOLLAR_SIGN('$'),
    PERCENT('%'),
    UP_ARROW('^'),
    AMPERSAND('&'),
    ASTERISK('*'),

    LEFT_PARENTHESIS('(', 4),
    RIGHT_PARENTHESIS(')', 4),
    MINUS('-'),
    UNDERSCORE('_'),
    PLUS_SIGN('+'),
    EQUALS_SIGN('='),
    LEFT_CURL_BRACE('{', 4),
    RIGHT_CURL_BRACE('}', 4),
    LEFT_BRACKET('[', 3),
    RIGHT_BRACKET(']', 3),

    COLON(':', 1),
    SEMI_COLON(';', 1),
    DOUBLE_QUOTE('"', 3),
    SINGLE_QUOTE('\'', 1),
    LEFT_ARROW('<', 4),
    RIGHT_ARROW('>', 4),
    QUESTION_MARK('?'),
    SLASH('/'),
    BACK_SLASH('\\'),
    LINE('|', 1),
    TILDE('~'),
    TICK('`', 2),
    PERIOD('.', 1),
    COMMA(',', 1),
    SPACE(' ', 3);

    /**
     * A lowercased name of the enum constant, used for identification purposes.
     */
    final String name;
    /**
     * The character represented by this enum constant.
     */
    final char character;
    /**
     * The display length (or width) of the character. This value is used in text alignment and formatting.
     * The default length is 5 unless a custom value is provided.
     */
    int length = 5;

    DefaultCharacter(char character, int length) {
        this(character);
        this.length = length;
    }

    DefaultCharacter(char character) {
        this.character = character;
        name = name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns a string representation of the default character, including its name, character value, and length.
     *
     * @return a string representation of the character info
     */
    @Override
    public String toString() {
        return "DefaultCharacter{name='" + name + "', character='" + character + "', length=" + length + '}';
    }

    /**
     * Retrieves the display length for the specified character.
     * <p>
     * This static method iterates over the available default characters and returns the length
     * for the matching character. If the character is not found, a length of 0 is returned.
     * </p>
     *
     * @param character the character whose length is to be retrieved
     * @return the display length of the character, or 0 if the character is not defined
     */
    public static int getLength(char character) {
        for (DefaultCharacter def : values())
            if (def.character == character) return def.length;
        return 0;
    }
}
