//
// opetopic.net - build description
//

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val upickleVersion = "0.9.5"
val fastparseVersion = "2.2.2"
val scalatagsVersion = "0.8.2"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
  organization := "net.opetopic",
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-language:implicitConversions",
    "-feature",
    "-deprecation",
    "-unchecked"
  )
)

lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    includeFilter in (Assets, LessKeys.less) := "opetopic.less",
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % "1.1.4",
      "org.webjars" % "Semantic-UI" % "2.4.1",
      "org.webjars" % "jquery" % "2.2.4",
      "org.webjars" %% "webjars-play" % "2.8.0",
      guice
    ),
  )
  .enablePlugins(PlayScala)
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.0.0",
      "be.doeraene" %%% "scalajs-jquery" % "1.0.0",
      "com.lihaoyi" %%% "scalatags" % scalatagsVersion,
    )
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "com.lihaoyi" %% "fastparse" % fastparseVersion,
      "com.lihaoyi" %% "scalatags" % scalatagsVersion
    )
  )

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

