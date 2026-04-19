# 🔐 Identity Verification & Access Management System (IDVE)

Hey! This is my minor project where I tried to build a system that actually *controls who gets access* and *verifies if users are genuine* — not just a basic login/signup app.

---

## 💡 Why I built this

Most small applications allow users to sign up without any proper verification.  
This can lead to:
- Fake accounts  
- Security risks  
- No control over user access  

So I wanted to build something that:
- Verifies user identity  
- Controls access based on roles  
- Tracks important actions  

Basically, a **mini IAM (Identity and Access Management) system**

---

## ⚙️ What the system does

### 👤 For Users:
- Sign up using email (OTP verification included)
- Login using email/password or Google/GitHub
- Upload identity document
- Check verification status (Pending / Verified / Rejected)

### 👑 For Admin:
- View all users
- Approve or reject identity verification
- See activity logs (who did what and when)

---

## 🔐 Core Concepts Used

Instead of just building a login system, I focused on IAM concepts:

- **Authentication** → JWT, OAuth, OTP  
- **Authorization** → Role-based access (User/Admin)  
- **Identity Verification** → Document upload + approval  
- **Audit Logging** → Tracks system activity  

---

## 🛠️ Tech Stack

**Frontend**
- React (Vite)
- Tailwind CSS
- Axios

**Backend**
- Spring Boot
- Spring Security
- JWT
- OAuth (Google & GitHub)
- JavaMailSender (for OTP)

**Database**
- H2 (used for development)

---

## 🔄 How it works (flow)

1. User signs up → verifies email using OTP  
2. Logs in (email or OAuth)  
3. Uploads identity document  
4. Admin reviews and approves/rejects  
5. All actions are logged  


---

## ⚠️ Current Limitations

- Using H2 database (data resets on restart)
- Not deployed on cloud yet
- Document verification is manual

---

## 🚀 Future Improvements

- Switch to MySQL  
- Deploy on cloud (AWS / Vercel)  
- Add multi-factor authentication  
- Improve UI/UX further  

---

## 🙋‍♂️ About Me

This project is part of my B.Tech (3rd year) coursework.  
I built this to understand how real systems handle identity and access.

---

## ⭐ If you like it

Feel free to star the repo 🙂
