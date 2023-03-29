package com.afaneca.marvelchallenge.data.repository

import com.afaneca.marvelchallenge.common.Constants.DEFAULT_PAGE_SIZE
import com.afaneca.marvelchallenge.common.Resource
import com.afaneca.marvelchallenge.data.remote.MarvelRemoteDataSource
import com.afaneca.marvelchallenge.data.remote.entity.mapToDomain
import com.afaneca.marvelchallenge.domain.model.CharacterPage
import com.afaneca.marvelchallenge.domain.repository.CharacterRepository
import javax.inject.Inject

class LiveCharacterRepository @Inject constructor(
    private val remoteDataSource: MarvelRemoteDataSource,
) : CharacterRepository {
    override suspend fun getCharacters(page: Int, searchQuery: String?): Resource<CharacterPage> {
        return try {
            val itemsOffset = page * DEFAULT_PAGE_SIZE
            val response =
                remoteDataSource.getCharacters(offset = itemsOffset, searchQuery = searchQuery)

            response.body()?.data?.let { data ->
                // nr of total items
                val totalItems = data.total
                // nr of items in page
                val pageCount = data.count
                // Check if has reached last page
                val hasReachedPaginationEnd = totalItems <= itemsOffset + pageCount

                val pageList = data.results.map { item -> item.mapToDomain() }
                Resource.Success(
                    CharacterPage(pageList, hasReachedPaginationEnd)
                )
            } ?: run {
                Resource.Error("")
            }


        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "")
        }
    }
}