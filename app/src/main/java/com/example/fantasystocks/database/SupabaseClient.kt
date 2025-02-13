package com.example.fantasystocks.database

import androidx.compose.runtime.rememberCoroutineScope
import com.example.fantasystocks.API.StockResponse
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch

object SupabaseClient {
    private const val SUPABASE_URL = "https://lnfecoxuwybrlhzjqxkb.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxuZmVjb3h1d3licmxoempxeGtiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg5NDQwMTMsImV4cCI6MjA1NDUyMDAxM30.wCjoCRqMTLOWyPxX-9lMohKbxESbP8z6G0FM2Gk3GLY"

    @OptIn(SupabaseExperimental::class)
    val supabase = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth){
            host = "com.example.fantasystocks" // this can be anything, eg. your package name or app/company url (not your Supabase url)
            scheme = "fantasystocks"

            // On Android only, you can set OAuth and SSO logins to open in a custom tab, rather than an external browser:
            defaultExternalAuthAction = ExternalAuthAction.CustomTabs() //defaults to ExternalAuthAction.ExternalBrowser
        }
        install(Postgrest)
    }
}