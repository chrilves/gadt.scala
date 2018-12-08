// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

// voir http://www.wartremover.org/
lazy val warts =
  Warts.allBut(Wart.Recursion)

lazy val globalSettings: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "com.github.chrilves",
        scalaVersion := "2.12.7",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    addCompilerPlugin("io.tryp" % "splain" % "0.3.4" cross CrossVersion.patch),
    addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.8" cross CrossVersion.binary),
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "1.5.0",
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

lazy val web =
  project
    .in(file("web"))
    .enablePlugins(ScalaJSPlugin)
    .settings(globalSettings : _*)
    .settings(
      name := "prime-web",
      libraryDependencies ++= Seq(
        "chrilves" %%% "typed-web" % "0.1.0-SNAPSHOT",
        "org.scala-js" %%% "scalajs-dom" % "0.9.5"
      ),
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(coreJS)