package com.kenzan.msl.account.edge.services;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.google.common.base.Optional;
import com.kenzan.msl.account.client.dao.AlbumsByUserDao;
import com.kenzan.msl.account.client.dao.ArtistsByUserDao;
import com.kenzan.msl.account.client.dao.SongsByUserDao;
import com.kenzan.msl.account.client.services.CassandraAccountService;
import com.kenzan.msl.account.edge.TestConstants;
import com.kenzan.msl.catalog.client.dao.AlbumArtistBySongDao;
import com.kenzan.msl.catalog.client.dao.SongsAlbumsByArtistDao;
import com.kenzan.msl.catalog.client.dao.SongsArtistByAlbumDao;
import com.kenzan.msl.catalog.client.services.CassandraCatalogService;
import io.swagger.model.MyLibrary;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import rx.Observable;

import java.util.Iterator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RatingsHelper.class, CassandraAccountService.class, CassandraCatalogService.class })
public class LibraryServiceTest {

    private TestConstants tc = TestConstants.getInstance();
    private CassandraAccountService cassandraAccountService;
    private Observable<ResultSet> observableResultSet;

    private CassandraCatalogService cassandraCatalogService;

    @Before
    public void init()
        throws Exception {
        ResultSet resultSet = createMock(ResultSet.class);
        observableResultSet = Observable.just(resultSet);

        PowerMock.mockStatic(CassandraAccountService.class);
        cassandraAccountService = createMock(CassandraAccountService.class);
        PowerMock.expectNew(CassandraAccountService.class).andReturn(cassandraAccountService);
    }

