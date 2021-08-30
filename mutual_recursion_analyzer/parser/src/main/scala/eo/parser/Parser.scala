package eo.parser

import com.github.tarao.nonempty.collection.NonEmpty
import eo.core.ast._
import eo.core.ast.astparams.EOExprOnly

import higherkindness.droste.data.Fix
import eo.backend.eolang.ToEO.instances._
import eo.backend.eolang.ToEO.ops._
import eo.backend.eolang.inlineorlines.ops._


import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

class WorkflowTokenReader(val tokens: Seq[Token]) extends Reader[Token] {
  override def first: Token = tokens.head

  override def atEnd: Boolean = tokens.isEmpty

  override def pos: Position = NoPosition

  override def rest: Reader[Token] = new WorkflowTokenReader(tokens.tail)
}

object Parser extends Parsers {
  override type Elem = Token

  def parse(tokens: Seq[Token]): Either[ParserError, EOProg[EOExprOnly]] = {
    val reader = new WorkflowTokenReader(tokens)
    phrase(program)(reader) match {
      case Success(result, _) => Right(result)
      case Error(msg, _) => Left(ParserError(msg))
      case Failure(msg, _) => Left(ParserError(msg))
    }
  }

  def apply(code: String): Either[CompilationError, EOProg[EOExprOnly]] = {
    val result = for {
      tokens <- {
        println(s"\nSOURCE CODE:\n$code")
        Lexer(code)
      }
      ast <- {
        println(s"\nTOKENS:\n$tokens")
        parse(tokens)
      }
    } yield ast

    result match {
      case Left(value) =>
        value match {
          case LexerError(msg) => println(s"LEXER ERROR: $msg")
          case ParserError(msg) => println(s"PARSER ERROR: $msg")
        }
      case Right(value) =>
        println(s"\nAST:\n$value")
        println("\nRESTORED PROGRAM:")
        println(value.toEO.allLinesToString)
    }
    result
  }

  private def identifier: Parser[IDENTIFIER] = {
    accept("identifier", { case id: IDENTIFIER => id })
  }

  private def phi: Parser[PHI] = {
    accept("phi", { case phi: PHI => phi })
  }

  private def accessibleAttributeName: Parser[ACCESSIBLE_ATTRIBUTE_NAME] =
    accept("accessibleAttributeName", {
      case name: ACCESSIBLE_ATTRIBUTE_NAME => name
    })

  private def literal: Parser[LITERAL] = {
    accept("literal", { case lit: LITERAL => lit })
  }

  private def single_line_comment: Parser[SINGLE_LINE_COMMENT] = {
    accept("single line comment", {
      case comment: SINGLE_LINE_COMMENT => comment
    })
  }

  private def meta: Parser[META] = {
    accept("meta", { case meta: META => meta })
  }

  private def createNonEmpty(
                              objs: Seq[EOBnd[EOExprOnly]]
                            ): NonEmpty[EOBnd[EOExprOnly], Vector[EOBnd[EOExprOnly]]] = {
    NonEmpty.from(objs) match {
      case Some(value) => value.toVector
      case None => throw new Exception("1 or more arguments expected, got 0.")
    }
  }

  private def extractEOExpr(bnd: EOBnd[EOExprOnly]): EOExprOnly = {
    bnd match {
      case EOAnonExpr(expr) => expr
      case EOBndExpr(_, expr) => expr
    }
  }

  private def createInverseDot(id: IDENTIFIER,
                               args: Vector[EOBnd[EOExprOnly]]): EOExprOnly = {
    if (args.tail.nonEmpty) {
      Fix[EOExpr](
        EOCopy(
          Fix[EOExpr](EODot(extractEOExpr(args.head), id.name)),
          createNonEmpty(args.tail)
        )
      )
    } else {
      Fix[EOExpr](EODot(extractEOExpr(args.head), id.name))
    }
  }

  def commentsOrNewlines: Parser[List[Token]] =
    rep(NEWLINE | single_line_comment)

  def program: Parser[EOProg[EOExprOnly]] = {
    opt(metas) ~ objects ^^ {
      case metas ~ objs =>
        EOProg(metas.getOrElse(EOMetas(None, Vector())), objs)
    }
  }

