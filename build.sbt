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
        scalaVersion := "3.2.2",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    Compile/compile/wartremoverErrors := warts,
    Compile/console/wartremoverErrors := warts,
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.9.0",
      "org.scalatest" %%% "scalatest" % "3.2.15" % Test
    )
    //, scalaJSUseMainModuleInitializer := true
  )

lazy val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("core"))
    .settings(globalSettings : _*)
    .settings(name := "core")
    .jsSettings(scalacOptions += "-scalajs")

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val prime =
  project
    .in(file("prime"))
    .settings(globalSettings : _*)
    .settings(
      name := "prime",
      maintainer := "christophe.calves@gmail.com"
    )
    .enablePlugins(JavaAppPackaging)
    .dependsOn(coreJVM)

lazy val web =
  project
    .in(file("web"))
    .enablePlugins(ScalaJSPlugin)
    .settings(globalSettings : _*)
    .settings(
      name := "prime-web",
      scalacOptions += "-scalajs",
      libraryDependencies ++= Seq(
        "chrilves" %%% "typed-web" % "0.1.0-SNAPSHOT",
        "org.scala-js" %%% "scalajs-dom" % "2.4.0"
      ),
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(coreJS)