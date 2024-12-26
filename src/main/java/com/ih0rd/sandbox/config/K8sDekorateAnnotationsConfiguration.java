package com.ih0rd.sandbox.config;

import io.dekorate.docker.annotation.DockerBuild;
import io.dekorate.helm.annotation.HelmChart;
import io.dekorate.kubernetes.annotation.*;
import io.dekorate.kubernetes.config.DeploymentStrategy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@KubernetesApplication(
        ingress = @Ingress(
                expose = true,
                ingressClassName = "nginx",
                host = "medium-article-part2.local",
                rules = {
                        @IngressRule(
                                host = "medium-article-part2.local",
                                serviceName = "medium-article-part2",
                                servicePortName = "8080"
                        )
                },
                tlsSecretName = "medium-article-part2-tls-secret",
                tlsHosts = {"medium-article-part2.local"}
        ),
        replicas = 2,
        ports = {@Port(name = "http", containerPort = 8080)},
        rollingUpdate = @RollingUpdate(maxSurge = "50%", maxUnavailable = "50%"),
        deploymentStrategy = DeploymentStrategy.RollingUpdate,
        labels = {@Label(key = "article", value = "medium-part2-code")},
        serviceType = ServiceType.LoadBalancer // Add LoadBalancer for work with Ingress
)
@DockerBuild(image = "custom-image:latest")
@HelmChart(name = "custom-helm-chart")
@Configuration // Just to register for Spring Boot app
public class K8sDekorateAnnotationsConfiguration {
}