  def metas: Parser[EOMetas] = {
    rep1(commentsOrNewlines ~> meta) ^^ { metas => {
      def processOtherMetas(other: List[META]): List[EOMeta] = other match {
        case META(name, text) :: tail if name == "+alias" =>
          val alias :: value :: _ =
            text.split(' ').filterNot(_.isEmpty).toList
          EOAliasMeta(alias, value) :: processOtherMetas(tail)
        case META(name, text) :: tail if name == "+rt" =>
          val rt :: value :: _ = text.split(' ').filterNot(_.isEmpty).toList
          EORTMeta(rt, value) :: processOtherMetas(tail)
        case META(_, _) :: tail => processOtherMetas(tail)
        case Nil => Nil
      }

      val (pkg, otherMetas) = metas.head match {
        case META(name, text) if name == "+package" =>
          (Some(text), metas.tail)
        case META(_, _) => (None, metas)
      }

      EOMetas(pkg, processOtherMetas(otherMetas).toVector)
    }
    }
  }

  def objects: Parser[Vector[EOBnd[EOExprOnly]]] = {
    rep(`object`) ^^
      (objs => objs.toVector)
  }

  def `object`: Parser[EOBnd[EOExprOnly]] = {
    commentsOrNewlines ~> (application | abstraction) <~ commentsOrNewlines
  }

  def application: Parser[EOBnd[EOExprOnly]] = {
    namedApplication | anonApplication
  }

  def simpleApplicationTarget: Parser[EOExprOnly] = {
    val data = literal ^^ {
      case CHAR(value) => Fix[EOExpr](EOCharData(value.charAt(0)))
      case FLOAT(value) => Fix[EOExpr](EOFloatData(value.toFloat))
      case STRING(value) => Fix[EOExpr](EOStrData(value))
      case INTEGER(value) => Fix[EOExpr](EOIntData(value.toInt))
    }

    val attr = accessibleAttributeName ^^ { name =>
      Fix[EOExpr](EOSimpleApp(name.name))
    }

    attr | data
    // TODO: do something about arrays (`*`)
  }

  def applicationTarget: Parser[EOExprOnly] = {
    val attributeChain: Parser[EOExprOnly] =
      simpleApplicationTarget ~ rep1(DOT ~> accessibleAttributeName) ^^ {
        case start ~ attrs =>
          attrs.foldLeft(start)((acc, id) => Fix[EOExpr](EODot(acc, id.name)))
      }
    attributeChain | simpleApplicationTarget
  }

  def singleLineApplication: Parser[EOExprOnly] = {
    val justTarget = applicationTarget
    val parenthesized = LPAREN ~> singleLineApplication <~ RPAREN
    val horizontalApplicationArgs
    : Parser[NonEmpty[EOBnd[EOExprOnly], Vector[EOBnd[EOExprOnly]]]] = {
      rep1(justTarget | parenthesized) ^^
        (args => createNonEmpty(args.map(EOAnonExpr(_))))
    }
    val justApplication = (parenthesized | justTarget) ~ horizontalApplicationArgs ^^ {
      case trg ~ args => Fix[EOExpr](EOCopy(trg, args))
    }

    justApplication | parenthesized | justTarget
  }

  def verticalApplicationArgs
  : Parser[NonEmpty[EOBnd[EOExprOnly], Vector[EOBnd[EOExprOnly]]]] = {
    INDENT ~> rep1(`object`) <~ DEDENT ^^
      (argList => createNonEmpty(argList))
  }

  def namedApplication: Parser[EOBndExpr[EOExprOnly]] = {
    val noArgs = singleLineApplication ~ name ^^ {
      case target ~ name =>
        EOBndExpr(name, target)
    }
    val inverseDot = identifier ~ DOT ~ name ~ verticalApplicationArgs ^^ {
      case id ~ _ ~ name ~ args =>
        EOBndExpr(name, createInverseDot(id, args))
    }
    val withArgs = singleLineApplication ~ name ~ verticalApplicationArgs ^^ {
      case target ~ name ~ args =>
        EOBndExpr(name, Fix[EOExpr](EOCopy(target, args)))
    }

    inverseDot | withArgs | noArgs
  }

