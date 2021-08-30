import sbt._
import sbt.Keys._

object Dependencies {
  object V {
    val cats = "2.3.0"
    val catsMtl = "1.2.0"
    val catsEffect = "2.5.1"
    val scalaTest = "3.2.9"
    val scalaCheck = "3.2.9.0"
    val nonEmpty = "0.2.0"
    val monocle = "3.0.0-RC2"
    val parserCombinators = "2.0.0"
    val droste = "0.8.0"
  }

  val cats = Seq(
    "org.typelevel" %% "cats-core" % V.cats,
    "org.typelevel" %% "cats-mtl" % V.catsMtl
  )

  val catsEffect = Seq(
    "org.typelevel" %% "cats-effect" % V.catsEffect
  )

  val scalaTest = Seq(
    "org.scalactic" %% "scalactic" % V.scalaTest % Test,
    "org.scalatest" %% "scalatest" % V.scalaTest % Test,
    "org.scalatestplus" %% "scalacheck-1-15" % V.scalaCheck % Test
  )

  val nonEmpty = Seq(
    "com.github.tarao" %% "nonempty" % V.nonEmpty
  )

  val monocle = Seq(
    "dev.optics" %% "monocle-core"  % V.monocle,
    "dev.optics" %% "monocle-macro" % V.monocle
  )

  val parserCombinators = Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % V.parserCombinators
  )

  val droste = Seq(
    "io.higherkindness" %% "droste-scalacheck" % V.droste
  )

  val allCats: Seq[ModuleID] = cats ++ catsEffect

  val common: Seq[ModuleID] = allCats ++ scalaTest ++ nonEmpty ++ monocle

  val core: Seq[ModuleID] = common ++ droste
  val parser: Seq[ModuleID] = core ++ parserCombinators
  val cli: Seq[ModuleID] = common
}
