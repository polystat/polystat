package eo.sandbox

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import eo.analysis.mutualrec.naive.mutualrec.programs._
import eo.backend.eolang.ToEO.instances._
import eo.backend.eolang.ToEO.ops._
import eo.backend.eolang.inlineorlines.ops._
import eo.sandbox.programs.mutualRecursionExample

import scala.util.chaining._

object Sandbox extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    exitCode <- IO.pure(ExitCode.Success)
    mutualRecEORepr: String = mutualRecursionExample.toEO.allLinesToString
    _ <- IO(mutualRecEORepr.tap(println))

    topLevelObjects <- resolveMethodsReferencesForEOProgram[IO](mutualRecursionExample)

    mutualRec <- findMutualRecursionInTopLevelObjects(topLevelObjects)
    mutualRecFiltered = mutualRec.filter(_.nonEmpty)

    _ <- IO.delay(println())
    _ <- IO.delay(
      for {
        mutualRecDep <- mutualRecFiltered
        (method, depChains) <- mutualRecDep.toVector
        depChain <- depChains.toVector
      } yield for {
        mutualRecMeth <- depChain.lastOption
      } yield {
        val mutualRecString =
          s"Method `${method.parentObject.objName}.${method.name}` " ++
          s"is mutually recursive with method " ++
          s"`${mutualRecMeth.parentObject.objName}.${mutualRecMeth.name}`"

         val dependencyChainString = depChain.append(method).map(m => s"${m.parentObject.objName}.${m.name}").mkString_(" -> ")

        println(mutualRecString ++ " through the following possible code path:\n" ++ dependencyChainString)
      }
    )
  } yield exitCode
}
