name := "Apart"

version := "1.0"

scalaVersion := "2.11.6"

compile <<= (compile in Compile) dependsOn compileSkelcl

lazy val compileSkelcl = taskKey[Unit]("Updates and builds the SkelCL submodule.")

compileSkelcl := {
  import language.postfixOps
  import scala.sys.process._
  "echo y" #| "./skelcl.sh" !
}

scalacOptions ++= Seq("-Xmax-classfile-name", "100")

javaOptions += "-Djava.library.path=" + baseDirectory(_ / "lib/SkelCL/build/executor").value

scalaSource in Compile <<= baseDirectory(_ / "src/main")

scalaSource in Test <<= baseDirectory(_ / "src/test")

javaSource in Compile <<= baseDirectory(_ / "src/main")

javaSource in Test <<= baseDirectory(_ / "src/test")

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.6" 

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.6"

libraryDependencies += "junit" % "junit" % "4.11"

libraryDependencies += "commons-cli" % "commons-cli" % "1.3.1"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

libraryDependencies += "org.clapper" %% "argot" % "1.0.3"

scalacOptions in (Compile,doc) := Seq("-implicits", "-diagrams")

unmanagedSourceDirectories in Compile += baseDirectory.value / "lib/ArithExpr/src/main/"

unmanagedSourceDirectories in Test += baseDirectory.value / "lib/ArithExpr/src/main/"

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;benchmarks.*;.*Test.*;junit.*;.*interop.*;.*arithmetic.*;.*testing.*"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-a")

fork := true
