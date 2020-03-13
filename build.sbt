// voir http://www.wartremover.org/
lazy val warts = {
  import Wart._
  Warts.allBut(Recursion, StringPlusAny, Nothing, Any)
}

lazy val globalSettings: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "com.github.chrilves",
        scalaVersion := "2.13.1",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    addCompilerPlugin("io.tryp" % "splain" % "0.5.1" cross CrossVersion.patch),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary),
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.1.1",
      "org.scalatest" %%% "scalatest" % "3.1.1" % Test
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
        "org.scala-js" %%% "scalajs-dom" % "1.0.0"
      ),
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(coreJS)