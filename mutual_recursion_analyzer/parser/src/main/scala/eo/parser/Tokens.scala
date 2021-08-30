package eo.parser

sealed trait Token

// complex tokens
case class SINGLE_LINE_COMMENT(text: String) extends Token
case class INDENTATION(level: Int) extends Token
case class META(name: String, text: String) extends Token

sealed trait ACCESSIBLE_ATTRIBUTE_NAME extends Token {
  val name: String
}

case class IDENTIFIER(name: String) extends ACCESSIBLE_ATTRIBUTE_NAME
case class PHI(name: "@" = "@") extends ACCESSIBLE_ATTRIBUTE_NAME
case class RHO(name: "^" = "^") extends ACCESSIBLE_ATTRIBUTE_NAME
case class SELF(name: "$" = "$") extends ACCESSIBLE_ATTRIBUTE_NAME


sealed trait LITERAL extends Token {
  val value: String
}
case class STRING(value: String) extends LITERAL
case class CHAR(value: String) extends LITERAL
case class INTEGER(value: String) extends LITERAL
case class FLOAT(value: String) extends LITERAL

// delimiters
case object INDENT extends Token
case object DEDENT extends Token
case object LBRACKET extends Token
case object RBRACKET extends Token
case object LPAREN extends Token
case object RPAREN extends Token
case object ARRAY_DELIMITER extends Token
case object NEWLINE extends Token

// standalone tokens
case object EXCLAMATION_MARK extends Token
case object COLON extends Token
case object DOT extends Token
case object PLUS extends Token
case object ASSIGN_NAME extends Token
case object SLASH extends Token
case object DOTS extends Token


