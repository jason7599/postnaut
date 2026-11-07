package com.jason7599.postnaut.post;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

interface MyPostViewRepository extends Repository<MyPostView, UUID> {

    Optional<MyPostView> findByPosterIdAndClientId(UUID posterId, UUID clientId);

    Window<MyPostView> findByPosterId(UUID posterId, ScrollPosition pos, Sort sort, Limit limit);

    Window<MyPostView> findByPosterIdAndIsExpired(UUID posterId, boolean isExpired, ScrollPosition pos, Sort sort, Limit limit);
}
