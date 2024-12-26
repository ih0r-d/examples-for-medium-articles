package com.ih0rd.sandbox;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SharedUtils {

    private static final String K8S_MANIFEST = "META-INF/dekorate/kubernetes.yml";

    public static KubernetesList loadKubernetesResources() {
        return Serialization.unmarshalAsList(Objects.requireNonNull(SharedUtils.class.getClassLoader().getResourceAsStream(K8S_MANIFEST)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends HasMetadata> T findFirst(Class<T> clazz) {
        KubernetesList list = loadKubernetesResources();
        assertNotNull(list);

        return (T) list.getItems().stream()
                .filter(clazz::isInstance)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Resource of type " + clazz.getName() + " not found"));
    }

    public static <T> T read(String path, Class<T> clazz) throws IOException {
        return Serialization.yamlMapper().readValue(SharedUtils.class.getClassLoader()
                .getResourceAsStream(path), clazz);
    }

}
