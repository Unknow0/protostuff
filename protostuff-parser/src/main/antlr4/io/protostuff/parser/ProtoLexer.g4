//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

lexer grammar ProtoLexer;

options {
   language=Java;
}

EQ:        '=';
SEMICOLON: ';';
COMMA:     ',';
DOT:       '.';
    
LT:      '<';
GT:      '>';
LCURLY:  '{';
RCURLY:  '}';
LPAREN:  '(';
RPAREN:  ')';
LSQUARE: '[';
RSQUARE: ']';
    
TO:  'to';
MAX: 'max';

PACKAGE :     'package';
IMPORT:       'import';
WEAK:         'weak';
PUBLIC:       'public';

OPTION:       'option';
OPT_FOR:      'optimize_for';
SPEED:        'SPEED';
CODE_SIZE:    'CODE_SIZE';
LITE_RUNTIME: 'LITE_RUNTIME';

SERVICE:      'service';
RPC:          'rpc';
STREAM:       'stream';
RETURNS:      'returns';

RESERVED:     'reserved';
EXTENSIONS:   'extensions';
EXTEND:       'extend';

REQUIRED:     'required';
OPTIONAL:     'optional';
REPEATED:     'repeated';

MESSAGE:      'message';
GROUP:        'group';
ONEOF:        'oneof';
ENUM:         'enum';
MAP:          'map';
    
INT32:        'int32';
INT64:        'int64';
UINT32:       'uint32';
UINT64:       'uint64';
SINT32:       'sint32';
SINT64:       'sint64';
FIXED32:      'fixed32';
FIXED64:      'fixed64';
SFIXED32:     'sfixed32';
SFIXED64:     'sfixed64';
FLOAT:        'float';
DOUBLE:       'double';
BOOL:         'bool';
STRING:       'string';
BYTES:        'bytes';


VOID:   'void';

TRUE:   'true';
FALSE:   'false';

ID  
    :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

NUMBER
    :   [+-]?([0-9]+ ('.' [0-9]*)? 
    | '.' [0-9]+) ([eE] [-+]? [0-9]+)?
    | [+-]? 'inf'
    | 'nan'
    ;
    
HEX :   [+-]? '0' [xX] HEX_DIGIT+;
OCTAL: [+-]? '0' [0-7]+;
    
DOC
    :   '///' ~('\n'|'\r')* '\r'? '\n'
    ;
    
COMMENT
    :   ('//' ~('\n'|'\r')* '\r'? '\n'
    |   '/*' .*? '*/') -> skip;

WS  :   [ \t\r\n']+;
    
STRING_LITERAL:
   '"' ( ESC_SEQ | ~('\\'|'"') )* '"' 
   ;

fragment HEX_DIGIT
    :   [0-9a-fA-F]
    ;

fragment ESC_SEQ
    :   '\\' [avbtnfr"'\\]
    |   '\\' [xX] HEX_DIGIT HEX_DIGIT
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
