package com.kenzan.msl.account.edge.services.impl;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.kenzan.msl.account.edge.services.RatingsService;
import com.kenzan.msl.common.ContentType;
import com.kenzan.msl.ratings.client.dto.AverageRatingsDto;
import com.kenzan.msl.ratings.client.dto.UserRatingsDto;
import com.kenzan.msl.ratings.client.services.RatingsDataClientService;
import io.swagger.model.AlbumInfo;
import io.swagger.model.ArtistInfo;
import io.swagger.model.SongInfo;

import java.util.List;
import java.util.UUID;

public class RatingsServiceImpl implements RatingsService {

  private final RatingsDataClientService ratingsDataClientService;

  /**
   * Constructor
   *
   * @param ratingsDataClientService com.kenzan.msl.ratings.client.services.RatingsDataClientService
   */
  @Inject
  public RatingsServiceImpl(final RatingsDataClientService ratingsDataClientService) {
    this.ratingsDataClientService = ratingsDataClientService;
  }

  /**
   * Adds average and user ratings to a list of AlbumInfo objects
   *
   * @param albumList List&lt;AlbumInfo&gt;
   * @param userUuid java.util.UUID
   */
  @Override
  public void processAlbumRatings(List<AlbumInfo> albumList, UUID userUuid) {
    for (AlbumInfo albumInfo : albumList) {
      Optional<AverageRatingsDto> averageRatingsDto =
          ratingsDataClientService
              .getAverageRating(UUID.fromString(albumInfo.getAlbumId()), ContentType.ALBUM.value)
              .toBlocking().first();

      if (averageRatingsDto.isPresent()) {
        long average =
            averageRatingsDto.get().getNumRating() / averageRatingsDto.get().getSumRating();
        albumInfo.setAverageRating((int) average);
      }

      Optional<UserRatingsDto> userRatingsDto =
          ratingsDataClientService
              .getUserRating(userUuid, ContentType.ALBUM.value,
                  UUID.fromString(albumInfo.getAlbumId())).toBlocking().first();
      if (userRatingsDto.isPresent()) {
        albumInfo.setPersonalRating(userRatingsDto.get().getRating());
      }

    }
  }

  /**
   * Adds average and user ratings to a list of ArtistInfo objects
   *
   * @param artistList List&lt;ArtistInfo&gt;
   * @param userUuid java.util.UUID
   */
  @Override
  public void processArtistRatings(List<ArtistInfo> artistList, UUID userUuid) {
    for (ArtistInfo artistInfo : artistList) {
      Optional<AverageRatingsDto> averageRatingsDto =
          ratingsDataClientService
              .getAverageRating(UUID.fromString(artistInfo.getArtistId()), ContentType.ARTIST.value)
              .toBlocking().first();

      if (averageRatingsDto.isPresent()) {
        long average =
            averageRatingsDto.get().getNumRating() / averageRatingsDto.get().getSumRating();
        artistInfo.setAverageRating((int) average);
      }

      Optional<UserRatingsDto> userRatingsDto =
          ratingsDataClientService
              .getUserRating(userUuid, ContentType.ARTIST.value,
                  UUID.fromString(artistInfo.getArtistId())).toBlocking().first();
      if (userRatingsDto.isPresent()) {
        artistInfo.setPersonalRating(userRatingsDto.get().getRating());
      }

    }
  }

  /**
   * Adds average and user rating data on a list of SongInfo objects
   *
   * @param songList List&lt;SongInfo&gt;
   * @param userUuid java.util.UUID
   */
  @Override
  public void processSongRatings(List<SongInfo> songList, UUID userUuid) {
    for (SongInfo songInfo : songList) {
      Optional<AverageRatingsDto> averageRatingsDto =
          ratingsDataClientService
              .getAverageRating(UUID.fromString(songInfo.getSongId()), ContentType.SONG.value)
              .toBlocking().first();

      if (averageRatingsDto.isPresent()) {
        long average =
            averageRatingsDto.get().getNumRating() / averageRatingsDto.get().getSumRating();
        songInfo.setAverageRating((int) average);
      }

      Optional<UserRatingsDto> userRatingsDto =
          ratingsDataClientService
              .getUserRating(userUuid, ContentType.SONG.value,
                  UUID.fromString(songInfo.getSongId())).toBlocking().first();
      if (userRatingsDto.isPresent()) {
        songInfo.setPersonalRating(userRatingsDto.get().getRating());
      }
    }
  }

}
