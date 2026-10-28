package com.singlechat.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

// Inisialisasi Supabase Client Global
val supabase = createSupabaseClient(
    supabaseUrl = "https://bjeyhsnxixwwtkztzroy.supabase.co/rest/v1/", // Ganti dengan URL Supabase Anda
    supabaseKey = "bjeyhsnxixwwtkztzroy"  // Ganti dengan Anon Key Supabase Anda
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
    install(Storage)
}
