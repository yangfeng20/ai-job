FROM ccr.ccs.tencentyun.com/maple20/jdk17_env:1.0
WORKDIR /app/
COPY ./ai-job.jar /app/
RUN ls /app
EXPOSE 9100
EXPOSE 6768
ENTRYPOINT ["java", "-jar","-XX:+StartAttachListener"]
CMD ["/app/ai-job.jar"]