import Dependencies._
import sbt._

lazy val root = (project in file(".")).settings(
  name := "worldquery",
  version      in ThisBuild := "0.2.0",
  organization in ThisBuild := "com.packpng",
  scalaVersion in ThisBuild := "2.13.3",

  resolvers += "Paper MC" at "https://papermc.io/repo/repository/maven-public/",
  libraryDependencies ++= thirdPartyDeps
)