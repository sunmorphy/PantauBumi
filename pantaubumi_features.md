# PantauBumi — Android Feature List

> **Stack:** Jetpack Compose · Kotlin · Hilt · Room · Retrofit · MapLibre · WorkManager · Firebase FCM  
> **Color theme:** Embun (sage green `#2E7D5E`)  
> **API base:** FastAPI + Neon PostgreSQL

---

## 🔴 Core Features (Must Have for Submission)

### 1. Location Detection
- Auto-detect GPS coordinates on launch via `FusedLocationProviderClient`
- All API calls (`/risk`, `/weather`, `/evacuation`, `/alerts`) depend on `lat`/`lng`
- Fallback to last known location if GPS is temporarily unavailable
- Manual city picker shown if location permission is denied
- **Permission:** `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION`

---

### 2. Risk Dashboard (Beranda)
- Fetch `/risk` and `/weather` simultaneously using `async`/`await` coroutines (not sequential)
- **Risk banner** — full-width color-coded card (🔴 HIGH / 🟡 MEDIUM / 🟢 LOW)
  - Map `critical` and `high` → `RiskLevel.HIGH` on Android side
  - AI prediction subtitle generated client-side from score thresholds:
    ```kotlin
    fun buildAiPrediction(flood: Double, landslide: Double, earthquake: Double): String
    ```
- **3 hazard cards** — Banjir, Longsor, Gempa with individual risk levels
- **3 stat cards** — Curah Hujan (mm/hr), Level Sungai (m), Magnitudo
- **Evacuation card** — nearest point from `GET /evacuation?limit=1`
- **Notification bell** — badge count from `GET /alerts?limit=3`
- Auto-refresh every 5 minutes via `LaunchedEffect` timer
- Pull-to-refresh with `PullToRefreshBox`

---

### 3. Alert History (Peringatan)
- Fetch `GET /alerts` with **cursor-based pagination** using Paging 3
  - Use `before_id` cursor, **not** `page=` parameter
- Group alerts by date header: `Hari ini` / `Kemarin` / date string
- Color-coded dot per severity (🔴/🟡/🟢)
- Derive `title` from `type` field client-side:
  ```kotlin
  "flood"      → "Peringatan banjir"
  "landslide"  → "Potensi longsor"
  "earthquake" → "Gempa terdeteksi"
  ```
- `subtitle` mapped from `message` field
- Empty state — illustration + "Tidak ada peringatan"
- Pull-to-refresh resets the cursor

---

### 4. Community Reports (Laporan)
- Fetch `GET /reports` with category filter chips: Semua / Banjir / Longsor / Gempa
- Report list: avatar initials circle, name, message, category badge, AI verified badge, timestamp
- **FAB** opens `ModalBottomSheet` for new report submission
- Submit via `POST /reports` with `X-Device-ID` header
- Flag reports via `POST /reports/{id}/flag` with `X-Device-ID` header
- Show verification result (`verified` + `verification_score`) in confirmation sheet
- Max 100 reports shown (no infinite scroll for MVP)

---

### 5. Evacuation Map (Peta)
- Full-screen **MapLibre** map
- Evacuation point markers from `GET /evacuation?limit=10`
- Community report pins from `GET /reports?radius=50`
- Tapping a marker opens bottom sheet with details + directions
- Floating location button to re-center on user
- Offline tile cache downloaded on first WiFi launch
- Offline banner shown when no internet — confirms cached tiles are active
- Layer toggle chips: Banjir / Longsor / Gempa / Laporan *(MVP: filter report pins only)*
- **Note:** `GET /risk/zones` (GeoJSON polygons) is a post-MVP feature

---

### 6. Push Notifications
- `PantauBumiMessagingService` extends `FirebaseMessagingService`
- Display alerts via `NotificationManager` with `pantaubumi_alerts` channel
- Register token via `POST /fcm-token` on first launch
- Re-register on `onNewToken` callback
- Persist FCM token in **DataStore** — required for DELETE later
- `DELETE /fcm-token` called when user disables all notifications in settings

