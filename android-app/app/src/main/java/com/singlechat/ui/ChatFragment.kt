package com.singlechat.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.singlechat.R
import com.singlechat.data.MessageModel
import com.singlechat.data.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

class ChatFragment : Fragment() {

    private var groupToken: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    // Launcher modern untuk memilih file (Gambar / PDF) dari memori HP
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadFileToSupabase(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 1. Ambil data sesi dari SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("SingleChatPrefs", Context.MODE_PRIVATE)
        groupToken = sharedPreferences.getString("GROUP_TOKEN", null)
        userEmail = sharedPreferences.getString("USER_EMAIL", "User")
        userId = sharedPreferences.getString("USER_ID", "")

        // 2. Aktifkan pendengar database real-time jika token tersedia
        if (groupToken != null) {
            setupRealtimeChat()
        }

        // 3. Inisialisasi Komponen UI
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<Button>(R.id.btnSend)
        val btnAttach = view.findViewById<Button>(R.id.btnAttach)
        val btnVoiceCall = view.findViewById<Button>(R.id.btnVoiceCall)

        // Pemicu Kirim Pesan Teks
        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, "text")
                etMessage.setText("")
            }
        }

        // Pemicu Lampiran Berkas
        btnAttach.setOnClickListener {
            filePickerLauncher.launch("*/*") 
        }

        // Pemicu Panggilan Suara Kelompok
        btnVoiceCall.setOnClickListener {
            startGroupVoiceCall()
        }
    }

    private fun setupRealtimeChat() {
        lifecycleScope.launch {
            try {
                val myChannel = supabase.channel("public:messages")
                val changeFlow = myChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "messages"
                }

                changeFlow.onEach { action ->
                    val newMessage = action.decodeRecord<MessageModel>()
                    if (newMessage.group_token == groupToken) {
                        requireActivity().runOnUiThread {
                            updateChatAdapter(newMessage)
                        }
                    }
                }.launchIn(lifecycleScope)

                myChannel.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMessage(content: String, type: String) {
        lifecycleScope.launch {
            try {
                val messageMap = mapOf(
                    "group_token" to groupToken,
                    "sender_id" to userId,
                    "sender_email" to userEmail,
                    "message_type" to type,
                    "content" to content
                )
                supabase.from("messages").insert(messageMap)
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal mengirim: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadFileToSupabase(fileUri: Uri) {
        Toast.makeText(requireContext(), "Mengunggah file...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                val context = requireContext()
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val bytes = inputStream?.readBytes() ?: return@launch
                
                val mimeType = context.contentResolver.getType(fileUri) ?: ""
                val fileType = if (mimeType.contains("pdf")) "pdf" else "image"
                val fileExtension = if (fileType == "pdf") "pdf" else "jpg"
                val fileName = "chat_${UUID.randomUUID()}.$fileExtension"

                val bucket = supabase.storage.from("chat-files")
                bucket.upload(fileName, bytes)

                val publicUrl = bucket.publicUrl(fileName)
                sendMessage(publicUrl, fileType)

                requireActivity().runOnUiThread {
                    Toast.makeText(context, "File berhasil dikirim!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal upload file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startGroupVoiceCall() {
        if (groupToken.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token kelompok tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Membuat tautan ruang panggil unik menggunakan token kelompok instan
        val callUrl = "https://meet.jit.si/SingleChat-$groupToken"
        
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(callUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak dapat membuka aplikasi panggilan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateChatAdapter(message: MessageModel) {
        // Logika penyuntikan data baru ke RecyclerView Adapter Anda ditempatkan di sini
    }
}
