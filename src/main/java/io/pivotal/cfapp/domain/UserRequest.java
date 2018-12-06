package io.pivotal.cfapp.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class UserRequest {

	private String id;
    private String organization;
    private String spaceId;
    private String spaceName;
    private String appName;
    private String image;

    public static UserRequestBuilder from(UserRequest request) {
        return UserRequest
                .builder()
                	.id(request.getId())
                    .organization(request.getOrganization())
                    .spaceId(request.getSpaceId())
                    .spaceName(request.getSpaceName())
                    .appName(request.getAppName())
                    .image(request.getImage());
    }
}
