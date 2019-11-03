package org.oasis_eu.portal.services.jobs;

import org.joda.time.DateTime;
import org.oasis_eu.portal.dao.ImageRepository;
import org.oasis_eu.portal.model.images.Image;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.oasis_eu.portal.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!test")
public class ImageRefresher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRefresher.class);

    private final ImageRepository imageRepository;
    private final EnvPropertiesService envPropertiesService;
    private final ImageService imageService;

    public ImageRefresher(ImageRepository imageRepository, EnvPropertiesService envPropertiesService, ImageService imageService) {
        this.imageRepository = imageRepository;
        this.envPropertiesService = envPropertiesService;
        this.imageService = imageService;
    }

    @Scheduled(fixedRate = 600000)
    public void refreshOldImages() {
        LOGGER.debug("Refreshing images");

        // every 10 minutes, try to download the 10 oldest images not already downloaded in the last 60 minutes (phew)
        List<Image> images =
                imageRepository.findByDownloadedTimeBefore(DateTime.now().minusMinutes(60),
                        PageRequest.of(0, 10, Sort.Direction.ASC, "downloadedTime"));

        LOGGER.debug("Found {} image(s) to refresh", images.size());

        images.forEach(i -> imageService.getImageForURLInEnvConfig(i.getUrl(), i.getImageFormat(), true,
                envPropertiesService.getDefaultConfig()));
    }
}
