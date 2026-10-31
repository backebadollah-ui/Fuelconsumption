# ⛽ مدیریت هوشمند سوخت و نگهداری خودرو | Fuel & Vehicle Maintenance Tracker

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-4285F4?logo=jetpackcompose&logoColor=white)
![Database](https://img.shields.io/badge/Database-Room%20SQLite-003B57?logo=sqlite&logoColor=white)
![Developed With](https://img.shields.io/badge/Developed%20With-Google-4285F4?logo=google&logoColor=white)

یک اپلیکیشن بومی (Native) اندروید با رابط کاربری مدرن، خوانا و دو زبانه (فارسی و انگلیسی) برای ثبت دقیق سوخت‌گیری‌ها، مدیریت سرویس‌های دوره‌ای خودرو، تحلیل آماری مخارج و نگهداری امن اطلاعات به‌صورت آفلاین و آنلاین.

---

## ✨ امکانات و قابلیت‌های کلیدی

### ۱. مدیریت و ثبت سوابق سوخت‌گیری (Fuel Tracking)
- **ثبت کامل اطلاعات سوخت‌گیری:** ثبت کیلومترشمار، مقدار سوخت، هزینه کل، تاریخ و یادداشت برای هر بار مراجعه به جایگاه (همراه با قابلیت ویرایش و حذف).
- **محاسبه هوشمند مصرف سوخت:** محاسبه خودکار مسافت طی‌شده از سوخت‌گیری قبلی و نرخ مصرف سوخت در هر دوره و در کل سوابق.
- **داشبورد آماری لحظه‌ای:** نمایش گیج گرافیکی میانگین مصرف، مجموع هزینه‌ها، مسافت کل پیموده‌شده و کل سوخت مصرفی در یک نگاه.
- **واحدهای متنوع اندازه‌گیری و پولی:** پشتیبانی از استانداردهای متریک و امپریال (`L/100km`، `km/L` و `MPG`) و انتخاب سریع واحد پول (`تومان`، `ریال ﷼`، `$` و `€`).

### ۲. مدیریت سرویس‌ها و تعمیرات دوره‌ای (Services & Maintenance)
- **پوشش کامل دسته‌بندی‌های خودرو:** ثبت سوابق در بخش‌های «روغن و فیلترها»، «تعمیرات و قطعات»، «بیمه خودرو»، «لاستیک و باد»، «معاینه فنی و دوره‌ای»، «کارواش و نظافت» و «سایر هزینه‌ها».
- **یادآور هوشمند سررسید سرویس:** تعیین کیلومتر سرویس بعدی و نمایش هشدارهای رنگی برای سرویس‌های نزدیک به موعد یا منقضی‌شده بر اساس آخرین کیلومتر ثبت‌شده خودرو.
- **جزئیات کامل و فیلتر سریع:** امکان ثبت نام تعمیرگاه/مرکز خدمات، یادداشت‌های فنی و فیلتر کردن لیست سوابق بر اساس نوع سرویس.

### ۳. گزارشات تحلیلی و نمودارهای گرافیکی (Analytics & Reports)
- **شاخص‌های کلیدی مالکیت خودرو (KPIs):** محاسبه مجموع هزینه‌های خودرو (سوخت + سرویس)، هزینه دقیق به ازای هر کیلومتر پیمایش، میانگین مصرف دوره و مسافت کل.
- **نمودار روند تغییرات مصرف سوخت:** رسم منحنی تغییرات مصرف سوخت در دفعات مختلف به همراه نمایش مقادیر کمترین و بیشترین مصرف.
- **نمودار میله‌ای هزینه‌های ماهانه:** مقایسه بصری مخارج ماهانه به تفکیک هزینه سوخت و هزینه سرویس‌ها.
- **تفکیک سهم هزینه‌ها:** نمایش نوار توزیع رنگی و درصد سهم هر بخش از کل مخارج خودرو.
- **فیلتر زمانی منعطف و بازه دلخواه کاربر:** امکان مشاهده گزارشات در بازه‌های «کل تاریخ»، «۳ ماه اخیر»، «۶ ماه اخیر»، «۱ سال اخیر» و همچنین **انتخاب بازه زمانی کاملاً دلخواه (از تاریخ تا تاریخ)** همراه با میانبرهای سریع.

### ۴. تقویم هجری شمسی و رابط کاربری دو زبانه (Solar Hijri Calendar & Bilingual UI)
- **پشتیبانی بومی از تاریخ شمسی (جلالی):** دارای انتخابگر تاریخ اختصاصی هجری شمسی با محاسبه دقیق سال‌های کبیسه و نام ماه‌های فارسی در تمامی فرم‌ها و فیلترهای گزارش‌گیری.
- **دو زبانه (فارسی / انگلیسی):** تغییر آنی زبان برنامه با یک کلیک و هماهنگی خودکار فرمت نمایش تاریخ‌ها (شمسی در حالت فارسی و میلادی در حالت انگلیسی).
- **طراحی با کنتراست و خوانایی بالا:** بهره‌گیری از سلسله‌مراتب تایپوگرافی واضح و پالت رنگی استاندارد Material Design 3 برای خوانایی حداکثری اعداد، برچسب‌ها و نمودارها.

### ۵. پشتیبان‌گیری و بازگردانی داده‌ها — آفلاین و آنلاین (Backup & Restore)
- **بکاپ و بازیابی آفلاین:** خروجی گرفتن از تمام اطلاعات (سوخت‌گیری‌ها، سرویس‌ها و تنظیمات) در قالب فایل استاندارد `JSON` روی حافظه دستگاه، اشتراک‌گذاری مستقیم فایل از طریق پیام‌رسان‌ها یا ایمیل، و بازگردانی آسان از فایل.
- **بکاپ و بازیابی آنلاین (ابری):** همگام‌سازی ابری داده‌ها با استفاده از کلید اختصاصی همگام‌سازی (Sync Key) و پشتیبانی از ذخیره‌سازی در فضاهای ابری مانند Google Drive جهت انتقال اطلاعات بین دستگاه‌های مختلف.
- **دو حالت بازگردانی هوشمند:** امکان انتخاب بین **ادغام با داده‌های فعلی (Merge)** بدون حذف سوابق قبلی، و یا **جایگزینی کامل اطلاعات (Replace)**.

---

## 🛠️ تکنولوژی‌ها و معماری نرم‌افزار

| بخش | تکنولوژی مورد استفاده |
| :--- | :--- |
| **زبان برنامه‌نویسی** | Kotlin |
| **رابط کاربری** | Jetpack Compose + Material Design 3 |
| **معماری** | MVVM (Model - View - ViewModel) + Unidirectional Data Flow |
| **پایگاه داده محلی** | Room Persistence Library (SQLite) |
| **مدیریت همزمانی و وضعیت** | Kotlin Coroutines & StateFlow |
| **مصورسازی داده‌ها** | Custom Compose `Canvas` Charts |
| **حداقل نسخه اندروید** | Android 7.0 (API 24) — Target SDK 36 |

---

## 🚀 نحوه ساخت و اجرا (Build & Run)

۱. مخزن پروژه را کلون کنید:
```bash
git clone <repository-url>
cd <repository-folder>
```
۲. پروژه را در **Android Studio** باز کنید تا وابستگی‌های Gradle همگام‌سازی شوند.
۳. اپلیکیشن را روی شبیه‌ساز یا دستگاه فیزیکی اندروید اجرا کنید، یا از طریق خط فرمان فایل APK نسخه دیباگ را بسازید:
```bash
./gradlew assembleDebug
```

---

## 🌐 English Overview

**Fuel & Vehicle Maintenance Tracker** is a modern, bilingual (Persian/English) native Android application built with **Kotlin** and **Jetpack Compose**.

- **Fuel Logging:** Track odometer readings, fuel volume, total cost, interval distance, and real-time fuel efficiency (`L/100km`, `km/L`, `MPG`).
- **Service & Maintenance Management:** Log oil changes, repairs, insurance, tires, inspections, and car washes with smart odometer-based due reminders.
- **Visual Analytics & Custom Date Ranges:** Interactive `Canvas` trend lines, monthly fuel-vs-service bar charts, category share breakdowns, and flexible custom date-range filtering.
- **Solar Hijri (Jalali) & Gregorian Calendar:** Built-in Persian Solar Hijri date picker alongside Gregorian support.
- **Online & Offline Backup/Restore:** Full JSON local export/import, file sharing, and cloud sync key backup/restore with Merge or Replace modes.

---

## 📌 درباره پروژه (About This Project)

این نرم‌افزار به‌عنوان یک **پروژه شخصی (Personal Project)** تعریف و پیاده‌سازی شده و **تماماً با استفاده از ابزارها و اکوسیستم گوگل (Google / Google AI Studio)** توسعه داده شده است.

> *Developed as a **personal project** and built **entirely with Google**.*
