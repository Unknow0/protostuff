parser grammar Proto2Parser;

options {
	language=Java;
	tokenVocab = ProtoLexer;
	superClass = AbstractParser;
}

ident:
      ID
    | PACKAGE
    | IMPORT
    | WEAK
    | PUBLIC
    | OPTION
    | OPT_FOR
    | SPEED
    | CODE_SIZE
    | LITE_RUNTIME
    | SERVICE
    | RPC
    | STREAM
    | RETURNS
    | RESERVED
    | EXTENSIONS
    | EXTEND
    | REQUIRED
    | OPTIONAL
    | REPEATED
    | MESSAGE
    | GROUP
    | ONEOF
    | ENUM
    | MAP
    | INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | FLOAT
    | DOUBLE
    | BOOL
    | STRING
    | BYTES
    | VOID
    | TRUE
    | FALSE
    | MAX | TO;
fullId: ident (DOT ident)*;

constant:
	STRING_LITERAL
	| TRUE
	| FALSE
	| NUMBER
	| HEX
	| OCTAL
	;

typeScalar:
	  STRING | BOOL | BYTES
	| INT32 | UINT32 | SINT32 | FIXED32 | SFIXED32
	| INT64 | UINT64 | SINT64 | FIXED64 | SFIXED64
	;
typeMap:
	MAP LT typeScalar WS? COMMA WS? type GT;

type:
	  typeScalar
	| typeMap
	| fullId
	;

label: ( REQUIRED | OPTIONAL | REPEATED);

field:
	 (label WS)? type WS
		ident WS? EQ WS? NUMBER WS? fieldOpt? SEMICOLON
	;
group
	:
	(label WS)? GROUP WS ident WS?
		EQ WS? NUMBER WS? LCURLY
		(WS | messageBody)*
		RCURLY
	;
oneof:
	ONEOF WS ID WS? LCURLY (
		optionStmt
		| field
		| WS )* RCURLY
	;
extend: EXTEND WS ident WS? LCURLY ( WS | field | group )* RCURLY;
	
range: NUMBER ( WS TO (WS NUMBER | WS MAX) )?;
ranges: range (WS? COMMA WS? range)?;
fieldNames: STRING_LITERAL (WS? COMMA WS? STRING_LITERAL)?;

extentions:
	EXTENSIONS WS ranges WS? SEMICOLON
	;

reserved: RESERVED WS (ranges |fieldNames) WS? SEMICOLON;

importStmt:
	IMPORT WS ((WEAK | PUBLIC) WS)? STRING_LITERAL WS? SEMICOLON
	;

packageStmt
	:
	PACKAGE WS fullId WS? SEMICOLON
	;

option:
	OPT_FOR WS? EQ WS? (
		CODE_SIZE
		| SPEED
		| LITE_RUNTIME
		)
	| optName WS? EQ WS? (constant|fullId)
	;
optName: (fullId | LPAREN fullId RPAREN) (DOT fullId)?;
fieldOpt:
	WS? LSQUARE WS? option ( WS? COMMA  WS? option)* WS* RSQUARE
	;
optionStmt: OPTION WS option WS* SEMICOLON	;
	
messageBody:
	  optionStmt
	| messageStmt
	| enumStmt
	| field
	| group
	| oneof
	| extentions
	| reserved
	| extend
	;

enumField:
	ident WS? EQ WS? NUMBER fieldOpt? SEMICOLON
	;
enumStmt:
	ENUM WS ident WS? LCURLY (
		  WS
		| optionStmt
		| reserved
		| enumField
	)* RCURLY SEMICOLON?
	;

messageStmt: 
	MESSAGE WS ident WS? LCURLY
	   (WS | messageBody)*
	RCURLY
	;
rpc:
	RPC WS ident WS? LPAREN WS? (STREAM WS)? fullId WS? RPAREN
	WS? RETURNS WS? LPAREN WS? (STREAM WS)? fullId WS? RPAREN
	WS? rpcBody SEMICOLON;
rpcBody: (LCURLY (WS | optionStmt)* RCURLY)?; 
serviceStmt: SERVICE WS ident WS? LCURLY (WS | option | rpc)* RCURLY;
statement
	:
	importStmt
	| packageStmt
	| optionStmt
	| messageStmt
	| enumStmt
	| extend
	| serviceStmt
	;
	
load: (WS | statement)*;