package eo.backend.eolang

import eo.backend.eolang.inlineorlines._
import eo.core.ast.Constants
import monocle.Monocle.index
import monocle.macros.{ GenIso, GenPrism }
import monocle.{ Iso, Optional, Setter }

/**
  * Type class for bindable EO representation, i. e. types that can represent EO
  * code that can be bound to a variable.
  * @tparam R Type of the EO code.
  * @tparam NR Type of the name in EO to which the EO code represented as
  *            `R` will be bound.
  */
trait EOBndRepr[R, NR] {
  def bindToName: R => NR => R
}

object EOBndRepr {
  def apply[R, NR](implicit eoRepr: EOBndRepr[R, NR]): EOBndRepr[R, NR] = eoRepr

  object ops {
    def bindToName[R, NR](src: R)(name: NR)(implicit eoRepr: EOBndRepr[R, NR]): R =
      EOBndRepr[R, NR].bindToName(src)(name)

    implicit class EOReprOps[R, NR](val src: R) extends AnyVal {
      def bindToName(name: NR)(implicit eoRepr: EOBndRepr[R, NR]): R =
        EOBndRepr[R, NR].bindToName(src)(name)
    }
  }

  object instances {
    implicit val stringEORepr: EOBndRepr[String, String] =
      new EOBndRepr[String, String] {
        override def bindToName: String => String => String =
          (src: String) => (name: String) => s"${src} ${Constants.SYMBS.BINDING} ${name}"
      }

    implicit val inlineOrLinesEORepr: EOBndRepr[InlineOrLines, String] =
      new EOBndRepr[InlineOrLines, String] {
        def linesStringVectorIso: Iso[Lines, Vector[String]] =
          Iso[Lines, Vector[String]](_.value.toVector)(v => Lines(v.toIterable))
        val firstLineOpt: Optional[Vector[String], String] = index(0)
        def boundExprOpt[S]: String => Setter[S, String] => S => S =
          (name: String) => (s: Setter[S, String]) => s
            .modify(expr => s"${expr} ${Constants.SYMBS.BINDING} ${name}")

        override def bindToName: InlineOrLines => String => InlineOrLines =
          (iol: InlineOrLines) => (name: String) => {
            val inlineOpt =
              GenPrism[InlineOrLines, Inline] andThen
              GenIso[Inline, String]
            val linesOpt =
              GenPrism[InlineOrLines, Lines] andThen
              linesStringVectorIso           andThen
              firstLineOpt

            val bindInline = boundExprOpt(name)(inlineOpt)
            val bindLines = boundExprOpt(name)(linesOpt)

            (bindInline compose bindLines)(iol)
          }
      }
  }
}
