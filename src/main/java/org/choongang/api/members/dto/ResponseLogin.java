package org.choongang.api.members.dto;

import lombok.Builder;

@Builder
public record ResponseLogin(
        String accessToken
) {}
