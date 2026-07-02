# Qaspro Scanner

A free, unlimited, offline document scanner for Android — a CamScanner-style app
that never charges you and never needs the internet for its core features.

## Why it's free & unlimited
Everything runs **on-device** using Google ML Kit — no servers, no subscriptions,
no per-scan limits, no ads:

- **Smart Scan** — auto edge detection, perspective correction, filters, multi-page
- **ID Cards** — front + back onto one document
- **Extract Text (OCR)** — pull text out of any scan
- **Import Images / Files** — scan from your gallery
- **Save as PDF** — every scan is a shareable PDF
- **Share / Open** — send to WhatsApp, email, etc.

## Get the app (APK)
1. Every push builds the app automatically (see the **Actions** tab).
2. Open the latest successful run → download the **qaspro-scanner-apk** artifact.
3. Unzip, copy `app-debug.apk` to your phone, and install it
   (allow "install from unknown sources").

## Coming next
- Convert to Word / Excel / PowerPoint
- Photo translation (on-device ML Kit Translate)
- Book / Whiteboard / Slides modes
- Timestamp watermark
- Solver AI & Formula (optional AI add-on)

## Tech
Kotlin · Jetpack Compose · Google ML Kit (Document Scanner + Text Recognition).
minSdk 26 · targetSdk 34.