---

### 7. Onboarding (First Launch Only)
- 3-slide pager shown once, tracked via DataStore flag
- **Slide 1** — "Pantau risiko bencana di sekitarmu secara real-time." + SVG illustration
- **Slide 2** — "Dapatkan peringatan sebelum banjir, longsor, atau gempa terjadi." + SVG illustration
- **Slide 3** — "Tetap aman, bahkan tanpa sinyal internet." + SVG illustration + permission requests
- Request `ACCESS_FINE_LOCATION` + `POST_NOTIFICATIONS` (API 33+ only) on slide 3
- Skippable via "Lewati" text button
- Uses `accompanist-permissions` for runtime permission handling

---

### 8. Settings (Pengaturan)
- **Notifikasi section** — toggles per hazard type (Banjir / Longsor / Gempa), stored in DataStore
- **Ambang Risiko section** — slider: only notify when risk ≥ Medium / High
- **Peta Offline section** — "Download peta offline" button + progress indicator + storage size
- **Tentang section** — app version, data sources (BMKG, USGS, Open-Meteo, PetaBencana), licenses
- `POST /fcm-token` — called when notifications re-enabled
- `DELETE /fcm-token` — called when all notifications disabled (read token from DataStore)

---

## 🟡 Device & System Features

### 9. Device ID Generation
- On first launch, generate `UUID.randomUUID()` and persist in `SharedPreferences`
- Attach as `X-Device-ID` header on **every** `POST /reports` and `POST /reports/{id}/flag`
- Never shown to the user — silent background identity for anti-spam

```kotlin
fun getOrCreateDeviceId(context: Context): String {
    val prefs = context.getSharedPreferences("pantaubumi", Context.MODE_PRIVATE)
    return prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("device_id", it).apply()
    }
}
```

---

### 10. Background Risk Monitoring
- `WorkManager` `PeriodicWorkRequest` running every **15 minutes** (Android minimum)
- Calls `/risk` and `/alerts` silently in background
- Fires a **local notification** if new HIGH/CRITICAL risk detected since last check
- Acts as fallback if FCM delivery fails (unreliable networks during disasters)

```kotlin
class RiskMonitorWorker @AssistedInject constructor(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val risk = riskRepository.getRisk(lat, lng)
        if (risk.overallRisk >= RiskLevel.HIGH) {
            notificationManager.showRiskAlert(risk)
        }
        return Result.success()
    }
}
```

---

### 11. Offline Data Caching (Room)
Cache last known API responses in Room — show immediately on screen open while network request is in flight.

| Room Table | Source API | Used By |
|---|---|---|
| `risk_cache` | `GET /risk` | Dashboard |
| `weather_cache` | `GET /weather` | Dashboard |
| `evacuation_cache` | `GET /evacuation` | Dashboard, Peta |
| `alert_cache` | `GET /alerts` | Peringatan |

- Show `"Data dari cache — diperbarui X menit lalu"` banner when offline
- Critical for disaster scenarios where mobile networks degrade

---

### 12. Network State Awareness
- Observe `ConnectivityManager` for online/offline transitions
- Show offline banner on Dashboard, Peringatan, and Laporan screens
- Resume network calls automatically when connectivity is restored
- MapLibre offline tiles activate automatically when offline

```kotlin
val isOnline = connectivity.observeNetworkState()
    .stateIn(viewModelScope, SharingStarted.Eagerly, true)
```

---

### 13. Offline Map Tile Download
- Triggered by "Download peta offline" button in Settings
- `WorkManager` `OneTimeWorkRequest` downloads MBTiles for ~30km radius around user
- Stores tiles in internal storage
- Progress shown in a system notification
- MapLibre uses cached tiles automatically when offline

---

## 🟢 UX Features (Important for Scoring)

### 14. Loading States
- Use `ShimmerCard` components for all loading states — never a blank screen
- Show skeletons while data loads, then animate in real content
- Every screen (Dashboard, Peringatan, Peta, Laporan) must have a defined loading UI

