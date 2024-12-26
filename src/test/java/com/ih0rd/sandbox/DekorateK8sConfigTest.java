package com.ih0rd.sandbox;

import io.dekorate.helm.model.Chart;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DekorateK8sConfigTest {

    private static KubernetesList K8S_RESOURCES;
    private static final String EXPECTED_DEPLOYMENT_NAME = "medium-articles-k8s-generation-sandbox";
    private static final String EXPECTED_IMAGE = "custom-image:latest";
    private static final String EXPECTED_INGRESS_HOST = "medium-article-part2.local";
    private static final String EXPECTED_TLS_SECRET = "medium-article-part2-tls-secret";
    private static final String EXPECTED_HELM_CHART_NAME = "custom-helm-chart";

    @BeforeAll
    static void setupAll() {
        K8S_RESOURCES = SharedUtils.loadKubernetesResources();
    }

    @Test
    public void shouldGenerateDeployment() {
        assertNotNull(K8S_RESOURCES, "Kubernetes resources should not be null");
        Deployment deployment = SharedUtils.findFirst(Deployment.class);
        assertNotNull(deployment, "Deployment resource should exist");
        assertEquals(EXPECTED_DEPLOYMENT_NAME, deployment.getMetadata().getName(), "Deployment name mismatch");

        final AnyType expectedRollingUpdateMaxValue = new AnyType("50%");
        // Validate replicas and strategy
        assertEquals(2, deployment.getSpec().getReplicas(), "Replica count mismatch");
        assertEquals("RollingUpdate", deployment.getSpec().getStrategy().getType(), "Deployment strategy mismatch");
        assertEquals(expectedRollingUpdateMaxValue, deployment.getSpec().getStrategy().getRollingUpdate().getMaxSurge(), "MaxSurge mismatch");
        assertEquals(expectedRollingUpdateMaxValue, deployment.getSpec().getStrategy().getRollingUpdate().getMaxUnavailable(), "MaxUnavailable mismatch");
    }

    @Test
    public void shouldSetContainerImage() {
        Deployment deployment = SharedUtils.findFirst(Deployment.class);
        assertNotNull(deployment, "Deployment resource should exist");

        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst();
        assertNotNull(container, "Container should exist in Deployment");
        assertEquals(EXPECTED_IMAGE, container.getImage(), "Container image mismatch");
    }


    @Test
    public void shouldExposeServiceAsLoadBalancer() {
        Service service = SharedUtils.findFirst(Service.class);
        assertNotNull(service, "Service resource should exist");
        assertEquals("LoadBalancer", service.getSpec().getType(), "Service type mismatch");
    }

    @Test
    public void shouldGenerateHelmChart() throws IOException {
        Chart chart = SharedUtils.read("META-INF/dekorate/helm/kubernetes/" + EXPECTED_HELM_CHART_NAME + "/Chart.yaml", Chart.class);
        assertNotNull(chart, "Helm chart should exist");
        assertEquals(EXPECTED_HELM_CHART_NAME, chart.getName(), "Helm chart name mismatch");
    }

    @Test
    public void shouldGenerateIngress() {
        Ingress ingress = SharedUtils.findFirst(Ingress.class);
        assertNotNull(ingress, "Ingress resource should exist");
        assertEquals(EXPECTED_INGRESS_HOST, ingress.getSpec().getRules().getFirst().getHost(), "Ingress host mismatch");

        // Validate TLS
        assertEquals(EXPECTED_TLS_SECRET, ingress.getSpec().getTls().getFirst().getSecretName(), "TLS secret name mismatch");
        assertTrue(ingress.getSpec().getTls().getFirst().getHosts().contains(EXPECTED_INGRESS_HOST), "TLS host mismatch");
    }
}
