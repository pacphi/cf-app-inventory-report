package io.pivotal.cfapp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DockerImageCount {

	private String image;
    private long total;

    public String toCsv() {
        return String.join(",", image, String.valueOf(total));
    }

    public static String headers() {
        return String.join(",", "docker image", "total");
    }
}