    @Test
    public void getLibrary()
        throws Exception {
        mockRatingsHelper();

        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        replay(cassandraAccountService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        MyLibrary myLibrary = ls.get(cassandraAccountService, tc.USER_ID.toString());

        assertEquals(myLibrary.getAlbums().size(), 1);
        assertEquals(myLibrary.getAlbums().get(0).getAlbumId(), tc.ALBUMS_BY_USER_DAO.getAlbumId().toString());
        assertEquals(myLibrary.getAlbums().get(0).getAlbumName(), tc.ALBUMS_BY_USER_DAO.getAlbumName());
        assertEquals(myLibrary.getAlbums().get(0).getArtistId(), tc.ALBUMS_BY_USER_DAO.getArtistId().toString());
        assertEquals(myLibrary.getAlbums().get(0).getArtistName(), tc.ALBUMS_BY_USER_DAO.getArtistName());

        assertEquals(myLibrary.getArtists().size(), 1);
        assertEquals(myLibrary.getArtists().get(0).getArtistName(), tc.ARTISTS_BY_USER_DAO.getArtistName());
        assertEquals(myLibrary.getArtists().get(0).getArtistId(), tc.ARTISTS_BY_USER_DAO.getArtistId().toString());

        assertEquals(myLibrary.getSongs().size(), 1);
        assertEquals(myLibrary.getSongs().get(0).getSongName(), tc.SONGS_BY_USER_DAO.getSongName());
        assertEquals(myLibrary.getSongs().get(0).getSongId(), tc.SONGS_BY_USER_DAO.getSongId().toString());
        assertEquals(myLibrary.getSongs().get(0).getArtistName(), tc.SONGS_BY_USER_DAO.getArtistName());
        assertEquals(myLibrary.getSongs().get(0).getArtistId(), tc.SONGS_BY_USER_DAO.getArtistId().toString());
        assertEquals(myLibrary.getSongs().get(0).getAlbumName(), tc.SONGS_BY_USER_DAO.getAlbumName());
        assertEquals(myLibrary.getSongs().get(0).getAlbumId(), tc.SONGS_BY_USER_DAO.getAlbumId().toString());
    }

    @Test(expected = RuntimeException.class)
    public void testAddArtist()
        throws Exception {

        // Mock get MyLibrary Expectations
        mockRatingsHelper();
        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        mockLibraryServiceHelperGetArtist();

        expect(cassandraAccountService.addOrUpdateArtistsByUser(EasyMock.anyObject())).andThrow(new RuntimeException());

        replay(cassandraAccountService);
        replay(cassandraCatalogService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.add(cassandraAccountService, tc.ARTIST_UUID.toString(), tc.USER_ID.toString(), "Artist");
    }

    @Test(expected = RuntimeException.class)
    public void testAddArtistGetEmptyArtistException()
        throws Exception {
        // Mock get MyLibrary Expectations
        mockRatingsHelper();
        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        mockLibraryServiceHelperGetNullArtist();

        replay(cassandraAccountService);
        replay(cassandraCatalogService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.add(cassandraAccountService, tc.ARTIST_UUID.toString(), tc.USER_ID.toString(), "Artist");
    }

    @Test(expected = RuntimeException.class)
    public void testAddAlbumGetEmptyAlbumException()
        throws Exception {
        // Mock get MyLibrary Expectations
        mockRatingsHelper();
        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        mockLibraryServiceHelperGetNullAlbum();

        replay(cassandraAccountService);
        replay(cassandraCatalogService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.add(cassandraAccountService, tc.ALBUM_UUID.toString(), tc.USER_ID.toString(), "Album");
    }

    @Test(expected = RuntimeException.class)
    public void testAddSongGetEmptySongException()
        throws Exception {
        // Mock get MyLibrary Expectations
        mockRatingsHelper();
        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        mockLibraryServiceHelperGetNullSong();

        replay(cassandraAccountService);
        replay(cassandraCatalogService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.add(cassandraAccountService, tc.SONG_UUID.toString(), tc.USER_ID.toString(), "Song");
    }

    @Test(expected = RuntimeException.class)
    public void testAddAlbum()
        throws Exception {
        // Mock get MyLibrary Expectations
        mockRatingsHelper();
        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        mockLibraryServiceHelperGetAlbum();

        expect(cassandraAccountService.addOrUpdateAlbumsByUser(EasyMock.anyObject())).andThrow(new RuntimeException());

        replay(cassandraAccountService);
        replay(cassandraCatalogService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.add(cassandraAccountService, tc.ALBUM_UUID.toString(), tc.USER_ID.toString(), "Album");
    }

    @Test(expected = RuntimeException.class)
    public void testAddSong()
        throws Exception {
        // Mock get MyLibrary Expectations
        mockRatingsHelper();
        getMyLibraryAlbumsExpectations();
        getMyLibraryArtistsExpectations();
        getMyLibrarySongsExpectations();

        mockLibraryServiceHelperGetSong();

        expect(cassandraAccountService.addOrUpdateSongsByUser(EasyMock.anyObject())).andThrow(new RuntimeException());

        replay(cassandraAccountService);
        replay(cassandraCatalogService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.add(cassandraAccountService, tc.SONG_UUID.toString(), tc.USER_ID.toString(), "Song");
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveArtist()
        throws Exception {
        getMyLibraryArtistsExpectations();
        expect(cassandraAccountService.deleteArtistsByUser(tc.USER_ID, tc.FAVORITES_TIMESTAMP, tc.ARTIST_UUID))
            .andReturn(null);
        getMyLibraryArtistsExpectations();
        replay(cassandraAccountService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.remove(cassandraAccountService, tc.ARTIST_UUID.toString(), String.valueOf(tc.FAVORITES_TIMESTAMP.getTime()),
                  tc.USER_ID.toString(), "Artist");
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveAlbum()
        throws Exception {
        getMyLibraryAlbumsExpectations();
        expect(cassandraAccountService.deleteAlbumsByUser(tc.USER_ID, tc.FAVORITES_TIMESTAMP, tc.ALBUM_UUID))
            .andReturn(null);
        getMyLibraryAlbumsExpectations();
        replay(cassandraAccountService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.remove(cassandraAccountService, tc.ALBUM_UUID.toString(), String.valueOf(tc.FAVORITES_TIMESTAMP.getTime()),
                  tc.USER_ID.toString(), "Album");
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveSong()
        throws Exception {
        getMyLibrarySongsExpectations();
        expect(cassandraAccountService.deleteSongsByUser(tc.USER_ID, tc.FAVORITES_TIMESTAMP, tc.SONG_UUID))
            .andReturn(null);
        getMyLibrarySongsExpectations();
        replay(cassandraAccountService);
        PowerMock.replayAll();

        /* *************************** */

        LibraryService ls = new LibraryService();
        ls.remove(cassandraAccountService, tc.SONG_UUID.toString(), String.valueOf(tc.FAVORITES_TIMESTAMP.getTime()),
                  tc.USER_ID.toString(), "Song");
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveArtistException() {
        LibraryService ls = new LibraryService();
        ls.remove(null, tc.ARTIST_UUID.toString(), String.valueOf(tc.FAVORITES_TIMESTAMP.getTime()),
                  tc.USER_ID.toString(), "Artist");
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveAlbumException() {
        LibraryService ls = new LibraryService();
        ls.remove(null, tc.ALBUM_UUID.toString(), String.valueOf(tc.FAVORITES_TIMESTAMP.getTime()),
                  tc.USER_ID.toString(), "Album");
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveSongException() {
        LibraryService ls = new LibraryService();
        ls.remove(null, tc.SONG_UUID.toString(), String.valueOf(tc.FAVORITES_TIMESTAMP.getTime()),
                  tc.USER_ID.toString(), "Song");
    }

    private void getMyLibraryAlbumsExpectations() {
        Result<AlbumsByUserDao> albumsByUserDaoResult = PowerMockito.mock(Result.class);
        expect(cassandraAccountService.getAlbumsByUser(tc.USER_ID, Optional.absent(), Optional.absent()))
            .andReturn(observableResultSet);
        expect(cassandraAccountService.mapAlbumsByUser(observableResultSet))
            .andReturn(Observable.just(albumsByUserDaoResult));

        Iterator albumsByUserDaoIterator = tc.albumsByUserDaoArrayList.iterator();
        PowerMockito.when(albumsByUserDaoResult.iterator()).thenReturn(albumsByUserDaoIterator);
    }

    private void getMyLibrarySongsExpectations() {
        Result<SongsByUserDao> songsByUserDaoResult = PowerMockito.mock(Result.class);
        expect(cassandraAccountService.getSongsByUser(tc.USER_ID, Optional.absent(), Optional.absent()))
            .andReturn(observableResultSet);
        expect(cassandraAccountService.mapSongsByUser(observableResultSet)).andReturn(Observable
                                                                                          .just(songsByUserDaoResult));

        Iterator songsByUserDaoIterator = tc.songsByUserDaoArrayList.iterator();
        PowerMockito.when(songsByUserDaoResult.iterator()).thenReturn(songsByUserDaoIterator);
    }

    private void getMyLibraryArtistsExpectations() {
        Result<ArtistsByUserDao> artistsByUserDaoResult = PowerMockito.mock(Result.class);
        expect(cassandraAccountService.getArtistsByUser(tc.USER_ID, Optional.absent(), Optional.absent()))
            .andReturn(observableResultSet);
        expect(cassandraAccountService.mapArtistByUser(observableResultSet))
            .andReturn(Observable.just(artistsByUserDaoResult));

        Iterator artistsByUserDaoIterator = tc.artistsByUserDaoArrayList.iterator();
        PowerMockito.when(artistsByUserDaoResult.iterator()).thenReturn(artistsByUserDaoIterator);
    }

    private void mockRatingsHelper()
        throws Exception {
        PowerMock.mockStatic(RatingsHelper.class);
        RatingsHelper ratingsHelper = createMock(RatingsHelper.class);
        PowerMock.expectNew(RatingsHelper.class).andReturn(ratingsHelper);
        expect(RatingsHelper.getInstance()).andReturn(ratingsHelper).anyTimes();
    }

    private void mockCassandraCatalogService()
        throws Exception {
        PowerMock.mockStatic(CassandraCatalogService.class);
        cassandraCatalogService = createMock(CassandraCatalogService.class);
        PowerMock.expectNew(CassandraCatalogService.class).andReturn(cassandraCatalogService);

        expect(CassandraCatalogService.getInstance()).andReturn(cassandraCatalogService).anyTimes();
    }

    private void mockLibraryServiceHelperGetArtist()
        throws Exception {
        mockCassandraCatalogService();

        Result<SongsAlbumsByArtistDao> songsAlbumsByArtistDaoResult = PowerMockito.mock(Result.class);
        tc.songsAlbumsByArtistDao.setArtistId(tc.ARTIST_UUID_2);
        PowerMockito.when(songsAlbumsByArtistDaoResult.one()).thenReturn(tc.songsAlbumsByArtistDao);

        expect(cassandraCatalogService.getSongsAlbumsByArtist(tc.ARTIST_UUID, Optional.absent()))
            .andReturn(observableResultSet);

        expect(cassandraCatalogService.mapSongsAlbumsByArtist(observableResultSet))
            .andReturn(Observable.just(songsAlbumsByArtistDaoResult));
    }

    private void mockLibraryServiceHelperGetAlbum()
        throws Exception {
        mockCassandraCatalogService();

        Result<SongsArtistByAlbumDao> songsArtistByAlbumDaoResult = PowerMockito.mock(Result.class);
        tc.songsArtistByAlbumDao.setAlbumId(tc.ALBUM_UUID_2);
        PowerMockito.when(songsArtistByAlbumDaoResult.one()).thenReturn(tc.songsArtistByAlbumDao);

        expect(cassandraCatalogService.getSongsArtistByAlbum(tc.ALBUM_UUID, Optional.absent()))
            .andReturn(observableResultSet);

        expect(cassandraCatalogService.mapSongsArtistByAlbum(observableResultSet))
            .andReturn(Observable.just(songsArtistByAlbumDaoResult));
    }

    private void mockLibraryServiceHelperGetSong()
        throws Exception {
        mockCassandraCatalogService();

        Result<AlbumArtistBySongDao> albumArtistBySongDaoResult = PowerMockito.mock(Result.class);
        tc.albumArtistBySongDao.setSongId(tc.SONG_UUID_2);
        PowerMockito.when(albumArtistBySongDaoResult.one()).thenReturn(tc.albumArtistBySongDao);

        expect(cassandraCatalogService.getAlbumArtistBySong(tc.SONG_UUID, Optional.absent()))
            .andReturn(observableResultSet);

        expect(cassandraCatalogService.mapAlbumArtistBySong(observableResultSet))
            .andReturn(Observable.just(albumArtistBySongDaoResult));
    }

    private void mockLibraryServiceHelperGetNullArtist()
        throws Exception {
        mockCassandraCatalogService();

        expect(cassandraCatalogService.getSongsAlbumsByArtist(tc.ARTIST_UUID, Optional.absent()))
            .andReturn(observableResultSet);

        expect(cassandraCatalogService.mapSongsAlbumsByArtist(observableResultSet)).andReturn(Observable.just(null));
    }

    private void mockLibraryServiceHelperGetNullAlbum()
        throws Exception {
        mockCassandraCatalogService();

        expect(cassandraCatalogService.getSongsArtistByAlbum(tc.ALBUM_UUID, Optional.absent()))
            .andReturn(observableResultSet);

        expect(cassandraCatalogService.mapSongsArtistByAlbum(observableResultSet)).andReturn(Observable.just(null));
    }

    private void mockLibraryServiceHelperGetNullSong()
        throws Exception {
        mockCassandraCatalogService();

        expect(cassandraCatalogService.getAlbumArtistBySong(tc.SONG_UUID, Optional.absent()))
            .andReturn(observableResultSet);

        expect(cassandraCatalogService.mapAlbumArtistBySong(observableResultSet)).andReturn(Observable.just(null));
    }

}