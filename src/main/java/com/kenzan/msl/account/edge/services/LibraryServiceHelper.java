/*
 * Copyright 2015, Kenzan, All rights reserved.
 */
package com.kenzan.msl.account.edge.services;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.google.common.base.Optional;
import com.kenzan.msl.catalog.client.dto.AlbumArtistBySongDto;
import com.kenzan.msl.catalog.client.dto.SongsAlbumsByArtistDto;
import com.kenzan.msl.catalog.client.dto.SongsArtistByAlbumDto;
import com.kenzan.msl.catalog.client.services.CassandraCatalogService;
import com.kenzan.msl.common.bo.AlbumBo;
import com.kenzan.msl.common.bo.ArtistBo;
import com.kenzan.msl.common.bo.SongBo;
import rx.Observable;

import java.util.UUID;

public class LibraryServiceHelper {

    /**
     * Retrieves a specific artist from teh songs_albums_by_artist cassandra table
     *
     * @param artistId java.util.UUID
     * @return Optional&lt;ArtistBo&gt;
     */
    public Optional<ArtistBo> getArtist(final UUID artistId) {
        CassandraCatalogService cassandraCatalogService = CassandraCatalogService.getInstance();

        Observable<ResultSet> observableArtist = cassandraCatalogService.getSongsAlbumsByArtist(artistId,
                                                                                                Optional.absent());

        Result<SongsAlbumsByArtistDto> mappingResult = cassandraCatalogService.mapSongsAlbumsByArtist(observableArtist)
            .toBlocking().first();

        if ( mappingResult == null ) {
            return Optional.absent();
        }

        ArtistBo artistBo = new ArtistBo();
        SongsAlbumsByArtistDto songsAlbumsByArtistDto = mappingResult.one();

        artistBo.setArtistId(songsAlbumsByArtistDto.getArtistId());
        artistBo.setArtistName(songsAlbumsByArtistDto.getArtistName());

        if ( songsAlbumsByArtistDto.getArtistGenres() != null && songsAlbumsByArtistDto.getArtistGenres().size() > 0 ) {
            artistBo.setGenre(songsAlbumsByArtistDto.getArtistGenres().iterator().next());
        }
        if ( songsAlbumsByArtistDto.getSimilarArtists() != null ) {
            for ( UUID similarArtistUuid : songsAlbumsByArtistDto.getSimilarArtists().keySet() ) {
                artistBo.getSimilarArtistsList().add(similarArtistUuid.toString());
            }
        }

        return Optional.of(artistBo);
    }

    /**
     * Retrieves a specific album from the songs_artist_by_album cassandra table
     *
     * @param albumId java.util.UUID
     * @return Optional&lt;AlbumBo&gt;
     */
    public Optional<AlbumBo> getAlbum(final UUID albumId) {
        CassandraCatalogService cassandraCatalogService = CassandraCatalogService.getInstance();

        Observable<ResultSet> observableAlbum = cassandraCatalogService.getSongsArtistByAlbum(albumId,
                                                                                              Optional.absent());

        Result<SongsArtistByAlbumDto> mapResults = cassandraCatalogService.mapSongsArtistByAlbum(observableAlbum)
            .toBlocking().first();

        if ( null == mapResults ) {
            return Optional.absent();
        }

        AlbumBo albumBo = new AlbumBo();
        SongsArtistByAlbumDto songsArtistByAlbumDto = mapResults.one();

        albumBo.setAlbumId(songsArtistByAlbumDto.getAlbumId());
        albumBo.setAlbumName(songsArtistByAlbumDto.getAlbumName());
        albumBo.setArtistId(songsArtistByAlbumDto.getArtistId());
        albumBo.setArtistName(songsArtistByAlbumDto.getArtistName());
        albumBo.setImageLink(songsArtistByAlbumDto.getImageLink());

        if ( songsArtistByAlbumDto.getArtistGenres() != null && songsArtistByAlbumDto.getArtistGenres().size() > 0 ) {
            albumBo.setGenre(songsArtistByAlbumDto.getArtistGenres().iterator().next());
        }

        return Optional.of(albumBo);
    }

    /**
     * Retrieves a specific album from the songs_artist_by_album cassandra table
     *
     * @param songId java.util.UUID
     * @return Optional&lt;SongBo&gt;
     */
    public Optional<SongBo> getSong(final UUID songId) {
        CassandraCatalogService cassandraCatalogService = CassandraCatalogService.getInstance();

        Observable<ResultSet> observableSong = cassandraCatalogService.getAlbumArtistBySong(songId, Optional.absent());

        Result<AlbumArtistBySongDto> mapResults = cassandraCatalogService.mapAlbumArtistBySong(observableSong)
            .toBlocking().first();

        if ( null == mapResults ) {
            return Optional.absent();
        }

        SongBo songBo = new SongBo();
        AlbumArtistBySongDto albumArtistBySongDto = mapResults.one();

        songBo.setSongId(albumArtistBySongDto.getSongId());
        songBo.setSongName(albumArtistBySongDto.getSongName());
        songBo.setAlbumId(albumArtistBySongDto.getAlbumId());
        songBo.setAlbumName(albumArtistBySongDto.getAlbumName());
        songBo.setArtistId(albumArtistBySongDto.getArtistId());
        songBo.setArtistName(albumArtistBySongDto.getArtistName());
        songBo.setDuration(albumArtistBySongDto.getSongDuration());
        songBo.setYear(albumArtistBySongDto.getAlbumYear());

        if ( albumArtistBySongDto.getArtistGenres() != null && albumArtistBySongDto.getArtistGenres().size() > 0 ) {
            songBo.setGenre(albumArtistBySongDto.getArtistGenres().iterator().next());
        }

        return Optional.of(songBo);
    }
}
