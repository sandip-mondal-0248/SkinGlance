<h1 align="center">🌟 SkinGlance</h1>

<p align="center"><i>Smart. Simple. Skin Health on Your Fingertips.</i></p>
<p align="center">
SkinGlance is a Android application that lets users detect potential skin cancer by analyzing skin lesion images. Using a cloud-hosted AI model, it offers fast and reliable predictions directly on your phone.
</p>

---

## 🔧 Built With

* 🤖 **Kotlin** - Native Android development
* 🚀 **Retrofit** - For sending HTTP requests to the backend
* 📷 **Camera & Gallery Integration** - Select or capture lesion images
* 🔗 Connects with [SkinGlance-ModelAPI](https://github.com/sandip-mondal-0248/SkinGlance-ModelAPI)

---

## 📋 Features

* ✅ Real-time cropping feature for precise lesion selection
* 📸 Capture or select skin lesion image
* ↗️ Send image to backend API using Retrofit
* 🔄 Receive diagnosis (Benign or Malignant)
* 📈 Show prediction result with confidence
* 🚨 Error Handling: Prevents crashes by displaying clear error messages, for example network error.
* 🌟 Lightweight and fast on all Android devices


---

## 📷 App UI

> *Screenshots coming soon.*

---

## 🚀 How It Works

1. User captures or selects an image of a skin lesion
2. App sends the image via HTTP POST to the FastAPI server (`/predict` endpoint)
3. Backend processes image using trained CNN ensemble and returns prediction
4. App displays the result in a user-friendly format

---

## ⚡ Setup Instructions

1. 🔁 Clone the repository:

   ```bash
   git clone https://github.com/your-username/SkinGlance-Android.git
   ```

2. 📂 Open in Android Studio

3. ✉️ In `ResultActivity.kt`, update the base URL of your FastAPI server:

   ```kotlin
   val retrofit = Retrofit.Builder()
            .baseUrl("http://YOUR_SERVER_IP:8000/") //Replace with your FastAPI server base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
   ```

4. ▶️ Build and run the app on emulator or physical device

---

## 🚫 Disclaimer

This app is for educational and research purposes only. It is **not a substitute for professional medical advice**. Always consult a doctor for medical diagnosis.


---

## 🤝 Contributing

Pull requests are welcome! Feel free to fork the project and submit suggestions.

---

## 📬 Contact

For collaboration or questions:

**Sandip Mondal**
🌐 [https://sandipmondal.bio.link/](https://sandipmondal.bio.link/)
🔗 [Linkedin](http://www.linkedin.com/in/sandip-mondal-483934248/)
