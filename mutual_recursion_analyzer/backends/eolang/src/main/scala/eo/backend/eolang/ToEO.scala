package eo.backend.eolang

import cats.implicits.toBifunctorOps
import higherkindness.droste.data.Fix
import eo.backend.eolang.EOBndRepr.instances._
import eo.backend.eolang.ToEO.ops.ToEOOps
import eo.backend.eolang.ToEOBnd.instances._
import eo.backend.eolang.ToEOBnd.ops._
import eo.backend.eolang.inlineorlines._
import eo.backend.eolang.inlineorlines.ops._
import eo.core.ast._
import eo.core.ast.astparams._
import eo.utils.text._

trait ToEO[T, R] {
  def toEO(node: T): R
}

object ToEO {
  def apply[T, R](implicit toEO: ToEO[T, R]): ToEO[T, R] = toEO

  object ops {
    def toEO[T, R](node: T)(implicit toEO: ToEO[T, R]): R = ToEO[T, R].toEO(node)

    implicit class ToEOOps[T, R](val node: T) extends AnyVal {
      def toEO(implicit toEO: ToEO[T, R]): R = ToEO[T, R] toEO (node)
    }
  }

  object instances {
    implicit val eoExprOnlyToEO: ToEO[EOExprOnly, InlineOrLines] =
      new ToEO[EOExprOnly, InlineOrLines] {
        override def toEO(node: EOExprOnly): InlineOrLines =
          Fix.un(node).toEO
      }

    // Program ///////////////////////////////////////////////////////////////////
    implicit val progToEO: ToEO[EOProg[EOExprOnly], Lines] =
      new ToEO[EOProg[EOExprOnly], Lines] {
        override def toEO(node: EOProg[EOExprOnly]): Lines = {
          val metas = node.metas.toEO.toIterable
          val program = node.bnds.flatMap(_.toEO.toIterable)

          Lines(metas ++ program)
        }
      }

    // Metas  //////////////////////////////////////////////////////////////////
    implicit val metasToEO: ToEO[EOMetas, Lines] =
      new ToEO[EOMetas, Lines] {
        override def toEO(node: EOMetas): Lines = Lines(
          node.pack.map(p => s"${Constants.SYMBS.META_PREFIX}package ${p}") ++
          node.metas.map(_.toEO.value)
        )
      }

    // Concrete metas //////////////////////////////////////////////////////////
    implicit val metaToEO: ToEO[EOMeta, Inline] =
      new ToEO[EOMeta, Inline] {
        override def toEO(node: EOMeta): Inline = node match {
          case a: EOAliasMeta => a.toEO
          case rt: EORTMeta   => rt.toEO
        }
      }

    implicit val aliasMetaToEO: ToEO[EOAliasMeta, Inline] =
      new ToEO[EOAliasMeta, Inline] {
        override def toEO(node: EOAliasMeta): Inline =
          Inline(s"${Constants.SYMBS.META_PREFIX}alias ${node.alias} ${node.src}")
      }

    implicit val rtMetaToEO: ToEO[EORTMeta, Inline] =
      new ToEO[EORTMeta, Inline] {
        override def toEO(node: EORTMeta): Inline =
          Inline(s"${Constants.SYMBS.META_PREFIX}rt ${node.rtName} ${node.src}")
      }


    // Binding name ////////////////////////////////////////////////////////////
    implicit val bndNameToEO: ToEO[BndName, String] =
      new ToEO[BndName, String] {
        override def toEO(node: BndName): String = node match {
          case l: LazyName  => l.toEO
          case c: ConstName => c.toEO
        }
      }

    implicit val lazyBndToEO: ToEO[LazyName, String] =
      new ToEO[LazyName, String] {
        override def toEO(node: LazyName): String = node.name
      }

    implicit val constBndToEO: ToEO[ConstName, String] =
      new ToEO[ConstName, String] {
        override def toEO(node: ConstName): String = s"${node.name}${Constants.SYMBS.CONST_MOD}"
      }

    // Binding /////////////////////////////////////////////////////////////////
    implicit val bndToEO: ToEO[EOBnd[EOExprOnly], InlineOrLines] =
      new ToEO[EOBnd[EOExprOnly], InlineOrLines] {
        override def toEO(node: EOBnd[EOExprOnly]): InlineOrLines = node match {
          case a: EOAnonExpr[EOExprOnly] => a.toEO
          case b: EOBndExpr[EOExprOnly]  => b.toEO
        }
      }

