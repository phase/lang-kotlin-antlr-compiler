grammar Lang;

program: importDeclaration* externalDeclaration* EOF;

id_p: ID ('.' ID)*;

importDeclaration
    : 'import' id_p (',' id_p)*
    ;

externalDeclaration
    : functionDeclaration
    | variableDeclaration
    | classDeclaration
    | traitDeclaration
    ;

functionPrototype: attributeList ID '(' argumentList ')' typeAnnotation?;
functionDeclaration: functionPrototype statementList (expression | statement | blockStatement);
variableDeclaration: variableModifier variableSignature ('=' expression)?;

functionCall: ID '(' expressionList ')';
methodCall: ID '.' ID '(' expressionList ')';

variableModifier
    : 'let'
    | 'var'
    ;

attributeList
    :
    | attribute attributeList
    ;

attribute: '@' ID ('(' ID (',' ID)* ')')?;

variableReassignment: ID '=' expression;

statement
    : fieldSetter
    | variableDeclaration
    | variableReassignment
    | methodCall
    | functionCall
    | 'continue'
    | 'break'
    | 'return' expression
    ;

blockStatement
    : 'if' expression statementList elif* elseStatement? ';'
    | 'for' ID 'in' expression statementList ';'
    | 'while' expression statementList ';'
    ;

elif: 'elif' expression statementList;
elseStatement: 'else' statementList;

statementList
    :
    | statement
    | blockStatement
    | blockStatement statementList
    | statement ',' statementList
    ;

classDeclaration
    : 'class' ID (':' ID (',' ID)*)? externalDeclaration* ';'
    ;

traitDeclaration
    : 'trait' ID functionPrototype* ';'
    ;

expressionList
    :
    | expression
    | expression ',' expressionList
    ;

expression
    : ('-'|'+') expression
    | expression '.' ID // field getter
    | expression ('*'|'/'|'*.'|'/.') expression
    | expression ('+'|'-'|'-.'|'+.') expression
    | expression ('>'|'>='|'<'|'<='|'=='|'!=') expression
    | '!' expression
    | expression ('&'|'&&') expression
    | expression ('|'|'||') expression
    | '~' expression
    | '(' expression ')'
    | expression '`' ID '`' expression // infix function
    | methodCall
    | classInitializer
    | functionCall
    | ID
    | STRING
    | HEX
    | INT
    | FLOAT
    | 'true'
    | 'false'
    ;

fieldSetter: expression '.' ID '=' expression;

classInitializer: 'new' ID '(' expressionList ')';

argumentList: argument (',' argument)*|;

argument
    : variableSignature
    | variableSignature '=' expression
    ;

typeAnnotation: ':' ID;
variableSignature: ID typeAnnotation?;

HEX: '0' ('x'|'X') HEXDIGIT+ [Ll]?;

INT: DIGIT+ [Ll]?;

fragment HEXDIGIT: ('0'..'9'|'a'..'f'|'A'..'F');

FLOAT
    : DIGIT+ '.' DIGIT* EXP? [dq]?
    | DIGIT+ EXP? [dq]?
    | '.' DIGIT+ EXP? [dq]?
    ;

fragment DIGIT: '0'..'9';
fragment EXP: ('E' | 'e') ('+' | '-')? INT;

STRING
    : '"' ( ESC | ~[\\"] )*? '"'
    | '\'' ( ESC | ~[\\'] )*? '\''
    ;

fragment ESC
    : '\\' [abtnfrv"'\\]
    | UNICODE_ESCAPE
    | HEX_ESCAPE
    | OCTAL_ESCAPE
    ;

fragment UNICODE_ESCAPE
    : '\\' 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    | '\\' 'u' '{' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT '}'
    ;

fragment OCTAL_ESCAPE
    : '\\' [0-3] [0-7] [0-7]
    | '\\' [0-7] [0-7]
    | '\\' [0-7]
    ;

fragment HEX_ESCAPE
    : '\\' HEXDIGIT HEXDIGIT?
    ;

ID: LETTER (LETTER|DIGIT|'_')*;

fragment LETTER: [a-zA-Z];

COMMENT: '#' .*? '\r'? '\n' -> type(WS);

WS: [ \r\n\t\u000C]+ -> skip;
