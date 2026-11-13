# Voice AI Recorder - Android App

Ứng dụng Android ghi âm giọng nói và gửi trực tiếp lên AI (Google Gemini, OpenAI Whisper hoặc Server tùy chỉnh).

## Tính năng nổi bật
1. **Ghi âm trực tiếp**: Ghi âm giọng nói người dùng chất lượng cao (AAC/MP4).
2. **Cấu hình API Key linh hoạt ở bên ngoài**:
   - Có thể thay đổi ngay trên màn hình ứng dụng.
   - Tự động xuất và đọc file cấu hình tại: `/sdcard/Android/data/com.example.voiceai/files/api_config.txt`.
   - Khi hết token, bạn chỉ cần mở file `api_config.txt` bằng bất kỳ ứng dụng quản lý file nào trên Android và sửa `API_KEY=...` mà không cần cài lại app.
3. **Tương thích AI**:
   - Hỗ trợ trực tiếp **Google Gemini 1.5 Flash API** (gửi âm thanh dạng base64 kèm prompt nhận diện tiếng Việt).
   - Hỗ trợ các server nhận dạng giọng nói **Whisper / FastAPI Multipart**.

---

## Cách lấy file APK từ GitHub Actions (Tự động build)

1. Tạo một repository mới trên GitHub (ví dụ: `VoiceAIApp`).
2. Tải toàn bộ các file trong thư mục này lên repository đó.
3. Chuyển sang tab **Actions** trên GitHub:
   - GitHub Actions sẽ tự động kích hoạt workflow **Build Android APK** (thời gian build khoảng 1-2 phút).
4. Sau khi hoàn thành, nhấn vào lần chạy (Run) mới nhất, cuộn xuống phần **Artifacts** để tải file **`VoiceAI-Debug-APK.zip`**.
5. Giải nén và cài đặt file `.apk` vào điện thoại Android.
