name := "MapSaver"
organization := "fr.epicanard"
scalaVersion := "3.0.0"
version := "0.0.1-SNAPSHOT"
useCoursier := false

resolvers ++= Dependencies.resolvers

libraryDependencies += Dependencies.scalaPluginLoader
libraryDependencies += Dependencies.spigot
libraryDependencies ++= Dependencies.circe

assemblyPackageScala / assembleArtifact := false
assembly / assemblyMergeStrategy := {
  case "plugin.yml" =>
    MergeStrategy.first /* always choose our own plugin.yml if we shade other plugins */
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
