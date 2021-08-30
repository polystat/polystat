package eo.sandbox

import com.github.tarao.nonempty.collection.NonEmpty
import eo.core.ast._
import eo.core.ast.astparams.EOExprOnly
import higherkindness.droste.data.Fix

object programs {
  val mutualRecursionExample: EOProg[EOExprOnly] = EOProg(
    EOMetas(
      pack = Some("sandbox"),
      metas = Vector(
        EOAliasMeta("stdout", "org.eolang.io.stdout"),
        EOAliasMeta("sprintf", "org.eolang.txt.sprintf"),
      )
    ),
    Vector(
      EOBndExpr(
        EOAnyName(LazyName("base")),
        Fix[EOExpr](
          EOObj(
            freeAttrs = Vector(),
            varargAttr = None,
            bndAttrs = Vector(
              EOBndExpr(
                EOAnyName(LazyName("x")),
                Fix[EOExpr](EOSimpleApp("memory"))
              ),
              EOBndExpr(
                EOAnyName(LazyName("f")),
                Fix[EOExpr](
                  EOObj(
                    freeAttrs = Vector(LazyName("self"), LazyName("v")),
                    varargAttr = None,
                    bndAttrs = Vector(
                      EOBndExpr(
                        EODecoration(),
                        Fix[EOExpr](
                          EOCopy(
                            Fix[EOExpr](EODot(Fix[EOExpr](EOSimpleApp("x")), "write")),
                            NonEmpty[Vector[EOBnd[EOExprOnly]]](
                              EOAnonExpr(Fix[EOExpr](EOSimpleApp("v")))
                            )
                          )
                        )
                      )
                    )
                  )
                )
              ),
              EOBndExpr(
                EOAnyName(LazyName("g")),
                Fix[EOExpr](
                  EOObj(
                    freeAttrs = Vector(LazyName("self"), LazyName("v")),
                    varargAttr = None,
                    bndAttrs = Vector(
                      EOBndExpr(
                        EODecoration(),
                        Fix[EOExpr](
                          EOCopy(
                            Fix[EOExpr](EODot(Fix[EOExpr](EOSimpleApp("self")), "f")),
                            NonEmpty[Vector[EOBnd[EOExprOnly]]](
                              EOAnonExpr(Fix[EOExpr](EOSimpleApp("self"))),
                              EOAnonExpr(Fix[EOExpr](EOSimpleApp("v")))
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
      ),


      EOBndExpr(
        EOAnyName(LazyName("derived")),
        Fix[EOExpr](
          EOObj(
            freeAttrs = Vector(),
            varargAttr = None,
            bndAttrs = Vector(
              EOBndExpr(EODecoration(), Fix[EOExpr](EOSimpleApp("base"))),
              EOBndExpr(
                EOAnyName(LazyName("f")),
                Fix[EOExpr](
                  EOObj(
                    freeAttrs = Vector(LazyName("self"), LazyName("v")),
                    varargAttr = None,
                    bndAttrs = Vector(
                      EOBndExpr(
                        EODecoration(),
                        Fix[EOExpr](
                          EOCopy(
                            Fix[EOExpr](EODot(Fix[EOExpr](EOSimpleApp("self")), "g")),
                            NonEmpty[Vector[EOBnd[EOExprOnly]]](
                              EOAnonExpr(Fix[EOExpr](EOSimpleApp("self"))),
                              EOAnonExpr(Fix[EOExpr](EOSimpleApp("v")))
                            )
                          )
                        )
                      )
                    ),
                  )
                )
              )
            )
          )
        )
      ),
    )
  )
}
