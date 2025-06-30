$c = Get-Content pom.xml; $c[17] = "    <name>CourtLink</name>"; $c | Set-Content pom.xml
