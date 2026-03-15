package com.example.tengxuncvm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeImagesRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeImagesResponse;
import com.tencentcloudapi.cvm.v20170312.models.Image;

import com.example.tengxuncvm.web.ImageDto;

@Service
public class ImageService {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 5;

    private final CvmClientFactory clientFactory;

    public ImageService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public List<ImageDto> listImages(String region) {
        try {
            CvmClient client = clientFactory.create(region);
            List<Image> allImages = queryImagesByPages(client);
            if (allImages.isEmpty()) {
                return Collections.emptyList();
            }

            return allImages.stream()
                    .filter(this::notArm64Image)
                    .sorted(Comparator
                            .comparing((Image image) -> imageRank(image), Comparator.reverseOrder())
                            .thenComparing(Image::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(Image::getImageName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to list images.", ex);
        }
    }

    private List<Image> queryImagesByPages(CvmClient client) throws TencentCloudSDKException {
        List<Image> result = new ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();

        for (int page = 0; page < MAX_PAGES; page++) {
            DescribeImagesRequest request = new DescribeImagesRequest();
            request.setLimit((long) PAGE_SIZE);
            request.setOffset((long) page * PAGE_SIZE);

            DescribeImagesResponse response = client.DescribeImages(request);
            Image[] images = response.getImageSet();
            if (images == null || images.length == 0) {
                break;
            }

            for (Image image : images) {
                if (image == null || image.getImageId() == null) {
                    continue;
                }
                if (dedup.add(image.getImageId())) {
                    result.add(image);
                }
            }

            if (images.length < PAGE_SIZE) {
                break;
            }
        }
        return result;
    }

    private int imageRank(Image image) {
        String name = lower(image.getImageName());
        String osName = lower(image.getOsName());
        String type = lower(image.getImageType());

        int score = 0;

        // 优先公共镜像
        if ("public_image".equals(type)) {
            score += 100;
        }

        // 关键词加权：ubuntu +40, server +20, lts +20
        if (containsAny(name, osName, "ubuntu")) {
            score += 40;
        }
        if (containsAny(name, osName, "server")) {
            score += 20;
        }
        if (containsAny(name, osName, "lts")) {
            score += 20;
        }

        // 常见稳定版本额外加分
        if (containsAny(name, osName, "20.04", "22.04", "24.04")) {
            score += 10;
        }

        // 兜底系统偏好，保证非 ubuntu 也有可用排序
        if (containsAny(name, osName, "centos")) {
            score += 8;
        } else if (containsAny(name, osName, "debian")) {
            score += 6;
        } else if (containsAny(name, osName, "windows")) {
            score += 4;
        } else if (containsAny(name, osName, "tencentos", "tlinux")) {
            score += 2;
        }

        return score;
    }

    private boolean notArm64Image(Image image) {
        String name = lower(image.getImageName());
        String osName = lower(image.getOsName());
        return !(containsAny(name, osName, "arm64", "aarch64", "tk4", "uefi", "tencentos", "grid"));
    }

    private boolean isRecommended(Image image) {
        return imageRank(image) > 0;
    }

    private boolean containsAny(String name, String osName, String... keys) {
        return Arrays.stream(keys).anyMatch(key -> name.contains(key) || osName.contains(key));
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private ImageDto toDto(Image image) {
        ImageDto dto = new ImageDto();
        dto.setImageId(image.getImageId());
        dto.setImageName(image.getImageName());
        dto.setImageType(image.getImageType());
        dto.setImageOsName(image.getOsName());
        dto.setImageSize(image.getImageSize());
        dto.setCreatedTime(image.getCreatedTime());
        dto.setRecommended(isRecommended(image));
        return dto;
    }
}
