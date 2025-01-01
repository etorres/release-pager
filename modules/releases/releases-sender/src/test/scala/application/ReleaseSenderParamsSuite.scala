package es.eriktorr.pager
package application

import cats.implicits.catsSyntaxEitherId
import com.monovore.decline.{Command, Help}
import munit.FunSuite

final class ReleaseSenderParamsSuite extends FunSuite:
  test("should load default parameters"):
    assertEquals(
      Command(name = "name", header = "header")(ReleaseSenderParams.opts).parse(List.empty),
      ReleaseSenderParams(false).asRight[Help],
    )

  test("should load parameters from application arguments"):
    assertEquals(
      Command(name = "name", header = "header")(ReleaseSenderParams.opts).parse(List("-v")),
      ReleaseSenderParams(true).asRight[Help],
    )