---

### 15. Error States
Handle 3 failure scenarios distinctly on every screen:

| Scenario | Behavior |
|---|---|
| No internet | Show cached data + offline banner |
| Server error (5xx) | Show retry button |
| Empty results | Show illustration + descriptive message |

Never crash. Never show a blank white screen.

---

### 16. Pull-to-Refresh
- `PullToRefreshBox` (Material 3) on Dashboard, Peringatan, Laporan
- Clears cache, refetches fresh data
- Shows `"Diperbarui barusan"` Snackbar on success

---

### 17. Rate Limit Handling (429)
- When `POST /reports` returns `429`, parse the `message` field for cooldown duration
- Show remaining time in a Snackbar: *"Terlalu banyak laporan. Coba lagi dalam 7 menit."*
- Disable submit button with countdown timer in ViewModel

```kotlin
if (response.code == 429) {
    val minutes = response.message?.extractMinutes() ?: 10
    _uiState.update { it.copy(cooldownMinutes = minutes) }
}
```

---

### 18. Report Submission UX
- 10-minute client-side cooldown between submissions
- Submit button shows countdown: `"Kirim laporan (8 menit lagi)"`
- After success: show confirmation bottom sheet with `verified` status + `verification_score`
- Auto-dismiss confirmation after 3 seconds

---

### 19. Location Permission Rationale
- Show **in-app rationale dialog** before Android's system permission dialog
- If permanently denied (`shouldShowRationale = false`): show settings deep-link card
- Never silently fail — always guide the user to fix the issue
- Gracefully degrade to manual city picker if permission remains denied

---

### 20. Dark Mode
- `Theme.kt` already supports full dark mode via `EmbunColors`
- All custom colors must use **theme tokens**, never hardcoded hex
- Test every screen in dark mode before submission
- Dark scheme uses `Earth900` (`#1F1810`) as background surface

---

## 📋 Feature Summary

| # | Feature | Priority | Screen(s) |
|---|---|---|---|
| 1 | Location Detection | 🔴 Must | All |
| 2 | Risk Dashboard | 🔴 Must | Beranda |
| 3 | Alert History | 🔴 Must | Peringatan |
| 4 | Community Reports | 🔴 Must | Laporan |
| 5 | Evacuation Map | 🔴 Must | Peta |
| 6 | Push Notifications | 🔴 Must | System |
| 7 | Onboarding | 🔴 Must | Onboarding |
| 8 | Settings | 🔴 Must | Pengaturan |
| 9 | Device ID Generation | 🟡 Should | System |
| 10 | Background Risk Monitoring | 🟡 Should | System |
| 11 | Offline Caching (Room) | 🟡 Should | All |
| 12 | Network State Awareness | 🟡 Should | All |
| 13 | Offline Map Tile Download | 🟡 Should | Peta, Pengaturan |
| 14 | Loading States | 🟢 UX | All |
| 15 | Error States | 🟢 UX | All |
| 16 | Pull-to-Refresh | 🟢 UX | Beranda, Peringatan, Laporan |
| 17 | Rate Limit Handling (429) | 🟢 UX | Laporan |
| 18 | Report Submission UX | 🟢 UX | Laporan |
| 19 | Location Permission Rationale | 🟢 UX | Onboarding |
| 20 | Dark Mode | 🟢 UX | All |

---

## 🗓️ Suggested Build Order

```
Week 1–2   → Onboarding + Location + Device ID
Week 3–4   → Dashboard (Beranda) + API integration
Week 5     → Peringatan (Alerts) + Paging 3
Week 6     → Laporan (Reports) + submission flow
Week 7     → Peta (Map) + MapLibre setup
Week 8     → Push Notifications + FCM
Week 9     → Background Worker + Room caching
Week 10    → Settings + offline map download
Week 11    → Dark mode + error/loading states + polish
```

---

*Generated for PantauBumi · IDCamp Hackathon · Embun Theme · March 2026*
