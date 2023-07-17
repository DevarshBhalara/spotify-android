package com.simformsolutions.myspotify.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.simformsolutions.myspotify.data.model.local.LibraryDisplay
import com.simformsolutions.myspotify.data.model.local.LibraryItemType
import com.simformsolutions.myspotify.data.repository.UserLibraryRepository
import com.simformsolutions.myspotify.ui.base.BaseViewModel
import com.simformsolutions.myspotify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserLibraryViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository
) : BaseViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _libraryItems = MutableStateFlow<List<LibraryDisplay>>(emptyList())
    val libraryItems = _libraryItems.asStateFlow()

    private val _temp = MutableStateFlow<List<LibraryDisplay>>(emptyList())
    val temp = _temp.asStateFlow()

    fun getInitialLibraryItems() {
        _libraryItems.value = emptyList()
        _temp.value = emptyList()
        viewModelScope.launch {
            userLibraryRepository.getPlaylists().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { items ->
                            val data = items.items.map { playlist ->
                                LibraryDisplay(
                                    false,
                                    playlist.name,
                                    playlist.owner.displayName,
                                    playlist.images.firstOrNull()?.url ?: "",
                                    LibraryItemType.PLAYLIST,
                                    playlist.id
                                )
                            }
                            _isLoading.emit(false)
                            _libraryItems.emit(data)
                        }
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }

            userLibraryRepository.getAlbums().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { albumsData ->
                            val data = albumsData.items.map { item ->
                                LibraryDisplay(
                                    false, item.album.name, item.album.artists.firstOrNull()?.name
                                        ?: "N/A", item.album.images.firstOrNull()?.url
                                        ?: "", LibraryItemType.ALBUM, item.album.id
                                )
                            }
                            _isLoading.emit(false)
                            _libraryItems.emit(data)
                        }
                    }

                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }

            userLibraryRepository.getFollowedArtists().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { followedArtists ->
                            val data = followedArtists.artists.items.map { artist ->
                                LibraryDisplay(
                                    false,
                                    artist.name,
                                    "hello",
                                    artist.images?.firstOrNull()?.url,
                                    LibraryItemType.ARTISTS,
                                    artist.id,
                                )
                            }
                            _isLoading.emit(false)
                            _libraryItems.emit(data)
                        }
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }
        }
    }

    fun getPlaylists() {
        _temp.value = emptyList()
        viewModelScope.launch {
            userLibraryRepository.getPlaylists().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { items ->
                            val data = items.items.map { playlist ->
                                LibraryDisplay(
                                    true,
                                    playlist.name,
                                    playlist.owner.displayName,
                                    playlist.images.firstOrNull()?.url ?: "",
                                    LibraryItemType.PLAYLIST,
                                    playlist.id
                                )
                            }
                            _isLoading.emit(false)
                            _temp.emit(data)
                        }
                    }

                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }
        }
    }

    fun getArtists() {
        _temp.value = emptyList()
        viewModelScope.launch {
            userLibraryRepository.getFollowedArtists().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { followedArtists ->
                            val data = followedArtists.artists.items.map { artist ->
                                LibraryDisplay(
                                    true,
                                    artist.name,
                                    "hello",
                                    artist.images?.firstOrNull()?.url,
                                    LibraryItemType.ARTISTS,
                                    artist.id,
                                )
                            }
                            _isLoading.emit(false)
                            _temp.emit(data)
                        }
                    }

                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }
        }
    }

    fun getAlbum() {
        _temp.value = emptyList()
        viewModelScope.launch {
            userLibraryRepository.getAlbums().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { albumsData ->
                            val data = albumsData.items.map { item ->
                                LibraryDisplay(
                                    true, item.album.name, item.album.artists.firstOrNull()?.name
                                        ?: "N/A", item.album.images.firstOrNull()?.url
                                        ?: "", LibraryItemType.ALBUM, item.album.id
                                )
                            }
                            _isLoading.emit(false)
                            _temp.emit(data)
                        }
                    }

                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }
        }
    }

    fun getSavedShows() {
        _temp.value = emptyList()
        viewModelScope.launch {
            userLibraryRepository.getSavedEpisodes().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { shows ->
                            val data = shows.items.map { item ->
                                LibraryDisplay(true, item.show?.name, item.show?.publisher, item.show?.images?.firstOrNull()?.url, LibraryItemType.PODCAST, item.show?.id)
                            }
                            _isLoading.emit(false)
                            _libraryItems.value = emptyList()
                            _temp.emit(data)
                        }
                    }

                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        _isLoading.emit(true)
                    }
                }
            }
        }
    }
}