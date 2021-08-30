package eo.backend.eolang

import eo.backend.eolang.inlineorlines._

abstract class ToEOBnd[T, R, NR](implicit toEO: ToEO[T, R], eoRepr: EOBndRepr[R, NR]) {
  def bndToEO: T => NR => R = eoRepr.bindToName compose toEO.toEO
}

object ToEOBnd {
  def apply[T, R, NR](implicit toEOBnd: ToEOBnd[T, R, NR]): ToEOBnd[T, R, NR] = toEOBnd

  object ops {
    def bndToEO[T, R, NR](src: T)(name: NR)(implicit toEOBnd: ToEOBnd[T, R, NR]): R =
      ToEOBnd[T, R, NR].bndToEO(src)(name)

    implicit class ToEOBndOps[T, R, NR](val src: T) extends AnyVal {
      def bndToEO(name: NR)(implicit toEOBnd: ToEOBnd[T, R, NR]): R =
        ToEOBnd[T, R, NR].bndToEO(src)(name)
    }
  }

  object instances {
    implicit def stringBndString[T](
      implicit toEO: ToEO[T, String],
      eoRepr: EOBndRepr[String, String]
    ): ToEOBnd[T, String, String] =
      new ToEOBnd[T, String, String] { }

    implicit def inlineOrLinesBndString[T](
      implicit toEO: ToEO[T, InlineOrLines],
      eoRepr: EOBndRepr[InlineOrLines, String]
    ): ToEOBnd[T, InlineOrLines, String] =
      new ToEOBnd[T, InlineOrLines, String] { }
  }
}
