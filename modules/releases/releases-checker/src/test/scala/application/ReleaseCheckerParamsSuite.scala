package es.eriktorr.pager
package application

import cats.implicits.catsSyntaxEitherId
import com.monovore.decline.{Command, Help}
import munit.FunSuite

final class ReleaseCheckerParamsSuite extends FunSuite:
  test("should load default parameters"):
    assertEquals(
      Command(name = "name", header = "header")(ReleaseCheckerParams.opts).parse(List.empty),
      ReleaseCheckerParams(false).asRight[Help],
    )

  test("should load parameters from application arguments"):
    assertEquals(
      Command(name = "name", header = "header")(ReleaseCheckerParams.opts).parse(List("-v")),
      ReleaseCheckerParams(true).asRight[Help],
    )
