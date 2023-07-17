package com.simformsolutions.myspotify.interceptor

import android.util.Log
import com.simformsolutions.myspotify.data.model.GrantType
import com.simformsolutions.myspotify.data.repository.AuthRepository
import com.simformsolutions.myspotify.helper.PreferenceHelper
import com.simformsolutions.myspotify.utils.PreferenceKeys
import com.simformsolutions.myspotify.utils.Resource
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.Calendar

class ApiAuthenticator(
    private val authRepository: Lazy<AuthRepository>, private val preferenceHelper: PreferenceHelper
) : Authenticator {

    private var accessToken = preferenceHelper.getString(PreferenceKeys.ACCESS_TOKEN, "")

    override fun authenticate(route: Route?, response: Response): Request? {
        // If Authorization token isn't present then provide access
        // token or try to generate new Authorization token if current
        // is expired.
        Log.d("token", accessToken)
        return if (response.request.headers["Authorization"] == null || regenerateToken()) {
            response.request.newBuilder().header("Authorization", "Bearer $accessToken").build()

        } else {
            null
        }
    }

    private fun regenerateToken(): Boolean {
        val body = hashMapOf(
            "grant_type" to GrantType.REFRESH_TOKEN.toString(),
            "refresh_token" to preferenceHelper.getString(PreferenceKeys.REFRESH_TOKEN, "")
        )

        return runBlocking {
            authRepository.get().refreshAccessToken(body).let { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val data = resource.data ?: return@runBlocking false
                        preferenceHelper.apply {
                            putString(PreferenceKeys.ACCESS_TOKEN, data.accessToken)
                            putInt(
                                PreferenceKeys.EXPIRES_AT,
                                Calendar.getInstance().get(Calendar.SECOND) + data.expireTime
                            )
                        }
                        accessToken = data.accessToken
                        Log.d("token", accessToken)
                        true
                    }

                    else -> false
                }
            }
        }
    }
}