    implicit val anonExprToEO: ToEO[EOAnonExpr[EOExprOnly], InlineOrLines] =
      new ToEO[EOAnonExpr[EOExprOnly], InlineOrLines] {
        override def toEO(node: EOAnonExpr[EOExprOnly]): InlineOrLines = node.expr.toEO
      }

    implicit val bndExprToEO: ToEO[EOBndExpr[EOExprOnly], InlineOrLines] =
      new ToEO[EOBndExpr[EOExprOnly], InlineOrLines] {
        override def toEO(node: EOBndExpr[EOExprOnly]): InlineOrLines =
          node.expr.bndToEO(node.bndName.name.toEO)
      }

      // Expression ////////////////////////////////////////////////////////////
      implicit val exprToEO: ToEO[EOExpr[EOExprOnly], InlineOrLines] =
        new ToEO[EOExpr[EOExprOnly], InlineOrLines] {
          override def toEO(node: EOExpr[EOExprOnly]): InlineOrLines = node match {
            case o: EOObj[EOExprOnly]  => o.toEO
            case a: EOApp[EOExprOnly]  => a.toEO
            case d: EOData[EOExprOnly] => d.toEO
          }
        }

      // / Object //////////////////////////////////////////////////////////////
      implicit val objToEO: ToEO[EOObj[EOExprOnly], InlineOrLines] =
        new ToEO[EOObj[EOExprOnly], InlineOrLines] {
          override def toEO(node: EOObj[EOExprOnly]): InlineOrLines = {
            val freeAttrsWithVararg =
              node.freeAttrs.map(_.toEO) ++
              node.varargAttr.map(va => s"${va.toEO}${Constants.SYMBS.VARARG_MOD}")

            val freeAttrsEO = Vector(
              Constants.SYMBS.FREE_ATTR_DECL_ST ++
              freeAttrsWithVararg.mkString(" ") ++
              Constants.SYMBS.FREE_ATTR_DECL_ED
            )

            val objBody = node.bndAttrs.flatMap(_.toEO.toIterable).map(indent)

            Lines(freeAttrsEO ++ objBody)
          }
        }

      // / Application /////////////////////////////////////////////////////////
      implicit val appToEO: ToEO[EOApp[EOExprOnly], InlineOrLines] =
        new ToEO[EOApp[EOExprOnly], InlineOrLines] {
          override def toEO(node: EOApp[EOExprOnly]): InlineOrLines = node match {
            case n: EOSimpleApp[EOExprOnly] => n.toEO : Inline
            case n: EODot[EOExprOnly]       => n.toEO
            case n: EOCopy[EOExprOnly]      => n.toEO : Lines
          }
        }

      implicit val simpleAppToEO: ToEO[EOSimpleApp[EOExprOnly], Inline] =
        new ToEO[EOSimpleApp[EOExprOnly], Inline] {
          override def toEO(node: EOSimpleApp[EOExprOnly]): Inline = Inline(node.name)
        }

      implicit val dotToEO: ToEO[EODot[EOExprOnly], InlineOrLines] =
        new ToEO[EODot[EOExprOnly], InlineOrLines] {
          def usualDotNotation(n: String)(s: String): String = s"${s}.${n}"

          def reverseDotNotation(n: String)(ls: Iterable[String]): Iterable[String] =
            Vector(s"${n}.") ++ ls.map(indent)

          def dotNotation(n: String)(eoExpr: InlineOrLines): InlineOrLines =
            eoExpr.bimap(
              usualDotNotation(n),
              reverseDotNotation(n)
            )

          def objCases(name: String)(obj: EOObj[EOExprOnly]): InlineOrLines = dotNotation(name)(obj.toEO)

          def appCases(name: String)(app: EOApp[EOExprOnly]): InlineOrLines = app match {
            case n: EOSimpleApp[EOExprOnly] => dotNotation(name)(n.toEO: Inline)
            case n: EODot[EOExprOnly]       => dotNotation(name)(n.src.toEO)
            case n: EOCopy[EOExprOnly]      => dotNotation(name)(n.toEO: Lines)
          }

          def dataCases(name: String)(data: EOData[EOExprOnly]): InlineOrLines = dotNotation(name)(data.toEO)

          override def toEO(node: EODot[EOExprOnly]): InlineOrLines = {
            Fix.un(node.src) match {
              case n: EOObj[EOExprOnly]  => objCases(node.name)(n)
              case n: EOApp[EOExprOnly]  => appCases(node.name)(n)
              case n: EOData[EOExprOnly] => dataCases(node.name)(n)
            }
          }
        }

