package me.croabeast.takion.character;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum DefaultCharacter implements CharacterInfo {
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

    final String name;
    final char character;
    int length = 5;

    DefaultCharacter(char character, int length) {
        this(character);
        this.length = length;
    }
    
    DefaultCharacter(char character) {
        this.character = character;
        name = name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String toString() {
        return "DefaultCharacter{name='" + name + "', character='" + character + "', length=" + length + '}';
    }
    
    public static int getLength(char character) {
        for (DefaultCharacter def : values())
            if (def.character == character) return def.length;

        return 0;
    }
}
