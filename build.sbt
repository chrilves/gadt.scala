// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

// voir http://www.wartremover.org/
lazy val warts =
  Warts.allBut(Wart.Recursion)

val safeScalaOptions =
  Seq(
    "-deprecation",
    "-encoding",
    "UTF8",
    "-explaintypes",
    "-feature",
    "-language:-dynamics",
    "-language:postfixOps",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:experimental.macros",
    "-unchecked",
    "-Xlint:_",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-macros:both",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-self-implicit",
    "-Ywarn-unused:_",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )

lazy val globalSettings: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "com.example",
        scalaVersion := "2.12.6",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    scalacOptions ++= safeScalaOptions,
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    addCompilerPlugin("io.tryp" % "splain" % "0.3.1" cross CrossVersion.patch),
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      //"org.typelevel" %%% "cats-core" % "1.2.0",
      "org.scalatest" %%% "scalatest" % "3.0.5" % Test
    )
    //, scalaJSUseMainModuleInitializer := true
  )

lazy val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("core"))
    .settings(globalSettings : _*)
    .settings(name := "core")

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val prime =
  project
    .in(file("prime"))
    .settings(globalSettings : _*)
    .settings(name := "prime")
    .dependsOn(coreJVM)