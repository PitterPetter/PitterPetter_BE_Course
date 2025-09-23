import groovy.sql.Sql

def props = System.properties
String url = props['db.url'] ?: 'jdbc:postgresql://localhost:5432/poi_db'
String user = props['db.user'] ?: 'poi_user'
String password = props['db.password'] ?: 'poi_pass'

println "Connecting to ${url} ..."

Sql sql = Sql.newInstance(url, user, password, 'org.postgresql.Driver')
println 'Connected.'

sql.close()
