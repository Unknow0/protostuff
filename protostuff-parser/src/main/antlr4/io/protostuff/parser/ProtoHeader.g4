grammar ProtoHeader;

@parser::members {
	public String version = "proto2";
}

file
	: ( SYNTAX | EDITION ) EQ STRING SCOLON {
		version = $STRING.text;
	 }
	;

COMMENT : '//' ~[\r\n]* -> skip;
WS : [ \t\r\n]+ -> skip; 
SYNTAX: 'syntax';
EDITION: 'edition';
EQ: '=';
SCOLON: ';';
STRING : '"' ~["\r\n]* '"' {
	String s=getText(); 
	setText(s.substring(1, s.length()-1));
};
ERROR: . {
	setType(EOF);
	_input.seek(_input.index() - 1);
};