  def anonApplication: Parser[EOAnonExpr[EOExprOnly]] = {
    val noArgs = singleLineApplication ^^ { target =>
      EOAnonExpr(target)
    }
    val inverseDot = identifier ~ DOT ~ verticalApplicationArgs ^^ {
      case id ~ _ ~ args =>
        EOAnonExpr(createInverseDot(id, args))
    }
    val withArgs = singleLineApplication ~ verticalApplicationArgs ^^ {
      case target ~ args =>
        EOAnonExpr(Fix[EOExpr](EOCopy(target, args)))
    }
    inverseDot | withArgs | noArgs
  }


  def abstraction: Parser[EOBnd[EOExprOnly]] = {
    namedAbsObj | anonAbsObj
  }

  def anonAbsObj: Parser[EOAnonExpr[EOExprOnly]] = {
    args ~ opt(boundAttrs) ^^ {
      case (params, vararg) ~ attrs =>
        EOAnonExpr(
          Fix[EOExpr](EOObj(params, vararg, attrs.getOrElse(Vector())))
        )
    }
  }

  def namedAbsObj: Parser[EOBndExpr[EOExprOnly]] = {
    args ~ name ~ opt(boundAttrs) ^^ {
      case (params, vararg) ~ name ~ attrs =>
        EOBndExpr(
          name,
          Fix[EOExpr](EOObj(params, vararg, attrs.getOrElse(Vector())))
        )
    }
  }

  def name: Parser[EOBndName] = {
    val lazyName = ASSIGN_NAME ~> identifier ^^
      (id => EOAnyName(LazyName(id.name)))
    val lazyPhi = ASSIGN_NAME ~> phi ^^
      (_ => EODecoration())
    val constName = ASSIGN_NAME ~> identifier <~ EXCLAMATION_MARK ^^
      (id => EOAnyName(ConstName(id.name)))

    constName | lazyPhi | lazyName
  }

  def args: Parser[(Vector[LazyName], Option[LazyName])] = {
    val vararg = LBRACKET ~> varargList <~ RBRACKET ^^ { pair =>
      (pair._1, Some(pair._2))
    }
    val noVararg = LBRACKET ~> argList <~ RBRACKET ^^ { vec =>
      (vec, None)
    }
    vararg | noVararg
  }

  def argList: Parser[Vector[LazyName]] = {
    rep(identifier | phi) ^^ { params =>
      params.map(id => LazyName(id.name)).toVector
    }
  }

  def varargList: Parser[(Vector[LazyName], LazyName)] = {
    rep(identifier | phi) <~ DOTS ^^ { ids =>
      (ids.init.map(id => LazyName(id.name)).toVector, LazyName(ids.last.name))
    }
  }

  def boundAttrs: Parser[Vector[EOBndExpr[EOExprOnly]]] = {
    val boundAttr = namedAbsObj | namedApplication
    val attrs = INDENT ~> rep1(
      commentsOrNewlines ~> boundAttr <~ commentsOrNewlines
    ) <~ DEDENT ^^
      (attrs => attrs.toVector)
    val noAttrs = commentsOrNewlines ^^ { _ =>
      Vector()
    }
    attrs | noAttrs
  }

  def main(args: Array[String]): Unit = {
    val code =
      """
        |# 123
        |+package sandbox
        |
        |# ooo
        |+rt jvm java8
        |# 000
        |# some meaningful text
        |[] > main
        |  a > namedA
        |  a.b.c > namedC
        |  a > aCopiedWithB
        |    b
        |  a > aCopiedWithBCopiedWithC
        |    b > bCopiedWithC
        |      c > justC
        |  c. > inverseDotExample
        |    b.
        |      a
        |  # some more text
        |  [@ b] > @
        |    [ad...] > one2!
        |  [a @...] > another
        |    # some more text
        |    [a b c d...] > another2
        |  # some unrelated comment
        |  a b c d > aAppliedToBCandD
        |  a (b (c d)) > rightAssociative
        |  ((a b) c) d > leftAssociative
        |  a.x v > axv
        |  a > msg
        |    1
        |    2
        |""".stripMargin

    apply(code)
    ()
  }

}