      implicit val copyToEO: ToEO[EOCopy[EOExprOnly], Lines] =
        new ToEO[EOCopy[EOExprOnly], Lines] {
          override def toEO(node: EOCopy[EOExprOnly]): Lines = Lines(
            node.trg.toEO.toIterable ++
              node.args.flatMap(_.toEO.toIterable).map(indent)
          )
        }

      // / Data ////////////////////////////////////////////////////////////////
      implicit val dataToEO: ToEO[EOData[EOExprOnly], InlineOrLines] =
        new ToEO[EOData[EOExprOnly], InlineOrLines] {
          override def toEO(node: EOData[EOExprOnly]): InlineOrLines = node match {
            case n: EOBytesData[EOExprOnly] => n.toEO : Inline
            case n: EOStrData[EOExprOnly]   => n.toEO : Inline
            case n: EORegexData[EOExprOnly] => n.toEO : Inline
            case n: EOIntData[EOExprOnly]   => n.toEO : Inline
            case n: EOFloatData[EOExprOnly] => n.toEO : Inline
            case n: EOCharData[EOExprOnly]  => n.toEO : Inline
            case n: EOBoolData[EOExprOnly]  => n.toEO : Inline
            case n: EOArray[EOExprOnly]     => n.toEO
          }
        }

      implicit val singleByteToEO: ToEO[EOSingleByte, Inline] =
        new ToEO[EOSingleByte, Inline] {
          override def toEO(node: EOSingleByte): Inline = Inline(node.byte.formatted("%X"))
        }

      implicit val bytesDataToEO: ToEO[EOBytesData[EOExprOnly], Inline] =
        new ToEO[EOBytesData[EOExprOnly], Inline] {
          override def toEO(node: EOBytesData[EOExprOnly]): Inline =
            Inline(node.bytes.map(_.toEO).mkString("-"))
        }

      implicit val srtDataToEO: ToEO[EOStrData[EOExprOnly], Inline] =
        new ToEO[EOStrData[EOExprOnly], Inline] {
          override def toEO(node: EOStrData[EOExprOnly]): Inline = Inline(s"\"${node.str}\"")
        }

      implicit val regexDataToEO: ToEO[EORegexData[EOExprOnly], Inline] =
        new ToEO[EORegexData[EOExprOnly], Inline] {
          // TODO: support suffixes
          override def toEO(node: EORegexData[EOExprOnly]): Inline = Inline(s"/${node.regex.regex}/")
        }

      implicit val intDataToEO: ToEO[EOIntData[EOExprOnly], Inline] =
        new ToEO[EOIntData[EOExprOnly], Inline] {
          override def toEO(node: EOIntData[EOExprOnly]): Inline = Inline(node.int.toString)
        }

      implicit val floatDataToEO: ToEO[EOFloatData[EOExprOnly], Inline] =
        new ToEO[EOFloatData[EOExprOnly], Inline] {
          override def toEO(node: EOFloatData[EOExprOnly]): Inline = Inline(node.num.toString)
        }

      implicit val charDataToEO: ToEO[EOCharData[EOExprOnly], Inline] =
        new ToEO[EOCharData[EOExprOnly], Inline] {
          override def toEO(node: EOCharData[EOExprOnly]): Inline = Inline(s"'${node.char}'")
        }

      implicit val boolDataToEO: ToEO[EOBoolData[EOExprOnly], Inline] =
        new ToEO[EOBoolData[EOExprOnly], Inline] {
          override def toEO(node: EOBoolData[EOExprOnly]): Inline = Inline(node.bool.toString)
        }

      implicit val arrayDataToEO: ToEO[EOArray[EOExprOnly], InlineOrLines] =
        new ToEO[EOArray[EOExprOnly], InlineOrLines] {
          override def toEO(node: EOArray[EOExprOnly]): InlineOrLines =
            if (node.elems.isEmpty)
              Inline(Constants.SYMBS.ARRAY_START)
            else
              Lines(
                Vector(Constants.SYMBS.ARRAY_START) ++
                  node.elems.flatMap(_.toEO.toIterable).map(indent)
              )
        }
    }
}
