resolvers += Resolver.url("typesafe", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

scriptedBufferLog := false

scriptedLaunchOpts ++= sys.process.javaVmArguments.filter(
  a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
)
