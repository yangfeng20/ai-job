FROM maple20/jdk17_env:1.0
WORKDIR /app/

# 请将DockerFile文件放在和最终的jar包同一目录
COPY ./ai-job.jar /app/
RUN ls /app

# api端口
EXPOSE 9100

# 后台配置中心端口
EXPOSE 6768

ENTRYPOINT ["java", "-jar","-XX:+StartAttachListener"]
CMD ["/app/ai-job.jar"]