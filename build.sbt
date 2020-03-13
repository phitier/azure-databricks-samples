name := "azure-sample"
version := "1.0"
scalaVersion := "2.11.8"

//Useful Scala Compiler Options
scalacOptions ++= Seq(
  //"-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

val sparkVersion = "2.4.4"
val hadoopVersion = "3.2.0"

// true pour faire le JAR, false pour run dans IntelliJ
val isALibrary = true


dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7.1"
dependencyOverrides += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.7.1"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-paranamer" % "2.6.7"


val excludeJacksonDataformatBinding = ExclusionRule(organization = "com.fasterxml.jackson.dataformat")
val excludeJacksonDatatypeBinding = ExclusionRule(organization = "com.fasterxml.jackson.datatype")

val sparkExcludes =
  (moduleId: ModuleID) => moduleId
    .exclude("org.apache.hadoop", "hadoop-client")
    .exclude("org.xerial.snappy", "snappy-java")
    .exclude("com.google.guava", "guava")
    .exclude("stax", "stax-api")
    .exclude("org.slf4j", "slf4j-log4j12")
    .exclude("org.eclipse.jetty.orbit", "javax.transaction")
    .exclude("org.eclipse.jetty.orbit", "javax.mail")
    .exclude("org.eclipse.jetty.orbit", "javax.activation")
    .exclude("com.twitter", "parquet-hadoop-bundle")
    .excludeAll(ExclusionRule(organization = "org.mortbay.jetty"))

val hadoopExcludes =
  (moduleId: ModuleID) => moduleId
    .exclude("org.slf4j", "slf4j-api")
    .exclude("javax.servlet", "servlet-api")
    .exclude("org.xerial.snappy", "snappy-java")
    .exclude("com.google.guava", "guava")
    .exclude("com.twitter", "parquet-hadoop-bundle")

val hadoopAzureDatalakeExcludes =
  (moduleId: ModuleID) => moduleId
    .exclude("org.apache.hadoop", "hadoop-common")
    .exclude("org.slf4j", "slf4j-api")
    .exclude("com.fasterxml.jackson.core", "jackson-core")

val azureSqlDbExclude =
  (moduleId: ModuleID) => moduleId
    .exclude("com.google.guava", "guava")
    .exclude("org.apache.spark", "spark-core_2.11")
    .exclude("org.apache.spark", "spark-sql_2.11")
    .exclude("org.slf4j", "slf4j-api")
    .excludeAll(excludeJacksonDataformatBinding, excludeJacksonDatatypeBinding)

/*
  if it's a library the scope is "compile" since we want the transitive dependencies on the library
  otherwise we set up the scope to "provided" because those dependencies will be assembled in the "assembly"
*/
lazy val assemblyDependenciesScope = if (isALibrary) "compile" else "compile"
lazy val sparkDependenciesScope = if (isALibrary) "provided" else "compile"
lazy val hadoopDependenciesScope = if (isALibrary) "provided" else "compile"

libraryDependencies ++= Seq(
  // spark dependencies
  sparkExcludes("org.apache.spark" %% "spark-core" % sparkVersion % sparkDependenciesScope),
  sparkExcludes("org.apache.spark" %% "spark-sql" % sparkVersion % sparkDependenciesScope),

  // hadoop dependencies
  hadoopExcludes("org.apache.hadoop" % "hadoop-common" % hadoopVersion % hadoopDependenciesScope),
  hadoopExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % hadoopDependenciesScope),
  hadoopExcludes("org.apache.hadoop" % "hadoop-azure" % hadoopVersion % hadoopDependenciesScope),
  hadoopExcludes("org.apache.hadoop" % "hadoop-azure-datalake" % hadoopVersion % hadoopDependenciesScope),

  // azure sql server connector
  azureSqlDbExclude("com.microsoft.azure" % "azure-sqldb-spark" % "1.0.2"),

  // Databricks Db-utils to Databricks clusters utilities
  "com.databricks" %% "dbutils-api" % "0.0.4" % "provided",

  // azure Key vauld client
  ("com.azure" % "azure-security-keyvault-secrets" % "4.1.0").
    excludeAll(excludeJacksonDataformatBinding, excludeJacksonDatatypeBinding),
  ("com.azure" % "azure-identity" % "1.0.3").
    excludeAll(excludeJacksonDataformatBinding, excludeJacksonDatatypeBinding),

  "com.google.guava" % "guava" % "14.0.1" % assemblyDependenciesScope,

  // misc
  "org.jmockit" % "jmockit" % "1.34" % "test"
)

// set an explicit main class
mainClass in assembly := Some("sample.ReadFromAzureDriver")

// merge strategies
assemblyMergeStrategy in assembly := {
  case PathList("org", "slf4j", xs@_*) => MergeStrategy.first
  case PathList("org", "joda", xs@_*) => MergeStrategy.first
  case PathList("org", "datanucleus", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
  case PathList("com", "codahale", xs@_*) => MergeStrategy.last
  case PathList("com", "yammer", xs@_*) => MergeStrategy.last
  case PathList("org", "datanucleus", xs@_*) => MergeStrategy.last
  case PathList("org", "objenesis", xs@_*) => MergeStrategy.last
  case PathList("javax", "xml", "stream", xs@_*) => MergeStrategy.first
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
  case PathList("javax", "activation", xs@_*) => MergeStrategy.last
  case PathList("javax", "mail", xs@_*) => MergeStrategy.last
  case PathList("javax", "transaction", xs@_*) => MergeStrategy.last
  case PathList("javax", "inject", xs@_*) => MergeStrategy.last
  case PathList("com", "fasterxml", xs@_*) => MergeStrategy.last
  case PathList("javax", "jdo", xs@_*) => MergeStrategy.last
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "unwanted.txt" => MergeStrategy.discard
  case "plugin.xml" => MergeStrategy.discard
  case "parquet.thrift" => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "mime.types" => MergeStrategy.last
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.last
  case PathList("META-INF", "mailcap") => MergeStrategy.last
  case PathList("META-INF", "mailcap.default") => MergeStrategy.last
  case PathList("META-INF", "mimetypes.default") => MergeStrategy.last
  case PathList("META-INF", "javamail.default.providers") => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case "hive-log4j.properties" => MergeStrategy.last
  case PathList(ps@_*) if ps.last endsWith "-site.xml" => MergeStrategy.discard
  case "krb5.conf" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// exclude Scala library JARs
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)
// speed assembly up
// assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheUnzip = false)
assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false)
