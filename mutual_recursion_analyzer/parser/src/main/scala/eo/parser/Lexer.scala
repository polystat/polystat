package eo.parser

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers



object Lexer extends RegexParsers {
  override def skipWhitespace = true

  override val whiteSpace: Regex = "[ \t\r\f]+".r

  def identifier: Parser[IDENTIFIER] = {
    "[a-z][a-z0-9_A-Z\\-]*".r ^^ {
      str => IDENTIFIER(str)
    }
  }

  def indentation: Parser[INDENTATION] = {
    "\n[ ]*".r ^^ { whitespace =>
      val nSpaces = whitespace.length - 1
      INDENTATION(nSpaces)
    }
  }

  def string: Parser[STRING] = {
    """"[^"]*"""".r ^^ {
      str => STRING(str)
    }
  }

  def integer: Parser[INTEGER] = {
    """[+-]?\b[0-9]+\b""".r ^^ {
      str => INTEGER(str)
    }
  }

  def single_line_comment: Parser[SINGLE_LINE_COMMENT] = {
      """#.*""".r ^^ {
      str => SINGLE_LINE_COMMENT(str.tail)
    }
  }

  def meta: Parser[META] = {
    """\+[a-z][a-z0-9_A-Z\-]*[ ].*""".r ^^ {
      str => {
        val split = str.split(" ", 2)
        META(split(0).stripMargin, split(1).stripMargin)
      }
    }
  }

  def tokens: Parser[List[Token]] = {
    phrase(
      rep1(
          lbracket
            | rbracket
            | lparen
            | rparen
            | array_delimiter

            | phi
            | rho
            | self
            | exclamation_mark
            | colon
            | dots
            | dot
            | assign_name
            | slash

            | meta
            | identifier
            | indentation
            | string
            | integer
            | single_line_comment
      )
    ) ^^ { rawTokens =>
      processIndentations(rawTokens)
    }
  }

  def apply(code: String): Either[LexerError, List[Token]] = {
    parse(tokens, code) match {
      case Success(result, _) => Right(result)
      case Error(msg, _) => Left(LexerError("ERROR: " + msg))
      case Failure(msg, _) => Left(LexerError("FAILURE: " + msg))
    }
  }

  private def processIndentations(tokens: List[Token],
                                  indents: List[Int] = List(0)): List[Token] = {
    tokens.headOption match {

      // if there is an increase in indentation level, we push this new level into the stack
      // and produce an INDENT
      case Some(INDENTATION(spaces)) if spaces > indents.head =>
        INDENT :: processIndentations(tokens.tail, spaces :: indents)

      // if there is a decrease, we pop from the stack until we have matched the new level,
      // producing a DEDENT for each pop
      case Some(INDENTATION(spaces)) if spaces < indents.head =>
        val (dropped, kept) = indents.partition(_ > spaces)
        (dropped map (_ => DEDENT)) ::: processIndentations(tokens.tail, kept)

      // if the indentation level stays unchanged, produce NEWLINE
      case Some(INDENTATION(spaces)) if spaces == indents.head =>
        NEWLINE :: processIndentations(tokens.tail, indents)

      // other tokens are ignored
      case Some(token) =>
        token :: processIndentations(tokens.tail, indents)

      // the final step is to produce a DEDENT for each indentation level still remaining, thus
      // "closing" the remaining open INDENTS
      case None =>
        indents.filter(_ > 0).map(_ => DEDENT)

    }
  }


  private def phi: Parser[PHI] = "@" ^^ (_ => PHI())
  private def rho: Parser[RHO] = "^" ^^ (_ => RHO())
  private def self: Parser[SELF] = "$" ^^ (_ => SELF())
  private def exclamation_mark = "!" ^^ (_ => EXCLAMATION_MARK)
  private def colon = ":" ^^ (_ => COLON)
  private def dot = "." ^^ (_ => DOT)
  private def assign_name = ">" ^^ (_ => ASSIGN_NAME)
  private def lbracket = "[" ^^ (_ => LBRACKET)
  private def rbracket = "]" ^^ (_ => RBRACKET)
  private def lparen = "(" ^^ (_ => LPAREN)
  private def rparen = ")" ^^ (_ => RPAREN)
  private def array_delimiter = "*" ^^ (_ => ARRAY_DELIMITER)
  private def slash = "/" ^^ (_ => SLASH)
  private def dots = "..." ^^ (_ => DOTS)

}
