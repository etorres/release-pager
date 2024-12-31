ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr.pager")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-source:future", // https://github.com/oleg-py/better-monadic-for
  "-Yexplicit-nulls", // https://docs.scala-lang.org/scala3/reference/other-new-features/explicit-nulls.html
  "-Ysafe-init", // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
  "-Wnonunit-statement",
  "-Wunused:all",
)

ThisBuild / semanticdbEnabled := true
ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

addCommandAlias(
  "check",
  "; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
)

lazy val MUnitFramework = new TestFramework("munit.Framework")
lazy val warts = Warts.unsafe.filter(_ != Wart.DefaultArguments)

lazy val baseSettings: Project => Project = _.settings(
  Compile / doc / sources := Seq(),
  Compile / compile / wartremoverErrors ++= warts,
  Test / compile / wartremoverErrors ++= warts,
  libraryDependencies ++= Seq(
    "io.chrisdavenport" %% "cats-scalacheck" % "0.3.2" % Test,
    "org.apache.logging.log4j" % "log4j-core" % "2.24.3" % Test,
    "org.apache.logging.log4j" % "log4j-layout-template-json" % "2.24.3" % Test,
    "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3" % Test,
    "org.scalameta" %% "munit" % "1.0.3" % Test,
    "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test,
    "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
    "org.typelevel" %% "scalacheck-effect" % "1.0.4" % Test,
    "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test,
  ),
  Test / envVars := Map(
    "SBT_TEST_ENV_VARS" -> "true",
  ),
  Test / testFrameworks += MUnitFramework,
  Test / testOptions += Tests.Argument(MUnitFramework, "--exclude-tags=online"),
)

lazy val `commons-db` = project
  .in(file("modules/commons/commons-db"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.zaxxer" % "HikariCP" % "6.2.1" exclude ("org.slf4j", "slf4j-api"),
      "io.github.iltotore" %% "iron" % "2.6.0",
      "org.flywaydb" % "flyway-core" % "11.1.0",
      "org.flywaydb" % "flyway-database-postgresql" % "11.1.0" % Runtime,
      "org.postgresql" % "postgresql" % "42.7.4" % Runtime,
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-free" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",
      "org.typelevel" %% "cats-collections-core" % "0.9.9",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
    ),
  )
  .dependsOn(`commons-lang` % "test->test;compile->compile")

lazy val `commons-lang` = project
  .in(file("modules/commons/commons-lang"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" % Test,
      "com.monovore" %% "decline" % "2.4.1",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.hypersistence" % "hypersistence-tsid" % "2.1.3",
      "org.typelevel" %% "cats-collections-core" % "0.9.9",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
    ),
  )

lazy val `commons-streams` = project
  .in(file("modules/commons/commons-streams"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "com.comcast" %% "ip4s-core" % "3.6.0",
      "com.github.fd4s" %% "fs2-kafka" % "3.6.0",
      "com.lmax" % "disruptor" % "3.4.4" % Runtime,
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "log4cats-core" % "2.7.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
    ),
  )
  .dependsOn(`commons-lang` % "test->test;compile->compile")

lazy val `notifications-dsl` = project
  .in(file("modules/notifications/notifications-dsl"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.github.iltotore" %% "iron-circe" % "2.6.0",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
    ),
  )
  .dependsOn(`commons-lang` % "test->test;compile->compile")

lazy val `notifications-impl` = project
  .in(file("modules/notifications/notifications-impl"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.github.iltotore" %% "iron-cats" % "2.6.0",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
    ),
  )
  .dependsOn(
    `commons-streams` % "test->test;compile->compile",
    `notifications-dsl` % "test->test;compile->compile",
    `subscriptions-dsl` % "test->test;compile->compile",
  )

lazy val `releases-checker` = project
  .in(file("modules/releases/releases-checker"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "com.github.eikek" %% "calev-core" % "0.7.2",
      "com.github.eikek" %% "calev-fs2" % "0.7.2",
      "com.lmax" % "disruptor" % "3.4.4" % Runtime,
      "com.monovore" %% "decline" % "2.4.1",
      "com.monovore" %% "decline-effect" % "2.4.1",
      "com.zaxxer" % "HikariCP" % "6.2.1",
      "io.circe" %% "circe-core" % "0.14.10",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.github.iltotore" %% "iron-decline" % "2.6.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.24.3" % Runtime,
      "org.apache.logging.log4j" % "log4j-layout-template-json" % "2.24.3" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3" % Runtime,
      "org.http4s" %% "http4s-client" % "0.23.30",
      "org.typelevel" %% "cats-collections-core" % "0.9.9",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",
      "org.typelevel" %% "log4cats-core" % "2.7.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
    ),
    Universal / maintainer := "https://github.com/etorres/release-pager",
  )
  .dependsOn(
    `notifications-impl` % "test->test;compile->compile",
    `subscriptions-impl` % "test->test;compile->compile",
  )
  .enablePlugins(JavaAppPackaging)

lazy val `releases-sender` = project
  .in(file("modules/releases/releases-sender"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(),
    Universal / maintainer := "https://github.com/etorres/release-pager",
  )
  .dependsOn(`commons-lang` % "test->test;compile->compile")
  .enablePlugins(JavaAppPackaging)

lazy val `subscriptions-dsl` = project
  .in(file("modules/subscriptions/subscriptions-dsl"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.hypersistence" % "hypersistence-tsid" % "2.1.3",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
    ),
  )
  .dependsOn(`commons-lang` % "test->test;compile->compile")

lazy val `subscriptions-impl` = project
  .in(file("modules/subscriptions/subscriptions-impl"))
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "com.lmax" % "disruptor" % "3.4.4" % Runtime,
      "com.zaxxer" % "HikariCP" % "6.2.1" exclude ("org.slf4j", "slf4j-api"),
      "io.circe" %% "circe-core" % "0.14.10",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.github.iltotore" %% "iron-cats" % "2.6.0",
      "org.http4s" %% "http4s-circe" % "0.23.30",
      "org.http4s" %% "http4s-client" % "0.23.30",
      "org.http4s" %% "http4s-core" % "0.23.30",
      "org.http4s" %% "http4s-ember-client" % "0.23.30",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-free" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC5" % Test,
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
      "org.typelevel" %% "cats-free" % "2.12.0",
      "org.typelevel" %% "case-insensitive" % "1.4.2",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
      "org.typelevel" %% "log4cats-core" % "2.7.0",
      "org.typelevel" %% "vault" % "3.6.0",
    ),
  )
  .dependsOn(
    `commons-db` % "test->test;compile->compile",
    `subscriptions-dsl` % "test->test;compile->compile",
  )

lazy val root = project
  .in(file("."))
  .aggregate(
    `commons-db`,
    `commons-lang`,
    `notifications-dsl`,
    `notifications-impl`,
    `releases-checker`,
    `releases-sender`,
    `subscriptions-dsl`,
    `subscriptions-impl`,
  )
  .settings(
    name := "release-pager",
    Compile / doc / sources := Seq(),
    publish := {},
    publishLocal := {},
  )
