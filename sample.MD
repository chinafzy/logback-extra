
`application.yml`
```yaml
berth:
  cities:
    - city1
    - city2

logging:
  file:
    path:
      /data/log/geo
```

`logback-geo.xml`
```xml
<included>
    <include resource="logback-extra-included.xml"/>
    <springProperty name="LOG_HOME" source="logging.file.path"/>
    <property name="geo_pattern" value="%d{yyyy-MM-dd HH:mm:ss.SSS} |# %msg%n"/>
    
    <for name="city" of-spring="berth.cities">
        <echo>init logger for ${city}</echo>

        <appender name="GEO_ORI.${city}" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>${LOG_HOME}/geo-${city}.log</file>
            <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
                <pattern>${geo_pattern}</pattern>
            </encoder>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <FileNamePattern>${LOG_HOME}/history/%d{yyyy-MM-dd}/geo-${city}.log.gz</FileNamePattern>
                <MaxHistory>0</MaxHistory>
            </rollingPolicy>
            <filter class="ch.qos.logback.classic.filter.LevelFilter">
                <level>INFO</level>
                <onMatch>accept</onMatch>
            </filter>
        </appender>

        <logger name="geo.ori.${city}" level="INFO" additivity="false">
            <appender-ref ref="GEO_ORI.${city}"/>
        </logger>
    </for>

    <appender name="GEO_ORI" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}/geo.log</file>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>${geo_pattern}</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--日志文件输出的文件名 -->
            <FileNamePattern>${LOG_HOME}/history/%d{yyyy-MM-dd}/geo.log.gz</FileNamePattern>
            <!--日志文件保留天数 -->
            <MaxHistory>0</MaxHistory>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>accept</onMatch>
        </filter>
    </appender>

    <logger name="geo.ori" level="INFO" additivity="false">
        <appender-ref ref="GEO_ORI"/>
    </logger>
</included>
```

`logback-spring.xml`
```xml
<configuration debug="false">
    <include resource="logback-geo.xml"/>

    <!--  others  -->
</configuration>
```

`Usage.java`
```java
void do1(String city, String reeStr) {
    LoggerFactory.getLogger("geo.ori." + city).info(reqStr);
}
```

`logs`
```shell
$ tree /data/log/geo
|-- geo-city1.log
|-- geo-city2.log
|-- geo.log
```
