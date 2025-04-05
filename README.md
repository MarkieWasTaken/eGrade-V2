# eGrade-V2
📚 A modern Java-based eGrade system built for schools, enabling parents to view student progress, teachers to assign grades, and admins to manage users and academic years with an intuitive UI and PostgreSQL backend.


# 🎓 eGrade – Java School Grading System

eGrade is a sleek and user-friendly Java desktop application designed for educational institutions. It allows **parents**, **teachers**, and **admins** to interact with a school grading system backed by a **PostgreSQL** database.

---

## 🚀 Features

### 👨‍👩‍👧 Parent Portal
- View your child's subjects and grades
- See grade averages and teacher comments
- Hover over grades to reveal feedback
- Beautiful color-coded score visualization

### 👨‍🏫 Teacher Dashboard
- Select subjects you teach
- View students enrolled in those subjects
- Add new grades with comments and dates

### 🛠️ Admin Panel
- Create students and parents
- Link parents to students
- Assign grade levels (Years 1–9)
- Automatically enroll students into appropriate subjects per year

---

## 🧰 Tech Stack

- **Language:** Java (Swing GUI)
- **Database:** PostgreSQL
- **Architecture:** MVC-like structure (UI ↔ Logic ↔ Database)
- **Build Tool:** Your IDE or `javac`/`java`

---

## How was the app made? (Summary)

First we created a database using https://dbdiagram.io/, an online database creation web app. We then looked for a server to host this on, and we chose Aiven cloud.
After this, we connected our hosted database with our SUPB, DataGrip in this instance. Then, we stored those connection credentials in an .env file. Using it, we could now execute console commands through Java.
For Java, we used JetBrain's IDE; IntelliJ. Installing the Swing GUI plugin, which allows for form-like UI design, we first designed the base of the UI with everything required for it to work. When done, we started writing the code
for the app to function, the SQL queries, UI handling, etc. After finishing, we put a final touch on the UI and the app was finished.

It was a learning experience. There were many challenges in finding a server, troubleshooting for the IDE, coding the SQL queries and so forth. However, it was enjoyable because it was a team effort
