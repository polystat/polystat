package eo.core.ast

import com.github.tarao.nonempty.collection.NonEmpty

import scala.util.matching.Regex

// Program ///////////////////////////////////////////////////////////////////
sealed case class EOProg[+A](
  metas: EOMetas,
  bnds: Vector[EOBnd[A]],
)

// Metas  ////////////////////////////////////////////////////////////////////
sealed case class EOMetas(
  pack: Option[String],
  metas: Vector[EOMeta]
)

// Concrete metas ////////////////////////////////////////////////////////////
sealed trait EOMeta

sealed case class EOAliasMeta(
  alias: String,
  src: String
) extends EOMeta

sealed case class EORTMeta(
  rtName: String,
  src: String
) extends EOMeta

// Binding name //////////////////////////////////////////////////////////////
sealed trait BndName {
  val name: String
}

sealed case class LazyName(
  override val name: String,
) extends BndName

sealed case class ConstName(
  override val name: String,
) extends BndName

// Binding ///////////////////////////////////////////////////////////////////
sealed trait EOBnd[+A] {
  val expr: A
}

sealed case class EOAnonExpr[+A](
  override val expr: A,
) extends EOBnd[A]

sealed case class EOBndExpr[+A](
  bndName: EOBndName,
  override val expr: A,
) extends EOBnd[A]

sealed trait EOBndName {
  val name: BndName
}

sealed case class EOAnyName(
  override val name: BndName
) extends EOBndName

sealed case class EODecoration(
  override val name: BndName = LazyName(Constants.ATTRS.DECORATION)
) extends EOBndName

// Expression ////////////////////////////////////////////////////////////////
sealed trait EOExpr[+A]

// / Object //////////////////////////////////////////////////////////////////
sealed case class EOObj[+A](
  freeAttrs: Vector[LazyName],
  varargAttr: Option[LazyName],
  bndAttrs: Vector[EOBndExpr[A]],
) extends EOExpr[A]

// / Application /////////////////////////////////////////////////////////////
sealed trait EOApp[+A] extends EOExpr[A]

sealed case class EOSimpleApp[+A](name: String) extends EOApp[A]

sealed case class EODot[+A](src: A, name: String) extends EOApp[A]

sealed case class EOCopy[+A](
  trg: A,
  args: NonEmpty[EOBnd[A], Vector[EOBnd[A]]]
) extends EOApp[A]

// / Data ////////////////////////////////////////////////////////////////////
sealed trait EOData[+A] extends EOExpr[A]

sealed case class EOSingleByte(byte: Byte)
sealed case class EOBytesData[+A](bytes: NonEmpty[EOSingleByte, Vector[EOSingleByte]]) extends EOData[A]

sealed case class EOStrData[+A](str: String) extends EOData[A]

sealed case class EORegexData[+A](regex: Regex) extends EOData[A]

sealed case class EOIntData[+A](int: Int) extends EOData[A]

sealed case class EOFloatData[+A](num: Float) extends EOData[A]

sealed case class EOCharData[+A](char: Char) extends EOData[A]

sealed case class EOBoolData[+A](bool: Boolean) extends EOData[A]

sealed case class EOArray[+A](elems: Vector[EOBnd[A]]) extends EOData[A]
