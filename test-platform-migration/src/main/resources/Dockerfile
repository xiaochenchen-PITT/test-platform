FROM registry.xyb2b.com.cn/base/jdk:1.8.0_101

ENV JAVA_OPTS="-Xmx1024M -Xms1024M -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+ParallelRefProcEnabled -XX:ErrorFile=./hs_err_pid%p.log -Xloggc:./gc.log -XX:HeapDumpPath=./ -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintClassHistogramBeforeFullGC -XX:+PrintClassHistogramAfterFullGC -XX:+PrintGCApplicationConcurrentTime"
ENV PROFILES_ACTIVE="dev"

ADD league-job-admin.jar /usr/local/release
ADD apiclient_cert.p12 /usr/local/release

WORKDIR /usr/local/release

CMD java -Dfile.encoding=utf-8 ${JAVA_OPTS} -jar league-job-admin.jar --spring.profiles.active=${env}